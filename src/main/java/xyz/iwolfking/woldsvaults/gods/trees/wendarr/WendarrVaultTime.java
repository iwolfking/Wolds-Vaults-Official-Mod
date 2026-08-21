package xyz.iwolfking.woldsvaults.gods.trees.wendarr;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.core.vault.time.TickStopwatch;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.gods.GodNodeState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Vault-clock arithmetic shared by the Wendarr time nodes. Draining and granting both go through
 * {@code DISPLAY_TIME}, which counting-up vaults ({@link TickStopwatch}) move in the opposite
 * direction - the same branch {@code VoidFluidExtension} carries. This is deliberately separate
 * from the clock-rate primitive: rate changes how fast the clock runs, these move the clock.
 *
 * <p>The drain debt is one ledger shared by Edge of Time and Temporal Shielding, because both bill
 * the same clock and both use Edge of Time's configured tick range. It lives under its own key in
 * the shared {@link GodNodeState} player scratch, so logout, vault-listener leave and respec all
 * clear it through the god core's own teardown and no static map is left here holding it.
 */
public final class WendarrVaultTime {
    /** Reserved scratch key: ticks of vault time the player has booked but not yet paid. */
    private static final String DRAIN_DEBT = "woldsvaults:wendarr_vault_time_debt";

    private WendarrVaultTime() {
    }

    /** Removes {@code ticks} of remaining vault time, never past zero. */
    public static void drainTicks(Vault vault, int ticks) {
        if (vault == null || ticks <= 0) {
            return;
        }
        TickClock clock = vault.get(Vault.CLOCK);
        if (clock == null) {
            return;
        }
        int display = clock.get(TickClock.DISPLAY_TIME);
        if (clock instanceof TickStopwatch) {
            clock.set(TickClock.DISPLAY_TIME, display + ticks);
        } else {
            clock.set(TickClock.DISPLAY_TIME, Math.max(0, display - ticks));
        }
    }

    /**
     * Books one hit's worth of vault time against the player. The roll is Edge of Time's
     * configured range for both nodes that bill it, which is how the shipped tree tuned Temporal
     * Shielding's cost.
     */
    public static void queueDrain(ServerPlayer player) {
        WendarrNodeHandlers.EdgeOfTimeParams params = WendarrNodeHandlers.params(WendarrNodes.EDGE_OF_TIME,
                WendarrNodeHandlers.EdgeOfTimeParams.class);
        int ticks = params.drain_min_ticks()
                + player.getRandom().nextInt(params.drain_max_ticks() - params.drain_min_ticks() + 1);
        debt(player).addAndGet(ticks);
    }

    /** Pays off everything the player owes, at most once a second and only inside a vault. */
    public static void settleDrain(ServerPlayer player, Vault vault) {
        if (vault == null) {
            return;
        }
        AtomicInteger debt = GodNodeState.<AtomicInteger>peek(player.getUUID(), DRAIN_DEBT).orElse(null);
        if (debt == null) {
            return;
        }
        drainTicks(vault, debt.getAndSet(0));
    }

    public static List<ServerPlayer> runners(Vault vault) {
        List<ServerPlayer> players = new ArrayList<>();
        if (vault == null) {
            return players;
        }
        for (Runner runner : vault.get(Vault.LISTENERS).getAll(Runner.class)) {
            runner.getPlayer().ifPresent(players::add);
        }
        return players;
    }

    private static AtomicInteger debt(ServerPlayer player) {
        return GodNodeState.get(player.getUUID(), DRAIN_DEBT, AtomicInteger::new);
    }
}
