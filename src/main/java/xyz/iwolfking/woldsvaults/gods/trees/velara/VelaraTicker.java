package xyz.iwolfking.woldsvaults.gods.trees.velara;

import iskallia.vault.world.data.ServerVaults;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** The Velara cross-player tree pass. Aura scans run in vaults only; modifiers apply everywhere. */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class VelaraTicker {
    private VelaraTicker() {
    }

    static void pass(MinecraftServer server, List<ServerPlayer> players) {
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

    /** Removes the vanilla attribute modifiers this tree writes, which outlive the player session. */
    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            VelaraModifiers.clear(player);
        }
    }
}
