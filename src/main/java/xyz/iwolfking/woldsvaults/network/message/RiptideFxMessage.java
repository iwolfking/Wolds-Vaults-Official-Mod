package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import xyz.iwolfking.woldsvaults.init.ModNetwork;

import java.util.function.Supplier;
import xyz.iwolfking.woldsvaults.client.fx.RiptideFx;

/**
 * Clientbound Tidebreaker riptide visuals: a shock cone pulse, a lightning arc from the dashing player to a mob,
 * the storm aura around the player, an impact burst on a mob, or the start (ticks above zero) or end of the
 * post-dash hover. Sent to everyone tracking the player; all geometry, poses and particles are built client side.
 */
public class RiptideFxMessage {
    public static final byte CONE = 0;
    public static final byte ARC = 1;
    public static final byte AURA = 2;
    public static final byte BURST = 3;
    public static final byte HOVER = 4;

    private final byte kind;
    private final int sourceId;
    private final int targetId;
    private final Vec3 first;
    private final Vec3 second;
    private final float ahead;
    private final float behind;
    private final float radius;
    private final boolean chilled;
    private final int ticks;

    public RiptideFxMessage(byte kind, int sourceId, int targetId, Vec3 first, Vec3 second, float ahead, float behind, float radius, boolean chilled, int ticks) {
        this.kind = kind;
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.first = first;
        this.second = second;
        this.ahead = ahead;
        this.behind = behind;
        this.radius = radius;
        this.chilled = chilled;
        this.ticks = ticks;
    }

    public static RiptideFxMessage cone(ServerPlayer player, Vec3 origin, Vec3 axis, float ahead, float behind, float radius) {
        return new RiptideFxMessage(CONE, player.getId(), -1, origin, axis, ahead, behind, radius, false, 0);
    }

    public static RiptideFxMessage arc(ServerPlayer player, Entity target, boolean chilled) {
        return new RiptideFxMessage(ARC, player.getId(), target.getId(), chest(player), center(target), 0.0F, 0.0F, 0.0F, chilled, 0);
    }

    public static RiptideFxMessage aura(ServerPlayer player) {
        return new RiptideFxMessage(AURA, player.getId(), -1, chest(player), Vec3.ZERO, 0.0F, 0.0F, 0.0F, false, 0);
    }

    public static RiptideFxMessage burst(ServerPlayer player, Entity target, boolean chilled) {
        return new RiptideFxMessage(BURST, player.getId(), target.getId(), center(target), Vec3.ZERO, target.getBbWidth(), target.getBbHeight(), 0.0F, chilled, 0);
    }

    public static RiptideFxMessage hover(ServerPlayer player, int ticks) {
        return new RiptideFxMessage(HOVER, player.getId(), -1, chest(player), Vec3.ZERO, 0.0F, 0.0F, 0.0F, false, Math.max(0, ticks));
    }

    public static void send(ServerPlayer player, RiptideFxMessage message) {
        ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), message);
    }

    private static Vec3 chest(Entity entity) {
        return entity.position().add(0.0, entity.getBbHeight() * 0.6, 0.0);
    }

    private static Vec3 center(Entity entity) {
        return entity.position().add(0.0, entity.getBbHeight() * 0.5, 0.0);
    }

    public static void encode(RiptideFxMessage message, FriendlyByteBuf buffer) {
        buffer.writeByte(message.kind);
        buffer.writeVarInt(message.sourceId);
        buffer.writeVarInt(message.targetId + 1);
        writeVec(buffer, message.first);
        writeVec(buffer, message.second);
        buffer.writeFloat(message.ahead);
        buffer.writeFloat(message.behind);
        buffer.writeFloat(message.radius);
        buffer.writeBoolean(message.chilled);
        buffer.writeVarInt(message.ticks);
    }

    public static RiptideFxMessage decode(FriendlyByteBuf buffer) {
        byte kind = buffer.readByte();
        int sourceId = buffer.readVarInt();
        int targetId = buffer.readVarInt() - 1;
        Vec3 first = readVec(buffer);
        Vec3 second = readVec(buffer);
        return new RiptideFxMessage(kind, sourceId, targetId, first, second, buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readBoolean(), buffer.readVarInt());
    }

    private static void writeVec(FriendlyByteBuf buffer, Vec3 vec) {
        buffer.writeDouble(vec.x);
        buffer.writeDouble(vec.y);
        buffer.writeDouble(vec.z);
    }

    private static Vec3 readVec(FriendlyByteBuf buffer) {
        return new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    public static void handle(RiptideFxMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> RiptideFx.accept(message)));
        context.setPacketHandled(true);
    }

    public byte getKind() {
        return this.kind;
    }

    public int getSourceId() {
        return this.sourceId;
    }

    public int getTargetId() {
        return this.targetId;
    }

    public Vec3 getFirst() {
        return this.first;
    }

    public Vec3 getSecond() {
        return this.second;
    }

    public float getAhead() {
        return this.ahead;
    }

    public float getBehind() {
        return this.behind;
    }

    public float getRadius() {
        return this.radius;
    }

    public boolean isChilled() {
        return this.chilled;
    }

    public int getTicks() {
        return this.ticks;
    }
}
