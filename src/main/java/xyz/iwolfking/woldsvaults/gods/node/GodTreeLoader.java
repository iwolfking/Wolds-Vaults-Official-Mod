package xyz.iwolfking.woldsvaults.gods.node;

import iskallia.vault.config.entry.SkillStyle;
import iskallia.vault.core.vault.influence.VaultGod;
import xyz.iwolfking.woldsvaults.config.gods.GodNodeEffectsConfig;
import xyz.iwolfking.woldsvaults.config.gods.GodTreeConfig;
import xyz.iwolfking.woldsvaults.config.gods.GodTreeGuiStylesConfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Turns one god's three config files into a {@link GodTreeModel}, asserting as it goes.
 *
 * <p>Every failure here is fatal and names the offending id. That is the whole point of the
 * loader: the old loader caught its own parse failures and returned an empty tree, so a single
 * mistyped id silently deleted a god's entire progression and rendered an uncharted sky.
 */
public final class GodTreeLoader {
    private GodTreeLoader() {
    }

    /** Builds and fully validates one god's tree. */
    public static GodTreeModel load(VaultGod god, GodTreeConfig tree, GodTreeGuiStylesConfig styles,
                                    GodNodeEffectsConfig effects) {
        Map<String, GodEffect> loadedEffects = loadEffects(god, effects);
        Map<String, GodNode> nodes = loadNodes(god, tree, loadedEffects);
        List<GodTreeModel.Edge> edges = new ArrayList<>();
        Map<String, Set<String>> adjacency = new LinkedHashMap<>();
        loadEdges(god, tree, nodes, edges, adjacency);
        adjacency.replaceAll((id, neighbours) -> java.util.Collections.unmodifiableSet(neighbours));

        List<GodTreeModel.Label> labels = new ArrayList<>();
        for (GodTreeConfig.LabelEntry label : tree.getLabels()) {
            if (label == null || label.text == null) {
                throw GodTreeConfigException.fail("God tree " + god.getName() + " has a label with no text");
            }
            labels.add(new GodTreeModel.Label(label.text, label.x, label.y));
        }

        Map<String, GodNodeType> effectTypes = resolveEffectTypes(god, nodes);
        Map<String, GodNodeHandler> handlers = new LinkedHashMap<>();
        for (GodEffect effect : loadedEffects.values()) {
            handlers.put(effect.id(), GodNodeHandlers.create(effect));
        }

        Map<String, SkillStyle> nodeStyles = new LinkedHashMap<>(styles.getStyles());
        GodTreeModel model = new GodTreeModel(god, nodes, edges, adjacency, labels, nodeStyles, loadedEffects,
                handlers, effectTypes);
        GodNodeValidator.validate(model);
        return model;
    }

    private static Map<String, GodEffect> loadEffects(VaultGod god, GodNodeEffectsConfig config) {
        GodNodeHandlerTypes.bootstrap();
        Map<String, GodEffect> effects = new LinkedHashMap<>();
        for (Map.Entry<String, GodNodeEffectsConfig.Entry> entry : config.getEffects().entrySet()) {
            String id = entry.getKey();
            GodNodeEffectsConfig.Entry raw = entry.getValue();
            if (raw == null || raw.handler() == null || raw.handler().isBlank()) {
                throw GodTreeConfigException.fail("God effect '" + id + "' (" + god.getName() + ") has no handler");
            }
            if (!GodNodeHandlers.isRegistered(raw.handler())) {
                throw GodTreeConfigException.fail("God effect '" + id + "' uses unknown handler type '"
                        + raw.handler() + "'; registered types are " + GodNodeHandlers.types());
            }
            if (raw.values() == null) {
                throw GodTreeConfigException.fail("God effect '" + id + "' has a malformed 'values' table; it must "
                        + "be an array of numbers");
            }
            Class<? extends GodEffectParams> paramsType = GodNodeHandlers.paramsType(raw.handler());
            GodEffectParams params = paramsType == null
                    ? null
                    : GodEffectParamsCodec.decode(id, raw.handler(), paramsType, raw.json());
            effects.put(id, new GodEffect(id, god, raw.handler(), raw.values(), params));
        }
        return effects;
    }

