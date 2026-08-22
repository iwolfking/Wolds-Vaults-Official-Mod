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
 * One god's constellation topology - {@code config/the_vault/gods/god_tree_<god>.json}. Holds
 * what a node IS (id, name, type, the effect it feeds, its cost) and how the lattice connects;
 * where it is drawn lives in {@link GodTreeGuiStylesConfig} and what its effect DOES lives in
 * {@link GodNodeEffectsConfig}, mirroring the talents / talents_gui_styles / skill_descriptions
 * triad the pack's other trees use.
 *
 * <p>A missing or unreadable file regenerates the shipped tree from {@link GodTreeDefaults}
 * rather than an empty one, and {@link PackAuthoredConfig} keeps the rejected file beside it, so a
 * hand-edit that fails to parse costs neither the tree nor the edit. {@link #isValid()} catches
 * everything wrong with this file that can be seen without reading the other two; anything needing
 * all three - an effect placed on no node, a handler type that is not registered - stays fatal in
 * {@code GodTreeLoader}, because falling back to one file's defaults cannot repair those.
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

    /**
     * Restores the god this config was constructed for. Gson allocates the loaded instance
     * without running the constructor, so the name the file was read under only survives by
     * being copied off the instance that read it.
     */
    @Override
    protected void onLoad(@Nullable Config oldConfigInstance) {
        if (oldConfigInstance instanceof GodTreeConfig previous) {
            this.god = previous.god;
        }
    }

    /**
     * Everything wrong with this file that is visible without the effect and style files: no nodes
     * at all, a node with no id or an unknown type, a duplicate id, no root to enter the tree from,
     * or an edge naming a node this file does not define. Failing here names the file in the base
     * reload report and falls back to the shipped tree; unlike a parse failure it leaves the file
     * on disk untouched, so the edit can be corrected in place.
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

    /**
     * Restores the shipped tree rather than an empty one.
     *
     * <p>The base config loader catches every read failure, calls {@code generateConfig()} and
     * writes the result straight back over the file, so an empty reset turned one bad character in
     * a hand-edited tree into permanent loss of that tree - and then a boot crash on every
     * restart, because validation found effects with no nodes left to place them on. Regenerating
     * from {@link GodTreeDefaults} makes that mistake self-healing.
     */
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
