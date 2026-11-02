package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * One player's hyperboss fight statistics, pushed clientbound by the DPS tracker a few times a
 * second while the fight is live. An inactive snapshot clears the HUD.
 */
public class BossDpsSnapshotMessage {
    public record Source(String label, double dealt, int hits) {
    }

    public record Snapshot(boolean active, boolean ended, double total, double fightDps, double windowDps,
                           int windowTicks, int durationTicks, int hits, int iframeBlocked, int iframeReduced,
                           int otherBlocked, double maxHit, double lastHit, float bossHealth, float bossMaxHealth,
                           List<Source> sources) {
        public static final Snapshot INACTIVE = new Snapshot(false, false, 0.0, 0.0, 0.0, 0, 0, 0, 0, 0, 0,
                0.0, 0.0, 0.0F, 0.0F, List.of());
    }

    private final Snapshot snapshot;

    public BossDpsSnapshotMessage(Snapshot snapshot) {
        this.snapshot = snapshot;
    }

    public static void encode(BossDpsSnapshotMessage message, FriendlyByteBuf buffer) {
        Snapshot s = message.snapshot;
        buffer.writeBoolean(s.active());
        buffer.writeBoolean(s.ended());
        buffer.writeDouble(s.total());
        buffer.writeDouble(s.fightDps());
        buffer.writeDouble(s.windowDps());
        buffer.writeVarInt(s.windowTicks());
        buffer.writeVarInt(s.durationTicks());
        buffer.writeVarInt(s.hits());
        buffer.writeVarInt(s.iframeBlocked());
        buffer.writeVarInt(s.iframeReduced());
        buffer.writeVarInt(s.otherBlocked());
        buffer.writeDouble(s.maxHit());
        buffer.writeDouble(s.lastHit());
        buffer.writeFloat(s.bossHealth());
        buffer.writeFloat(s.bossMaxHealth());
        buffer.writeVarInt(s.sources().size());
        for (Source source : s.sources()) {
            buffer.writeUtf(source.label(), 64);
            buffer.writeDouble(source.dealt());
            buffer.writeVarInt(source.hits());
        }
    }

    public static BossDpsSnapshotMessage decode(FriendlyByteBuf buffer) {
        boolean active = buffer.readBoolean();
        boolean ended = buffer.readBoolean();
        double total = buffer.readDouble();
        double fightDps = buffer.readDouble();
        double windowDps = buffer.readDouble();
        int windowTicks = buffer.readVarInt();
        int durationTicks = buffer.readVarInt();
        int hits = buffer.readVarInt();
        int iframeBlocked = buffer.readVarInt();
        int iframeReduced = buffer.readVarInt();
        int otherBlocked = buffer.readVarInt();
        double maxHit = buffer.readDouble();
        double lastHit = buffer.readDouble();
        float bossHealth = buffer.readFloat();
        float bossMaxHealth = buffer.readFloat();
        int count = buffer.readVarInt();
        List<Source> sources = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            sources.add(new Source(buffer.readUtf(64), buffer.readDouble(), buffer.readVarInt()));
        }
        return new BossDpsSnapshotMessage(new Snapshot(active, ended, total, fightDps, windowDps, windowTicks,
                durationTicks, hits, iframeBlocked, iframeReduced, otherBlocked, maxHit, lastHit, bossHealth,
                bossMaxHealth, sources));
    }

    public static void handle(BossDpsSnapshotMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> xyz.iwolfking.woldsvaults.client.hyper.ClientBossDps.update(message.snapshot)));
        context.setPacketHandled(true);
    }
}
