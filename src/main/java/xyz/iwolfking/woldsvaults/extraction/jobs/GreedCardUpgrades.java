package xyz.iwolfking.woldsvaults.extraction.jobs;

import iskallia.vault.config.greed.GreedTraderConfig;
import iskallia.vault.core.card.Card;
import iskallia.vault.core.card.CardEntry;
import iskallia.vault.core.card.modifier.card.GreedCardModifier;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.CardItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.config.ExtractionObjectiveConfig;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import java.util.Random;

/**
 * The Greed Infuser's card rules. A greed card entry embeds its whole modifier config rather than an id, so a card's
 * class is found by matching that config against the card modifier pools, and an upgrade rebuilds the entry from
 * the target class's modifier with the same direction and colour filters.
 */
public final class GreedCardUpgrades {
    public static final String LEGACY_CLASS = "legacy";

    private GreedCardUpgrades() {
    }

    private static ExtractionObjectiveConfig.GreedInfuserEntry config() {
        return xyz.iwolfking.woldsvaults.init.ModConfigs.EXTRACTION_OBJECTIVE.GREED_INFUSER;
    }

    /** The class this greed card belongs to ({@code weak} ... {@code elite}, or {@link #LEGACY_CLASS}), or null for anything else. */
    @Nullable
    public static String classOf(ItemStack stack) {
        CardEntry entry = singleGreedEntry(stack);
        if (entry == null) {
            return null;
        }
        CompoundTag own = configTag(entry);
        if (own == null) {
            return null;
        }
        for (String cls : allClasses()) {
            for (String id : poolIds(poolFor(cls))) {
                CompoundTag candidate = configTag(id);
                if (own.equals(candidate)) {
                    return cls;
                }
            }
        }
        return null;
    }

    /** Position of a class on the ladder, weakest first; the retired legacy class sits above everything. */
    public static int ladderIndex(String cls) {
        if (LEGACY_CLASS.equals(cls)) {
            return Integer.MAX_VALUE;
        }
        return config().classOrder.indexOf(cls);
    }

    /** The strongest class the greed trader shows at this greed rank, or null when it shows none. */
    @Nullable
    public static String classForGreedTier(int greedTier) {
        String best = null;
        int bestIndex = -1;
        for (GreedTraderConfig.TradeEntry entry : ModConfigs.GREED_TRADER.getAvailableEntries(greedTier)) {
            String poolId = entry.getPoolId();
            if (poolId == null || !poolId.startsWith(config().traderPoolPrefix)) {
                continue;
            }
            String cls = poolId.substring(config().traderPoolPrefix.length());
            int index = ladderIndex(cls);
            if (index > bestIndex && index != Integer.MAX_VALUE) {
                best = cls;
                bestIndex = index;
            }
        }
        return best;
    }

    /** Why this stack cannot go into an infuser upgrading to {@code target}, or null when it can. */
    @Nullable
    public static String refusal(ItemStack stack, String target) {
        if (stack.isEmpty()) {
            return null;
        }
        if (!(stack.getItem() instanceof CardItem)) {
            return "Only greed cards can be infused.";
        }
        Card card = CardItem.getCard(stack);
        if (card.getEntries().size() != 1 || !(card.getEntries().get(0).getModifier() instanceof GreedCardModifier)) {
            return "Only single greed cards can be infused.";
        }
        String cls = classOf(stack);
        if (cls == null) {
            return "This greed card is not recognised.";
        }
        if (LEGACY_CLASS.equals(cls)) {
            return "Legacy greed cards cannot be upgraded.";
        }
        int targetIndex = ladderIndex(target);
        if (targetIndex < 0) {
            return "This infuser has no target class.";
        }
        if (ladderIndex(cls) >= targetIndex) {
            return "This card is already " + displayName(cls) + " Greed; the infuser upgrades to " + displayName(target) + ".";
        }
        return null;
    }

