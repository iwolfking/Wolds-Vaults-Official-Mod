package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.config.VaultCrystalConfig;
import iskallia.vault.item.crystal.objective.CrystalObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = VaultCrystalConfig.SealEntry.class, remap = false)
public interface SealEntryAccessor {
    @Accessor
    CrystalObjective getObjective();
}
