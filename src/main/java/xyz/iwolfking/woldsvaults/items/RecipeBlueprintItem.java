package xyz.iwolfking.woldsvaults.items;

import iskallia.vault.item.BasicItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import xyz.iwolfking.woldsvaults.config.RecipeUnlocksConfig;
import xyz.iwolfking.woldsvaults.api.data.discovery.DiscoveredRecipesData;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.init.ModItems;

import javax.annotation.Nullable;
import java.util.List;

public class RecipeBlueprintItem extends BasicItem {

    public RecipeBlueprintItem(ResourceLocation id, Properties properties) {
        super(id, properties);
    }

    @Nullable
    public static ResourceLocation getRecipeUnlock(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof RecipeBlueprintItem) {
            String tagStr = stack.getOrCreateTag().getString("recipe");
            return ResourceLocation.parse(tagStr);
        } else {
            return null;
        }
    }

    @Nullable
    public static String getRecipeName(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof RecipeBlueprintItem) {
            String tagStr = stack.getOrCreateTag().getString("recipe");
            String name = ModConfigs.RECIPE_UNLOCKS.RECIPE_UNLOCKS.getOrDefault(ResourceLocation.parse(tagStr), new RecipeUnlocksConfig.Entry("Unknown Recipe Unlock", "")).NAME;
            if(name != null) {
                return name;
            }

        }

        return "";
    }

    @Nullable
    public static String getRecipeDescription(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof RecipeBlueprintItem) {
            String tagStr = stack.getOrCreateTag().getString("recipe");
            String name = ModConfigs.RECIPE_UNLOCKS.RECIPE_UNLOCKS.getOrDefault(ResourceLocation.parse(tagStr), new RecipeUnlocksConfig.Entry("Unknown Recipe Unlock", "")).DESCRIPTION;
            if(name != null) {
                return name;
            }

        }

        return "";
    }

    public static ItemStack create(String tag) {
        ItemStack stack = new ItemStack(ModItems.RECIPE_BLUEPRINT);
        stack.getOrCreateTag().putString("recipe", tag);
        return stack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack heldStack = player.getMainHandItem();
        if(!hand.equals(InteractionHand.MAIN_HAND) || !(heldStack.getItem() instanceof RecipeBlueprintItem) || !player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(heldStack);
        }

        if(!level.isClientSide()) {
            ResourceLocation recipeId = getRecipeUnlock(heldStack);
            MinecraftServer server = player.getServer();
            if(server != null) {
                if(!DiscoveredRecipesData.get(server).hasDiscovered(player, recipeId)) {
                    DiscoveredRecipesData.get(server).discoverRecipeAndBroadcast(recipeId, player);
                    level.playSound(player, player.getOnPos(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
                    heldStack.shrink(1);
                    return InteractionResultHolder.success(heldStack);
                }
                else {
                    player.displayClientMessage(new TextComponent("You have already unlocked this recipe!").withStyle(ChatFormatting.GREEN), true);
                }
            }

        }

        return InteractionResultHolder.pass(heldStack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, worldIn, tooltip, flag);
        String name = getRecipeName(stack);
        String desc = getRecipeDescription(stack);
        if (name != null) {
            MutableComponent text = (new TextComponent("Shift right-click to unlock recipe for ")).withStyle(ChatFormatting.GRAY).append((new TextComponent(name)).withStyle(ChatFormatting.GOLD).append(new TextComponent(".").withStyle(ChatFormatting.GRAY)));
            tooltip.add(text);
        }
        tooltip.add(new TextComponent(""));
        if (desc != null) {
            tooltip.add(new TextComponent(desc));
        }

    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return new TextComponent("Recipe Blueprint").append(" - " + getRecipeName(stack));
    }

    @Override
    public void fillItemCategory(CreativeModeTab category, @NotNull NonNullList<ItemStack> items) {
        if (category.equals(iskallia.vault.init.ModItems.VAULT_MOD_GROUP)) {
            //items.add(create("Waystones"));
        }
    }

}
