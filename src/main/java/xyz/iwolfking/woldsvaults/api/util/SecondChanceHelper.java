package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class SecondChanceHelper {

    private static final String LAST_ACTIVATION_TAG = "woldsvaults_second_chance_last_activation";
    private static final String ACTIVATION_COUNT_TAG = "woldsvaults_second_chance_activations";
    private static final String ACTIVE_RUN_TAG = "woldsvaults_second_chance_run";
    private static final float ACTIVATION_HEALTH_PERCENT = 0.65F;
    private static final float MINIMUM_ACTIVATION_HEALTH = 13.5F;
    private static final float REMAINDER_HEALTH_PERCENT = 0.05F;
    private static final long BASE_COOLDOWN_TICKS = 60L;
    private static final double COOLDOWN_GROWTH = 1.5D;
    private static final long MAX_COOLDOWN_TICKS = 12000L;

    /**
     * Second Chance may only save a player who is above both 65% of their maximum health and 13.5
     * absolute health, and whose cooldown from any earlier save has elapsed. The percentage gate
     * keeps it to genuine one shots on large health pools; the absolute gate keeps it from becoming
     * an immortality loop on a small one.
     */
    public static boolean canActivate(ServerPlayer player) {
        float health = player.getHealth();
        if (health < MINIMUM_ACTIVATION_HEALTH || health < player.getMaxHealth() * ACTIVATION_HEALTH_PERCENT) {
            return false;
        }
        if (!isCurrentRun(player)) {
            return true;
        }
        return player.level.getGameTime() - getLastActivation(player) >= getCooldownTicks(player);
    }

    /**
     * The health a player is left on when Second Chance saves them, scaled to their maximum health
     * so that the outcome is the same fraction of a health bar at every health pool size.
     */
    public static float getRemainderHealth(ServerPlayer player) {
        return player.getMaxHealth() * REMAINDER_HEALTH_PERCENT;
    }

    public static void markActivated(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        data.putInt(ACTIVATION_COUNT_TAG, getActivationCount(player) + 1);
        data.putString(ACTIVE_RUN_TAG, getActiveRunId(player));
        data.putLong(LAST_ACTIVATION_TAG, player.level.getGameTime());
    }

    /**
     * Every save within a vault multiplies the wait before the next one by 1.5, starting at three
     * seconds and stopping at ten minutes, so leaning on Second Chance repeatedly prices it out of
     * the rest of the run.
     */
    private static long getCooldownTicks(ServerPlayer player) {
        int activations = getActivationCount(player);
        if (activations <= 0) {
            return 0L;
        }
        double scaled = BASE_COOLDOWN_TICKS * Math.pow(COOLDOWN_GROWTH, activations - 1);
        return scaled >= MAX_COOLDOWN_TICKS ? MAX_COOLDOWN_TICKS : (long) scaled;
    }

    /**
     * The escalation is scoped to a single vault run: a count recorded against any other vault, or
     * recorded outside a vault entirely, reads as zero and starts the ladder over.
     */
    private static int getActivationCount(ServerPlayer player) {
        if (!isCurrentRun(player)) {
            return 0;
        }
        return player.getPersistentData().getInt(ACTIVATION_COUNT_TAG);
    }

    /**
     * Whether the stored ladder belongs to the vault the player is standing in. Leaving a vault or
     * entering a different one makes this false, which both resets the ladder and discards the
     * stored activation time - vault worlds keep their own clock, so a time recorded elsewhere
     * cannot be compared against this level's.
     */
    private static boolean isCurrentRun(ServerPlayer player) {
        String activeRun = getActiveRunId(player);
        return !activeRun.isEmpty() && activeRun.equals(player.getPersistentData().getString(ACTIVE_RUN_TAG));
    }

    private static String getActiveRunId(ServerPlayer player) {
        return VaultUtils.getVault(player.getLevel())
                .map(vault -> vault.get(Vault.ID))
                .map(UUID::toString)
                .orElse("");
    }

    private static long getLastActivation(ServerPlayer player) {
        return player.getPersistentData().getLong(LAST_ACTIVATION_TAG);
    }
}
