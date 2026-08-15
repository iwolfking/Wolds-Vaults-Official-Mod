package xyz.iwolfking.woldsvaults.milestones;

import iskallia.vault.core.vault.Vault;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.iwolfking.woldsvaults.milestones.network.MilestoneNetwork;
import xyz.iwolfking.woldsvaults.milestones.network.MilestoneSyncMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Throttles the two expensive parts of the engine: marking the SavedData dirty and pushing
 * counter changes to clients. Increments themselves never touch either.
 */
public class MilestoneFlusher {
    public static final int SYNC_INTERVAL = 20;
    public static final int SAVE_INTERVAL = 100;

    private static int ticks;

    private MilestoneFlusher() {
    }

    public static void tick(MinecraftServer server) {
        ticks++;
        if (ticks % SYNC_INTERVAL == 0) {
            syncDeltas(server);
        }
        if (ticks % SAVE_INTERVAL == 0) {
            MilestoneData.get(server).flush();
        }
    }

    private static void syncDeltas(MinecraftServer server) {
        MilestoneData data = MilestoneData.get(server);
        Map<UUID, Set<String>> pending = data.drainPendingSync();
        if (pending.isEmpty()) {
            return;
        }
        pending.forEach((playerId, ids) -> {
            ServerPlayer player = server.getPlayerList().getPlayer(playerId);
            if (player == null) {
                return;
            }
            Map<String, Long> changed = new HashMap<>();
            for (String id : ids) {
                changed.put(id, data.getValue(playerId, id));
            }
            MilestoneNetwork.INSTANCE.send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                    new MilestoneSyncMessage(false, changed));
        });
    }

    /**
     * Sends the player's entire milestone set. Used on login so the client mirror starts complete.
     */
    public static void syncAll(ServerPlayer player) {
        MilestoneData data = MilestoneData.get(player.server);
        data.clearPendingSync(player.getUUID());
        MilestoneNetwork.INSTANCE.send(
                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                new MilestoneSyncMessage(true, new HashMap<>(data.getAllValues(player.getUUID()))));
    }

    public static void flushNow(Vault vault) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null && vault != null && vault.has(Vault.ID)) {
            MilestoneData.get(server).flush();
        }
    }

    public static void flushNow(MinecraftServer server) {
        if (server != null) {
            MilestoneData.get(server).flush();
        }
    }
}
