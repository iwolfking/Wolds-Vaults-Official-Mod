package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.gear.GearRollHelper;
import iskallia.vault.gear.VaultGearLegendaryHelper;
import iskallia.vault.gear.VaultGearModifierHelper;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.modification.GearModification;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.item.gear.CharmItem;
import iskallia.vault.item.gear.VaultArmorItem;
import iskallia.vault.item.tool.JewelItem;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.expertise.type.JewelExpertise;
import iskallia.vault.skill.tree.ExpertiseTree;
import iskallia.vault.world.data.PlayerExpertisesData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.api.helper.WoldGearModifierHelper;
import xyz.iwolfking.woldsvaults.expertises.CraftsmanExpertise;

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

        //Randomly add a corrupted implicit
        if(data.getFirstValue(ModGearAttributes.IS_LOOT).orElse(false) && rand.nextFloat() < 0.02F) {
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
        else if(data.getFirstValue(ModGearAttributes.IS_LOOT).orElse(false) && rand.nextFloat() < 0.02F) {
            if(stack.getItem() instanceof JewelItem) {
                return;
            }
            VaultGearModifierHelper.lockRandomAffix(stack, rand);
        }
        //Randomly add unusual
        else if(data.getFirstValue(ModGearAttributes.IS_LOOT).orElse(false) && rand.nextFloat() < 0.03F) {
            WoldGearModifierHelper.removeRandomModifierAlways(stack, rand);
            WoldGearModifierHelper.addUnusualModifier(stack, player.level.getGameTime(), rand);
        }
        //Randomly improve gear rarity (if not a jewel)
        else if(data.getFirstValue(ModGearAttributes.IS_LOOT).orElse(false) && rand.nextFloat() < 0.04F) {
            if(stack.getItem() instanceof JewelItem) {
                return;
            }
            VaultGearModifierHelper.improveGearRarity(stack, rand);
        }
        //Randomly add ability enhancement (non functional atm)
        else if(data.getFirstValue(ModGearAttributes.IS_LOOT).orElse(false) && rand.nextFloat() < 0.01F && stack.getItem() instanceof VaultArmorItem armorItem) {
            if(armorItem.getEquipmentSlot(stack) != null && armorItem.getEquipmentSlot(stack).equals(EquipmentSlot.HEAD)) {
                VaultGearModifierHelper.createOrReplaceAbilityEnhancementModifier(stack, rand);
            }
        }
    }
}
