package xyz.iwolfking.woldsvaults.events;

import iskallia.vault.world.data.ServerVaults;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.items.FilterNecklaceItem;

@Mod.EventBusSubscriber(
        modid = WoldsVaults.MOD_ID
)
public class PlayerEvents {

    @SubscribeEvent(
            priority = EventPriority.HIGH
    )
    public static void onFilterNecklaceUse(EntityItemPickupEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            ItemEntity itemEntity = event.getItem();
            ItemStack stack = itemEntity.getItem();
            if (!stack.isEmpty()) {
                if(stack.hasTag() && stack.getTag() != null && stack.getTag().isEmpty()) {
                    stack.setTag(null);
                }

                ItemStack filterNecklaceStack = FilterNecklaceItem.getNecklace(player);
                if(filterNecklaceStack.getItem() instanceof FilterNecklaceItem filterNecklaceItem) {
                    ServerLevel world = player.getLevel();
                    if (ServerVaults.get(world).isPresent()) {
                        if(filterNecklaceItem.stackMatchesFilter(stack, filterNecklaceStack)) {
                            event.setCanceled(true);
                            itemEntity.remove(Entity.RemovalReason.DISCARDED);
                            world.playSound(
                                    null,
                                    player.blockPosition(),
                                    SoundEvents.ITEM_PICKUP,
                                    SoundSource.PLAYERS,
                                    0.2F,
                                    (world.random.nextFloat() - world.random.nextFloat()) * 1.4F + 2.0F
                            );
                        }
                    }
                }

            }
        }
    }
}
