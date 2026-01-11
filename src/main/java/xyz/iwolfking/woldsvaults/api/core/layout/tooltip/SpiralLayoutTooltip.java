package xyz.iwolfking.woldsvaults.api.core.layout.tooltip;

import iskallia.vault.core.world.generator.layout.ClassicSpiralLayout;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Rotation;
import xyz.iwolfking.woldsvaults.api.core.layout.tooltip.component.LayoutTooltipComponent;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Optional;

public class SpiralLayoutTooltip {

    /**
     * {@link ClassicSpiralLayout} wtf
     */
    public static @Nonnull Optional<LayoutTooltipComponent> getTooltipImage(CompoundTag data) {
        int halfLength = data.getInt("halfLength");
        int tunnelSpan = data.getInt("tunnel");

        // rotate clockwise, because we are drawing west facing spiral
        Rotation rotation = Rotation.valueOf(data.getString("rotation")).getRotated(Rotation.CLOCKWISE_90);

        int dia = halfLength * 2 + 1;

        LayoutTooltipComponent.RoomType[][] rooms = new LayoutTooltipComponent.RoomType[dia][dia];
        LayoutTooltipComponent.Tunnel[][] hTunnels = new LayoutTooltipComponent.Tunnel[dia][dia];
        LayoutTooltipComponent.Tunnel[][] vTunnels = new LayoutTooltipComponent.Tunnel[dia][dia];

        for (int r = 0; r < dia; r++) {
            Arrays.fill(rooms[r], LayoutTooltipComponent.RoomType.EMPTY);
            Arrays.fill(hTunnels[r], LayoutTooltipComponent.Tunnel.EMPTY);
            if (r < dia) {Arrays.fill(vTunnels[r], LayoutTooltipComponent.Tunnel.EMPTY);}
        }

        for (int x = -halfLength; x <= halfLength; x++) {
            for (int z = -halfLength; z <= halfLength; z++) {
                int distance = Math.max(Math.abs(x), Math.abs(z));
                if (distance > halfLength) {continue;}

                int row = x + halfLength;
                int col = z + halfLength;
                rooms[row][col] = (x == 0 && z == 0) ? LayoutTooltipComponent.RoomType.PORTAL : LayoutTooltipComponent.RoomType.ROOM;
            }
        }

        for (int x = -halfLength; x <= halfLength; x++) {
            for (int z = -halfLength; z <= halfLength; z++) {
                int row = x + halfLength;
                int col = z + halfLength;
                int ox = x;
                int oz = z;

                int temp = ox;
                ox = -oz;
                oz = -temp;

                int minHZ = 1 - Math.abs(oz);
                int maxHZ = oz > 0 ? oz : 1 - oz;
                if (col <= dia - 1 && ox >= minHZ && ox <= maxHZ) {
                    hTunnels[row][col] = LayoutTooltipComponent.Tunnel.TUNNEL;
                }

                ox = 2 * x;
                oz = 2 * z;
                temp = ox;
                ox = -oz;
                oz = -temp;

                int minVX = ox > 0 ? 3 - ox : ox + 1;
                int maxVX = Math.abs(ox);
                if (row <= dia - 1 && oz >= minVX && oz <= maxVX) {
                    vTunnels[row][col] = LayoutTooltipComponent.Tunnel.TUNNEL;
                }
            }
        }

        if (rotation == Rotation.NONE || rotation == Rotation.CLOCKWISE_180) {
            hTunnels = rotateHTunnels(hTunnels, rotation);
            vTunnels = rotateVTunnels(vTunnels, rotation);
        }
        if (rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90) {
            var origvTunnels = vTunnels;
            vTunnels = rotateHTunnels(hTunnels, rotation);
            hTunnels = rotateVTunnels(origvTunnels, rotation);
        }

        return Optional.of(new LayoutTooltipComponent(rooms, hTunnels, vTunnels, tunnelSpan));
    }


    private static LayoutTooltipComponent.Tunnel[][] rotateHTunnels(LayoutTooltipComponent.Tunnel[][] grid, Rotation rotation) {
        int n = grid.length;
        LayoutTooltipComponent.Tunnel[][] res = new LayoutTooltipComponent.Tunnel[n][n];
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n - 1; c++) {
                switch (rotation) {
                    case NONE -> res[r][c] = grid[r][c];
                    case CLOCKWISE_90 -> res[c][n - 1 - r] = grid[r][c];
                    case CLOCKWISE_180 -> res[n - 1 - r][n - 2 - c] = grid[r][c];
                    case COUNTERCLOCKWISE_90 -> res[n - 2 - c][r] = grid[r][c];
                }
            }
        }
        return res;
    }

    private static LayoutTooltipComponent.Tunnel[][] rotateVTunnels(LayoutTooltipComponent.Tunnel[][] grid, Rotation rotation) {
        int n = grid.length;
        LayoutTooltipComponent.Tunnel[][] res = new LayoutTooltipComponent.Tunnel[n][n];
        for (int r = 0; r < n - 1; r++) {
            for (int c = 0; c < n; c++) {
                switch (rotation) {
                    case NONE -> res[r][c] = grid[r][c];
                    case CLOCKWISE_90 -> res[c][n - 2 - r] = grid[r][c];
                    case CLOCKWISE_180 -> res[n - 2 - r][n - 1 - c] = grid[r][c];
                    case COUNTERCLOCKWISE_90 -> res[n - 1 - c][r] = grid[r][c];
                }
            }
        }
        return res;
    }

}