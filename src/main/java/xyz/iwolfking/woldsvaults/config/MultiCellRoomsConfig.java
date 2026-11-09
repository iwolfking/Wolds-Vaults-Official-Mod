package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * The template pools whose rooms span more than one vault grid cell, generated as
 * {@code vault_configs/layout/multi_cell_rooms/multi_cell_rooms.json}. A pool listed here is placed as a
 * {@code span} x {@code span} cell room wherever a layout, an objective or an inscription forces it; its templates
 * are listed so the client can recognise a discovered room's size from its template id alone.
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
            if (entry.templates == null || entry.templates.isEmpty()) {
                return "pool " + entry.pool + " lists no templates";
            }
        }
        return null;
    }

    public static class Entry {
        @Expose public String pool;
        @Expose public int span;
        @Expose public List<String> templates;

        public static Entry of(String pool, int span, List<String> templates) {
            Entry entry = new Entry();
            entry.pool = pool;
            entry.span = span;
            entry.templates = new ArrayList<>(templates);
            return entry;
        }
    }
}
