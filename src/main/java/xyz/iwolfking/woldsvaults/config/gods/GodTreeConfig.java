package xyz.iwolfking.woldsvaults.config.gods;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import xyz.iwolfking.woldsvaults.config.WoldsTable;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * One god's constellation topology, generated as {@code vault_configs/gods/trees/<god>.json}: node id, name,
 * type, effect and cost, plus the lattice edges. Layout is {@link GodTreeGuiStylesConfig}'s.
 */
public class GodTreeConfig extends Config implements WoldsTable {
    public static class NodeEntry {
        @Expose public String id;
        @Expose public String name;
        @Expose public String type;
        @Expose @Nullable public String effect;
        @Expose @Nullable public Integer cost;
        @Expose @Nullable public Boolean enabled;
    }

    public static class LabelEntry {
        @Expose public String text;
        @Expose public int x;
        @Expose public int y;
    }

    private static final Set<String> TYPES = Set.of("root", "stat", "minor", "major");

    @Expose private List<NodeEntry> nodes;
    @Expose private List<List<String>> edges;
    @Expose private List<LabelEntry> labels;

    @Override
    public String getName() {
        return "gods" + File.separator + "trees";
    }

    /** Refuses a tree with no nodes, a node with no id, an unknown type, a duplicate id, no root, or an edge naming an undefined node. */
    @Nullable
    @Override
    public String validate() {
        if (this.nodes == null || this.nodes.isEmpty()) {
            return ("it defines no nodes");
        }
        Set<String> ids = new HashSet<>();
        boolean hasRoot = false;
        for (NodeEntry node : this.nodes) {
            if (node == null || node.id == null || node.id.isBlank()) {
                return ("a node has no id");
            }
            if (!ids.add(node.id)) {
                return ("node id '" + node.id + "' appears twice");
            }
            if (node.type == null || !TYPES.contains(node.type.toLowerCase(Locale.ROOT))) {
                return ("node '" + node.id + "' has unknown type '" + node.type + "'");
            }
            hasRoot |= "root".equalsIgnoreCase(node.type);
        }
        if (!hasRoot) {
            return ("no node is a root, so nothing in it could ever be bought");
        }
        if (this.edges != null) {
            for (List<String> edge : this.edges) {
                if (edge == null || edge.size() != 2) {
                    return ("an edge is not a pair of node ids");
                }
                for (String end : edge) {
                    if (!ids.contains(end)) {
                        return ("an edge names node '" + end + "', which it does not define");
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected void reset() {
    }

    public List<NodeEntry> getNodes() {
        return this.nodes == null ? Collections.emptyList() : this.nodes;
    }

    public List<List<String>> getEdges() {
        return this.edges == null ? Collections.emptyList() : this.edges;
    }

    public List<LabelEntry> getLabels() {
        return this.labels == null ? Collections.emptyList() : this.labels;
    }
}
