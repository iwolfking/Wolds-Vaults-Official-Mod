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
    /** The Power Dump reservoir: banked mana, the windows that gate it, and the unpaid stage. */
    public static final class PowerDump {
        private float extra;
        private long expiry;
        private long continuousUntil;
        private float pending;
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

    /** Stages a surplus; nothing is live until {@link #commitPowerDumpExtra}. */
    public static void stagePowerDumpExtra(ServerPlayer player, float extraMana) {
        powerDump(player.getUUID()).pending = Math.max(extraMana, 0.0F);
    }

    /** Banks the staged surplus for {@code surplus_ttl_ticks}. A zero stage clears the live one. */
    public static void commitPowerDumpExtra(ServerPlayer player) {
        PowerDump reservoir = powerDump(player.getUUID());
        float extraMana = reservoir.pending;
        reservoir.pending = 0.0F;
        if (extraMana <= 0.0F) {
            reservoir.extra = 0.0F;
            reservoir.expiry = 0L;
            return;
        }
        int ttl = IdonaNodeHandlers.params(IdonaNodes.POWER_DUMP,
                IdonaNodeHandlers.PowerDumpParams.class).surplus_ttl_ticks();
        reservoir.extra = extraMana;
        reservoir.expiry = player.getLevel().getGameTime() + ttl;
    }

    public static void discardPowerDumpStage(ServerPlayer player) {
        GodNodeState.<PowerDump>peek(player.getUUID(), IdonaNodes.POWER_DUMP)
                .ifPresent(reservoir -> reservoir.pending = 0.0F);
    }

    public static float getPowerDumpExtra(ServerPlayer player) {
        PowerDump reservoir = GodNodeState.<PowerDump>peek(player.getUUID(), IdonaNodes.POWER_DUMP).orElse(null);
        if (reservoir == null || reservoir.extra <= 0.0F) {
            return 0.0F;
        }
        long now = player.getLevel().getGameTime();
        if (reservoir.expiry <= now) {
            reservoir.extra = 0.0F;
            reservoir.expiry = 0L;
            return 0.0F;
        }
        if (reservoir.continuousUntil > now) {
            return 0.0F;
        }
        return reservoir.extra;
    }

    /** Suppresses the Power Dump surplus for {@code continuous_grace_ticks}. */
    public static void markContinuousManaPayment(ServerPlayer player) {
        int grace = IdonaNodeHandlers.params(IdonaNodes.POWER_DUMP,
                IdonaNodeHandlers.PowerDumpParams.class).continuous_grace_ticks();
        powerDump(player.getUUID()).continuousUntil = player.getLevel().getGameTime() + grace;
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
