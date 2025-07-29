package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.config.gear.VaultGearTierConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = VaultGearTierConfig.ModifierTier.class, remap = false)
public interface ModifierGearTierAccessor {

    @Accessor("maxLevel")
    void setMaxLevel(int maxLevel);
}