    private static Map<String, GodNode> loadNodes(VaultGod god, GodTreeConfig tree, Map<String, GodEffect> effects) {
        Map<String, GodNode> nodes = new LinkedHashMap<>();
        for (GodTreeConfig.NodeEntry entry : tree.getNodes()) {
            if (entry == null || entry.id == null || entry.id.isBlank()) {
                throw GodTreeConfigException.fail("God tree " + god.getName() + " has a node with no id");
            }
            GodNodeType type = GodNodeType.fromName(entry.type, entry.id);
            String effect = entry.effect == null || entry.effect.isBlank() ? null : entry.effect;
            if (effect != null && !effects.containsKey(effect)) {
                throw GodTreeConfigException.fail("God tree node '" + entry.id + "' references unknown effect '"
                        + effect + "'");
            }
            int cost = entry.cost == null ? 1 : entry.cost;
            if (cost < 0) {
                throw GodTreeConfigException.fail("God tree node '" + entry.id + "' has negative cost " + cost);
            }
            String name = entry.name == null || entry.name.isBlank() ? entry.id : entry.name;
            GodNode node = new GodNode(entry.id, god, name, type, effect, cost,
                    entry.enabled == null || entry.enabled);
            if (nodes.put(entry.id, node) != null) {
                throw GodTreeConfigException.fail("Duplicate god tree node id '" + entry.id + "' in "
                        + god.getName() + "'s tree");
            }
        }
        return nodes;
    }

    private static void loadEdges(VaultGod god, GodTreeConfig tree, Map<String, GodNode> nodes,
                                  List<GodTreeModel.Edge> edges, Map<String, Set<String>> adjacency) {
        for (List<String> pair : tree.getEdges()) {
            if (pair == null || pair.size() != 2) {
                throw GodTreeConfigException.fail("God tree " + god.getName() + " has an edge that is not a pair of "
                        + "node ids: " + pair);
            }
            String from = pair.get(0);
            String to = pair.get(1);
            if (from == null || to == null || from.equals(to)) {
                throw GodTreeConfigException.fail("God tree " + god.getName() + " has a malformed edge "
                        + from + " -> " + to);
            }
            if (!nodes.containsKey(from)) {
                throw GodTreeConfigException.fail("God tree edge references unknown node '" + from + "' (edge "
                        + from + " -> " + to + ")");
            }
            if (!nodes.containsKey(to)) {
                throw GodTreeConfigException.fail("God tree edge references unknown node '" + to + "' (edge "
                        + from + " -> " + to + ")");
            }
            edges.add(new GodTreeModel.Edge(from, to));
            adjacency.computeIfAbsent(from, k -> new LinkedHashSet<>()).add(to);
            adjacency.computeIfAbsent(to, k -> new LinkedHashSet<>()).add(from);
        }
    }

    /**
     * Collapses the node type of every placement of an effect down to one value. The gate asks
     * for an effect, not a placement, so two placements of one effect disagreeing on type would
     * make carryover depend on which placement was read first.
     */
    private static Map<String, GodNodeType> resolveEffectTypes(VaultGod god, Map<String, GodNode> nodes) {
        Map<String, GodNodeType> types = new LinkedHashMap<>();
        for (GodNode node : nodes.values()) {
            if (node.effect() == null) {
                continue;
            }
            GodNodeType previous = types.put(node.effect(), node.type());
            if (previous != null && previous != node.type()) {
                throw GodTreeConfigException.fail("God effect '" + node.effect() + "' (" + god.getName()
                        + ") is placed as both " + previous.getName() + " and " + node.type().getName());
            }
        }
        return types;
    }
}
