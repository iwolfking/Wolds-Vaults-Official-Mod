package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.modifiers.gear.lightning.LightningFiend;

import java.util.function.Supplier;

/**
 * A dash key tapped while in lightning form, with the yaw the dash is relative to (the camera's, which a decoupled
 * third-person camera can turn away from the player's body): the server checks the form and cooldown, then dashes.
 */
public class LightningDashMessage {
    private final int direction;
    private final float yaw;

    public LightningDashMessage(LightningFiend.Dash direction, float yaw) {
        this(direction.ordinal(), yaw);
    }

    private LightningDashMessage(int direction, float yaw) {
        this.direction = direction;
        this.yaw = yaw;
    }

    public static void encode(LightningDashMessage message, FriendlyByteBuf buffer) {
        buffer.writeByte(message.direction);
        buffer.writeFloat(message.yaw);
    }

    public static LightningDashMessage decode(FriendlyByteBuf buffer) {
        return new LightningDashMessage(buffer.readByte(), buffer.readFloat());
    }

    public static void handle(LightningDashMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            LightningFiend.Dash[] dashes = LightningFiend.Dash.values();
            if (player != null && message.direction >= 0 && message.direction < dashes.length && Float.isFinite(message.yaw)) {
                LightningFiend.dash(player, dashes[message.direction], message.yaw);
            }
        });
        context.setPacketHandled(true);
    }
}
