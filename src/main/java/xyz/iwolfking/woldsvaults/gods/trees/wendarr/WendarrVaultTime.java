package xyz.iwolfking.woldsvaults.gods.trees.wendarr;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.core.vault.time.TickStopwatch;

import net.minecraft.server.level.ServerPlayer;

import xyz.iwolfking.woldsvaults.gods.GodNodeState;
import xyz.iwolfking.woldsvaults.gods.GodVaultUtil;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class WendarrVaultTime {
    /** Scratch key: ticks of vault time the player has booked but not yet paid. */
    private static final String DRAIN_DEBT = "woldsvaults:wendarr_vault_time_debt";

    private WendarrVaultTime() {
    }

    /** Removes {@code ticks} of remaining vault time, never past zero. Adds to a stopwatch clock. */
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

    /** Books one hit's worth of vault time, rolled from Edge of Time's range for either node. */
    public static void queueDrain(ServerPlayer player) {
        WendarrNodeHandlers.EdgeOfTimeParams params = WendarrNodeHandlers.params(WendarrNodes.EDGE_OF_TIME,
                WendarrNodeHandlers.EdgeOfTimeParams.class);
        int ticks = params.drain_min_ticks()
                + player.getRandom().nextInt(params.drain_max_ticks() - params.drain_min_ticks() + 1);
        debt(player).addAndGet(ticks);
    }

    /** Pays off everything the player owes. A null vault is a no-op and the debt is kept. */
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
        return GodVaultUtil.runners(vault);
    }

    private static AtomicInteger debt(ServerPlayer player) {
        return GodNodeState.get(player.getUUID(), DRAIN_DEBT, AtomicInteger::new);
    }
}
