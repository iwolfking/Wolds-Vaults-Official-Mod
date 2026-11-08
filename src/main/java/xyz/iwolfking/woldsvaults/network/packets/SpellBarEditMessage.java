package xyz.iwolfking.woldsvaults.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.augments.cast.AugmentCaster;
import xyz.iwolfking.woldsvaults.spellcraft.bar.SpellBars;

import java.util.function.Supplier;

/**
 * Client to server: set one bar slot. For the default bar {@code value} is an ability group or empty; for the bar of
 * the focus or wand in {@code inventorySlot} it is a bar entry (inherit, blank, ability or spell).
 */
public record SpellBarEditMessage(boolean item, int inventorySlot, int slot, String value) {
    public static void encode(SpellBarEditMessage message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.item);
        buffer.writeVarInt(message.inventorySlot);
        buffer.writeVarInt(message.slot);
        buffer.writeUtf(message.value);
    }

    public static SpellBarEditMessage decode(FriendlyByteBuf buffer) {
        return new SpellBarEditMessage(buffer.readBoolean(), buffer.readVarInt(), buffer.readVarInt(), buffer.readUtf());
    }

    public static void handle(SpellBarEditMessage message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) {
                return;
            }
            String error = message.item
                    ? SpellBars.editItem(player, message.inventorySlot, message.slot, message.value)
                    : SpellBars.editDefault(player, message.slot, message.value);
            if (error != null) {
                AugmentCaster.showMessage(player, "Spell bar: " + error);
            }
        });
        context.get().setPacketHandled(true);
    }
}
