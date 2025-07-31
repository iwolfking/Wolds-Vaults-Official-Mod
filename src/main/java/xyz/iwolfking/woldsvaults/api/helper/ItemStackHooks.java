package xyz.iwolfking.woldsvaults.api.helper;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public class ItemStackHooks {
    public static ItemStack copyWithCount(ItemStack stack, int count) {
        ItemStack copy = stack.copy();
        copy.setCount(count);
        return copy;
    }

    public static void giveItem(ServerPlayer player, ItemStack stack) {
        boolean addedItem = player.getInventory().add(stack);
        if (addedItem && stack.isEmpty()) {
            stack.setCount(1);
            ItemEntity itemEntity = player.drop(stack, false);
            if (itemEntity != null) {
                itemEntity.makeFakeItem();
            }

            player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
            player.inventoryMenu.broadcastChanges();
        }
        else {
            ItemEntity itemEntity = player.drop(stack, false);
            if(itemEntity != null) {
                itemEntity.setNoPickUpDelay();
                itemEntity.setOwner(player.getUUID());
            }
        }
    }

}
