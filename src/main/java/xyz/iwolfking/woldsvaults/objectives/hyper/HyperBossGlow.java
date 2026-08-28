package xyz.iwolfking.woldsvaults.objectives.hyper;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The hyperboss's red outline, so it reads apart from the white-glowing brutal bosses sharing its
 * arena. A glowing entity is drawn in its team's colour and in white when it has no team, which is
 * where the brutal bosses' white comes from, so the boss is put on a red scoreboard team of its own.
 * Team membership is what reaches the client - entity tags are not synced - so no extra packet is
 * needed.
 */
public final class HyperBossGlow {
    private static final String TEAM = "wv_hyperboss";

    private HyperBossGlow() {
    }

    /** Whether the given boss already carries the glowing flag, so a re-assert can be skipped. */
    public static boolean isMarked(LivingEntity boss) {
        return boss.hasGlowingTag();
    }

    /** Glows one hyperboss red, first dropping members whose entity no longer exists anywhere. */
    public static void mark(LivingEntity boss) {
        MinecraftServer server = boss.getServer();
        if (server == null) {
            WoldsVaults.LOGGER.error("The hyperboss has no server to reach the scoreboard through; "
                    + "it stays white instead of red.");
            return;
        }
        Scoreboard scoreboard = server.getScoreboard();
        PlayerTeam team = scoreboard.getPlayerTeam(TEAM);
        if (team == null) {
            team = scoreboard.addPlayerTeam(TEAM);
            team.setDisplayName(new TextComponent("Hyperboss"));
        }
        team.setColor(ChatFormatting.RED);
        pruneDeadMembers(server, scoreboard, team);
        boolean added = scoreboard.addPlayerToTeam(boss.getStringUUID(), team);
        boss.setGlowingTag(true);
        resend(server, team, boss.getStringUUID());
        WoldsVaults.LOGGER.info(
                "Hyperboss glow: {} #{} joined team {} (colour {}, {} member(s), newMember={}); glowing tag {}.",
                boss.getType().getRegistryName(), boss.getId(), TEAM, team.getColor().getName(),
                team.getPlayers().size(), added, boss.hasGlowingTag());
    }

    /**
     * Re-sends the team and its membership to every connected player. The scoreboard already
     * broadcasts both, so this only guards against a client that entered the vault dimension
     * between the two packets; it is silent on the client when the team is already known.
     */
    private static void resend(MinecraftServer server, PlayerTeam team, String member) {
        server.getPlayerList().broadcastAll(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, false));
        server.getPlayerList().broadcastAll(ClientboundSetPlayerTeamPacket.createPlayerPacket(
                team, member, ClientboundSetPlayerTeamPacket.Action.ADD));
    }

    /** Drops one boss from the team, on vault teardown; a missing team or member is not an error. */
    public static void release(@Nullable MinecraftServer server, @Nullable UUID bossId) {
        if (server == null || bossId == null) {
            return;
        }
        Scoreboard scoreboard = server.getScoreboard();
        PlayerTeam team = scoreboard.getPlayerTeam(TEAM);
        if (team == null) {
            return;
        }
        if (team.getPlayers().contains(bossId.toString())) {
            scoreboard.removePlayerFromTeam(bossId.toString(), team);
        }
    }

    /**
     * Scoreboard teams outlive the entities in them, so a boss that died without a teardown would
     * leave its uuid behind forever. Every member that resolves to nothing on any loaded level is
     * dropped whenever a new boss is marked.
     */
    private static void pruneDeadMembers(MinecraftServer server, Scoreboard scoreboard, PlayerTeam team) {
        List<String> stale = new ArrayList<>();
        for (String member : team.getPlayers()) {
            UUID id;
            try {
                id = UUID.fromString(member);
            } catch (IllegalArgumentException e) {
                WoldsVaults.LOGGER.warn("Team {} holds member '{}', which is not a uuid; leaving it alone.",
                        TEAM, member);
                continue;
            }
            if (!isAliveSomewhere(server, id)) {
                stale.add(member);
            }
        }
        stale.forEach(member -> scoreboard.removePlayerFromTeam(member, team));
    }

    private static boolean isAliveSomewhere(MinecraftServer server, UUID id) {
        for (ServerLevel level : server.getAllLevels()) {
            if (level.getEntity(id) instanceof LivingEntity living && living.isAlive()) {
                return true;
            }
        }
        return false;
    }
}
