package xyz.iwolfking.woldsvaults.client.network;

import iskallia.vault.util.InventoryUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackStorage;
import net.p3pp3rf1y.sophisticatedcore.client.render.ClientStorageContentsTooltip;
import xyz.iwolfking.woldsvaults.api.util.BackpackUtils;
import xyz.iwolfking.woldsvaults.network.packets.ClientboundBackpackContentsRefreshPacket;

import java.util.function.Supplier;

public class BackpackContentsRefreshPacketHandler {
    public static void handle(ClientboundBackpackContentsRefreshPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
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
        });

        context.setPacketHandled(true);
    }
}
