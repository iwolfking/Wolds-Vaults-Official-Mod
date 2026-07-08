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

    // The no-modify zone id the tile registered; Hyper removes the zone after the first
    // fight (vanilla removes it at an animation step that never runs, and a lingering zone
    // makes nearby POI chests unbreakable).
    @Accessor("zoneId")
    int getZoneId();
}
