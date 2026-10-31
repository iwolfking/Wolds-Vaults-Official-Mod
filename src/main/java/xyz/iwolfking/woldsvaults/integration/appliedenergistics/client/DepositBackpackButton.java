package xyz.iwolfking.woldsvaults.integration.appliedenergistics.client;

import appeng.client.gui.Icon;
import appeng.client.gui.widgets.IconButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.api.util.BackpackUtils;
import xyz.iwolfking.woldsvaults.integration.appliedenergistics.packet.ServerboundDepositBackpackToMEPacket;

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
        return backpack.isEmpty() ? Blocks.BARRIER.asItem() : backpack.getItem();
    }

    public List<Component> getTooltipMessage() {
        ItemStack backpack = findTargetBackpack();
        if (backpack.isEmpty()) {
            return List.of(
                    new TextComponent("Deposit Backpack"),
                    new TextComponent("No valid backpack found to deposit").withStyle(ChatFormatting.GRAY)
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
