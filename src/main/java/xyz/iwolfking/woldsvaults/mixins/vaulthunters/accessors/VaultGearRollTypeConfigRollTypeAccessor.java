package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.config.gear.VaultGearTypeConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = VaultGearTypeConfig.RollType.class, remap = false)
public interface VaultGearRollTypeConfigRollTypeAccessor {
    @Accessor("color")
    int getColor();

    @Accessor("color")
    void setColor(int color);
}
