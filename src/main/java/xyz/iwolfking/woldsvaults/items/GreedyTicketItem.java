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

/**
 * Currency for restocking Mr. Greedy's shop.
 *
 * <p>The ticket used to be consumable for +10 greed reputation. Reputation is the greed rework's
 * rank-up currency and is deliberately finite, so a consumable that mints it is a faucet straight
 * into rank progression; the reroll price moved onto tickets instead and the consume-for-rep use
 * was dropped. The item is now spent only by the greed trader's Restock button.</p>
 */
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
