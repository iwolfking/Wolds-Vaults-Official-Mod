package xyz.iwolfking.woldsvaults.api.util;

import net.minecraft.server.level.ServerPlayer;

public class SecondChanceHelper {

    private static final String LAST_ACTIVATION_TAG = "woldsvaults_second_chance_last_activation";
    private static final float ACTIVATION_HEALTH_PERCENT = 0.65F;
    private static final float MINIMUM_ACTIVATION_HEALTH = 13.5F;
    private static final float REMAINDER_HEALTH_PERCENT = 0.05F;
    private static final long COOLDOWN_TICKS = 60L;

    /**
     * Second Chance may only save a player who is above both 65% of their maximum health and 13.5
     * absolute health, and whose previous activation is at least 3 seconds old. The percentage gate
     * keeps it to genuine one shots on large health pools; the absolute gate keeps it from becoming
     * an immortality loop once a player's maximum health has been ground down.
     */
    public static boolean canActivate(ServerPlayer player) {
        float health = player.getHealth();
        if (health < MINIMUM_ACTIVATION_HEALTH || health < player.getMaxHealth() * ACTIVATION_HEALTH_PERCENT) {
            return false;
        }
        return player.level.getGameTime() - getLastActivation(player) >= COOLDOWN_TICKS;
    }

    /**
     * The health a player is left on when Second Chance saves them, scaled to their maximum health
     * so that the outcome is the same fraction of a health bar at every health pool size.
     */
    public static float getRemainderHealth(ServerPlayer player) {
        return player.getMaxHealth() * REMAINDER_HEALTH_PERCENT;
    }

    public static void markActivated(ServerPlayer player) {
        player.getPersistentData().putLong(LAST_ACTIVATION_TAG, player.level.getGameTime());
    }

    private static long getLastActivation(ServerPlayer player) {
        return player.getPersistentData().getLong(LAST_ACTIVATION_TAG);
    }
}
