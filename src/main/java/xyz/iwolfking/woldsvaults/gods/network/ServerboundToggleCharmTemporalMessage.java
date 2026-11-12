package xyz.iwolfking.woldsvaults.gods.network;

import com.blakebr0.cucumber.network.message.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.gods.charms.CharmTemporalManager;

import java.util.function.Supplier;

/** Toggles the equipped mythic charm's temporal blessing. All validation lives in {@link CharmTemporalManager}. */
public class ServerboundToggleCharmTemporalMessage extends Message<ServerboundToggleCharmTemporalMessage> {
    public ServerboundToggleCharmTemporalMessage() {
    }

    @Override
    public ServerboundToggleCharmTemporalMessage read(FriendlyByteBuf buffer) {
        return new ServerboundToggleCharmTemporalMessage();
    }

    @Override
    public void write(ServerboundToggleCharmTemporalMessage message, FriendlyByteBuf buffer) {
    }

    @Override
    public void onMessage(ServerboundToggleCharmTemporalMessage message, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                CharmTemporalManager.toggle(player);
            }
        });
        ctx.setPacketHandled(true);
    }
}
