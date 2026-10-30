package xyz.iwolfking.woldsvaults.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.blocks.containers.FloatingTextEditScreen;
import xyz.iwolfking.woldsvaults.blocks.tiles.ConfigurableFloatingTextTileEntity;

import java.util.function.Supplier;

public class OpenFloatingTextScreenPacket {
    private final BlockPos pos;

    public OpenFloatingTextScreenPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(OpenFloatingTextScreenPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
    }

    public static OpenFloatingTextScreenPacket decode(FriendlyByteBuf buf) {
        return new OpenFloatingTextScreenPacket(buf.readBlockPos());
    }

    public static void handle(OpenFloatingTextScreenPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPayloadHandler.handleOpenScreen(packet));
        });
        ctx.get().setPacketHandled(true);
    }

    private static class ClientPayloadHandler {
        private static void handleOpenScreen(OpenFloatingTextScreenPacket packet) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                BlockEntity be = mc.level.getBlockEntity(packet.pos);
                if (be instanceof ConfigurableFloatingTextTileEntity tile) {
                    mc.setScreen(new FloatingTextEditScreen(tile));
                }
            }
        }
    }
}