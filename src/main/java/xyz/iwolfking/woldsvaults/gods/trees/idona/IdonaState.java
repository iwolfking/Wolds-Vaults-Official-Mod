package xyz.iwolfking.woldsvaults.gods.trees.idona;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.player.Listener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.GodNodeState;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Idona tree's transient state. Everything here is in-memory and server-side only: it is
 * cheaper than a capability, and none of it is worth persisting because every accumulator it holds
 * is scoped to a single vault run.
 *
 * <p>Per-player scratch - the Pincushion hit counters and the Power Dump reservoir - lives in the
 * shared {@link GodNodeState} store, keyed by effect, so the god core's own teardown on logout,
 * vault-listener leave and respec covers it and no static per-player map remains here. What is
 * left in this class is the state that belongs to neither a player nor a vault: the prison marks,
 * keyed by entity id, and the cleave flag, which is a thread-local for the duration of one sweep.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class IdonaState {
    /** The Power Dump reservoir: mana banked by an instant cast, and the two windows that gate it. */
    public static final class PowerDump {
        private float extra;
        private long expiry;
        private long continuousUntil;
    }

    private static final Map<Integer, Long> PRISON_MARKS = new ConcurrentHashMap<>();
    private static final ThreadLocal<ServerPlayer> CLEAVING_PLAYER = new ThreadLocal<>();

    private IdonaState() {
    }

    public static int recordPincushionHit(ServerPlayer player, int targetId) {
        return pincushion(player.getUUID()).merge(targetId, 1, Integer::sum) - 1;
    }

    /**
     * Drops a dead target from every player's Pincushion counters. The counters are per player and
     * live in the shared scratch store, which is keyed by player rather than by target, so the
     * sweep walks the online players; the alternative is a stale count that a recycled entity id
     * would hand to the next mob to take that slot.
     */
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

    public static void setPowerDumpExtra(ServerPlayer player, float extraMana) {
        if (extraMana <= 0.0F) {
            PowerDump reservoir = GodNodeState.<PowerDump>peek(player.getUUID(), IdonaNodes.POWER_DUMP).orElse(null);
            if (reservoir != null) {
                reservoir.extra = 0.0F;
                reservoir.expiry = 0L;
            }
            return;
        }
        int ttl = IdonaNodeHandlers.params(IdonaNodes.POWER_DUMP,
                IdonaNodeHandlers.PowerDumpParams.class).surplus_ttl_ticks();
        PowerDump reservoir = powerDump(player.getUUID());
        reservoir.extra = extraMana;
        reservoir.expiry = player.getLevel().getGameTime() + ttl;
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

    /**
     * Called whenever a hold or toggle ability pays its running mana cost. While these payments
     * keep arriving the Power Dump surplus is suppressed outright, so channelled ability damage
     * can never ride a surplus banked by an earlier instant cast.
     */
    public static void markContinuousManaPayment(ServerPlayer player) {
        int grace = IdonaNodeHandlers.params(IdonaNodes.POWER_DUMP,
                IdonaNodeHandlers.PowerDumpParams.class).continuous_grace_ticks();
        powerDump(player.getUUID()).continuousUntil = player.getLevel().getGameTime() + grace;
    }

    /** Set for the duration of a cleave sweep so the hit handler can tell cleave from other AoE. */
    public static void beginCleave(ServerPlayer player) {
        CLEAVING_PLAYER.set(player);
    }

    public static void endCleave() {
        CLEAVING_PLAYER.remove();
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

    /**
     * Drops prison marks whose duration has already elapsed. Marks are keyed by entity id, which
     * belongs to no player and to no vault, so the only sweep that is safe to run while other
     * parties are still in their own runs is one that removes entries a lookup would have thrown
     * away anyway.
     */
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
}
