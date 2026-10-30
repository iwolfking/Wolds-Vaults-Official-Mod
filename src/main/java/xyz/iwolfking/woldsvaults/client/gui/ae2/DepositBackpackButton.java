package xyz.iwolfking.woldsvaults.client.gui.ae2;

import appeng.client.gui.Icon;
import appeng.client.gui.widgets.IconButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.api.util.BackpackUtils;
import xyz.iwolfking.woldsvaults.network.packets.ServerboundDepositBackpackToMEPacket;

import java.util.List;

public class DepositBackpackButton extends IconButton {
    public DepositBackpackButton() {
        super(button -> ModNetwork.sendToServer(new ServerboundDepositBackpackToMEPacket()));
    }

    protected Icon getIcon() {
        return Icon.TOOLBAR_BUTTON_BACKGROUND;
    }

    protected Item getItemOverlay() {
        ItemStack backpack = findTargetBackpack();
        return backpack.isEmpty() ? ModItems.BACKPACK.get() : backpack.getItem();
    }

    public List<Component> getTooltipMessage() {
        ItemStack backpack = findTargetBackpack();
        if (backpack.isEmpty()) {
            return List.of(
                    new TextComponent("Deposit Backpack"),
                    new TextComponent("No backpack found in your curios or inventory").withStyle(ChatFormatting.GRAY)
            );
        }
        return List.of(
                new TextComponent("Deposit Backpack"),
                new TextComponent("Import " + backpack.getHoverName().getString()).withStyle(ChatFormatting.GRAY),
                new TextComponent("Moves everything from this backpack into the network").withStyle(ChatFormatting.GRAY)
        );
    }

    private ItemStack findTargetBackpack() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return ItemStack.EMPTY;
        }

        return BackpackUtils.findBackpack(player);
    }
}
