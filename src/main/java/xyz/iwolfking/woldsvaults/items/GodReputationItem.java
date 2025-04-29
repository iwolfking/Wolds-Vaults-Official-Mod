package xyz.iwolfking.woldsvaults.items;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.BasicItem;
import iskallia.vault.research.ResearchTree;
import iskallia.vault.research.type.Research;
import iskallia.vault.world.data.PlayerReputationData;
import iskallia.vault.world.data.PlayerResearchesData;
import net.minecraft.ChatFormatting;
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
import xyz.iwolfking.woldsvaults.init.ModItems;

import javax.annotation.Nullable;
import java.util.List;

public class GodReputationItem extends BasicItem {

    public GodReputationItem(ResourceLocation id) {
        super(id);
    }

    @Nullable
    public static VaultGod getGod(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof GodReputationItem) {
            if(stack.hasTag() && stack.getTag() != null && stack.getTag().contains("god")) {
                return VaultGod.valueOf(stack.getTag().getString("god"));
            }
        }

        return null;
    }

    public static ItemStack create(String tag) {
        ItemStack stack = new ItemStack(ModItems.RESEARCH_TOKEN);
        stack.getOrCreateTag().putString("god", tag);
        return stack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack heldStack = player.getMainHandItem();
        if(!hand.equals(InteractionHand.MAIN_HAND) || !(heldStack.getItem() instanceof GodReputationItem) || !player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(heldStack);
        }

        if(!level.isClientSide()) {
            PlayerReputationData.addReputation(player.getUUID(), getGod(heldStack), 1);
            heldStack.shrink(1);
            level.playSound(player, player.getOnPos(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
            return InteractionResultHolder.success(heldStack);
        }

        return InteractionResultHolder.pass(heldStack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, worldIn, tooltip, flag);
        VaultGod attr = getGod(stack);
        if (attr != null) {
            String name = attr.getName();
            MutableComponent text = (new TextComponent("Shift right-click to gain 1 reputation with ")).withStyle(ChatFormatting.GRAY).append((new TextComponent(name)).withStyle(attr.getChatColor()).append(new TextComponent(".").withStyle(ChatFormatting.GRAY)));
            tooltip.add(text);
        }
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        VaultGod god = getGod(stack);
        if(god != null) {
            return new TextComponent("God Offering").append(" - " + god.getName());
        }

        return new TextComponent("God Offering");
    }

    @Override
    public void fillItemCategory(CreativeModeTab category, @NotNull NonNullList<ItemStack> items) {
        if (category.equals(iskallia.vault.init.ModItems.VAULT_MOD_GROUP)) {
            items.add(create("IDONA"));
            items.add(create("TENOS"));
            items.add(create("WENDARR"));
            items.add(create("VELARA"));
        }
    }

}
