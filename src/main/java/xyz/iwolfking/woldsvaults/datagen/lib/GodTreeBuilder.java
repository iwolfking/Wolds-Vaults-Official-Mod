package xyz.iwolfking.woldsvaults.datagen.lib;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Builder for one god's constellation tree definition, serialized to
 * {@code data/woldsvaults/god_trees/<god>.json} and parsed at runtime by
 * {@code GodTreeDefinition}. Edges are undirected; every node costs one god point. Ids must be
 * unique and every edge endpoint must be a declared node - {@link #build()} throws otherwise so
 * a broken layout fails the datagen run instead of shipping.
 */
public final class GodTreeBuilder {
    private final String god;
    private final JsonArray nodes = new JsonArray();
    private final JsonArray edges = new JsonArray();
    private final JsonArray labels = new JsonArray();
    private final Set<String> ids = new HashSet<>();
    private final Set<String> edgeKeys = new HashSet<>();

    public GodTreeBuilder(String god) {
        this.god = god;
    }

    public GodTreeBuilder root(String id, String name, int x, int y) {
        return this.add(id, name, "root", null, null, x, y, true);
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

    /**
     * A major whose mechanics are not implemented yet: rendered and inspectable in the tree, but
     * refused by the purchase path on both sides until it is re-declared with {@link #major}.
     */
    public GodTreeBuilder disabledMajor(String id, String name, String effect, String icon, int x, int y) {
        return this.add(id, name, "major", effect, icon, x, y, false);
    }

    /** A stat placement reserved for a shelved mechanic, like {@link #disabledMajor}. */
    public GodTreeBuilder disabledStat(String id, String name, String effect, String icon, int x, int y) {
        return this.add(id, name, "stat", effect, icon, x, y, false);
    }

    /** A minor reserved for a shelved mechanic, like {@link #disabledMajor}. */
    public GodTreeBuilder disabledMinor(String id, String name, String effect, String icon, int x, int y) {
        return this.add(id, name, "minor", effect, icon, x, y, false);
    }

    public GodTreeBuilder edge(String from, String to) {
        String key = from.compareTo(to) < 0 ? from + "|" + to : to + "|" + from;
        if (!this.edgeKeys.add(key)) {
            throw new IllegalStateException("Duplicate god tree edge " + from + " <-> " + to);
        }
        JsonObject edge = new JsonObject();
        edge.addProperty("from", from);
        edge.addProperty("to", to);
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
        if (icon != null) {
            node.addProperty("icon", icon);
        }
        node.addProperty("x", x);
        node.addProperty("y", y);
        if (!enabled) {
            node.addProperty("enabled", false);
        }
        this.nodes.add(node);
        return this;
    }

    public JsonObject build() {
        for (var element : this.edges) {
            JsonObject edge = element.getAsJsonObject();
            String from = edge.get("from").getAsString();
            String to = edge.get("to").getAsString();
            if (!this.ids.contains(from) || !this.ids.contains(to)) {
                throw new IllegalStateException("God tree edge references undeclared node: " + from + " <-> " + to);
            }
        }
        JsonObject root = new JsonObject();
        root.addProperty("god", this.god);
        root.add("nodes", this.nodes);
        root.add("edges", this.edges);
        root.add("labels", this.labels);
        return root;
    }
}
