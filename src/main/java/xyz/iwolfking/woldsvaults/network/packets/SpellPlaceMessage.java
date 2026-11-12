package xyz.iwolfking.woldsvaults.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.augments.FocusAugments;

import java.util.function.Supplier;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * Client to server: put a spell of the focus or wand in {@code inventorySlot} on a bar slot (-1 takes it off the bar)
 * with a priority (0 keeps the stored one). The server checks the item before storing anything.
 */
public record SpellPlaceMessage(int inventorySlot, int spellId, int slot, int prio) {
    public static void encode(SpellPlaceMessage message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.inventorySlot);
        buffer.writeVarInt(message.spellId);
        buffer.writeVarInt(message.slot + 1);
        buffer.writeVarInt(message.prio);
    }

    public static SpellPlaceMessage decode(FriendlyByteBuf buffer) {
        return new SpellPlaceMessage(buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt() - 1, buffer.readVarInt());
    }

    public static void handle(SpellPlaceMessage message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null) {
                try {
                    FocusAugments.placeSpell(player, message.inventorySlot, message.spellId, message.slot, message.prio);
                } catch (RuntimeException e) {
                    WoldLogger.error("Spellcraft: placing spell {} failed", message.spellId, e);
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}
