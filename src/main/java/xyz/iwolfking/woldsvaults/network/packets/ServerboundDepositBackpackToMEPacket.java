package xyz.iwolfking.woldsvaults.network.packets;

import appeng.menu.me.common.MEStorageMenu;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.integration.appliedenergistics.AppliedEnergisticsBackpackDeposit;

public record ServerboundDepositBackpackToMEPacket() {
    public static void encode(ServerboundDepositBackpackToMEPacket packet, FriendlyByteBuf buffer) {
    }

    public static ServerboundDepositBackpackToMEPacket decode(FriendlyByteBuf buffer) {
        return new ServerboundDepositBackpackToMEPacket();
    }

    public static void handle(ServerboundDepositBackpackToMEPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && player.containerMenu instanceof MEStorageMenu menu && menu.stillValid(player)) {
                AppliedEnergisticsBackpackDeposit.deposit(player, menu);
            }
        });
        context.setPacketHandled(true);
    }
}