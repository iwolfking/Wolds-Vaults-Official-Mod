package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.blocks.tiles.SuppressorTileEntity;

import java.util.function.Supplier;

public class SuppressorUpdateMessage {
    private final BlockPos pos;
    private final SuppressorTileEntity.SuppressorType type;
    private final boolean enabled;
    private final int range;

    public SuppressorUpdateMessage(BlockPos pos, SuppressorTileEntity.SuppressorType type, boolean enabled, int range) {
        this.pos = pos;
        this.type = type;
        this.enabled = enabled;
        this.range = range;
    }

    public SuppressorUpdateMessage(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.type = buffer.readEnum(SuppressorTileEntity.SuppressorType.class);
        this.enabled = buffer.readBoolean();
        this.range = buffer.readInt();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeEnum(type);
        buffer.writeBoolean(enabled);
        buffer.writeInt(range);
    }

    public static SuppressorUpdateMessage decode(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        SuppressorTileEntity.SuppressorType type = buffer.readEnum(SuppressorTileEntity.SuppressorType.class);
        boolean enabled = buffer.readBoolean();
        int range = buffer.readInt();
        return new SuppressorUpdateMessage(pos, type, enabled, range);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && player.level != null) {
                BlockEntity blockEntity = player.level.getBlockEntity(pos);
                if (blockEntity instanceof SuppressorTileEntity suppressor) {
                    SuppressorTileEntity.Config config = suppressor.getConfig(type);
                    if (config != null) {
                        config.setEnabled(enabled);
                        config.setRange(range);
                        suppressor.setChanged();
                        player.level.sendBlockUpdated(pos, suppressor.getBlockState(), suppressor.getBlockState(), 3);
                    }
                }
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}