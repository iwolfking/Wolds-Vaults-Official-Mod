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
 * Shared server logic for the reworked greed shop: the greedy-ticket reroll price, ticket
 * accounting, and the two offer types the base trader cannot express on its own.
 */
public final class GreedShopHelper {
    private static final int BASE_REROLL_TICKETS = 2;
    private static final int REROLLS_PER_PRICE_STEP = 2;
    private static final double XP_BURGER_RANK_FACTOR = 1.15D;

    private GreedShopHelper() {
    }

    /**
     * Greedy-ticket price of the next shop reroll: 2 tickets, rising by one every second reroll
     * (2, 2, 3, 3, 4, 4, ...). {@code resetCount} is cleared by the black market's daily tick, so
     * the ladder restarts once a day rather than only on a rank-up.
     */
    public static int rerollTicketCost(int resetCount) {
        return BASE_REROLL_TICKETS + Math.max(0, resetCount) / REROLLS_PER_PRICE_STEP;
    }

    /**
     * Greedy tickets the player can actually spend, counting held containers the same way the
     * greed coin balance does.
     */
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
     * Takes {@code amount} greedy tickets out of the player's inventory, or nothing at all when
     * they cannot cover it. Mirrors {@code GreedTraderData.consumeGreedCoins}.
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
     * Rolls one etching for a shop slot, split into the sheet's two tables.
     *
     * <p>Base {@code rollRandomEtching} draws uniformly from every etching whose
     * {@code minGreedTier} the player has reached, which cannot express "Regular Etching" and
     * "Powerful Etching" as two separately weighted, separately priced offers. The split reuses
     * {@code minGreedTier} as the classifier - an etching that gates on a rank at all is a
     * powerful one - so which table an etching sits in stays pack-tunable and no new config field
     * is needed. The rank gate itself still applies to both tables.</p>
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
     * Rolls the Greedy Meal with the sheet's rank scaling, {@code 2M-10M xp * 1.15^(rank - 1)}.
     * Base cannot do this because {@code rollXpBurger} is never handed the greed tier. Scavenger 1
     * pays the unscaled band; Legend sits at roughly 8.1x, so the top of the range stays well
     * inside {@code int}.
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
