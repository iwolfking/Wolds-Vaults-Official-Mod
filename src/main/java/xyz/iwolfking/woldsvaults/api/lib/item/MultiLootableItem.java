package xyz.iwolfking.woldsvaults.api.lib.item;

import iskallia.vault.item.BasicItem;
import iskallia.vault.item.ItemRelicBoosterPack;
import iskallia.vault.item.LootableItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public class MultiLootableItem extends BasicItem {
    private final Supplier<ItemStack> supplier;
    private final int itemCount;

    public MultiLootableItem(ResourceLocation id, Properties properties, Supplier<ItemStack> supplier, int itemCount) {
        super(id, properties);
        this.supplier = supplier;
        this.itemCount = itemCount;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (!world.isClientSide) {
            ItemStack heldStack = player.getItemInHand(hand);
            ItemRelicBoosterPack.successEffects(world, player.position());

            for(int i = 0; i < itemCount; i++) {
                ItemStack randomLoot = (ItemStack)this.supplier.get();

                while(randomLoot.getCount() > 0) {
                    int amount = Math.min(randomLoot.getCount(), randomLoot.getMaxStackSize());
                    ItemStack copy = randomLoot.copy();
                    copy.setCount(amount);
                    randomLoot.shrink(amount);
                    player.drop(copy, false, false);
                }
            }


            heldStack.shrink(1);
        }

        return super.use(world, player, hand);
    }
}
