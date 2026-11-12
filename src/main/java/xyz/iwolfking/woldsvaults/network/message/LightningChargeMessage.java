package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;
import xyz.iwolfking.woldsvaults.client.lightning.ClientLightningForm;

/** Tells a lightning player their Thunderstrike charge, the charge cap and the strike's multiplier at full charge. */
public class LightningChargeMessage {
    private final float charge;
    private final float chargeMax;
    private final float maxMultiplier;

    public LightningChargeMessage(float charge, float chargeMax, float maxMultiplier) {
        this.charge = charge;
        this.chargeMax = chargeMax;
        this.maxMultiplier = maxMultiplier;
    }

    public static void encode(LightningChargeMessage message, FriendlyByteBuf buffer) {
        buffer.writeFloat(message.charge);
        buffer.writeFloat(message.chargeMax);
        buffer.writeFloat(message.maxMultiplier);
    }

    public static LightningChargeMessage decode(FriendlyByteBuf buffer) {
        return new LightningChargeMessage(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
    }

    public static void handle(LightningChargeMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientLightningForm.charge(message.charge, message.chargeMax, message.maxMultiplier)));
        context.setPacketHandled(true);
    }
}
