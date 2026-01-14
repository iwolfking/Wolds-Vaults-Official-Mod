package xyz.iwolfking.woldsvaults.items;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import xyz.iwolfking.woldsvaults.blocks.tiles.TimeTrialTrophyBlockEntity;
import xyz.iwolfking.woldsvaults.init.ModItems;

import javax.annotation.Nullable;
import java.util.List;

public class TimeTrialTrophyItem extends BlockItem {

    public TimeTrialTrophyItem(Block block, Properties props) {
        super(block, props);
    }

    public static ItemStack create(TimeTrialTrophyBlockEntity.TrophyData data) {
            ItemStack trophyStack = new ItemStack(ModItems.TIME_TRIAL_TROPHY);
            CompoundTag trophyData = new CompoundTag();
            trophyData.put("TrophyData", data.toNbt());
            trophyStack.setTag(trophyData);
            return trophyStack;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {

        if (stack.hasTag() && stack.getTag().contains("TrophyData")) {
            CompoundTag tag = stack.getTag().getCompound("TrophyData");

            tooltip.add(new TextComponent("Winner: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(new TextComponent(tag.getString("Owner"))
                            .withStyle(ChatFormatting.GOLD)));

            tooltip.add(new TextComponent("Objective: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(new TextComponent(tag.getString("Objective"))
                            .withStyle(ChatFormatting.WHITE)));

            tooltip.add(new TextComponent("Ended: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(new TextComponent(tag.getString("EndDate"))
                            .withStyle(ChatFormatting.WHITE)));

            long ticks = tag.getLong("BestTimeTicks");
            tooltip.add(new TextComponent("Best Time: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(new TextComponent(String.format("%.2f s", ticks / 20.0))
                            .withStyle(ChatFormatting.AQUA)));
        }
    }
}
