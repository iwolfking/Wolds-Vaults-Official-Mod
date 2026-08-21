package xyz.iwolfking.woldsvaults.gods.node;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The whole-graph load-time assertions on a built {@link GodTreeModel}. Every one of them is
 * fatal: a config typo must never degrade into a node that quietly does nothing.
 *
 * <p>Per-entry assertions - unknown handler type, unknown effect, malformed edge, duplicate id -
 * are made by {@link GodTreeLoader} as it builds; what is left here is what can only be seen once
 * the whole tree exists.
 */
public final class GodNodeValidator {
    private GodNodeValidator() {
    }

    /**
     * Asserts that every effect is actually placed and every node is reachable from a root. An
     * empty tree passes: a god whose tree has not been authored yet is a legitimate state, and an
     * absent tree is not the same failure as a broken one.
     */
    public static void validate(GodTreeModel model) {
        if (model.isEmpty() && model.getEffects().isEmpty()) {
            return;
        }
        assertEveryEffectPlaced(model);
        assertEveryNodeReachable(model);
    }

    private static void assertEveryEffectPlaced(GodTreeModel model) {
        Set<String> placed = new LinkedHashSet<>();
        for (GodNode node : model.getNodes()) {
            if (node.effect() != null) {
                placed.add(node.effect());
            }
        }
        List<String> orphaned = new ArrayList<>();
        for (GodEffect effect : model.getEffects()) {
            if (!placed.contains(effect.id())) {
                orphaned.add(effect.id());
            }
        }
        if (!orphaned.isEmpty()) {
            throw GodTreeConfigException.fail("God effects of " + model.getGod().getName()
                    + " are configured but placed on no node: " + orphaned);
        }
    }

    private static void assertEveryNodeReachable(GodTreeModel model) {
        Set<String> reached = new LinkedHashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        for (GodNode node : model.getNodes()) {
            if (node.isRoot() && reached.add(node.id())) {
                queue.add(node.id());
            }
        }
        while (!queue.isEmpty()) {
            for (String neighbour : model.getAdjacent(queue.poll())) {
                if (reached.add(neighbour)) {
                    queue.add(neighbour);
                }
            }
        }
        List<String> unreachable = new ArrayList<>();
        for (GodNode node : model.getNodes()) {
            if (!reached.contains(node.id())) {
                unreachable.add(node.id());
            }
        }
        if (!unreachable.isEmpty()) {
            throw GodTreeConfigException.fail("God tree nodes of " + model.getGod().getName()
                    + " cannot be reached from any root: " + unreachable);
        }
    }
}
