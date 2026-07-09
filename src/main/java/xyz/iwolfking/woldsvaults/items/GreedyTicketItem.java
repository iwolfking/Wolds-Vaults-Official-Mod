package xyz.iwolfking.woldsvaults.items;

import iskallia.vault.world.data.PlayerGreedTreeData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** A rare hyper crate drop: right-click to consume one ticket for +10 greed reputation. */
public class GreedyTicketItem extends Item {
    public static final int REPUTATION_PER_TICKET = 10;

    public GreedyTicketItem(ResourceLocation id, Properties properties) {
        super(properties);
        this.setRegistryName(id);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            PlayerGreedTreeData.get(serverPlayer.getLevel()).addGreedReputation(serverPlayer, REPUTATION_PER_TICKET);
            stack.shrink(1);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6F, 1.4F);
            player.displayClientMessage(new TextComponent("+" + REPUTATION_PER_TICKET + " Greed Reputation")
                    .withStyle(ChatFormatting.GOLD), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (Screen.hasShiftDown()) {
            tooltip.add(new TextComponent("Consume to gain +" + REPUTATION_PER_TICKET + " Greed Reputation")
                    .withStyle(ChatFormatting.GOLD));
        } else {
            tooltip.add(new TextComponent("Hold SHIFT for more info").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
