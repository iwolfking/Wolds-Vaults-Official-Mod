package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.api.core.layout.multicell.TallRooms;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * The template pools whose rooms span more than one vault grid cell, generated as
 * {@code vault_configs/layout/multi_cell_rooms/multi_cell_rooms.json}. A pool listed here is placed as a
 * {@code span} x {@code span} cell room wherever a layout, an objective or an inscription forces it; its templates
 * are listed so the client can recognise a discovered room's size from its template id alone. A {@code height} above
 * the ordinary room height makes the room tall: the bedrock fill opens above it and a bedrock shell seals it.
 */
public class MultiCellRoomsConfig extends Config implements WoldsTable {
    @Expose public List<Entry> rooms;

    @Override
    public String getName() {
        return "layout" + File.separator + "multi_cell_rooms";
    }

    @Override
    protected void reset() {
    }

    public static MultiCellRoomsConfig inert() {
        MultiCellRoomsConfig config = new MultiCellRoomsConfig();
        config.rooms = new ArrayList<>();
        return config;
    }

    /** Span in cells of the multi-cell pool with this id, or 1 if the pool is an ordinary room pool. */
    public int spanOfPool(ResourceLocation pool) {
        if (pool == null || this.rooms == null) {
            return 1;
        }
        String id = pool.toString();
        for (Entry entry : this.rooms) {
            if (id.equals(entry.pool)) {
                return entry.span;
            }
        }
        return 1;
    }

    /** Height in blocks of the multi-cell pool with this id: its entry's height, or the ordinary room height. */
    public int heightOfPool(ResourceLocation pool) {
        if (pool == null || this.rooms == null) {
            return TallRooms.ROOM_HEIGHT;
        }
        String id = pool.toString();
        for (Entry entry : this.rooms) {
            if (id.equals(entry.pool)) {
                return entry.height > 0 ? entry.height : TallRooms.ROOM_HEIGHT;
            }
        }
        return TallRooms.ROOM_HEIGHT;
    }

    /** Span in cells of the room built from this template id, or 1 if it is an ordinary room. */
    public int spanOfTemplate(ResourceLocation template) {
        if (template == null || this.rooms == null) {
            return 1;
        }
        String id = template.toString();
        for (Entry entry : this.rooms) {
            if (entry.templates.contains(id)) {
                return entry.span;
            }
        }
        return 1;
    }

    @Nullable
    @Override
    public String validate() {
        if (this.rooms == null) {
            return "rooms is missing";
        }
        for (Entry entry : this.rooms) {
            if (entry == null || entry.pool == null || ResourceLocation.tryParse(entry.pool) == null) {
                return "an entry has no valid pool id";
            }
            if (entry.span < 3 || entry.span % 2 == 0) {
                return "pool " + entry.pool + " has span " + entry.span + "; spans must be odd and at least 3 to stay on the room grid";
            }
            if (entry.height != 0 && (entry.height < TallRooms.ROOM_HEIGHT || entry.height > TallRooms.MAX_HEIGHT)) {
                return "pool " + entry.pool + " has height " + entry.height + "; heights must be 0 (ordinary) or "
                        + TallRooms.ROOM_HEIGHT + " to " + TallRooms.MAX_HEIGHT;
            }
            if (entry.templates == null || entry.templates.isEmpty()) {
                return "pool " + entry.pool + " lists no templates";
            }
        }
        return null;
    }

    public static class Entry {
        @Expose public String pool;
        @Expose public int span;
        @Expose public int height;
        @Expose public List<String> templates;

        public static Entry of(String pool, int span, List<String> templates) {
            return of(pool, span, 0, templates);
        }

        public static Entry of(String pool, int span, int height, List<String> templates) {
            Entry entry = new Entry();
            entry.pool = pool;
            entry.span = span;
            entry.height = height;
            entry.templates = new ArrayList<>(templates);
            return entry;
        }
    }
}
