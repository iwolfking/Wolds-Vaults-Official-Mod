package xyz.iwolfking.woldsvaults.items;

import iskallia.vault.config.entry.vending.ProductEntry;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.BasicItem;
import iskallia.vault.item.ItemRelicBoosterPack;
import iskallia.vault.research.ResearchTree;
import iskallia.vault.research.type.Research;
import iskallia.vault.util.data.WeightedList;
import iskallia.vault.world.data.PlayerResearchesData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import xyz.iwolfking.woldsvaults.init.ModItems;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TargetedModBox extends BasicItem {

    public TargetedModBox(ResourceLocation id) {
        super(id);
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (!world.isClientSide) {
            ItemStack heldStack = player.getItemInHand(hand);
            String researchName = getResearchTag(heldStack);
            ServerLevel serverWorld = (ServerLevel)world;

            ResearchTree researches = PlayerResearchesData.get(serverWorld).getResearches(player);
            List<String> unlocked = new ArrayList(researches.getResearchesDone());

            if(!unlocked.contains(researchName)) {
                player.displayClientMessage(new TextComponent("You don't have the ").append((new TextComponent(researchName).withStyle(ChatFormatting.GOLD)).append(" research unlocked!")), true);
                return InteractionResultHolder.fail(heldStack);
            }

            ItemStack stack = ItemStack.EMPTY;
            WeightedList<ProductEntry> productEntryList = ModConfigs.MOD_BOX.POOL.get(researchName).entries;
            ProductEntry productEntry = productEntryList.getRandom(world.random);

            if (productEntry != null) {
                stack = productEntry.generateItemStack();
            }

            if (stack.isEmpty()) {
                ItemRelicBoosterPack.failureEffects(world, player.position());
            } else {
                while(stack.getCount() > 0) {
                    int amount = Math.min(stack.getCount(), stack.getMaxStackSize());
                    ItemStack copy = stack.copy();
                    copy.setCount(amount);
                    stack.shrink(amount);
                    player.drop(copy, false, false);
                }

                heldStack.shrink(1);
                ItemRelicBoosterPack.successEffects(world, player.position());
            }
        }

        return super.use(world, player, hand);
    }

    public static @NotNull String getResearchTag(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof TargetedModBox) {
            return stack.getOrCreateTag().getString("research");
        } else {
            return "None";
        }
    }

    public static @NotNull ItemStack create(String research) {
        ItemStack stack = new ItemStack(ModItems.TARGETED_MOD_BOX);
        stack.getOrCreateTag().putString("research", research);
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.add(new TextComponent("Research: ").append(new TextComponent(getResearchTag(stack)).withStyle(ChatFormatting.GOLD)));
    }

}
