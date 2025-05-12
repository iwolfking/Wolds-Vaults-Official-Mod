package xyz.iwolfking.woldsvaults.api.lib;

import iskallia.vault.network.message.transmog.DiscoveredEntriesMessage;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public interface PlayerGreedDataExtension {
    DiscoveredEntriesMessage getUpdatePacket(UUID playerId);

    void syncTo(ServerPlayer player);
}
