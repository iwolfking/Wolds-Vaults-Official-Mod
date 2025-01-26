package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.gear.VaultGearModifierHelper;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.data.VaultGearData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;

@Mixin(value = VaultGearModifierHelper.class, remap = false)
public class MixinCorruptGearModification {
    @Redirect(method = "setGearCorrupted", at = @At(value = "INVOKE", target = "Liskallia/vault/gear/data/VaultGearData;createOrReplaceAttributeValue(Liskallia/vault/gear/attribute/VaultGearAttribute;Ljava/lang/Object;)Ljava/lang/Object;"))
    private static Object dontCorruptDivineGear(VaultGearData instance, VaultGearAttribute vaultGearAttribute, Object o) {
        if(instance.getFirstValue(ModGearAttributes.DIVINE).isPresent()) {
            if(instance.getFirstValue(ModGearAttributes.DIVINE).get()) {
                return instance.createOrReplaceAttributeValue(iskallia.vault.init.ModGearAttributes.IS_CORRUPTED, false);
            }
        }

        return instance.createOrReplaceAttributeValue(iskallia.vault.init.ModGearAttributes.IS_CORRUPTED, Boolean.TRUE);
    }

}
