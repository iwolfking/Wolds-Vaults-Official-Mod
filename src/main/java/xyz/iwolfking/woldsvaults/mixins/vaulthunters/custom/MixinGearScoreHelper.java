package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.gear.GearScoreHelper;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.AttributeGearData;
import iskallia.vault.gear.data.GearDataCache;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModGearAttributes;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GearScoreHelper.class, remap = false)
public class MixinGearScoreHelper {

    @Inject(method = "getWeight", at = @At("RETURN"), cancellable = true)
    private static void modifyWeight(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        int weight = cir.getReturnValue();

        AttributeGearData attributeData = AttributeGearData.read(stack);
        if (!(attributeData instanceof VaultGearData)) {
            return;
        }

        GearDataCache cache = GearDataCache.of(stack);

        if (cache.hasModifierOfCategory(VaultGearModifier.AffixCategory.CORRUPTED)) {
            weight += 100000;
        }

        if (cache.hasModifierOfCategory(VaultGearModifier.AffixCategory.GREATER)) {
            weight += 100000;
        }

        if (cache.hasModifierOfCategory(VaultGearModifier.AffixCategory.valueOf("UNUSUAL"))) {
            weight += 1000000;
        }

        if(cache.hasAttribute(ModGearAttributes.HAMMER_SIZE)) {
            weight += 50000;
        }

        if(cache.hasAttribute(ModGearAttributes.JEWEL_SIZE)) {
            int jewelSize = attributeData.getFirstValue(ModGearAttributes.JEWEL_SIZE).orElse(10);
            int sizeDifferential = 10 - jewelSize;
            weight += 150000 * sizeDifferential;
        }

        cir.setReturnValue(weight);
    }
}
