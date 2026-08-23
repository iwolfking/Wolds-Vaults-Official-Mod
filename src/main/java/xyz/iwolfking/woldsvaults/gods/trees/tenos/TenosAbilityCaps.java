package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

/** Ability level caps that only a Tenos node lifts; the lift per point is read at query time. */
public final class TenosAbilityCaps {
    /** Keyed by the tiered skill's own id, not the ability id. */
    private static final Map<String, Gate> GATES = Map.of(
            "Vein_Miner_Chain", new Gate(TenosNodes.GLOBAL_VEINS, 30));

    private TenosAbilityCaps() {
    }

    public static boolean isGated(String skillId) {
        return skillId != null && GATES.containsKey(skillId);
    }

    /** Highest tier the player may reach on this skill. Ungated skills give {@link Integer#MAX_VALUE}. */
    public static int cap(String skillId, ServerPlayer player) {
        Gate gate = GATES.get(skillId);
        if (gate == null) {
            return Integer.MAX_VALUE;
        }
        int points = TenosNodes.points(player, gate.nodeId());
        if (points <= 0) {
            return gate.baseCap();
        }
        int levelsPerPoint = TenosNodeHandlers.params(gate.nodeId(),
                TenosNodeHandlers.GlobalVeinsParams.class).levels();
        return gate.baseCap() + levelsPerPoint * points;
    }

    public record Gate(String nodeId, int baseCap) {
    }
}
