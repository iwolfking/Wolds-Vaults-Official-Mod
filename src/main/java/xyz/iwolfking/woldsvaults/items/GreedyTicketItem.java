package xyz.iwolfking.woldsvaults.items;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Currency for restocking Mr. Greedy's shop, spent only by the greed trader's Restock button. */
public class GreedyTicketItem extends Item {
    public GreedyTicketItem(ResourceLocation id, Properties properties) {
        super(properties);
        this.setRegistryName(id);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (Screen.hasShiftDown()) {
            tooltip.add(new TextComponent("Spent at Mr. Greedy to restock his shop")
                    .withStyle(ChatFormatting.GOLD));
        } else {
            tooltip.add(new TextComponent("Hold SHIFT for more info").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
