package xyz.iwolfking.woldsvaults.api.core.layout.tooltip;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import xyz.iwolfking.woldsvaults.api.core.layout.impl.ClassicWaveLayout;
import xyz.iwolfking.woldsvaults.api.core.layout.tooltip.component.LayoutTooltipComponent;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Optional;

public class WaveLayoutTooltip {

    public static @Nonnull Optional<TooltipComponent> getTooltipImage(CompoundTag data) {
        int tunnelSpan = data.getInt("tunnel");
        int length = data.getInt("length");
        int amplitude = data.getInt("amplitude");
        double frequency = data.getDouble("frequency");
        var cwl = new ClassicWaveLayout(tunnelSpan, length, amplitude, frequency);

        length += 1; // idk why - probably <= vs <
        int dia =  length * 2;
        LayoutTooltipComponent.RoomType[][] rooms = new LayoutTooltipComponent.RoomType[dia][dia];
        LayoutTooltipComponent.Tunnel[][] hTunnels = new LayoutTooltipComponent.Tunnel[dia][dia - 1];
        LayoutTooltipComponent.Tunnel[][] vTunnels = new LayoutTooltipComponent.Tunnel[dia - 1][dia];
        for (int r = 0; r < dia; r++) {
            Arrays.fill(rooms[r], LayoutTooltipComponent.RoomType.EMPTY);
            if (r < dia - 1) {Arrays.fill(vTunnels[r], LayoutTooltipComponent.Tunnel.EMPTY);}
            Arrays.fill(hTunnels[r], LayoutTooltipComponent.Tunnel.EMPTY);
        }

        for (int z = -length; z < length; z++) {
            for (int x = -length; x < length; x++) {
                if (cwl.isRoomAllowed(x, z)) {
                    int row = z + length;
                    int col = x + length;

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

        return Optional.of(new LayoutTooltipComponent(rooms, hTunnels, vTunnels, tunnelSpan, null));

    }
}