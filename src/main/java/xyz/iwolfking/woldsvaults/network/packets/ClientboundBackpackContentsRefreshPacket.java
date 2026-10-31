package xyz.iwolfking.woldsvaults.network.packets;

import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.client.network.BackpackContentsRefreshPacketHandler;

public record ClientboundBackpackContentsRefreshPacket(UUID backpackUuid, CompoundTag backpackContents) {
    public static void encode(ClientboundBackpackContentsRefreshPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.backpackUuid);
        buffer.writeNbt(packet.backpackContents);
    }

    public static ClientboundBackpackContentsRefreshPacket decode(FriendlyByteBuf buffer) {
        return new ClientboundBackpackContentsRefreshPacket(buffer.readUUID(), buffer.readNbt());
    }

    public static void handle(ClientboundBackpackContentsRefreshPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            BackpackContentsRefreshPacketHandler.handle(packet, contextSupplier);
        }
    }

}
