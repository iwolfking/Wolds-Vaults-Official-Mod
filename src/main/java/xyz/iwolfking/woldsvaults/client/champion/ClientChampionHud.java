package xyz.iwolfking.woldsvaults.client.champion;

import net.minecraft.client.Minecraft;

/**
 * Client-side mirror of the Vault Champion's HUD state.
 *
 * <p>Carries its own staleness timeout rather than trusting a closing packet to arrive. A player who
 * disconnects mid-fight, or who is in the vault when the server stops, would otherwise be left with a
 * boss bar on their screen forever; a couple of seconds without an update simply clears it.
 */
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
