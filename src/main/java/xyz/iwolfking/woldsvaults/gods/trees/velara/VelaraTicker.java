package xyz.iwolfking.woldsvaults.gods.trees.velara;

import iskallia.vault.world.data.ServerVaults;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import top.theillusivec4.curios.api.event.CurioChangeEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The one periodic pass the Velara tree needs.
 *
 * <p>Everything expensive -  resolving which nodes are live, the Presence and Sanitation radius
 * scans, the Defender of the Faith charm census, the vanilla attribute modifiers and the Sacrifice
 * flock partition -  runs here once a second for the whole server, rather than per node per player
 * on a hot bus. The scans are gated on being inside a vault; the attribute modifiers are not,
 * because the stat nodes they sit beside come through the attribute snapshot everywhere.
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
            VelaraAuras.commit(Map.of(), Set.of());
            return;
        }
        for (ServerPlayer player : players) {
            VelaraNodeState.refresh(player);
        }

        Map<UUID, Integer> presence = new HashMap<>();
        Set<UUID> sanitized = new HashSet<>();
        for (ServerPlayer player : players) {
            boolean inVault = ServerVaults.get(player.level).isPresent();
            if (inVault) {
                collectAuras(player, presence, sanitized);
            }
            VelaraModifiers.update(player, inVault);
            Immortal.updateGlobalFactor(player);
        }
        VelaraAuras.commit(presence, sanitized);
        SacrificeFlocks.rebuildAll(server);
    }

    private static void collectAuras(ServerPlayer player, Map<UUID, Integer> presence, Set<UUID> sanitized) {
        if (VelaraNodeState.isActive(player, VelaraNode.PRESENCE)) {
            for (ServerPlayer ally : VelaraParty.alliesNear(player, VelaraValues.PRESENCE_RADIUS)) {
                presence.merge(ally.getUUID(), 1, Integer::sum);
            }
        }
        if (VelaraNodeState.isActive(player, VelaraNode.SANITATION)) {
            sanitized.add(player.getUUID());
            for (ServerPlayer ally : VelaraParty.alliesNear(player, VelaraValues.SANITATION_RADIUS)) {
                sanitized.add(ally.getUUID());
            }
        }
    }

    @SubscribeEvent
    public static void onCurioChange(CurioChangeEvent event) {
        if (event.getEntityLiving() instanceof ServerPlayer player) {
            VelaraNodeState.invalidate(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        VelaraNodeState.invalidate(event.getPlayer().getUUID());
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID playerId = event.getPlayer().getUUID();
        if (event.getPlayer() instanceof ServerPlayer player) {
            VelaraModifiers.clear(player);
        }
        VelaraNodeState.invalidate(playerId);
        VelaraAuras.clear(playerId);
        AdaptiveArmor.clear(playerId);
        FleetingPhysicality.clear(playerId);
        Immortal.clear(playerId);
        SacrificeFlocks.clearPlayer(playerId);
    }
}
