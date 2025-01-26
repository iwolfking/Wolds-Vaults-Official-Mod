package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.config.gear.VaultGearTypeConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = VaultGearTypeConfig.class, remap = false)
public interface VaultGearRollTypeConfigAccessor {
    @Accessor("rolls")
    Map<String, VaultGearTypeConfig.RollType> getRolls();
}
