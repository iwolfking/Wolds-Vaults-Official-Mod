package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.VaultMod;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.config.ConfigurableAttributeGenerator;
import iskallia.vault.gear.attribute.type.VaultGearAttributeType;
import iskallia.vault.gear.comparator.VaultGearAttributeComparator;
import iskallia.vault.gear.reader.VaultGearModifierReader;
import iskallia.vault.init.ModGearAttributeReaders;
import iskallia.vault.init.ModGearAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ModGearAttributes.class, remap = false)
public class MixinModGearAttribute {
    @Inject(method = "attr(Ljava/lang/String;Liskallia/vault/gear/attribute/type/VaultGearAttributeType;Liskallia/vault/gear/attribute/config/ConfigurableAttributeGenerator;Liskallia/vault/gear/reader/VaultGearModifierReader;Liskallia/vault/gear/comparator/VaultGearAttributeComparator;)Liskallia/vault/gear/attribute/VaultGearAttribute;", at = @At("HEAD"), cancellable = true)
    private static void renameAttributes(String name, VaultGearAttributeType type, ConfigurableAttributeGenerator<?, ?> generator, VaultGearModifierReader reader, VaultGearAttributeComparator comparator, CallbackInfoReturnable<VaultGearAttribute> cir) {
        if(name.equals("velocity")) {
            cir.setReturnValue(new VaultGearAttribute(VaultMod.id(name), type, generator, ModGearAttributeReaders.addedRoundedDecimalReader("Velocity", 14608287, 100.0F), comparator));
        }
        else if(name.equals("range")) {
            cir.setReturnValue(new VaultGearAttribute(VaultMod.id(name), type, generator, ModGearAttributeReaders.addedDecimalReader("Range", 16364415), comparator));
        }
    }
}
