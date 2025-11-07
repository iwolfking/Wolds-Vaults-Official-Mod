package xyz.iwolfking.woldsvaults.api.core.vault_events.impl.tasks;

import com.google.gson.annotations.Expose;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.vault.Vault;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.VaultEventTask;

public class ItemRewardTask implements VaultEventTask {

    private final WeightedList<ItemStack> items;

    public ItemRewardTask(WeightedList<ItemStack> items) {
        this.items = items;
    }

    @Override
    public void performTask(BlockPos pos, ServerPlayer player, Vault vault) {
        items.getRandom().ifPresent(player::addItem);
    }

    public static class Builder {
        public final WeightedList<ItemStack> items = new WeightedList<>();

        public Builder item(Item item, int count, double weight) {
            this.items.add(new ItemStack(item, count), weight);
            return this;
        }

        public ItemRewardTask build() {
            return new ItemRewardTask(items);
        }
    }
}
