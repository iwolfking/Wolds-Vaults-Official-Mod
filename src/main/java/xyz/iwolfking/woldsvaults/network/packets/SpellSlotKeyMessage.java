package xyz.iwolfking.woldsvaults.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.spellcraft.play.SpellBarInput;

import java.util.function.Supplier;

/** Client to server: a spell slot key was pressed ({@code down}) or released. */
public record SpellSlotKeyMessage(int slot, boolean down) {
    public static void encode(SpellSlotKeyMessage message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.slot);
        buffer.writeBoolean(message.down);
    }

    public static SpellSlotKeyMessage decode(FriendlyByteBuf buffer) {
        return new SpellSlotKeyMessage(buffer.readVarInt(), buffer.readBoolean());
    }

    public static void handle(SpellSlotKeyMessage message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null) {
                SpellBarInput.onSlotKey(player, message.slot, message.down);
            }
        });
        context.get().setPacketHandled(true);
    }
}
