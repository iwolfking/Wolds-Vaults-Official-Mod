package xyz.iwolfking.woldsvaults.mixins.vaulthunters.multicell;

import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.util.RegionPos;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.world.generator.cache.ConcurrentGridCache;
import iskallia.vault.core.world.generator.layout.GridLayout;
import iskallia.vault.core.world.template.configured.ConfiguredTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.api.core.layout.multicell.MultiCellFootprint;
import xyz.iwolfking.woldsvaults.api.core.layout.multicell.MultiCellPlacer;
import xyz.iwolfking.woldsvaults.api.core.layout.multicell.MultiCellRooms;

@Mixin(value = ConcurrentGridCache.class, remap = false)
public abstract class MixinConcurrentGridCacheMultiCell {

    /** Caches a multi-cell room once, under its anchor cell, so all its cells share one processed template. */
    @Inject(method = "getOrCreate", at = @At("HEAD"), cancellable = true)
    private void woldsvaults$cacheUnderAnchor(RegionPos region, GridLayout layout, Vault vault, ChunkRandom random, CallbackInfoReturnable<ConfiguredTemplate> cir) {
        MultiCellFootprint footprint = MultiCellRooms.at(vault, region.getX(), region.getZ());
        if (footprint == null || (region.getX() == footprint.anchorX() && region.getZ() == footprint.anchorZ())) {
            return;
        }
        RegionPos anchor = footprint.anchorRegion(region.getSizeX(), region.getSizeZ());
        cir.setReturnValue(((ConcurrentGridCache) (Object) this).getOrCreate(anchor, layout, vault, MultiCellPlacer.anchorRandom(vault, footprint)));
    }
}
