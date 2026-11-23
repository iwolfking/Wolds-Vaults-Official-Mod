package xyz.iwolfking.woldsvaults.integration.jei;

import iskallia.vault.core.Version;
import iskallia.vault.core.data.key.LootPoolKey;
import iskallia.vault.core.data.key.LootTableKey;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.util.WeightedTree;
import iskallia.vault.core.vault.VaultRegistry;
import iskallia.vault.core.world.loot.LootPool;
import iskallia.vault.core.world.loot.LootTable;
import iskallia.vault.core.world.loot.entry.ItemLootEntry;
import iskallia.vault.core.world.loot.entry.ReferenceLootEntry;
import iskallia.vault.core.world.roll.IntRoll;
import iskallia.vault.init.ModItems;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.vhapi.integration.jevh.LabeledLootInfo;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.medallions.GreedCrateLoot;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionTier;
import xyz.iwolfking.woldsvaults.milestones.RankRangeText;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The Medallion Vault Crate pages: one page per greed crate loot tier, titled with the medallion ranks that roll it,
 * listing the greed coins and every item of that tier's loot table with its chance per roll. Read from the loaded
 * medallion table and loot tables.
 */
public final class MedallionCrateLootPages {
    private MedallionCrateLootPages() {
    }

    public static List<LabeledLootInfo> create() {
        List<LabeledLootInfo> pages = new ArrayList<>();
        if (GreedMedallionTier.crateSettings() == null) {
            WoldLogger.error("The greed medallion table is not installed; JEI shows no Medallion Vault Crate pages.");
            return pages;
        }
        int topRank = 0;
        TreeMap<Integer, Span> spans = new TreeMap<>();
        for (GreedMedallionTier medallion : GreedMedallionTier.values()) {
            topRank = Math.max(topRank, medallion.getRankIndex());
            int tier = medallion.getGreedCrateLootTier();
            if (tier > 0) {
                spans.computeIfAbsent(tier, key -> new Span()).add(medallion);
            }
        }
        for (Map.Entry<Integer, Span> entry : spans.entrySet()) {
            Span span = entry.getValue();
            if (span.ranks != span.toRank - span.fromRank + 1) {
                WoldLogger.error("Greed crate loot tier {} is rolled by ranks that are not contiguous ({} to {}); its JEI page shows the whole span.",
                        entry.getKey(), span.fromRank, span.toRank);
            }
            ResourceLocation tableId = GreedCrateLoot.tableId(entry.getKey());
            LootTableKey key = VaultRegistry.LOOT_TABLE.getKey(tableId);
            LootTable table = key == null ? null : key.get(Version.latest());
            if (table == null) {
                WoldLogger.error("Greed crate loot table {} is not loaded; JEI skips its Medallion Vault Crate page.", tableId);
                continue;
            }
            List<ItemStack> stacks = new ArrayList<>();
            stacks.add(WoldsVaultsJeiPlugin.formatItemStack(new ItemStack(ModItems.GREED_COIN), span.minCoins, span.maxCoins, 1.0D, 1.0D, null,
                    I18n.get("jei.woldsvaults.medallion_crate.coins")));
            for (LootTable.Entry pool : table.getEntries()) {
                addPool(stacks, pool.getPool(), 1.0D, rollText(pool.getRoll()));
            }
            String ranks = RankRangeText.of(span.fromRank, span.toRank == topRank ? RankRangeText.OPEN : span.toRank,
                    rank -> I18n.get("screen.woldsvaults.greed.rank." + rank));
            pages.addAll(LabeledLootInfo.pages(stacks, new TranslatableComponent("jei.woldsvaults.medallion_crate.page", ranks), null));
        }
        return pages;
    }

    private static String rollText(IntRoll roll) {
        String count = roll.getMin() == roll.getMax() ? String.valueOf(roll.getMin()) : roll.getMin() + "-" + roll.getMax();
        return I18n.get("jei.woldsvaults.medallion_crate.rolls", count);
    }

    private static void addPool(List<ItemStack> stacks, WeightedTree<?> tree, double chance, String rollText) {
        WeightedList<Object> children = tree.getChildren();
        double total = children.getTotalWeight();
        if (total <= 0.0D) {
            return;
        }
        for (Map.Entry<Object, Double> child : children.entrySet()) {
            double childChance = chance * child.getValue() / total;
            Object node = child.getKey();
            if (node instanceof WeightedTree<?> subtree) {
                addPool(stacks, subtree, childChance, rollText);
            } else if (node instanceof ItemLootEntry item) {
                ItemStack stack = new ItemStack(item.getItem());
                if (item.getNbt() != null && !item.getNbt().isEmpty()) {
                    stack.setTag(item.getNbt().copy());
                }
                stacks.add(WoldsVaultsJeiPlugin.formatItemStack(stack, item.getCount().getMin(), item.getCount().getMax(),
                        childChance, 1.0D, null, rollText));
            } else if (node instanceof ReferenceLootEntry reference) {
                LootPoolKey poolKey = reference.getReference();
                LootPool pool = poolKey == null ? null : poolKey.get(Version.latest());
                if (pool == null) {
                    WoldLogger.errorOnce("medallion-crate-pool-" + reference.getReferenceId(),
                            "Loot pool {} referenced by a greed crate loot table is not loaded; JEI leaves it out.", reference.getReferenceId());
                    continue;
                }
                addPool(stacks, pool, childChance, rollText);
            } else {
                WoldLogger.errorOnce("medallion-crate-entry-" + node.getClass().getName(),
                        "A greed crate loot table holds a {} entry, which JEI cannot list; it is left out.", node.getClass().getSimpleName());
            }
        }
    }

    private static final class Span {
        private int fromRank = Integer.MAX_VALUE;
        private int toRank;
        private int ranks;
        private int minCoins = Integer.MAX_VALUE;
        private int maxCoins;

        private void add(GreedMedallionTier medallion) {
            this.fromRank = Math.min(this.fromRank, medallion.getRankIndex());
            this.toRank = Math.max(this.toRank, medallion.getRankIndex());
            this.ranks++;
            this.minCoins = Math.min(this.minCoins, medallion.getBaseGreedCoins());
            this.maxCoins = Math.max(this.maxCoins, medallion.getBaseGreedCoins());
        }
    }
}
