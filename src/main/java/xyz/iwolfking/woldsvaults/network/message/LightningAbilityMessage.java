package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.modifiers.gear.lightning.LightningFiend;

import java.util.function.Supplier;

/**
 * A click while in lightning form (Lightning Fiend 6-piece): which ability, the entity under the client's crosshair
 * (-1 for none) and the way the camera looks, which a third-person camera can turn away from the player's body. The
 * server checks the form, cooldown, range and sight, and falls back to aiming along the look itself.
 */
public class LightningAbilityMessage {
    public static final int ARC_LASH = 0;
    public static final int LIGHTNING_ROD = 1;
    public static final int THUNDERSTRIKE = 2;

    private final int ability;
    private final int targetId;
    private final Vec3 look;

    public LightningAbilityMessage(int ability, int targetId, Vec3 look) {
        this.ability = ability;
        this.targetId = targetId;
        this.look = look;
    }

    public static void encode(LightningAbilityMessage message, FriendlyByteBuf buffer) {
        buffer.writeByte(message.ability);
        buffer.writeInt(message.targetId);
        buffer.writeFloat((float) message.look.x);
        buffer.writeFloat((float) message.look.y);
        buffer.writeFloat((float) message.look.z);
    }

    public static LightningAbilityMessage decode(FriendlyByteBuf buffer) {
        return new LightningAbilityMessage(buffer.readByte(), buffer.readInt(), new Vec3(buffer.readFloat(), buffer.readFloat(), buffer.readFloat()));
    }

    public static void handle(LightningAbilityMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            Vec3 look = message.look;
            boolean usable = Double.isFinite(look.x) && Double.isFinite(look.y) && Double.isFinite(look.z) && look.lengthSqr() > 1.0E-4;
            look = usable ? look.normalize() : player.getLookAngle();
            if (message.ability == ARC_LASH) {
                LightningFiend.arcLash(player, look);
            } else if (message.ability == LIGHTNING_ROD) {
                LightningFiend.lightningRod(player, message.targetId, look);
            } else if (message.ability == THUNDERSTRIKE) {
                LightningFiend.thunderstrike(player);
            }
        });
        context.setPacketHandled(true);
    }
}
