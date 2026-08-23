package xyz.iwolfking.woldsvaults.config.gods;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import xyz.iwolfking.woldsvaults.config.PackAuthoredConfig;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * One god's constellation topology - {@code config/the_vault/gods/god_tree_<god>.json}: node id, name,
 * type, effect and cost, plus the lattice edges. Layout is {@link GodTreeGuiStylesConfig}'s.
 */
public class GodTreeConfig extends PackAuthoredConfig {
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

    private String god;

    @Expose private List<NodeEntry> nodes;
    @Expose private List<List<String>> edges;
    @Expose private List<LabelEntry> labels;

    public GodTreeConfig(String god) {
        this.god = god;
    }

    @Override
    public String getName() {
        return "gods" + File.separator + "god_tree_" + this.god;
    }

    /** Restores the god name, which Gson does not carry across into the loaded instance. */
    @Override
    protected void onLoad(@Nullable Config oldConfigInstance) {
        if (oldConfigInstance instanceof GodTreeConfig previous) {
            this.god = previous.god;
        }
    }

    /**
     * Refuses a file with no nodes, a node with no id, an unknown type, a duplicate id, no root, or an
     * edge naming an undefined node. A refusal falls back to the shipped tree, leaving the file untouched.
     */
    @Override
    protected boolean isValid() {
        if (this.nodes == null || this.nodes.isEmpty()) {
            return this.invalid("it defines no nodes");
        }
        Set<String> ids = new HashSet<>();
        boolean hasRoot = false;
        for (NodeEntry node : this.nodes) {
            if (node == null || node.id == null || node.id.isBlank()) {
                return this.invalid("a node has no id");
            }
            if (!ids.add(node.id)) {
                return this.invalid("node id '" + node.id + "' appears twice");
            }
            if (node.type == null || !TYPES.contains(node.type.toLowerCase(Locale.ROOT))) {
                return this.invalid("node '" + node.id + "' has unknown type '" + node.type + "'");
            }
            hasRoot |= "root".equalsIgnoreCase(node.type);
        }
        if (!hasRoot) {
            return this.invalid("no node is a root, so nothing in it could ever be bought");
        }
        if (this.edges != null) {
            for (List<String> edge : this.edges) {
                if (edge == null || edge.size() != 2) {
                    return this.invalid("an edge is not a pair of node ids");
                }
                for (String end : edge) {
                    if (!ids.contains(end)) {
                        return this.invalid("an edge names node '" + end + "', which it does not define");
                    }
                }
            }
        }
        return true;
    }

    private boolean invalid(String reason) {
        WoldsVaults.LOGGER.error("God tree config {} is unusable: {}. Falling back to the shipped tree.",
                this.getName(), reason);
        return false;
    }

    /** Restores the shipped tree from {@link GodTreeDefaults} rather than an empty one. */
    @Override
    protected void reset() {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.labels = new ArrayList<>();
        GodTreeBuilder shipped = GodTreeDefaults.forGod(this.god);
        if (shipped == null) {
            WoldsVaults.LOGGER.error("No shipped god tree for '{}'; regenerating it empty.", this.god);
            return;
        }
        GodTreeConfig defaults = this.getGson().fromJson(shipped.buildTree(), GodTreeConfig.class);
        this.nodes = defaults.nodes;
        this.edges = defaults.edges;
        this.labels = defaults.labels;
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
