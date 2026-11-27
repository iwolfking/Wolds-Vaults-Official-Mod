package xyz.iwolfking.woldsvaults.client.hyper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import xyz.iwolfking.woldsvaults.network.message.BossDpsSnapshotMessage.Snapshot;

/** Client mirror of the local player's hyperboss fight statistics; dropped when the client level changes. */
public final class ClientBossDps {
    private static Snapshot snapshot = Snapshot.INACTIVE;
    private static ClientLevel level;

    private ClientBossDps() {
    }

    public static void update(Snapshot snapshot) {
        ClientBossDps.snapshot = snapshot;
        ClientBossDps.level = Minecraft.getInstance().level;
    }

    public static Snapshot get() {
        if (Minecraft.getInstance().level != level) {
            snapshot = Snapshot.INACTIVE;
            level = null;
        }
        return snapshot;
    }
}
