package xyz.iwolfking.woldsvaults.network.packets;

import appeng.menu.me.common.MEStorageMenu;
import com.refinedmods.refinedstorage.container.GridContainerMenu;
import dan200.computercraft.shared.UpgradeManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContainer;
import net.p3pp3rf1y.sophisticatedbackpacks.upgrades.deposit.DepositUpgradeWrapper;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedstorage.common.gui.LimitedBarrelContainerMenu;
import owmii.powah.lib.client.screen.container.AbstractContainerScreen;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.api.util.BackpackUtils;
import xyz.iwolfking.woldsvaults.integration.appliedenergistics.AppliedEnergisticsBackpackDeposit;
import xyz.iwolfking.woldsvaults.integration.refinedstorage.RefinedStorageBackpackDeposit;
import xyz.iwolfking.woldsvaults.integration.sophisticatedstorage.StorageContainerDeposit;

import java.util.function.Supplier;

public class DepositBackpackPacket {
    private final int slot;

    public DepositBackpackPacket(int slot) {
        this.slot = slot;
    }

    public static void encode(DepositBackpackPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.slot);
    }

    public static DepositBackpackPacket decode(FriendlyByteBuf buf) {
        return new DepositBackpackPacket(buf.readVarInt());
    }

    public static void handle(
            DepositBackpackPacket msg,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if(player == null) {
                return;
            }

            AbstractContainerMenu menu = player.containerMenu;
            ItemStack backpack = ItemStack.EMPTY;

            if(msg.slot == -1) {
                backpack = BackpackUtils.getCuriosBackpack(player);
            }
            else if (msg.slot < 0 || msg.slot >= menu.slots.size()) {
                return;
            }

            if(backpack.isEmpty()) {
                backpack = menu.getSlot(msg.slot).getItem();
            }


            if (!(backpack.getItem() instanceof BackpackItem)) {
                return;
            }

            if(!BackpackUtils.hasUpgradeOfType(DepositUpgradeWrapper.class, backpack)) {
                return;
            }

            if(menu instanceof MEStorageMenu meStorageMenu) {
                AppliedEnergisticsBackpackDeposit.deposit(player, meStorageMenu, backpack);
            }
            else if(menu instanceof GridContainerMenu gridContainerMenu) {
                RefinedStorageBackpackDeposit.deposit(player, gridContainerMenu, backpack);
            }
//            else if(menu instanceof StorageContainerMenuBase<?> storageContainerMenuBase && !(storageContainerMenuBase instanceof BackpackContainer)) {
//                StorageContainerDeposit.deposit(player, storageContainerMenuBase, backpack);
//            }
            else if(!(menu instanceof BackpackContainer) && !menu.slots.isEmpty()) {
                BackpackUtils.depositInventoryToMenu(backpack, menu, 0, menu.slots.size(), player);
            }
        });

        context.setPacketHandled(true);
    }
}