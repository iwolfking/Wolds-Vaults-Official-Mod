package xyz.iwolfking.woldsvaults.gods.trees.idona;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.player.Listener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Idona tree's transient per-player and per-target state. Everything here is in-memory and
 * server-side only: it is cheaper than a capability, and none of it is worth persisting because
 * every accumulator it holds is scoped to a single vault run.
 *
 * <p>Cleared on vault leave, vault end, player death and logout -  the same lifecycle the milestone
 * engine uses -  so a Pincushion stack or a Power Dump reservoir can never leak across runs.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class IdonaState {
    private static final Map<UUID, Map<Integer, Integer>> PINCUSHION = new ConcurrentHashMap<>();
    private static final Map<Integer, Long> PRISON_MARKS = new ConcurrentHashMap<>();
    private static final Map<UUID, Float> POWER_DUMP_EXTRA = new ConcurrentHashMap<>();
    private static final ThreadLocal<ServerPlayer> CLEAVING_PLAYER = new ThreadLocal<>();

    private IdonaState() {
    }

    public static int recordPincushionHit(ServerPlayer player, int targetId) {
        Map<Integer, Integer> perTarget = PINCUSHION.computeIfAbsent(player.getUUID(), id -> new ConcurrentHashMap<>());
        return perTarget.merge(targetId, 1, Integer::sum) - 1;
    }

    public static void clearPincushionTarget(int targetId) {
        PINCUSHION.values().forEach(perTarget -> perTarget.remove(targetId));
    }

    /** Marks a mob as having survived a glacial prison; the mark expires on its own. */
    public static void markPrisonSurvivor(int targetId, long gameTime) {
        PRISON_MARKS.put(targetId, gameTime + IdonaNodes.PRISON_WARDEN_DURATION_TICKS);
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
            POWER_DUMP_EXTRA.remove(player.getUUID());
        } else {
            POWER_DUMP_EXTRA.put(player.getUUID(), extraMana);
        }
    }

    public static float getPowerDumpExtra(ServerPlayer player) {
        return POWER_DUMP_EXTRA.getOrDefault(player.getUUID(), 0.0F);
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
        PINCUSHION.remove(playerId);
        POWER_DUMP_EXTRA.remove(playerId);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        clear(event.getPlayer().getUUID());
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

    static void registerVaultHooks() {
        CommonEvents.LISTENER_LEAVE.register(IdonaState.class, data ->
                data.getListener().ifPresent(Listener.ID, IdonaState::clear));
        CommonEvents.VAULT_END.register(IdonaState.class, data -> {
            PINCUSHION.clear();
            POWER_DUMP_EXTRA.clear();
            PRISON_MARKS.clear();
        });
    }
}
