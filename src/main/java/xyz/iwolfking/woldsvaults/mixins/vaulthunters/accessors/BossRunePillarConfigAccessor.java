package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.block.entity.BossRunePillarTileEntity;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.world.data.entity.PartialEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = BossRunePillarTileEntity.Config.class, remap = false)
public interface BossRunePillarConfigAccessor {
    // The boss roster the boss-room palette shipped into the pillar's config; vanilla rolls
    // from it exactly once (position-seeded, so the same boss forever) — Hyper rerolls per arm.
    @Accessor("boss")
    WeightedList<PartialEntity> getBossPool();
}
