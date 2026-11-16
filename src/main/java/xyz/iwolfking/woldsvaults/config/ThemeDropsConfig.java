package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The theme drops, generated as {@code vault_configs/loot/theme_drops/theme_drops.json}. Theme groups name sets of
 * vault theme ids. Each rule, keyed by its id, grafts its payload loot table into tiers of a tiered chest table when
 * the vault's theme is in the rule's group, its medallion rank is at least the rule's minimum and the chest block is
 * one of the rule's chests. Shares are keyed by tier index (common 0 to mythic 4): the fraction of that tier's draws
 * the payload takes.
 */
public class ThemeDropsConfig extends Config implements WoldsTable {

    public static class Rule {
        @Expose public String themeGroup;
        @Expose public int minRank;
        @Expose public List<String> chests;
        @Expose public Map<Integer, Double> shares;
        @Expose public String payload;
    }

    @Expose public Map<String, List<String>> themeGroups;
    @Expose public Map<String, Rule> rules;

    @Override
    public String getName() {
        return "loot" + File.separator + "theme_drops";
    }

    /**
     * Null when the table can be installed. Refuses unknown groups, malformed ids, negative tiers, shares outside
     * (0, 1), and any chest and tier whose shares over all rules could reach 1.
     */
    @Nullable
    @Override
    public String validate() {
        if (this.themeGroups == null || this.themeGroups.isEmpty()) {
            return "it lists no theme groups";
        }
        for (Map.Entry<String, List<String>> group : this.themeGroups.entrySet()) {
            if (group.getValue() == null || group.getValue().isEmpty()) {
                return "theme group " + group.getKey() + " lists no themes";
            }
            for (String theme : group.getValue()) {
                if (theme == null || ResourceLocation.tryParse(theme) == null) {
                    return "theme group " + group.getKey() + " has a malformed theme id " + theme;
                }
            }
        }
        if (this.rules == null || this.rules.isEmpty()) {
            return "it lists no rules";
        }
        Map<String, Map<Integer, Double>> totals = new HashMap<>();
        for (Map.Entry<String, Rule> entry : this.rules.entrySet()) {
            String problem = validateRule(entry.getKey(), entry.getValue(), totals);
            if (problem != null) {
                return problem;
            }
        }
        return null;
    }

    @Nullable
    private String validateRule(String id, Rule rule, Map<String, Map<Integer, Double>> totals) {
        if (rule == null) {
            return "rule " + id + " is empty";
        }
        if (rule.themeGroup == null || !this.themeGroups.containsKey(rule.themeGroup)) {
            return "rule " + id + " names an unknown theme group " + rule.themeGroup;
        }
        if (rule.minRank < 0) {
            return "rule " + id + " has a negative minimum rank";
        }
        if (rule.payload == null || ResourceLocation.tryParse(rule.payload) == null) {
            return "rule " + id + " has a malformed payload id " + rule.payload;
        }
        if (rule.chests == null || rule.chests.isEmpty()) {
            return "rule " + id + " lists no chests";
        }
        if (rule.shares == null || rule.shares.isEmpty()) {
            return "rule " + id + " lists no shares";
        }
        for (Map.Entry<Integer, Double> share : rule.shares.entrySet()) {
            if (share.getKey() == null || share.getKey() < 0) {
                return "rule " + id + " has a share for an invalid tier " + share.getKey();
            }
            if (share.getValue() == null || !(share.getValue() > 0.0D && share.getValue() < 1.0D)) {
                return "rule " + id + " has a share outside (0, 1) in tier " + share.getKey();
            }
        }
        for (String chest : rule.chests) {
            if (chest == null || ResourceLocation.tryParse(chest) == null) {
                return "rule " + id + " has a malformed chest id " + chest;
            }
            Map<Integer, Double> chestTotals = totals.computeIfAbsent(chest, key -> new HashMap<>());
            for (Map.Entry<Integer, Double> share : rule.shares.entrySet()) {
                double total = chestTotals.merge(share.getKey(), share.getValue(), Double::sum);
                if (total >= 1.0D) {
                    return "the shares of tier " + share.getKey() + " in chest " + chest + " reach 1 with rule " + id;
                }
            }
        }
        return null;
    }

    @Override
    protected void reset() {
    }
}
