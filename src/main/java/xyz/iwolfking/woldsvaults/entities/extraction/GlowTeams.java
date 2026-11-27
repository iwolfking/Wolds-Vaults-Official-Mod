package xyz.iwolfking.woldsvaults.entities.extraction;

import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * Glow outline colours for extraction entities: an entity's outline is drawn in its scoreboard team's colour, so each
 * colour has one team and an entity joins the team of the colour it should glow in.
 */
public final class GlowTeams {
    private static final String PREFIX = "wv_ext_glow_";

    private GlowTeams() {
    }

    /** Puts the entity on the team of the named chat colour, unless it is already there; logs and skips an unknown name. */
    public static void setColour(LivingEntity entity, String colourName) {
        ChatFormatting colour = ChatFormatting.getByName(colourName);
        if (colour == null || !colour.isColor()) {
            WoldLogger.errorOnce("extraction_glow_colour_" + colourName,
                    "Extraction glow colour '{}' is not a chat colour; {} keeps its current outline.", colourName, entity.getType().getRegistryName());
            return;
        }
        Scoreboard scoreboard = entity.level.getScoreboard();
        String name = PREFIX + colour.getName();
        PlayerTeam current = scoreboard.getPlayersTeam(entity.getStringUUID());
        if (current != null && current.getName().equals(name)) {
            return;
        }
        PlayerTeam team = scoreboard.getPlayerTeam(name);
        if (team == null) {
            team = scoreboard.addPlayerTeam(name);
            team.setColor(colour);
        }
        scoreboard.addPlayerToTeam(entity.getStringUUID(), team);
    }

    /** Whether a chat colour name is one an outline can use. */
    public static boolean isColour(String colourName) {
        ChatFormatting colour = colourName == null ? null : ChatFormatting.getByName(colourName);
        return colour != null && colour.isColor();
    }
}
