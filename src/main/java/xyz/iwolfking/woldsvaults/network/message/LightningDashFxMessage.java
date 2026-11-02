package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * A lightning dash starting: who is dashing, their path (feet positions, start to end) and how many ticks it takes.
 * Nearby clients draw the player as lightning crossing it; the dashing player's own client also carries them along.
 */
public class LightningDashFxMessage {
    private static final int MAX_POINTS = 1024;

    private final int entityId;
    private final List<Vec3> path;
    private final int ticks;
    private final long seed;

    public LightningDashFxMessage(int entityId, List<Vec3> path, int ticks, long seed) {
        this.entityId = entityId;
        this.path = path;
        this.ticks = ticks;
        this.seed = seed;
    }

    public static void encode(LightningDashFxMessage message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.entityId);
        buffer.writeVarInt(message.ticks);
        buffer.writeLong(message.seed);
        int size = Math.min(message.path.size(), MAX_POINTS);
        buffer.writeVarInt(size);
        for (Vec3 point : message.path.subList(0, size)) {
            buffer.writeDouble(point.x);
            buffer.writeDouble(point.y);
            buffer.writeDouble(point.z);
        }
    }

    public static LightningDashFxMessage decode(FriendlyByteBuf buffer) {
        int entityId = buffer.readVarInt();
        int ticks = buffer.readVarInt();
        long seed = buffer.readLong();
        int size = Math.min(buffer.readVarInt(), MAX_POINTS);
        List<Vec3> path = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            path.add(new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()));
        }
        return new LightningDashFxMessage(entityId, path, ticks, seed);
    }

    public static void handle(LightningDashFxMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (!message.path.isEmpty() && message.ticks > 0) {
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> xyz.iwolfking.woldsvaults.client.lightning.ClientLightningForm.dash(message.entityId, message.path, message.ticks, message.seed)));
        }
        context.setPacketHandled(true);
    }
}
