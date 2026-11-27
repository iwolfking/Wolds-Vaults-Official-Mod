package xyz.iwolfking.woldsvaults.client.spellcraft.state;

import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * The stat values the server last mirrored to this client, cleared on logout. The client's stat helpers return the
 * mirrored value for the local player, so the statistics tab and spell previews show the same numbers the server uses.
 */
public final class MirroredStats {
    private static UUID clientPlayer;
    private static float[] clientValues;

    private MirroredStats() {
    }

    /** Called on the client when a mirror arrives. */
    public static void receive(UUID player, float[] values) {
        clientPlayer = player;
        clientValues = values;
    }

    public static void reset() {
        clientPlayer = null;
        clientValues = null;
    }

    /** The mirrored value of a stat for this entity on the client, or null when there is none to use. */
    @Nullable
    public static Float client(Entity entity, int stat) {
        if (entity == null || !entity.level.isClientSide() || clientValues == null || !entity.getUUID().equals(clientPlayer)) {
            return null;
        }
        return clientValues[stat];
    }
}
