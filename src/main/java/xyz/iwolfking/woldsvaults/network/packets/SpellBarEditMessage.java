package xyz.iwolfking.woldsvaults.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.spellcraft.cast.SpellMessages;
import xyz.iwolfking.woldsvaults.spellcraft.play.DefaultBar;

import java.util.function.Supplier;

/** Client to server: set one slot of the default bar to an ability group, or clear it with an empty value. */
public record SpellBarEditMessage(int slot, String value) {
    public static void encode(SpellBarEditMessage message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.slot);
        buffer.writeUtf(message.value);
    }

    public static SpellBarEditMessage decode(FriendlyByteBuf buffer) {
        return new SpellBarEditMessage(buffer.readVarInt(), buffer.readUtf());
    }

    public static void handle(SpellBarEditMessage message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) {
                return;
            }
            String error = DefaultBar.editDefault(player, message.slot, message.value);
            if (error != null) {
                SpellMessages.show(player, "Spell bar: " + error);
            }
        });
        context.get().setPacketHandled(true);
    }
}
