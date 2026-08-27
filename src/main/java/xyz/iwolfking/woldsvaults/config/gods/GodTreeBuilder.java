package xyz.iwolfking.woldsvaults.config.gods;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Builder for one god's constellation, emitted as {@code god_tree_<god>.json} (topology) and
 * {@code god_tree_<god>_gui_styles.json} (layout). Edges are undirected id pairs and every node costs
 * one god point; a duplicate id or an edge naming an undeclared node throws.
 */
public final class GodTreeBuilder {
    private static final String FRAME_STAT = "CIRCLE";
    private static final String FRAME_NOTABLE = "SQUARE";
    private static final int DEFAULT_COST = 1;

    private final String god;
    private final JsonArray nodes = new JsonArray();
    private final JsonArray edges = new JsonArray();
    private final JsonArray labels = new JsonArray();
    private final Map<String, JsonObject> styles = new LinkedHashMap<>();
    private final Set<String> ids = new HashSet<>();
    private final Set<String> edgeKeys = new HashSet<>();

    public GodTreeBuilder(String god) {
        this.god = god;
    }

    /** A constellation's entry point; its effect is named after the node, so its ledger key is its id. */
    public GodTreeBuilder root(String id, String name, int x, int y) {
        return this.add(id, name, "root", id, null, x, y, true);
    }

    public GodTreeBuilder stat(String id, String name, String effect, String icon, int x, int y) {
        return this.add(id, name, "stat", effect, icon, x, y, true);
    }

    public GodTreeBuilder minor(String id, String name, String effect, String icon, int x, int y) {
        return this.add(id, name, "minor", effect, icon, x, y, true);
    }

    public GodTreeBuilder major(String id, String name, String effect, String icon, int x, int y) {
        return this.add(id, name, "major", effect, icon, x, y, true);
    }

    /** A major rendered and inspectable in the tree, but refused by the purchase path on both sides. */
    public GodTreeBuilder disabledMajor(String id, String name, String effect, String icon, int x, int y) {
        return this.add(id, name, "major", effect, icon, x, y, false);
    }

    public GodTreeBuilder disabledStat(String id, String name, String effect, String icon, int x, int y) {
        return this.add(id, name, "stat", effect, icon, x, y, false);
    }

    public GodTreeBuilder disabledMinor(String id, String name, String effect, String icon, int x, int y) {
        return this.add(id, name, "minor", effect, icon, x, y, false);
    }

    public GodTreeBuilder edge(String from, String to) {
        String key = from.compareTo(to) < 0 ? from + "|" + to : to + "|" + from;
        if (!this.edgeKeys.add(key)) {
            throw new IllegalStateException("Duplicate god tree edge " + from + " <-> " + to);
        }
        JsonArray edge = new JsonArray();
        edge.add(from);
        edge.add(to);
        this.edges.add(edge);
        return this;
    }

    public GodTreeBuilder label(String text, int x, int y) {
        JsonObject label = new JsonObject();
        label.addProperty("text", text);
        label.addProperty("x", x);
        label.addProperty("y", y);
        this.labels.add(label);
        return this;
    }

    private GodTreeBuilder add(String id, String name, String type, String effect, String icon, int x, int y, boolean enabled) {
        if (!this.ids.add(id)) {
            throw new IllegalStateException("Duplicate god tree node id " + id);
        }
        JsonObject node = new JsonObject();
        node.addProperty("id", id);
        node.addProperty("name", name);
        node.addProperty("type", type);
        if (effect != null) {
            node.addProperty("effect", effect);
        }
        node.addProperty("cost", DEFAULT_COST);
        node.addProperty("enabled", enabled);
        this.nodes.add(node);

        JsonObject style = new JsonObject();
        style.addProperty("x", x);
        style.addProperty("y", y);
        style.addProperty("frameType", "stat".equals(type) ? FRAME_STAT : FRAME_NOTABLE);
        if (icon != null) {
            style.addProperty("icon", icon);
        }
        this.styles.put(id, style);
        return this;
    }

    /** The topology half, {@code god_tree_<god>.json}, read by {@code GodTreeConfig}. */
    public JsonObject buildTree() {
        for (var element : this.edges) {
            JsonArray edge = element.getAsJsonArray();
            String from = edge.get(0).getAsString();
            String to = edge.get(1).getAsString();
            if (!this.ids.contains(from) || !this.ids.contains(to)) {
                throw new IllegalStateException("God tree edge references undeclared node: " + from + " <-> " + to
                        + " in " + this.god + "'s tree");
            }
        }
        JsonObject root = new JsonObject();
        root.add("nodes", this.nodes);
        root.add("edges", this.edges);
        root.add("labels", this.labels);
        return root;
    }

    /** The layout half, {@code god_tree_<god>_gui_styles.json}, read by {@code GodTreeGuiStylesConfig}. */
    public JsonObject buildStyles() {
        JsonObject entries = new JsonObject();
        this.styles.forEach(entries::add);
        JsonObject root = new JsonObject();
        root.add("styles", entries);
        return root;
    }
}
