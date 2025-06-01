package xyz.iwolfking.woldsvaults.network.message;

import iskallia.vault.container.NBTElementContainer;
import iskallia.vault.core.net.ArrayBitBuffer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import xyz.iwolfking.woldsvaults.data.skill.PlayerDivinityData;
import xyz.iwolfking.woldsvaults.gui.menus.divinity.DivinityTree;
import xyz.iwolfking.woldsvaults.init.ModContainers;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

public class ServerboundOpenDivinityScreenMessage {
    public static final ServerboundOpenDivinityScreenMessage INSTANCE = new ServerboundOpenDivinityScreenMessage();

    public ServerboundOpenDivinityScreenMessage() {
    }

    public static void encode(ServerboundOpenDivinityScreenMessage message, FriendlyByteBuf buffer) {
    }

    public static ServerboundOpenDivinityScreenMessage decode(FriendlyByteBuf buffer) {
        return INSTANCE;
    }

    public static void handle(ServerboundOpenDivinityScreenMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null) {
                PlayerDivinityData playerPowersData = PlayerDivinityData.get((ServerLevel)sender.level);
                final DivinityTree powerTree = playerPowersData.getDivinityTree(sender);
                NetworkHooks.openGui(sender, new MenuProvider() {
                    @Nonnull
                    public Component getDisplayName() {
                        return new TranslatableComponent("container.vault.divinity");
                    }

                    @ParametersAreNonnullByDefault
                    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
                        return new NBTElementContainer<>(() -> {
                            return ModContainers.DIVINITY_TAB_CONTAINER;
                        }, i, playerInventory.player, powerTree);
                    }
                }, (buffer) -> {
                    ArrayBitBuffer buffer1 = ArrayBitBuffer.empty();
                    powerTree.writeBits(buffer1);
                    buffer.writeLongArray(buffer1.toLongArray());
                });
            }
        });
        context.setPacketHandled(true);
    }
}
