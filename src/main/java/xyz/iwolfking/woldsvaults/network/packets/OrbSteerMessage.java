package xyz.iwolfking.woldsvaults.network.packets;

import iskallia.vault.entity.entity.LightningOrbEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.augments.effect.LightningOrbs;

import java.util.function.Supplier;

/**
 * Server to client: a steered Ball of Lightning's new direction and speed (Homing / Orbit), so the client's copy
 * moves where the server's does.
 */
public record OrbSteerMessage(int entityId, Vec3 direction, float speed) {
    public static void encode(OrbSteerMessage message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.entityId);
        buffer.writeDouble(message.direction.x);
        buffer.writeDouble(message.direction.y);
        buffer.writeDouble(message.direction.z);
        buffer.writeFloat(message.speed);
    }

    public static OrbSteerMessage decode(FriendlyByteBuf buffer) {
        return new OrbSteerMessage(buffer.readVarInt(), new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
                buffer.readFloat());
    }

    public static void handle(OrbSteerMessage message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> apply(message)));
        context.get().setPacketHandled(true);
    }

    private static void apply(OrbSteerMessage message) {
        Entity entity = Minecraft.getInstance().level == null ? null : Minecraft.getInstance().level.getEntity(message.entityId);
        if (entity instanceof LightningOrbEntity orb) {
            LightningOrbs.steer(orb, message.direction, message.speed);
        }
    }
}
