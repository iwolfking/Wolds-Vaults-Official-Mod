package xyz.iwolfking.woldsvaults.mixins.vaulthunters.multicell;

import iskallia.vault.core.util.RegionPos;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.stat.DiscoveredRoomStat;
import iskallia.vault.core.world.generator.GridGenerator;
import iskallia.vault.core.world.generator.layout.VaultGridLayout;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.api.core.layout.multicell.MultiCellFootprint;
import xyz.iwolfking.woldsvaults.api.core.layout.multicell.MultiCellRooms;

import java.util.Optional;

@Mixin(value = DiscoveredRoomStat.class, remap = false)
public abstract class MixinDiscoveredRoomStatMultiCell {

    /** Resolves every cell of a multi-cell room to its anchor, so the room is discovered and counted once. */
    @Inject(method = "resolveRoom", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$resolveToAnchor(Vault vault, GridGenerator generator, VaultGridLayout layout, RegionPos region,
                                                    CallbackInfoReturnable<Optional<DiscoveredRoomStat.RoomDiscovery>> cir) {
        MultiCellFootprint footprint = MultiCellRooms.at(vault, region.getX(), region.getZ());
        if (footprint == null || (region.getX() == footprint.anchorX() && region.getZ() == footprint.anchorZ())) {
            return;
        }
        cir.setReturnValue(DiscoveredRoomStat.resolveRoom(vault, generator, layout, footprint.anchorRegion(region.getSizeX(), region.getSizeZ())));
    }
}
