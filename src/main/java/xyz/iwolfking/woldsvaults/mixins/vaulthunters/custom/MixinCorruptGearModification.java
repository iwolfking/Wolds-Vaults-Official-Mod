package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.gear.VaultGearModifierHelper;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.modification.GearModification;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;

import java.util.List;
import java.util.Random;

@Mixin(value = VaultGearModifierHelper.class, remap = false)
public class MixinCorruptGearModification {
    /**
     * @author iwolfking
     * @reason Add Divine attribute
     */
    @Overwrite
    public static GearModification.Result setGearCorrupted(ItemStack stack) {
        VaultGearData data = VaultGearData.read(stack);
        if (!data.isModifiable()) {
            return GearModification.Result.errorUnmodifiable();
        } else {
            if(data.hasAttribute(ModGearAttributes.DIVINE)) {
                Random random = new Random();
                if(random.nextBoolean()) {
                    List<VaultGearModifier<?>> divineModList = data.getModifiersFulfilling(vaultGearModifier -> vaultGearModifier.getAttribute().equals(ModGearAttributes.DIVINE));
                    for(VaultGearModifier<?> mod : divineModList) {
                        data.removeModifier(mod);
                        break;
                    }

                    data.write(stack);
                }

                return GearModification.Result.makeSuccess();
            }

            data.createOrReplaceAttributeValue(iskallia.vault.init.ModGearAttributes.IS_CORRUPTED, true);
            data.write(stack);
            return GearModification.Result.makeSuccess();
        }
    }
}