    /**
     * Rewrites the card in place into {@code target} class at tier 1, keeping its direction, target colour, own colour,
     * groups and model. Returns false (and logs) when the card could not be matched or the target has no counterpart.
     */
    public static boolean upgrade(ItemStack stack, String target) {
        CardEntry old = singleGreedEntry(stack);
        if (old == null) {
            WoldLogger.error("Greed Infuser was asked to upgrade {} which is not a single greed card; left unchanged.", stack);
            return false;
        }
        CompoundTag own = configTag(old);
        if (own == null) {
            WoldLogger.error("Greed card {} has no readable modifier config; left unchanged.", stack);
            return false;
        }
        String key = directionKey(own);
        String targetId = null;
        for (String id : poolIds(poolFor(target))) {
            CompoundTag candidate = configTag(id);
            if (candidate != null && key.equals(directionKey(candidate))) {
                targetId = id;
                break;
            }
        }
        if (targetId == null) {
            WoldLogger.error("Card modifier pool '{}' has no card with direction/colour {}; the card was left unchanged.", poolFor(target), key);
            return false;
        }
        CardEntry.Config targetConfig = ModConfigs.CARD_MODIFIERS.getValues().get(targetId);
        CardEntry fresh = targetConfig.toEntry();
        fresh.setColors(new HashSet<>(old.getColors()));
        fresh.getGroups().addAll(old.getGroups());
        if (old.getModel() != null) {
            fresh.setModel(old.getModel());
        }
        fresh.getModifier().onPopulate();
        List<CardEntry> entries = new ArrayList<>();
        entries.add(fresh);
        CardItem.setCard(stack, new Card(1, entries));
        return true;
    }

    /** A fresh tier-1 greed card of the given class with a random direction and colour, for testing; empty when the class has no pool. */
    public static ItemStack createCard(String cls, Random random) {
        WeightedList<String> pool = ModConfigs.CARD_MODIFIERS.getPools().get(poolFor(cls));
        String id = pool == null ? null : pool.getRandom(random).orElse(null);
        CardEntry.Config config = id == null ? null : ModConfigs.CARD_MODIFIERS.getValues().get(id);
        if (config == null) {
            WoldLogger.error("Card modifier pool '{}' is missing or empty; no greed card was made.", poolFor(cls));
            return ItemStack.EMPTY;
        }
        CardEntry entry = config.toEntry();
        CardEntry.Color[] colors = CardEntry.Color.values();
        entry.setColors(new HashSet<>(List.of(colors[random.nextInt(colors.length)])));
        entry.getModifier().onPopulate();
        List<CardEntry> entries = new ArrayList<>();
        entries.add(entry);
        return CardItem.create(new Card(1, entries));
    }

    public static String displayName(String cls) {
        if (cls == null || cls.isEmpty()) {
            return "";
        }
        return cls.substring(0, 1).toUpperCase(Locale.ROOT) + cls.substring(1);
    }

    @Nullable
    private static CardEntry singleGreedEntry(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof CardItem)) {
            return null;
        }
        Card card = CardItem.getCard(stack);
        if (card.getEntries().size() != 1) {
            return null;
        }
        CardEntry entry = card.getEntries().get(0);
        return entry.getModifier() instanceof GreedCardModifier ? entry : null;
    }

    private static List<String> allClasses() {
        List<String> classes = new ArrayList<>(config().classOrder);
        classes.add(LEGACY_CLASS);
        return classes;
    }

    private static String poolFor(String cls) {
        return LEGACY_CLASS.equals(cls) ? config().legacyCardPool : config().cardPoolPrefix + cls;
    }

    private static List<String> poolIds(String poolId) {
        WeightedList<String> pool = ModConfigs.CARD_MODIFIERS.getPools().get(poolId);
        return pool == null ? List.of() : new ArrayList<>(pool.keySet());
    }

    @Nullable
    private static CompoundTag configTag(CardEntry entry) {
        return entry.getModifier() instanceof GreedCardModifier greed ? greed.getConfig().writeNbt().orElse(null) : null;
    }

    @Nullable
    private static CompoundTag configTag(String modifierId) {
        CardEntry.Config config = ModConfigs.CARD_MODIFIERS.getValues().get(modifierId);
        if (config == null || !(config.value instanceof GreedCardModifier greed)) {
            return null;
        }
        return greed.getConfig().writeNbt().orElse(null);
    }

    private static String directionKey(CompoundTag config) {
        return String.valueOf(tagOrEmpty(config, "targetNeighborTypes")) + "|" + tagOrEmpty(config, "targetColorFilter") + "|" + tagOrEmpty(config, "targetGroupFilter");
    }

    private static String tagOrEmpty(CompoundTag config, String key) {
        Tag tag = config.get(key);
        return tag == null ? "" : tag.toString();
    }
}
