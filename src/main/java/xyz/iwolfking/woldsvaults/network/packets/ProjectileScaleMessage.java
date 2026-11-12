package xyz.iwolfking.woldsvaults.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.augments.effect.AugmentTags;

import java.util.function.Supplier;

/**
 * Server to client: an augmented projectile's Size Up / Size Down scale, so the client draws it scaled and its copy
 * of the hitbox matches the server's.
 */
public record ProjectileScaleMessage(int entityId, float scale) {
    public static void encode(ProjectileScaleMessage message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.entityId);
        buffer.writeFloat(message.scale);
    }

    public static ProjectileScaleMessage decode(FriendlyByteBuf buffer) {
        return new ProjectileScaleMessage(buffer.readVarInt(), buffer.readFloat());
    }

    public static void handle(ProjectileScaleMessage message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> apply(message)));
        context.get().setPacketHandled(true);
    }

    private static void apply(ProjectileScaleMessage message) {
        Entity entity = Minecraft.getInstance().level == null ? null : Minecraft.getInstance().level.getEntity(message.entityId);
        if (entity != null) {
            entity.getPersistentData().putFloat(AugmentTags.SCALE, message.scale);
            entity.refreshDimensions();
        }
    }
}
