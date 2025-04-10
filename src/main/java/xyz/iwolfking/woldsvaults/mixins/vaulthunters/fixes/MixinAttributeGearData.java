package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.data.AttributeGearData;
import iskallia.vault.init.ModGearAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = AttributeGearData.class, remap = false)
public class MixinAttributeGearData<T> {
    @Redirect(method = "createOrReplaceAttributeValue", at = @At(value = "INVOKE", target = "Liskallia/vault/gear/data/AttributeGearData;isModifiable()Z", ordinal = 0))
    private boolean allowModificationOfTransmogsOnCorrupted(AttributeGearData instance, @Local(argsOnly = true) VaultGearAttribute<T> attribute) {
        if(attribute.equals(ModGearAttributes.GEAR_MODEL)) {
            return true;
        }

        return instance.isModifiable();
    }
}
