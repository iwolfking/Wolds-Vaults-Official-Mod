package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.gear.reader.VaultGearModifierReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = VaultGearModifierReader.class, remap = false)
public interface VaultGearModifierReaderAccessor {
    @Mutable
    @Accessor("modifierName")
    void woldsvaults$setModifierName(String modifierName);
}
