package xyz.iwolfking.woldsvaults.gods.trees.wendarr;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.core.vault.time.TickStopwatch;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Vault-clock arithmetic shared by the Wendarr time nodes. Draining and granting both go through
 * {@code DISPLAY_TIME}, which counting-up vaults ({@link TickStopwatch}) move in the opposite
 * direction -  the same branch {@code VoidFluidExtension} carries. This is deliberately separate
 * from the clock-rate primitive: rate changes how fast the clock runs, these move the clock.
 */
public final class WendarrVaultTime {
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
}
