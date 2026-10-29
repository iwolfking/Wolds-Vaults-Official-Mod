package xyz.iwolfking.woldsvaults.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import xyz.iwolfking.woldsvaults.workstation.WorkstationTabs;

import java.util.function.Supplier;

public class ServerboundSwitchWorkstationPacket {
    private final BlockPos pos;

    public ServerboundSwitchWorkstationPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(ServerboundSwitchWorkstationPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
    }

    public static ServerboundSwitchWorkstationPacket decode(FriendlyByteBuf buf) {
        return new ServerboundSwitchWorkstationPacket(buf.readBlockPos());
    }

    public static void handle(ServerboundSwitchWorkstationPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender == null) {
                return;
            }
            Level world = sender.getCommandSenderWorld();
            if (!world.hasChunkAt(packet.pos)) {
                return;
            }
            BlockState state = world.getBlockState(packet.pos);
            boolean isWorkstation = WorkstationTabs.isWorkstation(state.getBlock());
            if (!isWorkstation) {
                return;
            }

            ItemStack heldStack = sender.containerMenu.getCarried();
            if (!heldStack.isEmpty()) {
                sender.containerMenu.setCarried(ItemStack.EMPTY);
            }

            MenuProvider provider = state.getMenuProvider(world, packet.pos);
            if (provider != null) {
                NetworkHooks.openGui(sender, provider, packet.pos);
            } else if (world.getBlockEntity(packet.pos) instanceof MenuProvider beProvider) {
                NetworkHooks.openGui(sender, beProvider, packet.pos);
            }

            if (!heldStack.isEmpty()) {
                sender.containerMenu.setCarried(heldStack);
                sender.connection.send(new ClientboundContainerSetSlotPacket(-1, -1, -1, heldStack));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
