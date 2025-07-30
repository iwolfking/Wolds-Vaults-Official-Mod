package xyz.iwolfking.woldsvaults.events;

import iskallia.vault.item.KnowledgeBrewItem;
import iskallia.vault.item.MentorsBrewItem;
import iskallia.vault.item.VaultDollItem;
import iskallia.vault.util.SideOnlyFixer;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.helper.GameruleHelper;
import xyz.iwolfking.woldsvaults.blocks.DollDismantlingBlock;
import xyz.iwolfking.woldsvaults.init.ModGameRules;
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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if(event.getCrafting().getItem() instanceof VaultDollItem && !GameruleHelper.isEnabled(ModGameRules.ENABLE_VAULT_DOLLS, event.getPlayer().getLevel())) {
            cancelCraft(event, event.getCrafting());
        }
        else if(event.getCrafting().getItem() instanceof KnowledgeBrewItem && !GameruleHelper.isEnabled(iskallia.vault.init.ModGameRules.ALLOW_KNOWLEDGE_BREW, event.getPlayer().getLevel())) {
            cancelCraft(event, event.getCrafting());
        }
        else if(event.getCrafting().getItem() instanceof MentorsBrewItem && !GameruleHelper.isEnabled(iskallia.vault.init.ModGameRules.ALLOW_MENTOR_BREW, event.getPlayer().getLevel())) {
            cancelCraft(event, event.getCrafting());
        }
        else if(event.getCrafting().getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof DollDismantlingBlock && !GameruleHelper.isEnabled(ModGameRules.ENABLE_VAULT_DOLLS, event.getPlayer().getLevel())) {
            cancelCraft(event, event.getCrafting());
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemTooltip(ItemTooltipEvent event) {
        Player player = event.getPlayer();
        if(player != null) {
            if(event.getItemStack().getItem() instanceof VaultDollItem && !GameruleHelper.isEnabled(ModGameRules.ENABLE_VAULT_DOLLS, event.getPlayer().getLevel())) {
                event.getToolTip().add(new TextComponent("Vault Dolls are disabled on this server!").withStyle(ChatFormatting.RED));
            }
            else if(event.getItemStack().getItem() instanceof KnowledgeBrewItem && !GameruleHelper.isEnabled(iskallia.vault.init.ModGameRules.ALLOW_KNOWLEDGE_BREW, event.getPlayer().getLevel())) {
                event.getToolTip().add(new TextComponent("Knowledge Brew are disabled on this server!").withStyle(ChatFormatting.RED));
            }
            else if(event.getItemStack().getItem() instanceof MentorsBrewItem && !GameruleHelper.isEnabled(iskallia.vault.init.ModGameRules.ALLOW_MENTOR_BREW, event.getPlayer().getLevel())) {
                event.getToolTip().add(new TextComponent("Mentor's Brew are disabled on this server!").withStyle(ChatFormatting.RED));
            }
            else if(event.getItemStack().getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof DollDismantlingBlock && !GameruleHelper.isEnabled(ModGameRules.ENABLE_VAULT_DOLLS, event.getPlayer().getLevel())) {
                event.getToolTip().add(new TextComponent("Vault Dolls are disabled on this server!").withStyle(ChatFormatting.RED));
            }
        }

    }

    private static void sendDisabledMessage(ItemStack item) {
        MutableComponent name = (MutableComponent) item.getDisplayName();
        Style style = Style.EMPTY.withColor(TextColor.fromRgb(-203978));
        name.setStyle(style);
        Component text = name.append(new TextComponent(" cannot be crafted on this server!"));
        Minecraft.getInstance().gui.setOverlayMessage(text, false);
    }

    private static void cancelCraft(PlayerEvent.ItemCraftedEvent event, ItemStack craftedItemStack) {
            Player player = event.getPlayer();
            Container craftingMatrix = event.getInventory();
            if (event.getPlayer().level.isClientSide) {
                sendDisabledMessage(craftedItemStack);
            }

            for (int i = 0; i < craftingMatrix.getContainerSize(); i++) {
                ItemStack itemStack = craftingMatrix.getItem(i);
                if (itemStack != ItemStack.EMPTY) {
                    ItemStack itemStackToDrop = itemStack.copy();
                    itemStackToDrop.setCount(1);
                    player.drop(itemStackToDrop, false, false);
                }
            }

            int slot = SideOnlyFixer.getSlotFor(player.getInventory(), craftedItemStack);
            if (slot != -1) {
                ItemStack stackInSlot = player.getInventory().getItem(slot);
                if (stackInSlot.getCount() < craftedItemStack.getCount()) {
                    craftedItemStack.setCount(stackInSlot.getCount());
                }

                stackInSlot.shrink(craftedItemStack.getCount());
            } else {
                craftedItemStack.shrink(craftedItemStack.getCount());
            }
        }
}
