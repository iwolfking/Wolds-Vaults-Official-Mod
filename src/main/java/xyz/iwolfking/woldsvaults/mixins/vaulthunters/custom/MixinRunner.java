package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.VaultMod;
import iskallia.vault.core.Version;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.common.FruitEatenEvent;
import iskallia.vault.core.random.ChunkRandom;
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
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.lib.IRottenFruit;
import xyz.iwolfking.woldsvaults.api.util.WoldVaultUtils;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.items.alchemy.AlchemyIngredientItem;
import xyz.iwolfking.woldsvaults.items.alchemy.CatalystItem;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.CrateLootGeneratorAccessor;
import xyz.iwolfking.woldsvaults.modifiers.vault.RemoveBlacklistModifier;
import xyz.iwolfking.woldsvaults.api.util.VaultModifierUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

@Mixin(value = Runner.class, remap = false)
public abstract class MixinRunner extends Listener {


    @Inject(method = "initServer", at = @At("TAIL"))
    private void addGreedCoinsToCrate(VirtualWorld world, Vault vault, CallbackInfo ci) {
        CommonEvents.CRATE_AWARD_EVENT.register(this, event -> {
            int greedTier = PlayerGreedTreeData.get(event.getPlayer().getLevel()).getGreedTier(event.getPlayer().getUUID());
            if(vault.get(Vault.LEVEL).get(VaultLevel.VALUE) >= 100 && greedTier > 0) {
                ResourceLocation lootTableKey = WoldsVaults.id("greed_crate_bonus_" + VaultUtils.getMainObjectiveKey(vault));

                if(!VaultRegistry.LOOT_TABLE.contains(lootTableKey)) {
                    return;
                }

                LootTableGenerator generator =
                        new LootTableGenerator(Version.latest(), VaultRegistry.LOOT_TABLE.getKey(lootTableKey), 0F);
                generator.generate(ChunkRandom.ofNanoTime());

                Iterator<ItemStack> rewardIterator = generator.getItems();
                while (rewardIterator.hasNext()) {
                    ItemStack reward = rewardIterator.next();
                    if(reward.getItem().equals(ModItems.GREED_COIN)) {
                        reward.setCount(reward.getCount() + (greedTier - 1));
                    }
                    ((CrateLootGeneratorAccessor)event.getCrateLootGenerator()).getAdditionalItemsWolds().add(reward);
                }
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
        float adjustedRotChance = rotChance * (1.0F - scaledEffectiveness);

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
