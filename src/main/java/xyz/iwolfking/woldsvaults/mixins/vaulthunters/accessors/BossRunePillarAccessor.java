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

    // The no-modify zone id the tile registered. Hyper scopes the zone to each fight:
    // resetting the id and re-running the tile's onLoad registers a fresh vanilla zone on
    // arm, and the escalation manager removes it when the boss dies (vanilla removes it at
    // an animation step that never runs, and a lingering zone makes nearby POI chests
    // unbreakable).
    @Accessor("zoneId")
    int getZoneId();

    @Accessor("zoneId")
    void setZoneId(int zoneId);
}
