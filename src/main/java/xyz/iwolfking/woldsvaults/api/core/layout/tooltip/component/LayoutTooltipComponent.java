package xyz.iwolfking.woldsvaults.api.core.layout.tooltip.component;


import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record LayoutTooltipComponent(RoomType[][] rooms, Tunnel[][] horizontalTunnels, Tunnel[][] verticalTunnels, int tunnelSpan, Long seed) implements TooltipComponent {

    public LayoutTooltipComponent(RoomType[][] rooms, Tunnel[][] horizontalTunnels, Tunnel[][] verticalTunnels, int tunnelSpan, Long seed) {
        Bounds b = findRoomBounds(rooms);
        this.rooms = trimEmptyRooms(rooms, b);
        this.horizontalTunnels = trimEmptyHorizontalTunnels(horizontalTunnels, b);
        this.verticalTunnels = trimEmptyVerticalTunnels(verticalTunnels, b);
        this.tunnelSpan = tunnelSpan;
        this.seed = seed;
    }

    /**
     * removes empty cells from rooms
     */
    public static RoomType[][] trimEmptyRooms(RoomType[][] rooms, Bounds b) {
        RoomType[][] out = new RoomType[b.height()][b.width()];
        for (int r = 0; r < b.height(); r++) {
            System.arraycopy(rooms[b.top + r], b.left, out[r], 0, b.width());
        }
        return out;
    }


    public static Bounds findRoomBounds(RoomType[][] rooms) {

        int h = rooms.length;
        int w = rooms[0].length;

        int top = 0, bottom = h - 1;
        int left = 0, right = w - 1;

//        if (1==1) return  new Bounds(top, bottom, left, right);
        while (top <= bottom && isRowEmpty(rooms, top)) top++;
        while (bottom >= top && isRowEmpty(rooms, bottom)) bottom--;
        while (left <= right && isColEmpty(rooms, left)) left++;
        while (right >= left && isColEmpty(rooms, right)) right--;

        return new Bounds(top, bottom, left, right);
    }


    private static boolean isRowEmpty(RoomType[][] rooms, int row) {
        for (RoomType r : rooms[row]) {
            if (r != RoomType.EMPTY) return false;
        }
        return true;
    }

    private static boolean isColEmpty(RoomType[][] rooms, int col) {
        for (RoomType[] room : rooms) {
            if (room[col] != RoomType.EMPTY) return false;
        }
        return true;
    }

    public static Tunnel[][] trimEmptyHorizontalTunnels(Tunnel[][] hTunnels, Bounds b) {
        int newH = b.height();
        int newW = b.width() - 1;

        Tunnel[][] out = new Tunnel[newH][Math.max(0, newW)];
        for (int r = 0; r < newH; r++) {
            if (newW > 0) {
                System.arraycopy(
                    hTunnels[b.top + r],
                    b.left,
                    out[r],
                    0,
                    newW
                );
            }
        }
        return out;
    }

    public static Tunnel[][] trimEmptyVerticalTunnels(Tunnel[][] vTunnels, Bounds b) {
        int newH = b.height() - 1;
        int newW = b.width();

        Tunnel[][] out = new Tunnel[Math.max(0, newH)][newW];
        for (int r = 0; r < newH; r++) {
            System.arraycopy(
                vTunnels[b.top + r],
                b.left,
                out[r],
                0,
                newW
            );
        }
        return out;
    }


    public enum RoomType {
        ROOM,
        INSCRIPTION,
        PORTAL,
        EMPTY
    }

    public enum Tunnel {
        TUNNEL,
        EMPTY
    }

    public record Bounds(int top, int bottom, int left, int right) {
        public int width()  { return right - left + 1; }
        public int height() { return bottom - top + 1; }
    }

}