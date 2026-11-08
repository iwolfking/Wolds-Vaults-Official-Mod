package xyz.iwolfking.woldsvaults.mixins.vaulthunters.multicell;

import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.util.RegionPos;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.world.generator.layout.VaultGridLayout;
import iskallia.vault.core.world.template.PlacementSettings;
import iskallia.vault.core.world.template.Template;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.api.core.layout.multicell.MultiCellFootprint;
import xyz.iwolfking.woldsvaults.api.core.layout.multicell.MultiCellPlacer;
import xyz.iwolfking.woldsvaults.api.core.layout.multicell.MultiCellRooms;

@Mixin(value = VaultGridLayout.class, remap = false)
public abstract class MixinVaultGridLayoutMultiCell {

    /** Any cell of a multi-cell room gets the room's single anchor-built template, skipping room-choice listeners. */
    @Inject(method = "getAt", at = @At("HEAD"), cancellable = true)
    private void woldsvaults$placeMultiCellRoom(Vault vault, RegionPos region, RandomSource random, PlacementSettings settings, CallbackInfoReturnable<Template> cir) {
        MultiCellFootprint footprint = MultiCellRooms.at(vault, region.getX(), region.getZ());
        if (footprint != null) {
            cir.setReturnValue(MultiCellPlacer.build(vault, footprint, region.getSizeX(), region.getSizeZ(), settings));
        }
    }
}
