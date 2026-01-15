package xyz.iwolfking.woldsvaults.api.core.layout.tooltip;

import iskallia.vault.core.world.generator.layout.ClassicPolygonLayout;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import xyz.iwolfking.woldsvaults.api.core.layout.tooltip.component.LayoutTooltipComponent;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Optional;

public class PolygonLayoutTooltip {

    public static @Nonnull Optional<TooltipComponent> getTooltipImage(CompoundTag data) {
        int[] vertices = data.getIntArray("vertices");
        int tunnelSpan = data.getInt("tunnel");

        // grid size
        int minX = vertices[0];
        int maxX = vertices[0];
        int minZ = vertices[1];
        int maxZ = vertices[1];


        for (int i = 0; i < vertices.length; i += 2) {
            int x = vertices[i];
            int z = vertices[i + 1];
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minZ = Math.min(minZ, z);
            maxZ = Math.max(maxZ, z);
        }

        int width = maxX - minX + 1;
        int height = maxZ - minZ + 1;

        LayoutTooltipComponent.RoomType[][] rooms = new LayoutTooltipComponent.RoomType[height][width];
        LayoutTooltipComponent.Tunnel[][] hTunnels = new LayoutTooltipComponent.Tunnel[height][width - 1];
        LayoutTooltipComponent.Tunnel[][] vTunnels = new LayoutTooltipComponent.Tunnel[height - 1][width];

        for (int row = 0; row < height; row++) {
            Arrays.fill(rooms[row], LayoutTooltipComponent.RoomType.EMPTY);
        }
        for (int row = 0; row < height; row++) {
            Arrays.fill(hTunnels[row], LayoutTooltipComponent.Tunnel.EMPTY);
        }

        // rooms
        var cpl = new ClassicPolygonLayout(tunnelSpan, vertices);
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (cpl.containsPoint(x, z)) {
                    int row = z - minZ;
                    int col = x - minX;
                    rooms[row][col] = (x == 0 && z == 0) ? LayoutTooltipComponent.RoomType.PORTAL : LayoutTooltipComponent.RoomType.ROOM;
                }
            }
        }

        // horizontal tunnels
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width - 1; col++) {
                if (rooms[row][col] != LayoutTooltipComponent.RoomType.EMPTY
                    && rooms[row][col + 1] != LayoutTooltipComponent.RoomType.EMPTY) {
                    hTunnels[row][col] = LayoutTooltipComponent.Tunnel.TUNNEL;
                }
            }
        }

        // vertical tunnels
        for (int row = 0; row < height - 1; row++) {
            for (int col = 0; col < width; col++) {
                if (rooms[row][col] != LayoutTooltipComponent.RoomType.EMPTY
                    && rooms[row + 1][col] != LayoutTooltipComponent.RoomType.EMPTY) {
                    vTunnels[row][col] = LayoutTooltipComponent.Tunnel.TUNNEL;
                }
            }
        }
        return Optional.of(new LayoutTooltipComponent(rooms, hTunnels, vTunnels, tunnelSpan));
    }
}