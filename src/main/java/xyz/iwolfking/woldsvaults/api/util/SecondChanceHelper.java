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
    private static final long BASE_COOLDOWN_TICKS = 80L;
    private static final int MAX_COOLDOWN_DOUBLINGS = 40;

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
     * Every save within a vault doubles the wait before the next one is available, starting at four
     * seconds, so leaning on Second Chance repeatedly prices it out of the rest of the run.
     */
    private static long getCooldownTicks(ServerPlayer player) {
        int activations = getActivationCount(player);
        if (activations <= 0) {
            return 0L;
        }
        return BASE_COOLDOWN_TICKS << Math.min(activations - 1, MAX_COOLDOWN_DOUBLINGS);
    }

    /**
     * The escalation is scoped to a single vault run: a count recorded against any other vault, or
     * recorded outside a vault entirely, reads as zero and starts the ladder over.
     */
    private static int getActivationCount(ServerPlayer player) {
        String activeRun = getActiveRunId(player);
        CompoundTag data = player.getPersistentData();
        if (activeRun.isEmpty() || !activeRun.equals(data.getString(ACTIVE_RUN_TAG))) {
            return 0;
        }
        return data.getInt(ACTIVATION_COUNT_TAG);
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
