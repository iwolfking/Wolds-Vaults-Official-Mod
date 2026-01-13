package xyz.iwolfking.woldsvaults.items;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class ConfigurableFloatingTextBlockItem extends BlockItem {
    public ConfigurableFloatingTextBlockItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);

        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            ListTag linesTag = tag.getList("lines", Tag.TAG_COMPOUND);
            for (int i = linesTag.size() - 1; i >= 0; i--) {
                CompoundTag lineTag = linesTag.getCompound(i);
                String text = lineTag.getString("text");
                int color = lineTag.getInt("color");
                tooltip.add(new TextComponent(text).withStyle(s -> s.withColor(color)));
            }
            tooltip.add(new TextComponent("Scale: " + tag.getFloat("scale")));
        }
    }

}
