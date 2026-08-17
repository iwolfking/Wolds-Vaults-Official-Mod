package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.VaultMod;
import iskallia.vault.core.Version;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.common.CrateAwardEvent;
import iskallia.vault.core.event.common.FruitEatenEvent;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultLevel;
import iskallia.vault.core.vault.VaultRegistry;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.core.world.loot.generator.LootTableGenerator;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.init.ModItems;
import iskallia.vault.skill.base.LearnableSkill;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.tree.ExpertiseTree;
import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import iskallia.vault.util.InventoryUtil;
import iskallia.vault.world.data.PlayerExpertisesData;
import iskallia.vault.world.data.PlayerGreedTreeData;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.lib.IRottenFruit;
import xyz.iwolfking.woldsvaults.api.util.LuckHelper;
import xyz.iwolfking.woldsvaults.api.util.WoldVaultUtils;
import xyz.iwolfking.woldsvaults.gods.trees.wendarr.WendarrFruit;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.items.alchemy.AlchemyIngredientItem;
import xyz.iwolfking.woldsvaults.items.alchemy.CatalystItem;
import xyz.iwolfking.woldsvaults.medallions.GreedCrateLoot;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionTier;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionVaultState;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.CrateLootGeneratorAccessor;
import xyz.iwolfking.woldsvaults.modifiers.vault.RemoveBlacklistModifier;
import xyz.iwolfking.woldsvaults.objectives.hyper.HyperCrateRewards;
import xyz.iwolfking.woldsvaults.api.util.VaultModifierUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Mixin(value = Runner.class, remap = false)
public abstract class MixinRunner extends Listener {

    @Inject(method = "initServer", at = @At("TAIL"))
    private void scaleSomeEventsWithLuck(VirtualWorld world, Vault vault, CallbackInfo ci) {
        CommonEvents.CHEST_CATALYST_GENERATION.register(this, event -> {
            event.setProbability(LuckHelper.getLuckAffectedChance((float) event.getProbability(), event.getPlayer()));
        });
        CommonEvents.CHEST_TRAP_GENERATION.register(this, event -> {
            event.setProbability(LuckHelper.getLuckAffectedChanceInverse((float) event.getProbability(), event.getPlayer()));
        });
        CommonEvents.SOUL_SHARD_CHANCE.register(this, event -> {
            event.setChance(LuckHelper.getLuckAffectedChance(event.getChance(), event.getKiller()));
        });
    }

    /**
     * CRATE_AWARD_EVENT is a server-global bus invoked twice (PRE/POST) for every crate awarded
     * in ANY vault; without this guard each live Runner's handlers would inject a full roll
     * into every crate on the server.
     */
    @Unique
    private boolean isNotOwnCratePreAward(CrateAwardEvent.Data event) {
        return event.getPhase() != CrateAwardEvent.Phase.PRE
                || event.getListener() == null
                || !Objects.equals(event.getListener().get(Listener.ID), this.get(Listener.ID));
    }

