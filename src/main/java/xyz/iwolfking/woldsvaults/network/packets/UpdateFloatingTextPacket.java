package xyz.iwolfking.woldsvaults.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.blocks.tiles.ConfigurableFloatingTextTileEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class UpdateFloatingTextPacket {
    private final BlockPos pos;
    private final List<ConfigurableFloatingTextTileEntity.TextLine> lines;
    private final boolean editable;
    private final float scale;

    public UpdateFloatingTextPacket(
        BlockPos pos,
        List<ConfigurableFloatingTextTileEntity.TextLine> lines,
        boolean editable,
        float scale
    ) {
        this.pos = pos;
        this.lines = lines;
        this.editable = editable;
        this.scale = scale;
    }

    public static void encode(UpdateFloatingTextPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeInt(msg.lines.size());

        for (var line : msg.lines) {
            buf.writeUtf(line.text);
            buf.writeInt(line.color);
            buf.writeBoolean(line.bold);
            buf.writeBoolean(line.italic);
            buf.writeBoolean(line.underlined);
        }

        buf.writeBoolean(msg.editable);
        buf.writeFloat(msg.scale);
    }

    public static UpdateFloatingTextPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        int count = buf.readInt();

        List<ConfigurableFloatingTextTileEntity.TextLine> lines = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ConfigurableFloatingTextTileEntity.TextLine line = new ConfigurableFloatingTextTileEntity.TextLine();
            line.text = buf.readUtf();
            line.color = buf.readInt();
            line.bold = buf.readBoolean();
            line.italic = buf.readBoolean();
            line.underlined = buf.readBoolean();
            lines.add(line);
        }

        boolean editable = buf.readBoolean();
        float scale = buf.readFloat();

        return new UpdateFloatingTextPacket(pos, lines, editable, scale);
    }

    public static void handle(UpdateFloatingTextPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            BlockEntity be = player.level.getBlockEntity(msg.pos);
            if (!(be instanceof ConfigurableFloatingTextTileEntity tile)) return;

            if (!tile.isEditable() && !player.hasPermissions(2)) return;

            tile.setLines(msg.lines);
            tile.setEditable(msg.editable);
            tile.setScale(msg.scale);

            tile.setChanged();
            player.level.sendBlockUpdated(
                msg.pos,
                tile.getBlockState(),
                tile.getBlockState(),
                3
            );
        });
        ctx.get().setPacketHandled(true);
    }
}
