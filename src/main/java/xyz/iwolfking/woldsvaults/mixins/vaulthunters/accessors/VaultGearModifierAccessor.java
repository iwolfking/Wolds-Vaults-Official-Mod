package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = VaultGearModifier.class, remap = false)
public interface VaultGearModifierAccessor<T> {

}
