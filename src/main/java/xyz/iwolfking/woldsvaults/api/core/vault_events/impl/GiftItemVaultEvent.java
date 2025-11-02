package xyz.iwolfking.woldsvaults.api.core.vault_events.impl;

import com.google.gson.annotations.Expose;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.vault.Vault;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.BasicVaultEvent;

public class GiftItemVaultEvent extends BasicVaultEvent {
    @Expose
    private final WeightedList<ItemStack> items;
    public GiftItemVaultEvent(String eventName, String eventDescription, String primaryColor, WeightedList<ItemStack> items) {
        super(eventName, eventDescription, primaryColor);
        this.items = items;
    }

    @Override
    public void triggerEvent(BlockPos pos, ServerPlayer player, Vault vault) {
        items.getRandom().ifPresent(itemStack -> {
            player.displayClientMessage(new TextComponent("You have been gifted " + itemStack.getCount() + "x ").append(itemStack.getDisplayName()).withStyle(ChatFormatting.GOLD), true);
            player.addItem(itemStack);
        });
        super.triggerEvent(pos, player, vault);
    }
}
