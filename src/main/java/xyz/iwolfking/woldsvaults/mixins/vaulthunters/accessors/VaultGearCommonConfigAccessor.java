package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.config.entry.IntRangeEntry;
import iskallia.vault.config.gear.VaultGearCommonConfig;
import iskallia.vault.gear.VaultGearRarity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = VaultGearCommonConfig.class, remap = false)
public interface VaultGearCommonConfigAccessor {
    @Accessor("craftingPotentialRanges")
    Map<VaultGearRarity, IntRangeEntry> getCraftingPotentialRanges();
}
