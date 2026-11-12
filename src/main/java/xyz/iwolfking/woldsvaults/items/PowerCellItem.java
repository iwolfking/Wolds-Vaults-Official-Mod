package xyz.iwolfking.woldsvaults.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import xyz.iwolfking.woldsvaults.init.ModCreativeTabs;

import javax.annotation.Nullable;
import java.util.List;

public class PowerCellItem extends Item {
    public PowerCellItem(ResourceLocation id) {
        super(new Properties().tab(ModCreativeTabs.WOLDS_VAULTS).fireResistant());
        this.setRegistryName(id);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(new TextComponent("Right-click an active extractor to refuel it.").withStyle(ChatFormatting.YELLOW));
        tooltip.add(new TextComponent("Vanishes when the vault ends.").withStyle(ChatFormatting.DARK_GRAY));
    }
}
