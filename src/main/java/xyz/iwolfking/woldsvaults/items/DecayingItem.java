package xyz.iwolfking.woldsvaults.items;

import com.google.common.collect.Multimap;
import iskallia.vault.core.vault.ClientVaults;
import iskallia.vault.init.ModItems;
import iskallia.vault.init.ModSounds;
import iskallia.vault.item.BasicItem;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.util.ComponentUtils;
import xyz.iwolfking.woldsvaults.util.VaultModifierUtils;

import java.rmi.ServerError;
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
                        player.getInventory().setItem(pSlotId, new ItemStack(ModItems.MEMORY_POWDER)); // TODO:

                        pLevel.playSound(null, pEntity.blockPosition(), ModSounds.ABILITY_ON_COOLDOWN, SoundSource.PLAYERS, 1.0F, 0.4F);
                    }
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        if(ClientVaults.getActive().isPresent() && VaultModifierUtils.isVaultCorrupted) {
            MutableComponent cmp0 = new TextComponent("Powers the Monolith.").withStyle(Style.EMPTY.withColor(TextColor.parseColor("#701233")).withItalic(true));
            MutableComponent cmp1 = new TextComponent("Slowly decays as you hold it.").withStyle(Style.EMPTY.withColor(TextColor.parseColor("#701233")).withItalic(true));

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
        return 1;
    }
}
