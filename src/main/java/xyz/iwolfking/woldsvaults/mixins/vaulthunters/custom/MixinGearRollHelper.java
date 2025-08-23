package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.config.gear.VaultEtchingConfig;
import iskallia.vault.gear.*;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.gear.data.JewelGearData;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.gear.modification.GearModification;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.item.gear.CharmItem;
import iskallia.vault.item.gear.EtchingItem;
import iskallia.vault.item.gear.VaultArmorItem;
import iskallia.vault.item.tool.JewelItem;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.tree.ExpertiseTree;
import iskallia.vault.world.data.PlayerExpertisesData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
import xyz.iwolfking.woldsvaults.api.helper.WoldGearModifierHelper;
import xyz.iwolfking.woldsvaults.expertises.CraftsmanExpertise;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;

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

    @Inject(method = "initializeGear(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;)V", at = @At(value = "INVOKE", target = "Liskallia/vault/gear/VaultGearModifierHelper;generateModifiers(Lnet/minecraft/world/item/ItemStack;Ljava/util/Random;)Liskallia/vault/gear/modification/GearModification$Result;", shift = At.Shift.AFTER))
    private static void initializeGearWithEffects(ItemStack stack, Player player, CallbackInfo ci, @Local VaultGearData data) {
        //Don't need to process jewels and other kinds of gear.
        if(stack.getItem() instanceof CharmItem) {
            return;
        }

        if(stack.getItem() instanceof JewelItem) {
            if(player == null) {
                return;
            }
        }

        if(data.getFirstValue(xyz.iwolfking.woldsvaults.init.ModGearAttributes.IS_ETCHED).orElse(false) && stack.getItem() instanceof VaultGearItem gearItem) {
            //woldsvaults$addRandomEtchingEntry(data, gearItem, stack);
        }

        if(!data.getFirstValue(ModGearAttributes.IS_LOOT).orElse(false)) {
            return;
        }

        int itemLevel = data.getItemLevel();

        //Randomly add a corrupted implicit
        if(itemLevel >= 65 && rand.nextFloat() <= 0.02F) {
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
        //Randomly frozen (if not a jewel)
        else if(itemLevel >= 25 && rand.nextFloat() <= 0.02F) {
            if(stack.getItem() instanceof JewelItem) {
                return;
            }
            VaultGearModifierHelper.lockRandomAffix(stack, rand);
        }
        //Randomly add unusual
        else if(itemLevel>= 20 && rand.nextFloat() <= 0.02F) {
            WoldGearModifierHelper.removeRandomModifierAlways(stack, rand);
            WoldGearModifierHelper.addUnusualModifier(stack, player.level.getGameTime(), rand);
        }
        //Randomly add greater modifier
        else if(itemLevel >= 40 && rand.nextFloat() <= 0.01F) {
            VaultGearLegendaryHelper.improveExistingModifier(stack, 1, rand, List.of(VaultGearModifier.AffixCategory.GREATER));
        }
        //Randomly improve gear rarity (if not a jewel)
        else if(rand.nextFloat() <= 0.04F) {
            if(stack.getItem() instanceof JewelItem) {
                return;
            }
            VaultGearModifierHelper.improveGearRarity(stack, rand);
        }

        //Randomly add ability enhancement (non functional atm)
        else if(rand.nextFloat() < 0.01F && stack.getItem() instanceof VaultArmorItem armorItem) {
            if(armorItem.getEquipmentSlot(stack) != null && armorItem.getEquipmentSlot(stack).equals(EquipmentSlot.HEAD)) {
                VaultGearModifierHelper.createOrReplaceAbilityEnhancementModifier(stack, rand);
            }
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

    @Unique
    private static boolean woldsVaults$canGenerateEtching(@Nullable Player player, VaultGearData data, ItemStack stack) {
        if(stack.getEquipmentSlot() != null && (stack.getEquipmentSlot().equals(EquipmentSlot.CHEST) || stack.getEquipmentSlot().equals(EquipmentSlot.FEET) || stack.getEquipmentSlot().equals(EquipmentSlot.HEAD) || stack.getEquipmentSlot().equals(EquipmentSlot.LEGS))) {
            return data.get(xyz.iwolfking.woldsvaults.init.ModGearAttributes.IS_ETCHED, VaultGearAttributeTypeMerger.anyTrue());
        }

        return false;
    }

    @Unique
    private static void woldsvaults$addRandomEtchingEntry(VaultGearData data, VaultGearItem gear, ItemStack gearStack) {
        if(data.hasAttribute(ModGearAttributes.ETCHING) || !data.isModifiable() || data.getRarity().equals(VaultGearRarity.UNIQUE)) {
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
            }
        }

        ItemStack etchingStack = EtchingItem.create(etchingId, etchingEntry, new Random(), data.getItemLevel()).orElse(ItemStack.EMPTY);
        if(etchingStack.isEmpty()) {
            return;
        }

        VaultGearData etchingData = VaultGearData.read(etchingStack);

        data.createOrReplaceAttributeValue(ModGearAttributes.ETCHING, etchingId);
        etchingData.getModifiers(VaultGearModifier.AffixType.IMPLICIT).forEach(modifier -> data.addModifier(VaultGearModifier.AffixType.IMPLICIT, modifier));
        data.write(gearStack);
    }

    @Unique
    private static ResourceLocation woldsvaults$getRandomEtchingId() {
        List<ResourceLocation> etchings = ModConfigs.ETCHINGS.getEtchingIds().stream().toList();
        Random random = new Random();
        int randomIndex = random.nextInt(etchings.size());
        return etchings.get(randomIndex);
    }
}