    /**
     * The greed crate loot (GCL) bonus, keyed off the crystal's greed medallion. Replaces the
     * retired greed-tree bonus: no medallion means no bonus at all, the table is a function of the
     * medallion's GCL tier rather than of the vault objective, and greed coins are computed in
     * code from the medallion's base greed coins and the objective multiplier instead of being an
     * entry inside the table. Coins riding outside the table is also what retires the old hyper
     * double-pass - there is nothing left in the table that needs shielding from item quantity,
     * and the design requires the GCL table to be immune to crate tiers anyway.
     *
     * <p>Everything is written into the crate's additional-items list, which
     * {@code CrateLootGenerator.createLoot} appends raw. That bypass of the crate's item quantity
     * is deliberate and must be preserved.</p>
     */
    @Inject(method = "initServer", at = @At("TAIL"))
    private void addGreedCoinsToCrate(VirtualWorld world, Vault vault, CallbackInfo ci) {
        CommonEvents.CRATE_AWARD_EVENT.register(this, event -> {
            if(isNotOwnCratePreAward(event)) {
                return;
            }
            GreedMedallionTier medallion = GreedMedallionVaultState.get(vault).orElse(null);
            if(medallion == null) {
                return;
            }
            if(vault.get(Vault.LEVEL).get(VaultLevel.VALUE) < 100 || GreedCrateLoot.isExcludedVault(vault)) {
                return;
            }

            List<ItemStack> additionalItems =
                    ((CrateLootGeneratorAccessor)event.getCrateLootGenerator()).getAdditionalItemsWolds();

            int coins = GreedCrateLoot.coinsForCrate(vault, medallion);
            if(coins > 0) {
                additionalItems.add(new ItemStack(ModItems.GREED_COIN, coins));
            }

            int greedCrateLootTier = medallion.getGreedCrateLootTier();
            if(greedCrateLootTier <= 0) {
                return;
            }

            ResourceLocation lootTableKey = GreedCrateLoot.tableId(greedCrateLootTier);
            if(!VaultRegistry.LOOT_TABLE.contains(lootTableKey)) {
                WoldsVaults.LOGGER.error("Greed crate loot table {} is not registered; the {} medallion's crate bonus is lost. Check config/the_vault/gen/loot_tables.json.",
                        lootTableKey, medallion.getPathName());
                return;
            }

            LootTableGenerator generator =
                    new LootTableGenerator(Version.latest(), VaultRegistry.LOOT_TABLE.getKey(lootTableKey), 0F);
            generator.generate(ChunkRandom.ofNanoTime());

            Iterator<ItemStack> rewardIterator = generator.getItems();
            while (rewardIterator.hasNext()) {
                additionalItems.add(rewardIterator.next());
            }
        });
    }

    /**
     * Injects the score-gated hyper crate rewards. Failures are caught and logged because the
     * VH event bus swallows handler exceptions silently.
     */
    @Inject(method = "initServer", at = @At("TAIL"))
    private void addHyperScoreRewardsToCrate(VirtualWorld world, Vault vault, CallbackInfo ci) {
        CommonEvents.CRATE_AWARD_EVENT.register(this, event -> {
            if(isNotOwnCratePreAward(event)) {
                return;
            }
            try {
                int greedTier = PlayerGreedTreeData.get(event.getPlayer().getLevel()).getGreedTier(event.getPlayer().getUUID());
                List<ItemStack> rewards = HyperCrateRewards.rollForVault(vault, greedTier, JavaRandom.ofNanoTime());
                if (!rewards.isEmpty()) {
                    ((CrateLootGeneratorAccessor) event.getCrateLootGenerator()).getAdditionalItemsWolds().addAll(rewards);
                    WoldsVaults.LOGGER.info("Injected {} hyper score-tier reward stacks into the completion crate.", rewards.size());
                }
            } catch (Exception e) {
                WoldsVaults.LOGGER.error("Hyper score-tier crate injection failed!", e);
            }
        });
    }

    @Inject(method = "lambda$initServer$3", at = @At("TAIL"))
    private void handleFruitRotting(VirtualWorld world, Vault vault, FruitEatenEvent.Data data, CallbackInfo ci) {
        if(!ModConfigs.VAULT_FRUIT_CONFIG.enableFruitRotting) {
            return;
        }

        Random random = new Random();
        float rotChance = ((IRottenFruit)data.getFruit()).getRotChance();
        AttributeSnapshot snapshot = AttributeSnapshotHelper.getInstance().getSnapshot(data.getPlayer());

        float effectiveness = snapshot.getAttributeValue(ModGearAttributes.FRUIT_EFFECTIVENESS, VaultGearAttributeTypeMerger.floatSum());
        float scaledEffectiveness = effectiveness / (1.0F + effectiveness);
        float adjustedRotChance = WendarrFruit.adjustRotChance(data.getPlayer(), rotChance * (1.0F - scaledEffectiveness));

        //Trigger rotting stack
        if(random.nextFloat() <= adjustedRotChance) {
            long rotCount = VaultModifierUtils.getCountOfModifiers(vault, WoldsVaults.id("rotting"));
            if(rotCount >= ModConfigs.VAULT_FRUIT_CONFIG.rotAllowance) {
                return;
            }

            VaultModifierUtils.addModifier(vault, WoldsVaults.id("rotting"), 1);
            WoldVaultUtils.sendMessageToAllRunners(vault, new TranslatableComponent("woldsvaults.special.fruit_rotting"), true);

            if(rotCount + 1 >= ModConfigs.VAULT_FRUIT_CONFIG.rotAllowance) {
                VaultModifierUtils.addModifier(vault, VaultMod.id("rotten"), 1);
                WoldVaultUtils.sendMessageToAllRunners(vault, new TranslatableComponent("woldsvaults.special.rotten"));
            }
        }
    }

