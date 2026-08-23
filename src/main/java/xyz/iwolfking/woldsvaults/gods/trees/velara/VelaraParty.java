package xyz.iwolfking.woldsvaults.gods.trees.velara;

import iskallia.vault.world.data.VaultPartyData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Ally resolution over {@link VaultPartyData}, not the vault listener roster. */
public final class VelaraParty {
    private VelaraParty() {
    }

    /** The leader UUID of the player's party, or the player's own id when they are unpartied. */
    public static UUID groupKey(ServerPlayer player) {
        Optional<VaultPartyData.Party> party = partyOf(player);
        if (party.isEmpty()) {
            return player.getUUID();
        }
        UUID leader = party.get().getLeader();
        return leader == null ? player.getUUID() : leader;
    }

    public static Optional<VaultPartyData.Party> partyOf(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return Optional.empty();
        }
        return VaultPartyData.get(server).getParty(player.getUUID());
    }

    public static boolean isAlly(ServerPlayer player, UUID other) {
        if (player.getUUID().equals(other)) {
            return false;
        }
        return partyOf(player).map(party -> party.hasMember(other)).orElse(false);
    }

    /** Online party members on the same level, excluding {@code player}. A radius of 0 means any. */
    public static List<ServerPlayer> alliesNear(ServerPlayer player, float radius) {
        MinecraftServer server = player.getServer();
        Optional<VaultPartyData.Party> party = partyOf(player);
        if (server == null || party.isEmpty()) {
            return Collections.emptyList();
        }
        double radiusSq = (double) radius * (double) radius;
        List<ServerPlayer> allies = new ArrayList<>();
        for (UUID memberId : party.get().getMembers()) {
            if (memberId.equals(player.getUUID())) {
                continue;
            }
            ServerPlayer member = server.getPlayerList().getPlayer(memberId);
            if (member == null || member.getLevel() != player.getLevel()) {
                continue;
            }
            if (radius > 0.0F && member.distanceToSqr(player) > radiusSq) {
                continue;
            }
            allies.add(member);
        }
        return allies;
    }
}
