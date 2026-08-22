package xyz.iwolfking.woldsvaults.gods.ultimates;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import xyz.iwolfking.woldsvaults.gods.combat.FinalDamageStage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The protective half of Savior: the timed damage reduction the shockwave leaves on everyone it
 * touched.
 *
 * <p>Like Copé's, this is its own damage-reduction source rather than the resistance stat, applied
 * as a final-damage sub-stage. That is what lets the sheet's 0.55 land in full: the resistance
 * attribute is clamped at 0.5 before its hard 0.95 ceiling, so routing Savior through it would
 * silently throw away most of the top three levels.
 */
public final class SaviorState {
    private static final Map<UUID, State> STATES = new ConcurrentHashMap<>();

    private SaviorState() {
    }

    public static void register() {
        FinalDamageStage.register(UltimateIds.SAVIOR_RESISTANCE_STAGE, FinalDamageStage.ORDER_REDUCTION,
                SaviorState::reduceIncoming);
    }

    /** Grants (or refreshes, taking the stronger of the two) the shockwave's protection. */
    public static void apply(ServerPlayer player, float resistance, int durationTicks) {
        STATES.merge(player.getUUID(), new State(resistance, durationTicks),
                (existing, added) -> new State(Math.max(existing.resistance, added.resistance),
                        Math.max(existing.remainingTicks, added.remainingTicks)));
    }

    public static boolean isActive(Player player) {
        return STATES.containsKey(player.getUUID());
    }

    private static float reduceIncoming(LivingDamageEvent event, float amount) {
        if (!(event.getEntityLiving() instanceof ServerPlayer player)) {
            return amount;
        }
        State state = STATES.get(player.getUUID());
        return state == null ? amount : amount * (1.0F - state.resistance);
    }

    public static void tick() {
        if (STATES.isEmpty()) {
            return;
        }
        STATES.values().removeIf(state -> --state.remainingTicks <= 0);
    }

    public static void clear(Player player) {
        STATES.remove(player.getUUID());
    }

    private static final class State {
        private final float resistance;
        private int remainingTicks;

        private State(float resistance, int remainingTicks) {
            this.resistance = resistance;
            this.remainingTicks = remainingTicks;
        }
    }

    /**
     * Drops every player's state. Called on server stop only: the modifiers these states apply are
     * transient, so the entries are what outlive a world, not the modifiers themselves.
     */
    public static void clearAll() {
        STATES.clear();
    }
}
