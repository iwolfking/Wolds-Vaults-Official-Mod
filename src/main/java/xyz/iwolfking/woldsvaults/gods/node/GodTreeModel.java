package xyz.iwolfking.woldsvaults.gods.node;

import iskallia.vault.config.entry.SkillStyle;
import iskallia.vault.core.vault.influence.VaultGod;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * One god's tree, assembled from its three config files and validated. Immutable, and a
 * multi-parent graph, so reachability runs off adjacency rather than a parent field.
 */
public final class GodTreeModel {
    public record Edge(String from, String to) {
    }

    public record Label(String text, int x, int y) {
    }

    private final VaultGod god;
    private final Map<String, GodNode> nodes;
    private final List<Edge> edges;
    private final Map<String, Set<String>> adjacency;
    private final List<Label> labels;
    private final Map<String, SkillStyle> styles;
    private final Map<String, GodEffect> effects;
    private final Map<String, GodNodeHandler> handlers;
    private final Map<String, GodNodeType> effectTypes;
    private final Map<String, GodNode> placements;

    GodTreeModel(VaultGod god, Map<String, GodNode> nodes, List<Edge> edges, Map<String, Set<String>> adjacency,
                 List<Label> labels, Map<String, SkillStyle> styles, Map<String, GodEffect> effects,
                 Map<String, GodNodeHandler> handlers, Map<String, GodNodeType> effectTypes) {
        this.god = god;
        this.nodes = Collections.unmodifiableMap(nodes);
        this.edges = Collections.unmodifiableList(edges);
        this.adjacency = Collections.unmodifiableMap(adjacency);
        this.labels = Collections.unmodifiableList(labels);
        this.styles = Collections.unmodifiableMap(styles);
        this.effects = Collections.unmodifiableMap(effects);
        this.handlers = Collections.unmodifiableMap(handlers);
        this.effectTypes = Collections.unmodifiableMap(effectTypes);
        Map<String, GodNode> firstPlacements = new LinkedHashMap<>();
        for (GodNode node : nodes.values()) {
            firstPlacements.putIfAbsent(node.ledgerKey(), node);
        }
        this.placements = Collections.unmodifiableMap(firstPlacements);
    }

    public VaultGod getGod() {
        return this.god;
    }

    public Collection<GodNode> getNodes() {
        return this.nodes.values();
    }

    @Nullable
    public GodNode getNode(String id) {
        return this.nodes.get(id);
    }

    public List<Edge> getEdges() {
        return this.edges;
    }

    public Set<String> getAdjacent(String id) {
        return this.adjacency.getOrDefault(id, Collections.emptySet());
    }

    public List<Label> getLabels() {
        return this.labels;
    }

    @Nullable
    public SkillStyle getStyle(String nodeId) {
        return this.styles.get(nodeId);
    }

    public Collection<GodEffect> getEffects() {
        return this.effects.values();
    }

    @Nullable
    public GodEffect getEffect(String effectId) {
        return this.effects.get(effectId);
    }

    @Nullable
    public GodNodeHandler getHandler(String effectId) {
        return this.handlers.get(effectId);
    }

    /** The node type every placement of {@code effectId} shares. */
    @Nullable
    public GodNodeType getEffectType(String effectId) {
        return this.effectTypes.get(effectId);
    }

    /** The first placement that banks under {@code ledgerKey}, or null when nothing here does. */
    @Nullable
    public GodNode placementOf(String ledgerKey) {
        return this.placements.get(ledgerKey);
    }

    public boolean isEmpty() {
        return this.nodes.isEmpty();
    }

    /** Whether a node exists, is enabled, is unowned, and is a root or touches an owned neighbour. */
    public boolean isPurchasable(String nodeId, Predicate<String> purchased) {
        GodNode node = this.nodes.get(nodeId);
        if (node == null || !node.enabled() || purchased.test(nodeId)) {
            return false;
        }
        if (node.isRoot()) {
            return true;
        }
        for (String neighbour : this.getAdjacent(nodeId)) {
            if (purchased.test(neighbour)) {
                return true;
            }
        }
        return false;
    }
}
