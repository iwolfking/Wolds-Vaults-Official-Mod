package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

/**
 * Ability level caps that only a Tenos node lifts.
 *
 * <p>Answer #25 extended {@code Vein_Miner_Chain} in the pack's {@code abilities.json} from 30 to
 * 38 tiers so Global Veins' "+8 effective levels" would have somewhere to land. Tier entries are
 * global, though, so on their own they raise chain miner's ceiling for everyone: any player with
 * enough added-ability-level gear reaches 38 without ever touching the node. This table restores
 * the intended shape - the extra tiers exist, but only Global Veins unlocks them.
 *
 * <p>The gate is the same one the node's own attribute contribution uses
 * ({@link TenosNodes#points}), now answered from the shared gate cache: points spent in the node
 * on the ACTIVE tree, or the node selected in the active god's minor-transfer slots. Points spent
 * on a tree that is not active do not count, which is why the cap moves with the equipped charm.
 *
 * <p>The lift per point is read from the effect at query time rather than held in the table. This
 * class is loaded during mod construction, before the config pass runs, so a constant taken from
 * config here would hold whatever existed at class-init time and never see a reload.
 */
public final class TenosAbilityCaps {
    /**
     * Keyed by the tiered skill's own id, not the ability id: Global Veins targets the
     * {@code Vein_Miner} ability, so its levels reach all five specialisations, but only the chain
     * miner specialisation has tiers past 30 to reach.
     */
    private static final Map<String, Gate> GATES = Map.of(
            "Vein_Miner_Chain", new Gate(TenosNodes.GLOBAL_VEINS, 30));

    private TenosAbilityCaps() {
    }

    public static boolean isGated(String skillId) {
        return skillId != null && GATES.containsKey(skillId);
    }

    /**
     * Highest effective tier the player may reach on this skill right now. Ungated skills report
     * {@link Integer#MAX_VALUE} so callers can compare unconditionally.
     */
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

    /** A gated skill: which node lifts it, and the cap without that node. */
    public record Gate(String nodeId, int baseCap) {
    }
}
