package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.block.entity.BossRunePillarTileEntity;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.world.data.entity.PartialEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = BossRunePillarTileEntity.Config.class, remap = false)
public interface BossRunePillarConfigAccessor {
    /** The weighted boss roster the pillar's palette baked in; Hyper rerolls from it per cycle. */
    @Accessor("boss")
    WeightedList<PartialEntity> getBossPool();
}
