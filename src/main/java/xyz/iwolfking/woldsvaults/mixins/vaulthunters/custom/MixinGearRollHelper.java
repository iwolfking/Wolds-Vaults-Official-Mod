package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.config.gear.VaultEtchingConfig;
import iskallia.vault.gear.*;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.gear.modification.GearModification;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.item.MagnetItem;
import iskallia.vault.item.gear.*;
import iskallia.vault.item.tool.JewelItem;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.tree.ExpertiseTree;
import iskallia.vault.world.data.PlayerExpertisesData;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.AncientUniqueHelper;
import xyz.iwolfking.woldsvaults.api.util.PrestigePowerHelper;
import xyz.iwolfking.woldsvaults.api.util.WoldGearModifierHelper;
import xyz.iwolfking.woldsvaults.expertises.CraftsmanExpertise;
import xyz.iwolfking.woldsvaults.expertises.EclecticGearExpertise;
import xyz.iwolfking.woldsvaults.milestones.Milestones;
import xyz.iwolfking.woldsvaults.prestige.CraftingPotentialPrestigePower;

import java.util.List;
import java.util.Random;

@Mixin(value = GearRollHelper.class, remap = false)
public class MixinGearRollHelper {
    @Shadow @Final public static Random rand;

    @Inject(method = "canGenerateLegendaryModifier", at = @At(value = "TAIL"), cancellable = true)
    private static void canGenerateLegendaryModifier(Player player, VaultGearData data, CallbackInfoReturnable<Boolean> cir) {
        if(data.equals(VaultGearData.empty())) {
            return;
        }

        if (data.getFirstValue(ModGearAttributes.CRAFTED_BY).isPresent() && rand.nextFloat() < ModConfigs.VAULT_GEAR_COMMON.getLegendaryModifierChance()) {
            ExpertiseTree expertises = PlayerExpertisesData.get((ServerLevel) player.getLevel()).getExpertises(player);
            int craftsmanLevel = 0;

            for (CraftsmanExpertise craftsmanExpertise : expertises.getAll(CraftsmanExpertise.class, Skill::isUnlocked)) {
                craftsmanLevel = craftsmanExpertise.getCraftsmanLevel();
            }
            if(craftsmanLevel > 0) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "initializeGear(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;)V", at = @At(value = "INVOKE", target = "Liskallia/vault/gear/VaultGearModifierHelper;generateAffixSlots(Lnet/minecraft/world/item/ItemStack;Ljava/util/Random;)V"))
    private static void initializeGearWithCraftingPotential(ItemStack stack, Player player, CallbackInfo ci) {
        if(player instanceof ServerPlayer serverPlayer) {
           List<CraftingPotentialPrestigePower> craftingPotentialPrestigePowers = PrestigePowerHelper.getPrestigePowersOfType(serverPlayer, CraftingPotentialPrestigePower.class);
           if(craftingPotentialPrestigePowers.isEmpty()) {
               return;
           }

           int additionalPotential = 0;

           for(CraftingPotentialPrestigePower power : craftingPotentialPrestigePowers) {
               additionalPotential += power.getPotentialIncrease();
           }

           if(additionalPotential > 0) {
               VaultGearData data = VaultGearData.read(stack);
               int currentPotential = data.getFirstValue(ModGearAttributes.MAX_CRAFTING_POTENTIAL).orElse(0);
               data.createOrReplaceAttributeValue(ModGearAttributes.MAX_CRAFTING_POTENTIAL, currentPotential + additionalPotential);
               data.createOrReplaceAttributeValue(ModGearAttributes.CRAFTING_POTENTIAL, currentPotential + additionalPotential);
               data.write(stack);
           }
        }
    }

    @Inject(method = "initializeGear(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;)V", at = @At(value = "INVOKE", target = "Liskallia/vault/gear/VaultGearModifierHelper;generateModifiers(Lnet/minecraft/world/item/ItemStack;Ljava/util/Random;)Liskallia/vault/gear/modification/GearModification$Result;", shift = At.Shift.AFTER))
    private static void initializeGearWithEffects(ItemStack stack, Player player, CallbackInfo ci, @Local VaultGearData data) {
        if(stack.getItem() instanceof CharmItem || stack.getItem() instanceof VaultNecklaceItem || stack.getItem() instanceof VaultCharmItem) {
            return;
        }

        if(stack.getItem() instanceof JewelItem) {
            if(player == null) {
                return;
            }
        }

        if(data.getFirstValue(xyz.iwolfking.woldsvaults.init.ModGearAttributes.IS_ETCHED).orElse(false) && stack.getItem() instanceof VaultGearItem gearItem) {
            woldsvaults$addRandomEtchingEntry(data, gearItem, stack);
        }

        if(!data.getFirstValue(ModGearAttributes.IS_LOOT).orElse(false)) {
            return;
        }

        float increasedSpecialRollsChance = 0.0F;

        if(player != null) {
            ExpertiseTree expertises = PlayerExpertisesData.get((ServerLevel) player.getLevel()).getExpertises(player);
            for (EclecticGearExpertise eclecticGearExpertise : expertises.getAll(EclecticGearExpertise.class, Skill::isUnlocked)) {
                increasedSpecialRollsChance += eclecticGearExpertise.getIncreasedChance();
            }
        }

        int itemLevel = data.getItemLevel();

        if(itemLevel >= 65 && rand.nextFloat() <= 0.02F + increasedSpecialRollsChance) {
            GearModification.Result result;

            if (rand.nextBoolean()) {
                result = VaultGearModifierHelper.generateCorruptedImplicit(stack, rand);
            } else {
                result = VaultGearLegendaryHelper.improveExistingModifier(stack, 1, rand, List.of(VaultGearModifier.AffixCategory.CORRUPTED));
            }

            if (result.success()) {
                VaultGearModifierHelper.setGearCorrupted(stack);
            }
        }
        else if(itemLevel >= 25 && rand.nextFloat() <= 0.02F + increasedSpecialRollsChance) {
            if(stack.getItem() instanceof JewelItem) {
                return;
            }
            VaultGearModifierHelper.lockRandomAffix(stack, rand);
        }
        else if(itemLevel>= 20 && rand.nextFloat() <= 0.02F + increasedSpecialRollsChance) {
            WoldGearModifierHelper.removeRandomModifierAlways(stack, rand);
            WoldGearModifierHelper.addUnusualModifier(stack, player.level.getGameTime(), rand);
        }
        else if(itemLevel >= 40 && rand.nextFloat() <= 0.01F + increasedSpecialRollsChance) {
            VaultGearLegendaryHelper.improveExistingModifier(stack, 1, rand, List.of(VaultGearModifier.AffixCategory.GREATER));
        }

    }

    @Inject(method = "initializeGear(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;)V", at = @At("TAIL"))
    private static void handleCorruptedJewelSize(ItemStack stack, Player player, CallbackInfo ci, @Local VaultGearData data) {
        if(stack.getItem() instanceof JewelItem) {
            if(data.getItemLevel() < 65 || rand.nextFloat() > 0.02F) {
                return;
            }

            GearModification.Result result = VaultGearLegendaryHelper.improveExistingModifier(stack, 1, rand, List.of(VaultGearModifier.AffixCategory.CORRUPTED));
            VaultGearData jData = VaultGearData.read(stack);
            if (result.success()) {
                List<VaultGearModifier<?>> sizeMods = jData.getModifiers(VaultGearModifier.AffixType.IMPLICIT).stream().filter(vaultGearModifier -> vaultGearModifier.getAttribute() == ModGearAttributes.JEWEL_SIZE).toList();
                if(!sizeMods.isEmpty()) {
                    jData.removeModifier(sizeMods.get(0));
                }

                jData.addModifier(VaultGearModifier.AffixType.IMPLICIT, new VaultGearModifier<>(ModGearAttributes.JEWEL_SIZE, rand.nextInt(1, 26)));
                jData.write(stack);
                VaultGearModifierHelper.setGearCorrupted(stack);
            }
        }
    }

    /** Decides whether an identifying unique becomes ancient, before the tier config resolves. */
    @Inject(method = "initializeUniqueGear(Liskallia/vault/gear/data/VaultGearData;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;)V", at = @At(value = "INVOKE", target = "Liskallia/vault/config/gear/VaultGearTierConfig;getConfig(Lnet/minecraft/world/item/ItemStack;)Ljava/util/Optional;"))
    private static void rollAncientUnique(VaultGearData data, ItemStack stack, Player player, CallbackInfo ci) {
        if(!AncientUniqueHelper.rollAncient(data, player, rand)) {
            return;
        }

        data.createOrReplaceAttributeValue(xyz.iwolfking.woldsvaults.init.ModGearAttributes.ANCIENT_UNIQUE, true);
        data.getFirstValue(ModGearAttributes.UNIQUE_ITEM_KEY)
                .flatMap(AncientUniqueHelper::getAncientName)
                .ifPresent(name -> data.createOrReplaceAttributeValue(ModGearAttributes.GEAR_NAME, name));
        data.write(stack);

        if(player instanceof ServerPlayer serverPlayer) {
            Milestones.onAncientUniqueIdentified(serverPlayer);
        }
    }

    @Unique
    private static boolean woldsVaults$canGenerateEtching(@Nullable Player player, VaultGearData data, ItemStack stack) {
        if(stack.getEquipmentSlot() != null) {
            return data.get(xyz.iwolfking.woldsvaults.init.ModGearAttributes.IS_ETCHED, VaultGearAttributeTypeMerger.anyTrue());
        }

        return false;
    }

    @Unique
    private static void woldsvaults$addRandomEtchingEntry(VaultGearData data, VaultGearItem gear, ItemStack gearStack) {
        if(data.hasAttribute(ModGearAttributes.ETCHING) || !data.isModifiable() || data.getRarity().equals(VaultGearRarity.UNIQUE) || gearStack.getItem() instanceof MagnetItem) {
            return;
        }

        ResourceLocation etchingId = woldsvaults$getRandomEtchingId();
        VaultEtchingConfig.EtchingEntry etchingEntry = ModConfigs.ETCHINGS.getEtchingConfig(etchingId);
        if(etchingEntry == null) {
            return;
        }

        List<String> groups = etchingEntry.getTypeGroups();
        if(!groups.isEmpty()) {
            VaultGearType type = gear.getGearType(gearStack);
            boolean allowed = groups.stream().anyMatch(g -> ModConfigs.ETCHINGS.getGroup(g).contains(type));
            if(!allowed) {
                woldsvaults$addRandomEtchingEntry(data, gear, gearStack);
                return;
            }
        }

        ItemStack etchingStack = EtchingItem.create(etchingId, etchingEntry, new Random(), data.getItemLevel()).orElse(ItemStack.EMPTY);

        WoldGearModifierHelper.addEtching(gearStack, etchingStack);
    }

    @Unique
    private static ResourceLocation woldsvaults$getRandomEtchingId() {
        List<ResourceLocation> etchings = ModConfigs.ETCHINGS.getEtchingIds().stream().toList();
        Random random = new Random();
        int randomIndex = random.nextInt(etchings.size());
        return etchings.get(randomIndex);
    }
}
