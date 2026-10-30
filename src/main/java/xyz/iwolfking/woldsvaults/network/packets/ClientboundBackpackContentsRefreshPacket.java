package xyz.iwolfking.woldsvaults.network.packets;

import java.util.UUID;
import java.util.function.Supplier;

import iskallia.vault.util.InventoryUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackStorage;
import net.p3pp3rf1y.sophisticatedcore.client.render.ClientStorageContentsTooltip;
import xyz.iwolfking.woldsvaults.api.util.BackpackUtils;

public record ClientboundBackpackContentsRefreshPacket(UUID backpackUuid, CompoundTag backpackContents) {
    public static void encode(ClientboundBackpackContentsRefreshPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.backpackUuid);
        buffer.writeNbt(packet.backpackContents);
    }

    public static ClientboundBackpackContentsRefreshPacket decode(FriendlyByteBuf buffer) {
        return new ClientboundBackpackContentsRefreshPacket(buffer.readUUID(), buffer.readNbt());
    }

    public static void handle(ClientboundBackpackContentsRefreshPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist == net.minecraftforge.api.distmarker.Dist.CLIENT) {
                BackpackStorage.get().setBackpackContents(packet.backpackUuid(), packet.backpackContents());
                try {
                    ClientStorageContentsTooltip.refreshContents();
                } catch (Throwable ignored) {
                }
                LocalPlayer player = Minecraft.getInstance().player;
                if(player == null) {
                    return;
                }
                InventoryUtil.findAllItems(player).forEach(itemAccess -> {
                    if(itemAccess.getItem() instanceof BackpackItem) {
                        BackpackUtils.refreshWrapper(itemAccess.getStack(), packet.backpackUuid());
                    }
                });
            }
        });
        context.setPacketHandled(true);
    }

}
