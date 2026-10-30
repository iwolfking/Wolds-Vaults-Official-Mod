package xyz.iwolfking.woldsvaults.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
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

    public UpdateFloatingTextPacket(BlockPos pos, List<ConfigurableFloatingTextTileEntity.TextLine> lines, boolean editable, float scale) {
        this.pos = pos;
        this.lines = lines;
        this.editable = editable;
        this.scale = scale;
    }

    public static void encode(UpdateFloatingTextPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);

        buf.writeInt(packet.lines.size());
        for (ConfigurableFloatingTextTileEntity.TextLine line : packet.lines) {
            buf.writeUtf(line.text);
            buf.writeInt(line.color);
            buf.writeBoolean(line.bold);
            buf.writeBoolean(line.italic);
            buf.writeBoolean(line.underlined);
        }

        buf.writeBoolean(packet.editable);
        buf.writeFloat(packet.scale);
    }

    public static UpdateFloatingTextPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();

        int size = buf.readInt();
        List<ConfigurableFloatingTextTileEntity.TextLine> lines = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            ConfigurableFloatingTextTileEntity.TextLine line = new ConfigurableFloatingTextTileEntity.TextLine(buf.readUtf());
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

    public static void handle(UpdateFloatingTextPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            BlockPos pos = packet.pos;

            if (!pos.closerThan(player.blockPosition(), 16.0)) {
                return;
            }

            BlockHitResult hitResult = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
            PlayerInteractEvent.RightClickBlock claimCheckEvent = new PlayerInteractEvent.RightClickBlock(player, player.getUsedItemHand(), pos, hitResult);
            MinecraftForge.EVENT_BUS.post(claimCheckEvent);

            if (claimCheckEvent.isCanceled()) {
                return; 
            }

            BlockEntity be = player.level.getBlockEntity(pos);
            if (be instanceof ConfigurableFloatingTextTileEntity tile) {
                tile.setLines(packet.lines);
                tile.setEditable(packet.editable);
                tile.setScale(packet.scale);
                tile.setChanged();

                player.level.sendBlockUpdated(pos, tile.getBlockState(), tile.getBlockState(), 3);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}