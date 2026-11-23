package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.blocks.DecorativeCrystalBlock;
import xyz.iwolfking.woldsvaults.init.ModNetwork;

import java.util.function.Supplier;

/** A player's choices for a decorative crystal: its shape and colour (by ordinal), variant, stage and whether it's Prismatic. */
public record DecorativeCrystalMessage(BlockPos pos, int shape, int variant, int stage, int color, boolean prismatic) {
    public static void encode(DecorativeCrystalMessage message, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(message.pos);
        buffer.writeVarInt(message.shape);
        buffer.writeVarInt(message.variant);
        buffer.writeVarInt(message.stage);
        buffer.writeVarInt(message.color);
        buffer.writeBoolean(message.prismatic);
    }

    public static DecorativeCrystalMessage decode(FriendlyByteBuf buffer) {
        return new DecorativeCrystalMessage(buffer.readBlockPos(), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(), buffer.readBoolean());
    }

    public static void handle(DecorativeCrystalMessage message, Supplier<NetworkEvent.Context> context) {
        ModNetwork.onServer(context, player -> DecorativeCrystalBlock.configure(player, message.pos, message.shape, message.variant, message.stage, message.color, message.prismatic));
    }
}
