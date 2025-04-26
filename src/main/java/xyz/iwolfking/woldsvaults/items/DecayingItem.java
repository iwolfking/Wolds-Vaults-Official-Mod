package xyz.iwolfking.woldsvaults.items;

import iskallia.vault.core.vault.ClientVaults;
import iskallia.vault.init.ModBlocks;
import iskallia.vault.init.ModSounds;
import iskallia.vault.item.BasicItem;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.util.ComponentUtils;
import xyz.iwolfking.woldsvaults.util.VaultUtil;

import java.util.List;

public class DecayingItem extends BasicItem {
    private final int secondsUntilExpired;
    private int tickCount;

    //TODO: Blacklist on Sophisticated
    public DecayingItem(ResourceLocation id, int secondsUntilExpired) {
        super(id);
        this.secondsUntilExpired = secondsUntilExpired;
    }

    @Override
    public int getBarWidth(ItemStack pStack) {
        int remainingSeconds = getRemainingSeconds(pStack);
        return Math.round((float) remainingSeconds / secondsUntilExpired * 13);
    }

    @Override
    public boolean isBarVisible(ItemStack pStack) {
        return pStack.getOrCreateTag().contains("RemainingSeconds");
    }

    @Override
    public int getBarColor(ItemStack pStack) {
        return TextColor.parseColor("#701233").getValue();
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if (!pLevel.isClientSide && pEntity instanceof ServerPlayer player) {
            if(!VaultUtil.isVaultCorrupted) {
                if (pStack.getTag() != null) {
                    if(pStack.getTag().contains("RemainingSeconds")) {
                        pStack.setTag(null);
                    }
                }
                return;
            }

            if (!pStack.getOrCreateTag().contains("RemainingSeconds")) {
                setRemainingSeconds(pStack, secondsUntilExpired);
            }

            if (pStack.hasTag()) {
                int remainingSeconds = getRemainingSeconds(pStack);

                if (pLevel.getGameTime() % 20 == 0) {
                    if (remainingSeconds > 0) {
                        setRemainingSeconds(pStack, remainingSeconds - 1);
                    } else {
                        pStack.setCount(0);
                        player.getInventory().setItem(pSlotId, new ItemStack(ModBlocks.SOOT));
                        pLevel.playSound(null, pEntity.blockPosition(), ModSounds.ABILITY_ON_COOLDOWN, SoundSource.PLAYERS, 1.0F, 0.4F);
                    }
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        if(ClientVaults.getActive().isPresent() && VaultUtil.isVaultCorrupted) {
            MutableComponent cmp0 = new TextComponent("Powers the Monolith.").withStyle(Style.EMPTY.withColor(TextColor.parseColor("#701233")).withItalic(true));
            MutableComponent cmp1 = new TextComponent("Slowly decays in your inventory.").withStyle(Style.EMPTY.withColor(TextColor.parseColor("#701233")).withItalic(true));

            tooltip.add(ComponentUtils.corruptComponent(cmp0));
            tooltip.add(ComponentUtils.corruptComponent(cmp1));
        } else {
            MutableComponent cmp0 = new TextComponent("Used as a fusion catalyst").withStyle(Style.EMPTY.withColor(TextColor.parseColor("#701233")).withItalic(true));

            tooltip.add(ComponentUtils.corruptComponent(cmp0));
        }
    }

    private void setRemainingSeconds(ItemStack stack, int seconds) {
        stack.getOrCreateTag().putInt("RemainingSeconds", seconds);
    }

    private int getRemainingSeconds(ItemStack stack) {
        return stack.getOrCreateTag().getInt("RemainingSeconds");
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return ClientVaults.getActive().isEmpty();
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return 55; // We do a minor bit of tomfoolery
    }
}