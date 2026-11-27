package xyz.iwolfking.woldsvaults.mixins.vaulthunters.multicell;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import iskallia.vault.core.util.RegionPos;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.world.generator.layout.ClassicCircleLayout;
import iskallia.vault.core.world.generator.layout.ClassicInfiniteLayout;
import iskallia.vault.core.world.generator.layout.ClassicPolygonLayout;
import iskallia.vault.core.world.generator.layout.ClassicPresetLayout;
import iskallia.vault.core.world.generator.layout.ClassicSpiralLayout;
import iskallia.vault.core.world.generator.layout.VaultLayout;
import org.spongepowered.asm.mixin.Mixin;
import xyz.iwolfking.woldsvaults.api.core.layout.impl.ClassicRingsLayout;
import xyz.iwolfking.woldsvaults.api.core.layout.impl.ClassicTunnelLayout;
import xyz.iwolfking.woldsvaults.api.core.layout.impl.ClassicWaveLayout;
import xyz.iwolfking.woldsvaults.api.core.layout.multicell.MultiCellRooms;

@Mixin(value = {
        ClassicInfiniteLayout.class,
        ClassicCircleLayout.class,
        ClassicPolygonLayout.class,
        ClassicSpiralLayout.class,
        ClassicPresetLayout.class,
        ClassicWaveLayout.class,
        ClassicRingsLayout.class,
        ClassicTunnelLayout.class
}, remap = false)
public abstract class MixinClassicLayoutsMultiCell {

    /** Reports every cell covered by a multi-cell room as ROOM, on the outermost layout query only. */
    @WrapMethod(method = "getType")
    private VaultLayout.PieceType woldsvaults$overlayMultiCellRooms(Vault vault, RegionPos region, Operation<VaultLayout.PieceType> original) {
        return MultiCellRooms.overlay(vault, region, () -> original.call(vault, region));
    }
}
