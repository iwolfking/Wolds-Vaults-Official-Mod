package xyz.iwolfking.woldsvaults.items;

import iskallia.vault.config.ResearchesGUIConfig;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.BasicItem;
import iskallia.vault.item.render.core.IManualModelLoading;
import iskallia.vault.item.render.core.SpecialItemRenderer;
import iskallia.vault.research.ResearchTree;
import iskallia.vault.research.type.Research;
import iskallia.vault.world.data.PlayerResearchesData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import xyz.iwolfking.woldsvaults.client.renderers.ResearchTokenRenderer;
import xyz.iwolfking.woldsvaults.init.ModItems;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class ResearchTokenItem extends BasicItem implements IManualModelLoading {

    public ResearchTokenItem(ResourceLocation id, Properties properties) {
        super(id, properties);
    }

    @Nullable
    public static Research getResearchTag(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof ResearchTokenItem) {
            String tagStr = stack.getOrCreateTag().getString("research");
            return ModConfigs.RESEARCHES.getByName(tagStr);
        } else {
            return null;
        }
    }

    @Nullable
    public static String getResearchTagString(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof ResearchTokenItem) {
            String tagStr = stack.getOrCreateTag().getString("research");
            Research research = ModConfigs.RESEARCHES.getByName(tagStr);
            if(research != null) {
                return research.getName();
            }

        }

        return "";
    }

    public static ItemStack create(String tag) {
        ItemStack stack = new ItemStack(ModItems.RESEARCH_TOKEN);
        stack.getOrCreateTag().putString("research", tag);
        return stack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack heldStack = player.getMainHandItem();
        if(!hand.equals(InteractionHand.MAIN_HAND) || !(heldStack.getItem() instanceof ResearchTokenItem) || !player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(heldStack);
        }

        if(!level.isClientSide()) {
            PlayerResearchesData data = PlayerResearchesData.get((ServerLevel) level);
            MinecraftServer server = player.getServer();
            if(server != null) {
                ResearchTree tree = data.getResearches(player);
                if(!tree.isResearched(getResearchTag(heldStack))) {
                    data.research((ServerPlayer) player, getResearchTag(heldStack)).sync((ServerPlayer) player);
                    heldStack.shrink(1);
                    level.playSound(player, player.getOnPos(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
                    return InteractionResultHolder.success(heldStack);
                }
            }

        }

        return InteractionResultHolder.pass(heldStack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, worldIn, tooltip, flag);
        Research attr = getResearchTag(stack);
        if (attr != null) {
            String name = attr.getName();
            MutableComponent text = (new TextComponent("Shift right-click to unlock ")).withStyle(ChatFormatting.GRAY).append((new TextComponent(name)).withStyle(ChatFormatting.GOLD).append(new TextComponent(".").withStyle(ChatFormatting.GRAY)));
            tooltip.add(text);
        }
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return new TextComponent("Research Token").append(" - " + getResearchTagString(stack));
    }

    @Override
    public void fillItemCategory(CreativeModeTab category, @NotNull NonNullList<ItemStack> items) {
        if (category.equals(iskallia.vault.init.ModItems.VAULT_MOD_GROUP)) {
            items.add(create("Waystones"));
        }
    }


    @Override
    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.IItemRenderProperties> consumer) {
        consumer.accept(new net.minecraftforge.client.IItemRenderProperties() {
            @Override
            public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
                return ResearchTokenRenderer.INSTANCE;
            }
        });
    }

    @Override
    public void loadModels(Consumer<ModelResourceLocation> consumer) {
        consumer.accept(new ModelResourceLocation("woldsvaults:research_token_base#inventory"));
        ResearchesGUIConfig config = new ResearchesGUIConfig().readConfig();
        ModConfigs.CONFIGS.remove(config);
        config.getStyles().forEach((name, style) -> {
            String[] split = style.icon.toString().split("/");
            String last = split[split.length - 1];
            consumer.accept(new ModelResourceLocation("woldsvaults:researches/" + last + "#inventory"));
        });
    }
}
