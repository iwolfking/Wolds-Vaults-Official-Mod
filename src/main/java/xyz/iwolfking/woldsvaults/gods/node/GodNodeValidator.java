package xyz.iwolfking.woldsvaults.gods.node;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The whole-graph load-time assertions on a built {@link GodTreeModel}, all of them fatal.
 * Per-entry assertions are made by {@link GodTreeLoader} as it builds.
 */
public final class GodNodeValidator {
    private GodNodeValidator() {
    }

    /** Asserts every effect is placed and every node reachable from a root; an empty tree passes. */
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
