package xyz.iwolfking.woldsvaults.gods.ultimates;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import xyz.iwolfking.woldsvaults.gods.combat.FinalDamageStage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Savior's timed damage reduction, a final-damage sub-stage of its own rather than the resistance stat, whose
 * clamp sits below the top levels.
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

    public static void clearAll() {
        STATES.clear();
    }
}
