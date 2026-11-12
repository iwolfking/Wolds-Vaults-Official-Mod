package xyz.iwolfking.woldsvaults.gods.trees.idona;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.player.Listener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.GodNodeState;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Idona tree's transient state, in memory and server-side only. Prison marks are keyed by
 * entity id and the cleave flag is a thread-local.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class IdonaState {
    /** Power Dump's per-player state: when a sneaking dump last emptied the bar. */
    public static final class PowerDump {
        private long lastSneakDrain = Long.MIN_VALUE;
    }

    private static final Map<Integer, Long> PRISON_MARKS = new ConcurrentHashMap<>();
    private static final ThreadLocal<ServerPlayer> CLEAVING_PLAYER = new ThreadLocal<>();

    private IdonaState() {
    }

    public static int recordPincushionHit(ServerPlayer player, int targetId) {
        return pincushion(player.getUUID()).merge(targetId, 1, Integer::sum) - 1;
    }

    /** Drops a dead target from every online player's Pincushion counters. */
    public static void clearPincushionTarget(int targetId) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            GodNodeState.<Map<Integer, Integer>>peek(player.getUUID(), IdonaNodes.PINCUSHION)
                    .ifPresent(counts -> counts.remove(targetId));
        }
    }

    /** Marks a mob as having survived a glacial prison; the mark expires on its own. */
    public static void markPrisonSurvivor(int targetId, long gameTime) {
        int duration = IdonaNodeHandlers.params(IdonaNodes.PRISON_WARDEN,
                IdonaNodeHandlers.PrisonWardenParams.class).duration_ticks();
        PRISON_MARKS.put(targetId, gameTime + duration);
    }

    public static boolean isPrisonSurvivor(int targetId, long gameTime) {
        Long expiry = PRISON_MARKS.get(targetId);
        if (expiry == null) {
            return false;
        }
        if (expiry <= gameTime) {
            PRISON_MARKS.remove(targetId);
            return false;
        }
        return true;
    }

    /** Records that a sneaking Power Dump just emptied the player's mana bar. */
    public static void markSneakDrain(ServerPlayer player) {
        powerDump(player.getUUID()).lastSneakDrain = player.getLevel().getGameTime();
    }

    /** Ticks since the last sneaking Power Dump drain, or {@link Long#MAX_VALUE} when there was none. */
    public static long ticksSinceSneakDrain(ServerPlayer player) {
        PowerDump state = GodNodeState.<PowerDump>peek(player.getUUID(), IdonaNodes.POWER_DUMP).orElse(null);
        if (state == null || state.lastSneakDrain == Long.MIN_VALUE) {
            return Long.MAX_VALUE;
        }
        return player.getLevel().getGameTime() - state.lastSneakDrain;
    }

    /** Enters a cleave sweep, returning the replaced scope. Pair with {@link #popCleave} in a finally. */
    public static ServerPlayer pushCleave(ServerPlayer player) {
        ServerPlayer previous = CLEAVING_PLAYER.get();
        CLEAVING_PLAYER.set(player);
        return previous;
    }

    public static void popCleave(ServerPlayer previous) {
        if (previous == null) {
            CLEAVING_PLAYER.remove();
        } else {
            CLEAVING_PLAYER.set(previous);
        }
    }

    public static boolean isCleaving(ServerPlayer player) {
        return CLEAVING_PLAYER.get() == player;
    }

    public static void clear(UUID playerId) {
        GodNodeState.clear(playerId, IdonaNodes.PINCUSHION);
        GodNodeState.clear(playerId, IdonaNodes.POWER_DUMP);
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof ServerPlayer player) {
            clear(player.getUUID());
            return;
        }
        int id = event.getEntityLiving().getId();
        clearPincushionTarget(id);
        PRISON_MARKS.remove(id);
    }

    private static Map<Integer, Integer> pincushion(UUID playerId) {
        return GodNodeState.get(playerId, IdonaNodes.PINCUSHION, ConcurrentHashMap::new);
    }

    private static PowerDump powerDump(UUID playerId) {
        return GodNodeState.get(playerId, IdonaNodes.POWER_DUMP, PowerDump::new);
    }

    private static void pruneExpiredPrisonMarks(long gameTime) {
        PRISON_MARKS.values().removeIf(expiry -> expiry <= gameTime);
    }

    static void registerVaultHooks() {
        CommonEvents.VAULT_END.register(IdonaState.class, data -> {
            Vault vault = data.getVault();
            if (vault != null && vault.has(Vault.LISTENERS)) {
                vault.get(Vault.LISTENERS).getAll()
                        .forEach(listener -> listener.ifPresent(Listener.ID, IdonaState::clear));
            }
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                pruneExpiredPrisonMarks(server.overworld().getGameTime());
            }
        });
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        PRISON_MARKS.clear();
    }
}
