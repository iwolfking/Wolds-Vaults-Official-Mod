package xyz.iwolfking.woldsvaults.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.spellcraft.play.SpellEdits;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Client to server: create (spell id 0), change or delete an augmented spell on the focus or wand in {@code
 * inventorySlot}. The server checks the whole item against the table before storing anything.
 */
public record SpellEditMessage(int inventorySlot, int spellId, String name, List<String> tokens, boolean delete) {
    private static final int MAX_TOKENS = 64;

    public static void encode(SpellEditMessage message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.inventorySlot);
        buffer.writeVarInt(message.spellId);
        buffer.writeUtf(message.name);
        buffer.writeVarInt(message.tokens.size());
        message.tokens.forEach(buffer::writeUtf);
        buffer.writeBoolean(message.delete);
    }

    public static SpellEditMessage decode(FriendlyByteBuf buffer) {
        int slot = buffer.readVarInt();
        int id = buffer.readVarInt();
        String name = buffer.readUtf();
        int count = Math.min(MAX_TOKENS, buffer.readVarInt());
        List<String> tokens = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            tokens.add(buffer.readUtf());
        }
        return new SpellEditMessage(slot, id, name, tokens, buffer.readBoolean());
    }

    public static void handle(SpellEditMessage message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null) {
                try {
                    SpellEdits.editSpell(player, message.inventorySlot, message.spellId, message.name, message.tokens, message.delete);
                } catch (RuntimeException e) {
                    WoldLogger.error("Spellcraft: saving spell {} failed", message.spellId, e);
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}
