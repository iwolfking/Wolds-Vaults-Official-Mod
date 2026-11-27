package xyz.iwolfking.woldsvaults.loot.themedrops;

import iskallia.vault.core.Version;
import iskallia.vault.core.data.key.LootTableKey;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultRegistry;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.core.vault.WorldManager;
import iskallia.vault.core.world.loot.LootPool;
import iskallia.vault.core.world.loot.LootTable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.config.ThemeDropsConfig;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionTier;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionVaultState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Decides which theme drops a vault chest gets. The installed rule table is indexed by chest block; at the chest's
 * hand-off, the rules whose theme group holds the vault's theme and whose minimum rank the vault's medallion reaches
 * are resolved into {@link ThemeDropInjections} for the chest's generator.
 */
public final class ThemeDrops {
    private static volatile Map<ResourceLocation, List<Rule>> rulesByChest;

    private ThemeDrops() {
    }

    private record Rule(String id, Set<ResourceLocation> themes, int minRank, Map<Integer, Double> shares, ResourceLocation payload) {
    }

    /** Rebuilds the rule index from the installed table; a null table leaves the feature inert. */
    public static void load(@Nullable ThemeDropsConfig table) {
        if (table == null) {
            rulesByChest = null;
            return;
        }
        Map<ResourceLocation, List<Rule>> byChest = new HashMap<>();
        for (Map.Entry<String, ThemeDropsConfig.Rule> entry : table.rules.entrySet()) {
            ThemeDropsConfig.Rule source = entry.getValue();
            Set<ResourceLocation> themes = Set.copyOf(table.themeGroups.get(source.themeGroup).stream()
                    .map(ResourceLocation::parse)
                    .toList());
            Rule rule = new Rule(entry.getKey(), themes, source.minRank, Map.copyOf(source.shares), ResourceLocation.parse(source.payload));
            for (String chest : source.chests) {
                byChest.computeIfAbsent(ResourceLocation.parse(chest), key -> new ArrayList<>()).add(rule);
            }
        }
        Map<ResourceLocation, List<Rule>> frozen = new HashMap<>();
        byChest.forEach((chest, rules) -> frozen.put(chest, List.copyOf(rules)));
        rulesByChest = Map.copyOf(frozen);
    }

    /**
     * The theme drops a chest of {@code block} rolling {@code tableKey} in {@code level} gets, or null for none. A
     * share for a tier the table lacks is skipped; a rule with no tier in the table, an unregistered payload, or
     * shares that would reach 1 in a tier logs once and is skipped.
     */
    @Nullable
    public static ThemeDropInjections resolve(Block block, @Nullable Level level, @Nullable LootTableKey tableKey, Version version) {
        Map<ResourceLocation, List<Rule>> index = rulesByChest;
        if (index == null || level == null || level.isClientSide || tableKey == null) {
            return null;
        }
        List<Rule> rules = index.get(block.getRegistryName());
        if (rules == null) {
            return null;
        }
        Optional<Vault> vault = VaultUtils.getVault(level);
        if (vault.isEmpty()) {
            return null;
        }
        ResourceLocation theme = vault.get().get(Vault.WORLD).get(WorldManager.THEME);
        if (theme == null) {
            return null;
        }
        int rank = GreedMedallionVaultState.get(vault.get()).map(GreedMedallionTier::getRankIndex).orElse(0);
        int tierCount = tierCount(tableKey, version);
        Map<Integer, List<ThemeDropInjections.Injection>> byTier = new HashMap<>();
        Map<Integer, Double> totals = new HashMap<>();
        for (Rule rule : rules) {
            if (rank >= rule.minRank() && rule.themes().contains(theme)) {
                add(rule, tableKey.getId(), tierCount, version, byTier, totals);
            }
        }
        return byTier.isEmpty() ? null : new ThemeDropInjections(byTier);
    }

    private static int tierCount(LootTableKey tableKey, Version version) {
        LootTable table = tableKey.get(version);
        if (table == null || table.getEntries().isEmpty()) {
            return 0;
        }
        return table.getEntries().get(0).getPool().getChildren().size();
    }

    private static void add(Rule rule, ResourceLocation tableId, int tierCount, Version version,
                            Map<Integer, List<ThemeDropInjections.Injection>> byTier, Map<Integer, Double> totals) {
        Map<Integer, Double> shares = new HashMap<>();
        rule.shares().forEach((tier, share) -> {
            if (tier < tierCount) {
                shares.put(tier, share);
            }
        });
        if (shares.isEmpty()) {
            WoldLogger.errorOnce("theme-drops-no-tier-" + rule.id() + "-" + tableId,
                    "Theme drop {} names no tier that loot table {} has; it is skipped there", rule.id(), tableId);
            return;
        }
        LootPool payload = payload(rule, version);
        if (payload == null) {
            return;
        }
        for (Map.Entry<Integer, Double> share : shares.entrySet()) {
            if (totals.getOrDefault(share.getKey(), 0.0D) + share.getValue() >= 1.0D) {
                WoldLogger.errorOnce("theme-drops-full-tier-" + rule.id() + "-" + tableId,
                        "Theme drop {} would bring the shares of tier {} in loot table {} to 1 or more; it is skipped there",
                        rule.id(), share.getKey(), tableId);
                return;
            }
        }
        shares.forEach((tier, share) -> {
            totals.merge(tier, share, Double::sum);
            byTier.computeIfAbsent(tier, key -> new ArrayList<>()).add(new ThemeDropInjections.Injection(share, payload));
        });
    }

    @Nullable
    private static LootPool payload(Rule rule, Version version) {
        LootTableKey key = VaultRegistry.LOOT_TABLE.getKey(rule.payload());
        LootTable table = key == null ? null : key.get(version);
        if (table == null || table.getEntries().isEmpty()) {
            WoldLogger.errorOnce("theme-drops-payload-" + rule.id(),
                    "Theme drop {} names payload loot table {}, which is not registered or has no entry; it is skipped",
                    rule.id(), rule.payload());
            return null;
        }
        return table.getEntries().get(0).getPool();
    }
}
