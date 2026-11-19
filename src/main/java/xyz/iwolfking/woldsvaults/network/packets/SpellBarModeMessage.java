package xyz.iwolfking.woldsvaults.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.spellcraft.bar.BarMode;
import xyz.iwolfking.woldsvaults.spellcraft.play.SpellBars;

import java.util.function.Supplier;

/** Client to server: switch the player's bar to classic mode or slot mode ({@link BarMode} ordinal). */
public record SpellBarModeMessage(int mode) {
    public static void encode(SpellBarModeMessage message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.mode);
    }

    public static SpellBarModeMessage decode(FriendlyByteBuf buffer) {
        return new SpellBarModeMessage(buffer.readVarInt());
    }

    public static void handle(SpellBarModeMessage message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) {
                return;
            }
            BarMode mode = BarMode.byOrdinal(message.mode);
            if (mode == null) {
                WoldLogger.error("Spell bar: {} sent bar mode {}, which does not exist; the mode was left unchanged",
                        player.getGameProfile().getName(), message.mode);
                return;
            }
            SpellBars.setMode(player, mode);
        });
        context.get().setPacketHandled(true);
    }
}
