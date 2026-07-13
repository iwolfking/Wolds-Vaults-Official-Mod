package xyz.iwolfking.woldsvaults.items;

import iskallia.vault.item.BasicItem;
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
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import xyz.iwolfking.woldsvaults.api.util.GodMasteryHelper;
import xyz.iwolfking.woldsvaults.init.ModCreativeTabs;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The awakened core: shift-right-click to consume, permanently raising the player's MAXIMUM
 * god reputation with all four Vault Gods by 1 (base 50). Raises only the cap — it grants no
 * reputation itself. Counts are stored per player in world SavedData (see {@link GodMasteryHelper}).
 */
public class GodsMasteryItem extends BasicItem {
    public GodsMasteryItem(ResourceLocation id) {
        super(id, new Item.Properties().tab(ModCreativeTabs.WOLDS_VAULTS).rarity(Rarity.EPIC));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND || !player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            int newCap = GodMasteryHelper.increaseMastery(serverPlayer);
            stack.shrink(1);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 0.6F);
            player.displayClientMessage(new TextComponent(
                    "The Vault Gods' limits recede — your maximum reputation with every god is now "
                            + newCap + ".").withStyle(ChatFormatting.GOLD), false);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (Screen.hasShiftDown()) {
            tooltip.add(new TextComponent("Shift-right-click to consume: permanently raises your")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(new TextComponent("MAXIMUM reputation with all four Vault Gods by 1.")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(new TextComponent("Grants no reputation itself — only room for more.")
                    .withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltip.add(new TextComponent("Hold SHIFT for more info").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
