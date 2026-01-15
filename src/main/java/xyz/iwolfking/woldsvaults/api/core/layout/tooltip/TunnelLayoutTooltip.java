package xyz.iwolfking.woldsvaults.api.core.layout.tooltip;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.NotNull;
import xyz.iwolfking.woldsvaults.api.core.layout.impl.ClassicTunnelLayout;
import xyz.iwolfking.woldsvaults.api.core.layout.tooltip.component.LayoutTooltipComponent;

import java.util.Arrays;
import java.util.Optional;

public class TunnelLayoutTooltip {

    public static @NotNull Optional<TooltipComponent> getTooltipImage(CompoundTag data) {
        int width = data.getInt("width");
        int height = data.getInt("height");
        int branchInterval = data.getInt("branchInterval");
        int tunnelSpan = data.getInt("tunnel");

        var csl = new ClassicTunnelLayout(tunnelSpan, width, height, branchInterval);

        width += 2;
        int dia = width *2;
        LayoutTooltipComponent.RoomType[][] rooms = new LayoutTooltipComponent.RoomType[dia][dia];
        LayoutTooltipComponent.Tunnel[][] hTunnels = new LayoutTooltipComponent.Tunnel[dia][dia - 1];
        LayoutTooltipComponent.Tunnel[][] vTunnels = new LayoutTooltipComponent.Tunnel[dia - 1][dia];
        for (int r = 0; r < dia; r++) {
            Arrays.fill(rooms[r], LayoutTooltipComponent.RoomType.EMPTY);
            if (r < dia - 1) {Arrays.fill(vTunnels[r], LayoutTooltipComponent.Tunnel.EMPTY);}
            Arrays.fill(hTunnels[r], LayoutTooltipComponent.Tunnel.EMPTY);
        }

        for (int z = -width; z < width; z++) {
            for (int x = -width; x < width; x++) {
                if (csl.isRoomAllowed(x, z)) {
                    int row = z + width;
                    int col = x + width;

                    rooms[row][col] = (x == 0 && z == 0) ? LayoutTooltipComponent.RoomType.PORTAL : LayoutTooltipComponent.RoomType.ROOM;
                }
            }
        }

        for (int row = 0; row < dia; row++) {
            for (int col = 0; col < dia - 1; col++) {
                if (rooms[row][col] != LayoutTooltipComponent.RoomType.EMPTY &&
                    rooms[row][col + 1] != LayoutTooltipComponent.RoomType.EMPTY) {
                    hTunnels[row][col] = LayoutTooltipComponent.Tunnel.TUNNEL;
                }
            }
        }

        for (int row = 0; row < dia - 1; row++) {
            for (int col = 0; col < dia; col++) {
                if (rooms[row][col] != LayoutTooltipComponent.RoomType.EMPTY &&
                    rooms[row + 1][col] != LayoutTooltipComponent.RoomType.EMPTY) {
                    vTunnels[row][col] = LayoutTooltipComponent.Tunnel.TUNNEL;
                }
            }
        }

        return Optional.of(new LayoutTooltipComponent(rooms, hTunnels, vTunnels, tunnelSpan));
    }
}