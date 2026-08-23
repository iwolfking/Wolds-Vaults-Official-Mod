package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.config.gear.VaultEtchingConfig;
import iskallia.vault.config.greed.GreedTraderConfig;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.gear.EtchingItem;
import iskallia.vault.util.InventoryUtil;
import iskallia.vault.world.data.PlayerGreedTraderData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.init.ModItems;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * Server-side greed shop logic: greedy-ticket reroll pricing, ticket accounting, and the etching and Greedy Meal
 * offer rolls.
 */
public final class GreedShopHelper {
    private static final int BASE_REROLL_TICKETS = 2;
    private static final int REROLLS_PER_PRICE_STEP = 2;
    private static final double XP_BURGER_RANK_FACTOR = 1.15D;

    private GreedShopHelper() {
    }

    /**
     * Greedy-ticket price of the next shop reroll: 2, rising by one every second reroll. {@code resetCount} is
     * cleared by the black market's daily tick.
     */
    public static int rerollTicketCost(int resetCount) {
        return BASE_REROLL_TICKETS + Math.max(0, resetCount) / REROLLS_PER_PRICE_STEP;
    }

    public static int countGreedyTickets(Player player) {
        int total = 0;
        for (InventoryUtil.ItemAccess access : InventoryUtil.findAllItems(player)) {
            if (access.getItem() == ModItems.GREEDY_TICKET) {
                total += access.getStack().getCount();
            }
        }
        return total;
    }

    /**
     * Takes {@code amount} greedy tickets out of the player's inventory, or nothing at all when they cannot cover
     * it.
     */
    public static boolean consumeGreedyTickets(ServerPlayer player, int amount) {
        if (amount <= 0) {
            return true;
        }
        List<InventoryUtil.ItemAccess> accesses = InventoryUtil.findAllItems(player);
        int available = 0;
        for (InventoryUtil.ItemAccess access : accesses) {
            if (access.getItem() == ModItems.GREEDY_TICKET) {
                available += access.getStack().getCount();
            }
        }
        if (available < amount) {
            return false;
        }
        int remaining = amount;
        for (InventoryUtil.ItemAccess access : accesses) {
            if (remaining <= 0) {
                break;
            }
            if (access.getItem() != ModItems.GREEDY_TICKET) {
                continue;
            }
            ItemStack stack = access.getStack();
            int toTake = Math.min(stack.getCount(), remaining);
            if (toTake <= 0) {
                continue;
            }
            if (toTake >= stack.getCount()) {
                access.setStack(ItemStack.EMPTY);
            } else {
                stack.shrink(toTake);
                access.setStack(stack);
            }
            remaining -= toTake;
        }
        return remaining <= 0;
    }

    /**
     * Rolls one etching for a shop slot, or null when nothing is eligible. {@code powerful} picks the table: an
     * etching with a nonzero {@code minGreedTier} is a powerful one.
     */
    public static PlayerGreedTraderData.TradeOffer rollEtching(GreedTraderConfig.TradeEntry entry,
                                                               int greedTier,
                                                               GreedTraderConfig config,
                                                               Random random,
                                                               boolean powerful) {
        VaultEtchingConfig etchings = ModConfigs.ETCHINGS;
        if (etchings == null) {
            return null;
        }
        List<Map.Entry<ResourceLocation, VaultEtchingConfig.EtchingEntry>> eligible = new ArrayList<>();
        for (ResourceLocation etchingId : etchings.getEtchingIds()) {
            VaultEtchingConfig.EtchingEntry etchingEntry = etchings.getEtchingConfig(etchingId);
            if (etchingEntry == null) {
                continue;
            }
            int gate = etchingEntry.getMinGreedTier();
            if (gate > greedTier || powerful != gate > 0) {
                continue;
            }
            eligible.add(Map.entry(etchingId, etchingEntry));
        }
        if (eligible.isEmpty()) {
            return null;
        }
        Map.Entry<ResourceLocation, VaultEtchingConfig.EtchingEntry> chosen =
                eligible.get(random.nextInt(eligible.size()));
        Optional<ItemStack> etchingStack =
                EtchingItem.create(chosen.getKey(), chosen.getValue(), random, greedTier);
        return etchingStack
                .map(stack -> new PlayerGreedTraderData.TradeOffer(stack, config.rollCoinCost(entry, random)))
                .orElse(null);
    }

    /**
     * Rolls the Greedy Meal, scaling the entry's xp band by {@code 1.15^(greedTier - 1)} and clamping to
     * {@code int}.
     */
    public static PlayerGreedTraderData.TradeOffer rollXpBurger(GreedTraderConfig.TradeEntry entry,
                                                                int greedTier,
                                                                GreedTraderConfig config,
                                                                Random random) {
        int minXp = entry.getMinAmount();
        int maxXp = entry.getMaxAmount();
        int baseXp = minXp + random.nextInt(Math.max(1, maxXp - minXp + 1));
        double scaled = baseXp * Math.pow(XP_BURGER_RANK_FACTOR, Math.max(0, greedTier - 1));
        ItemStack burgerStack = new ItemStack(iskallia.vault.init.ModItems.GREEDY_MEAL);
        burgerStack.getOrCreateTag().putInt(PlayerGreedTraderData.XP_BURGER_TAG,
                (int) Math.min(Integer.MAX_VALUE, Math.floor(scaled)));
        return new PlayerGreedTraderData.TradeOffer(burgerStack, config.rollCoinCost(entry, random));
    }
}
