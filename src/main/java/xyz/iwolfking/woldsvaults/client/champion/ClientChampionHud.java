package xyz.iwolfking.woldsvaults.client.champion;

import net.minecraft.client.Minecraft;

/** Client mirror of the Champion HUD state; inactive after {@link #STALE_TICKS} without an update. */
public final class ClientChampionHud {
    private static final int STALE_TICKS = 60;

    private static boolean active;
    private static float dealt;
    private static float pool;
    private static float damageMultiplier = 1.0F;
    private static long lastUpdate;

    private ClientChampionHud() {
    }

    public static void update(boolean active, float dealt, float pool, float damageMultiplier) {
        ClientChampionHud.active = active;
        ClientChampionHud.dealt = dealt;
        ClientChampionHud.pool = pool;
        ClientChampionHud.damageMultiplier = damageMultiplier;
        ClientChampionHud.lastUpdate = gameTime();
    }

    public static void clear() {
        active = false;
        dealt = 0.0F;
        pool = 0.0F;
        damageMultiplier = 1.0F;
        lastUpdate = Long.MIN_VALUE / 2;
    }

    public static boolean isActive() {
        return active && pool > 0.0F && gameTime() - lastUpdate <= STALE_TICKS;
    }

    public static float getDealt() {
        return dealt;
    }

    public static float getPool() {
        return pool;
    }

    public static float getDamageMultiplier() {
        return damageMultiplier;
    }

    private static long gameTime() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level == null ? 0L : minecraft.level.getGameTime();
    }
}
