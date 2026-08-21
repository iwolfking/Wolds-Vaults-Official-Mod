package xyz.iwolfking.woldsvaults.gods.tree;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * The static shape of one god's constellation tree: every node with its screen position, the
 * undirected lattice edges between them, and the constellation labels drawn onto the star chart.
 * Instances are parsed from the datagen-authored {@code data/woldsvaults/god_trees/<god>.json}
 * and are immutable; per-player purchase state lives in
 * {@link xyz.iwolfking.woldsvaults.gods.GodAlignmentData}.
 *
 * <p>The lattice is a true multi-parent graph - most nodes touch several neighbours and the
 * limbs contain loops - so reachability is expressed through adjacency rather than a parent
 * field: a node is purchasable when it is a root or when any adjacent node is already owned.
 *
 * <p>Node identity is two-layered. The node {@code id} is unique per tree position and is what
 * the purchase ledger's tree-node set stores; the {@code effect} names the shared mechanic the
 * node feeds (many stat nodes feed the same effect). {@link Node#ledgerKey()} is the key god
 * points are banked under, which the wave-2 node handlers read through
 * {@code GodAlignmentData.getPointsIn}.
 */
public final class GodTreeDefinition {
    public enum NodeType {
        ROOT, STAT, MINOR, MAJOR;

        static NodeType fromName(String name) {
            return NodeType.valueOf(name.toUpperCase(Locale.ROOT));
        }
    }

    public record Node(String id, String name, NodeType type, @Nullable String effect,
                       @Nullable ResourceLocation icon, int x, int y, boolean enabled) {
        /** The key this node's god points are banked under - its effect, or its own id for roots. */
        public String ledgerKey() {
            return this.effect != null ? this.effect : this.id;
        }

        public int cost() {
            return 1;
        }
    }

    public record Label(String text, int x, int y) {
    }

    public record Edge(String from, String to) {
    }

    private final VaultGod god;
    private final Map<String, Node> nodes;
    private final List<Edge> edges;
    private final Map<String, Set<String>> adjacency;
    private final List<Label> labels;

    private GodTreeDefinition(VaultGod god, Map<String, Node> nodes, List<Edge> edges,
                              Map<String, Set<String>> adjacency, List<Label> labels) {
        this.god = god;
        this.nodes = Collections.unmodifiableMap(nodes);
        this.edges = Collections.unmodifiableList(edges);
        this.adjacency = adjacency;
        this.labels = Collections.unmodifiableList(labels);
    }

    public static GodTreeDefinition parse(JsonObject json) {
        VaultGod god = VaultGod.fromName(json.get("god").getAsString());
        if (god == null) {
            throw new IllegalArgumentException("Unknown god '" + json.get("god").getAsString() + "' in god tree definition");
        }
        Map<String, Node> nodes = new LinkedHashMap<>();
        for (JsonElement element : json.getAsJsonArray("nodes")) {
            JsonObject nodeJson = element.getAsJsonObject();
            String id = nodeJson.get("id").getAsString();
            Node node = new Node(
                    id,
                    nodeJson.get("name").getAsString(),
                    NodeType.fromName(nodeJson.get("type").getAsString()),
                    nodeJson.has("effect") ? nodeJson.get("effect").getAsString() : null,
                    nodeJson.has("icon") ? new ResourceLocation(nodeJson.get("icon").getAsString()) : null,
                    nodeJson.get("x").getAsInt(),
                    nodeJson.get("y").getAsInt(),
                    !nodeJson.has("enabled") || nodeJson.get("enabled").getAsBoolean());
            if (nodes.put(id, node) != null) {
                throw new IllegalArgumentException("Duplicate god tree node id " + id);
            }
        }
        List<Edge> edges = new ArrayList<>();
        Map<String, Set<String>> adjacency = new LinkedHashMap<>();
        for (JsonElement element : json.getAsJsonArray("edges")) {
            JsonObject edgeJson = element.getAsJsonObject();
            String from = edgeJson.get("from").getAsString();
            String to = edgeJson.get("to").getAsString();
            if (!nodes.containsKey(from) || !nodes.containsKey(to)) {
                throw new IllegalArgumentException("God tree edge references unknown node: " + from + " -> " + to);
            }
            edges.add(new Edge(from, to));
            adjacency.computeIfAbsent(from, k -> new LinkedHashSet<>()).add(to);
            adjacency.computeIfAbsent(to, k -> new LinkedHashSet<>()).add(from);
        }
        adjacency.replaceAll((id, neighbours) -> Collections.unmodifiableSet(neighbours));
        List<Label> labels = new ArrayList<>();
        if (json.has("labels")) {
            for (JsonElement element : json.getAsJsonArray("labels")) {
                JsonObject labelJson = element.getAsJsonObject();
                labels.add(new Label(labelJson.get("text").getAsString(),
                        labelJson.get("x").getAsInt(), labelJson.get("y").getAsInt()));
            }
        }
        return new GodTreeDefinition(god, nodes, edges, adjacency, labels);
    }

    public VaultGod getGod() {
        return this.god;
    }

    public Collection<Node> getNodes() {
        return this.nodes.values();
    }

    @Nullable
    public Node getNode(String id) {
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

    /**
     * Whether a node can be bought right now by a player whose owned tree nodes are given by
     * {@code purchased}: it must exist, be enabled, not be owned yet, and be a root or touch at
     * least one owned neighbour.
     */
    public boolean isPurchasable(String nodeId, Predicate<String> purchased) {
        Node node = this.nodes.get(nodeId);
        if (node == null || !node.enabled() || purchased.test(nodeId)) {
            return false;
        }
        if (node.type() == NodeType.ROOT) {
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
