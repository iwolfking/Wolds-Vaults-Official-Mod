package xyz.iwolfking.woldsvaults.config.gods;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One god's constellation topology - {@code config/the_vault/gods/god_tree_<god>.json}. Holds
 * what a node IS (id, name, type, the effect it feeds, its cost) and how the lattice connects;
 * where it is drawn lives in {@link GodTreeGuiStylesConfig} and what its effect DOES lives in
 * {@link GodNodeEffectsConfig}, mirroring the talents / talents_gui_styles / skill_descriptions
 * triad the pack's other trees use.
 *
 * <p>Shipping empty is a legitimate state: a god whose tree has not been authored yet simply has
 * no nodes, and validation has nothing to reject.
 */
public class GodTreeConfig extends Config {
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

    @Override
    protected void reset() {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.labels = new ArrayList<>();
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
