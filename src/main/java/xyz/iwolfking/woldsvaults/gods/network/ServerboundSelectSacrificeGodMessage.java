package xyz.iwolfking.woldsvaults.gods.network;

import com.blakebr0.cucumber.network.message.Message;
import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.gods.sacrifice.SacrificeAltarLogic;

import java.util.function.Supplier;

public class ServerboundSelectSacrificeGodMessage extends Message<ServerboundSelectSacrificeGodMessage> {
    private VaultGod god;

    public ServerboundSelectSacrificeGodMessage() {
    }

    public ServerboundSelectSacrificeGodMessage(VaultGod god) {
        this.god = god;
    }

    @Override
    public ServerboundSelectSacrificeGodMessage read(FriendlyByteBuf buffer) {
        return new ServerboundSelectSacrificeGodMessage(buffer.readEnum(VaultGod.class));
    }

    @Override
    public void write(ServerboundSelectSacrificeGodMessage message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.god);
    }

    @Override
    public void onMessage(ServerboundSelectSacrificeGodMessage message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer sender = context.get().getSender();
            if (sender != null && message.god != null) {
                SacrificeAltarLogic.selectGod(sender, message.god);
            }
        });
        context.get().setPacketHandled(true);
    }
}
