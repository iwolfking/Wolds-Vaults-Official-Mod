package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.block.entity.BossRunePillarTileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = BossRunePillarTileEntity.class, remap = false)
public interface BossRunePillarAccessor {
    // runeMinimum has no setter; the Hyper objective pins it to 0 so the pillar never
    // advertises a rune requirement (arming is our phase-gated shift-click instead).
    @Accessor("runeMinimum")
    void setRuneMinimum(int runeMinimum);
}
