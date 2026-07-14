package xyz.iwolfking.woldsvaults.api.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.data.PlayerGodMasteryData;

import java.util.UUID;

/**
 * God's Mastery consumables permanently raise a player's maximum god reputation (base 50)
 * with all four Vault Gods. Counts are stored per player UUID in
 * {@link PlayerGodMasteryData} world SavedData, so a raised cap survives deaths, relogs
 * and server restarts.
 */
public final class GodMasteryHelper {
    public static final int BASE_CAP = 50;

    /**
     * The effective reputation cap for the player whose reputation is currently being read
     * or written. PlayerReputationData's per-player Entry has no player context of its own,
     * so the outer static methods publish the cap here before the Entry-level clamps run.
     */
    private static final ThreadLocal<Integer> CURRENT_CAP = ThreadLocal.withInitial(() -> BASE_CAP);

    private GodMasteryHelper() {
    }

    /**
     * Mastery count by uuid; 0 when no server is available (client-side callers).
     */
    public static int getMastery(UUID playerUUID) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server == null ? 0 : PlayerGodMasteryData.get(server).getMastery(playerUUID);
    }

    public static int capFor(Player player) {
        return BASE_CAP + getMastery(player.getUUID());
    }

    public static void pushCap(UUID playerUUID) {
        CURRENT_CAP.set(BASE_CAP + getMastery(playerUUID));
    }

    public static int currentCap() {
        return CURRENT_CAP.get();
    }

    /**
     * Consumes one mastery: bumps the player's stored count and returns the new cap.
     */
    public static int increaseMastery(ServerPlayer player) {
        int mastery = PlayerGodMasteryData.get(player.getLevel().getServer()).increaseMastery(player.getUUID());
        WoldsVaults.LOGGER.info("{} consumed God's Mastery #{}: god reputation cap is now {}.",
                player.getGameProfile().getName(), mastery, BASE_CAP + mastery);
        return BASE_CAP + mastery;
    }
}
