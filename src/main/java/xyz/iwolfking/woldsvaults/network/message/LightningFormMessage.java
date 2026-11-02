package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Tells a player they became lightning (Lightning Fiend 6-piece): for how long, and the cooldowns of its dashes and clicks. */
public class LightningFormMessage {
    private final int ticks;
    private final int dashCooldownTicks;
    private final int lashCooldownTicks;
    private final int rodCooldownTicks;

    public LightningFormMessage(int ticks, int dashCooldownTicks, int lashCooldownTicks, int rodCooldownTicks) {
        this.ticks = ticks;
        this.dashCooldownTicks = dashCooldownTicks;
        this.lashCooldownTicks = lashCooldownTicks;
        this.rodCooldownTicks = rodCooldownTicks;
    }

    public static void encode(LightningFormMessage message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.ticks);
        buffer.writeVarInt(message.dashCooldownTicks);
        buffer.writeVarInt(message.lashCooldownTicks);
        buffer.writeVarInt(message.rodCooldownTicks);
    }

    public static LightningFormMessage decode(FriendlyByteBuf buffer) {
        return new LightningFormMessage(buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt());
    }

    public static void handle(LightningFormMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> xyz.iwolfking.woldsvaults.client.lightning.ClientLightningForm.start(message.ticks, message.dashCooldownTicks, message.lashCooldownTicks, message.rodCooldownTicks)));
        context.setPacketHandled(true);
    }
}
