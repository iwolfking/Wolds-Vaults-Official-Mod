package xyz.iwolfking.woldsvaults.items;

import iskallia.vault.block.entity.CompanionHomeTileEntity;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.item.BasicItem;
import iskallia.vault.item.CompanionItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import xyz.iwolfking.woldsvaults.init.ModItems;

import javax.annotation.Nullable;
import java.util.List;

public class CompanionRerollingItem extends BasicItem {
    public CompanionRerollingItem(ResourceLocation id) {
        super(id);
    }

    public static InteractionResult rerollCompanionModifier(Level level, ItemStack stackInHand, Player player, BlockPos pos) {
        if(level.isClientSide) {
            return InteractionResult.PASS;
        }

        if(player == null) {
            return InteractionResult.FAIL;
        }

        if(level.getBlockEntity(pos) instanceof CompanionHomeTileEntity home && stackInHand.getItem() instanceof CompanionRerollingItem) {
            if(!home.getCompanion().isEmpty() && player.isShiftKeyDown()) {
                ItemStack companion = home.getCompanion();
                VaultModifier<?> modifier;
                if(stackInHand.hasTag() && stackInHand.getOrCreateTag().contains("modifier")) {
                    String modifierId = stackInHand.getOrCreateTag().getString("modifier");
                    modifier = VaultModifierRegistry.get(new ResourceLocation(modifierId));
                }
                else {
                    modifier = CompanionItem.getRandomTemporalModifier();
                }

                CompanionItem.setTemporalModifier(companion, modifier.getId());
                stackInHand.shrink(1);
                player.displayClientMessage(new TextComponent("Your Companion now has the ").append(modifier.getNameComponent()).append(" modifier!"), true);
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.PASS;
    }

    public Component getModifierName(ItemStack stack) {
        if(stack.hasTag()) {
            if(stack.getOrCreateTag().contains("modifier")) {
                String modifierId = stack.getOrCreateTag().getString("modifier");
                VaultModifier<?> modifier = VaultModifierRegistry.get(new ResourceLocation(modifierId));
                if(modifier != null) {
                    return modifier.getNameComponent();
                }
            }
        }

        return null;

    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if(player == null) {
            return InteractionResult.FAIL;
        }

        return CompanionRerollingItem.rerollCompanionModifier(context.getLevel(), player.getItemInHand(context.getHand()), player, context.getClickedPos());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        if(getModifierName(stack) != null) {
            tooltip.add(new TextComponent("Sneak and right-click on a Companion Home containing a Companion to set its temporal modifier to ").append(getModifierName(stack)));
        }
        else {
            tooltip.add(new TextComponent("Sneak and right-click on a Companion Home containing a Companion to reroll its temporal modifier!"));
        }
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        TextComponent modifierName = (TextComponent) getModifierName(stack);
        if(modifierName != null) {
            return new TextComponent("Companion Temporalizer - ").append(modifierName.withStyle(ChatFormatting.WHITE));
        }

        return new TextComponent("Companion Temporalizer");
    }

    @Override
    public void fillItemCategory(CreativeModeTab category, @NotNull NonNullList<ItemStack> items) {
        if (category.equals(iskallia.vault.init.ModItems.VAULT_MOD_GROUP)) {
            items.add(new ItemStack(ModItems.COMPANION_REROLLER));
        }
    }
}
