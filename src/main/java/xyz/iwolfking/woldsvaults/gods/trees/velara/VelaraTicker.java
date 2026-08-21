package xyz.iwolfking.woldsvaults.gods.trees.velara;

import iskallia.vault.world.data.ServerVaults;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The cross-player pass the Velara tree needs once a second.
 *
 * <p>Everything here is a computation over more than one player at a time, which is why it is not
 * a set of {@code TickContributor}s: the Presence and Sanitation radius scans have to see every
 * caster before they can decide any recipient's total, the Defender of the Faith charm census
 * reads the whole party, the vanilla attribute modifiers compose several nodes multiplicatively
 * into one value per attribute, and the Sacrifice flock partition is per vault rather than per
 * player. The scans are gated on being inside a vault; the attribute modifiers are not, because
 * the stat nodes they sit beside come through the attribute snapshot everywhere.
 *
 * <p>Resolving which nodes are live is no longer done here: {@code GodNodeCache} is dropped on the
 * same cadence by the shared ticker, and invalidated immediately on charm change, login, logout,
 * dimension change and respawn by the god core's own lifecycle handlers.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class VelaraTicker {
    private static final int PERIOD_TICKS = 20;

    private VelaraTicker() {
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null || server.getTickCount() % PERIOD_TICKS != 0) {
            return;
        }
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        if (players.isEmpty()) {
            return;
        }

        Map<UUID, Integer> presence = new HashMap<>();
        Set<UUID> sanitized = new HashSet<>();
        for (ServerPlayer player : players) {
            boolean inVault = ServerVaults.get(player.level).isPresent();
            if (inVault) {
                collectAuras(player, presence, sanitized);
            }
            VelaraModifiers.update(player, inVault);
            VelaraImmortal.updateGlobalFactor(player);
        }
        VelaraAuras.commit(players, presence, sanitized);
        VelaraSacrificeFlocks.rebuildAll(server);
    }

    private static void collectAuras(ServerPlayer player, Map<UUID, Integer> presence, Set<UUID> sanitized) {
        if (VelaraNodes.isActive(player, VelaraNodes.PRESENCE)) {
            for (ServerPlayer ally : VelaraParty.alliesNear(player, VelaraValues.presenceRadius())) {
                presence.merge(ally.getUUID(), 1, Integer::sum);
            }
        }
        if (VelaraNodes.isActive(player, VelaraNodes.SANITATION)) {
            sanitized.add(player.getUUID());
            for (ServerPlayer ally : VelaraParty.alliesNear(player, VelaraValues.sanitationRadius())) {
                sanitized.add(ally.getUUID());
            }
        }
    }

    /**
     * Velara's own logout leg. The node state, the gate cache and the active-god cache are torn
     * down by the god core; what is left is the vanilla attribute modifiers this tree writes,
     * which persist on the entity until they are explicitly removed.
     */
    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            VelaraModifiers.clear(player);
        }
    }
}
