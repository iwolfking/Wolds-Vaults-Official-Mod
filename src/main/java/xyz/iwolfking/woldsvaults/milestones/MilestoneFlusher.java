package xyz.iwolfking.woldsvaults.milestones;

import iskallia.vault.core.vault.Vault;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.iwolfking.woldsvaults.network.NetworkHandler;
import xyz.iwolfking.woldsvaults.milestones.network.GreedTablesMessage;
import xyz.iwolfking.woldsvaults.milestones.network.MilestoneStatusMessage;
import xyz.iwolfking.woldsvaults.milestones.network.MilestoneSyncMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Throttles marking the SavedData dirty and pushing counter changes to clients. */
public class MilestoneFlusher {
    public static final int SYNC_INTERVAL = 20;
    public static final int SAVE_INTERVAL = 100;

    private static final Map<UUID, MilestoneStatusMessage> LAST_STATUS = new ConcurrentHashMap<>();

    private static int ticks;

    private MilestoneFlusher() {
    }

    public static void tick(MinecraftServer server) {
        ticks++;
        if (ticks % SYNC_INTERVAL == 0) {
            syncDeltas(server);
            syncStatus(server);
        }
        if (ticks % SAVE_INTERVAL == 0) {
            MilestoneData.get(server).flush();
        }
    }

    /** Pushes the greed header numbers to each player, only when one of them has moved. */
    private static void syncStatus(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            MilestoneStatusMessage status = MilestoneStatusMessage.build(player);
            if (status.matches(LAST_STATUS.get(player.getUUID()))) {
                continue;
            }
            LAST_STATUS.put(player.getUUID(), status);
            NetworkHandler.INSTANCE.send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player), status);
        }
    }

    public static void forget(UUID playerId) {
        LAST_STATUS.remove(playerId);
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
            Map<String, Integer> claimed = new HashMap<>();
            for (String id : ids) {
                changed.put(id, data.getValue(playerId, id));
                claimed.put(id, data.getClaimedTiers(playerId, id));
            }
            NetworkHandler.INSTANCE.send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                    new MilestoneSyncMessage(false, changed, claimed, data.getPinned(playerId)));
        });
    }

    /** Sends the balance tables, then the player's whole milestone set, claim marks, pin and header. */
    public static void syncAll(ServerPlayer player) {
        MilestoneData data = MilestoneData.get(player.server);
        data.clearPendingSync(player.getUUID());
        NetworkHandler.INSTANCE.send(
                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                GreedTablesMessage.build());
        NetworkHandler.INSTANCE.send(
                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                new MilestoneSyncMessage(true, new HashMap<>(data.getAllValues(player.getUUID())),
                        new HashMap<>(data.getAllClaimedTiers(player.getUUID())), data.getPinned(player.getUUID())));
        MilestoneStatusMessage.sendTo(player);
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