    @Inject(method = "lambda$initServer$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isCreative()Z", shift = At.Shift.AFTER, remap = true), cancellable = true)
    private void preventCancelingInteraction(VirtualWorld world, PlayerInteractEvent event, CallbackInfo ci) {
        if(ServerVaults.get(world).isPresent()) {
            Vault vault = ServerVaults.get(world).get();
            List<VaultModifier<?>> modifiers = vault.get(Vault.MODIFIERS).getModifiers();
            for(VaultModifier<?> modifier : modifiers) {
                if(modifier instanceof RemoveBlacklistModifier removeBlacklistModifier) {
                    if(removeBlacklistModifier.properties().shouldUseAsBlacklist() && removeBlacklistModifier.properties().getWhitelist().isEmpty() && removeBlacklistModifier.properties().shouldApplyToItems()) {
                        ci.cancel();
                    }
                }
            }
        }
    }

    @Inject(method = "lambda$initServer$1", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isCreative()Z", shift = At.Shift.AFTER), cancellable = true)
    private void preventCancelingInteraction(VirtualWorld world, BlockEvent.EntityPlaceEvent event, CallbackInfo ci) {
        if(ServerVaults.get(world).isPresent()) {
            Vault vault = ServerVaults.get(world).get();
            List<VaultModifier<?>> modifiers = vault.get(Vault.MODIFIERS).getModifiers();
            for(VaultModifier<?> modifier : modifiers) {
                if(modifier instanceof RemoveBlacklistModifier removeBlacklistModifier) {
                    if(removeBlacklistModifier.properties().shouldUseAsBlacklist() && removeBlacklistModifier.properties().getWhitelist().isEmpty() && removeBlacklistModifier.properties().shouldApplyToBlocks()) {
                        ci.cancel();
                    }
                }
            }
        }
    }

    @Inject(method = "onJoin", at = @At(value = "INVOKE", target = "Liskallia/vault/core/vault/player/Listener;onJoin(Liskallia/vault/core/world/storage/VirtualWorld;Liskallia/vault/core/vault/Vault;)V"))
    private void addRandomPositiveModifiers(VirtualWorld world, Vault vault, CallbackInfo ci) {
        if(this.getPlayer().isPresent()) {
            ServerPlayer player = this.getPlayer().get();
            ExpertiseTree expertiseTree = PlayerExpertisesData.get(player.server).getExpertises(player);
            int surpriseModifiersExpertiseLevel = 0;
            for(Skill expertise : expertiseTree.skills) {
                if(expertise.getId().equals("Surprise_Favors")) {
                    surpriseModifiersExpertiseLevel = ((LearnableSkill)expertise).getSpentLearnPoints();
                    break;
                }
            }
            if(surpriseModifiersExpertiseLevel > 0) {
                if(world.getRandom().nextFloat() < (surpriseModifiersExpertiseLevel * 0.2F)) {
                    if(VaultUtils.isSpecialVault(vault) || VaultUtils.isRawVault(vault) || VaultUtils.isTrialVault(vault)) {
                        return;
                    }
                    else {
                        VaultModifierUtils.addModifierFromPool(vault, VaultMod.id("random_positive"));
                    }
                }
            }
        }
    }

    @Inject(method = "onLeave", at = @At(value = "TAIL"))
    private void addLeaveEvents(VirtualWorld world, Vault vault, CallbackInfo ci) {
        this.getPlayer().ifPresent(player ->  {
            for(InventoryUtil.ItemAccess items : InventoryUtil.findAllItems(player)) {
                ItemStack stack = items.getStack();
                if (stack.getItem() instanceof AlchemyIngredientItem || stack.getItem() instanceof CatalystItem) {
                    items.setStack(ItemStack.EMPTY);
                }
            }
        });
    }
}
