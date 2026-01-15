package xyz.iwolfking.woldsvaults.api.core.layout.tooltip;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import xyz.iwolfking.woldsvaults.api.core.layout.tooltip.component.LayoutTooltipComponent;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Optional;

public class RingsLayoutTooltip {

    public static @Nonnull Optional<TooltipComponent> getTooltipImage(CompoundTag data) {
        int tunnelSpan = data.getInt("tunnel");
        int radius = data.getInt("radius");
        int ringInterval = data.getInt("ringInterval");

        int numRings = Math.max(1, radius / ringInterval);

        int dia = 1 + radius * 2;
        LayoutTooltipComponent.RoomType[][] rooms = new LayoutTooltipComponent.RoomType[dia][dia];
        LayoutTooltipComponent.Tunnel[][] hTunnels = new LayoutTooltipComponent.Tunnel[dia][dia - 1];
        LayoutTooltipComponent.Tunnel[][] vTunnels = new LayoutTooltipComponent.Tunnel[dia - 1][dia];
        for (int r = 0; r < dia; r++) {
            Arrays.fill(rooms[r], LayoutTooltipComponent.RoomType.EMPTY);
            if (r < dia - 1) {Arrays.fill(vTunnels[r], LayoutTooltipComponent.Tunnel.EMPTY);}
            Arrays.fill(hTunnels[r], LayoutTooltipComponent.Tunnel.EMPTY);
        }

        int r2 = radius * radius;

        // rooms
        for (int z = -radius; z <= radius; z++) {
            for (int x = -radius; x <= radius; x++) {
                if (x * x + z * z <= r2) {
                    int row = z + radius;
                    int col = x + radius;

                    int dist = Math.max(Math.abs(x), Math.abs(z));

                    if (dist == 0) {
                        rooms[row][col] = (x == 0 && z == 0) ? LayoutTooltipComponent.RoomType.PORTAL : LayoutTooltipComponent.RoomType.ROOM;
                    }

                    if (dist > numRings)  {
                        continue;
                    }

                    if (x == 0 || z == 0) {
                        rooms[row][col] = (x == 0 && z == 0) ? LayoutTooltipComponent.RoomType.PORTAL : LayoutTooltipComponent.RoomType.ROOM;
                    }

                    if (dist % ringInterval == 0) {
                        rooms[row][col] = (x == 0 && z == 0) ? LayoutTooltipComponent.RoomType.PORTAL : LayoutTooltipComponent.RoomType.ROOM;
                    }
                }
            }
        }

        // horizontal tunnel
        for (int row = 0; row < dia; row++) {
            for (int col = 0; col < dia - 1; col++) {
                if (rooms[row][col] != LayoutTooltipComponent.RoomType.EMPTY &&
                    rooms[row][col + 1] != LayoutTooltipComponent.RoomType.EMPTY) {
                    hTunnels[row][col] = LayoutTooltipComponent.Tunnel.TUNNEL;
                }
            }
        }

        // vertical tunnel
        for (int row = 0; row < dia - 1; row++) {
            for (int col = 0; col < dia; col++) {
                if (rooms[row][col] != LayoutTooltipComponent.RoomType.EMPTY &&
                    rooms[row + 1][col] != LayoutTooltipComponent.RoomType.EMPTY) {
                    vTunnels[row][col] = LayoutTooltipComponent.Tunnel.TUNNEL;
                }
            }
        }
        return Optional.of(new LayoutTooltipComponent(rooms, hTunnels, vTunnels, tunnelSpan, null));
    }
}