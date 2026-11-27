package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import iskallia.vault.core.vault.stat.VaultChestType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Every greed milestone's numbers, generated as {@code vault_configs/greed/milestones/greed_milestones.json}: tier
 * thresholds, the reputation each tier banks, and the greed rank tag that gates a challenge crystal. Vault of Vaults
 * and Dedicated Looter carry their real requirements alongside it, their own counter being pass/fail.
 */
public class GreedMilestonesConfig extends Config implements WoldsTable {

    public static class Entry {
        @Expose public List<Long> thresholds;
        @Expose public List<Integer> reputation;
        @Expose @Nullable public Integer requiredRank;

        @Nullable
        String validate() {
            if (this.thresholds == null || this.thresholds.isEmpty()) {
                return "has no tiers";
            }
            if (this.reputation == null || this.reputation.size() != this.thresholds.size()) {
                return "needs one reputation value per tier";
            }
            for (int index = 1; index < this.thresholds.size(); index++) {
                if (this.thresholds.get(index) <= this.thresholds.get(index - 1)) {
                    return "puts tier " + index + " at " + this.thresholds.get(index) + ", not above tier " + (index - 1);
                }
            }
            return null;
        }

        public long[] thresholds() {
            long[] values = new long[this.thresholds.size()];
            for (int index = 0; index < values.length; index++) {
                values[index] = this.thresholds.get(index);
            }
            return values;
        }

        public int[] reputation() {
            int[] values = new int[this.reputation.size()];
            for (int index = 0; index < values.length; index++) {
                values[index] = this.reputation.get(index);
            }
            return values;
        }

        public int requiredRank() {
            return this.requiredRank == null ? 0 : this.requiredRank;
        }
    }

    public static class VaultOfVaultsEntry {
        @Expose public long mobKills;
        @Expose public long chestsPerType;
        @Expose public long doorsPerType;
        @Expose public long ores;
    }

    public static class DedicatedLooterEntry {
        @Expose public long perChestType;
        @Expose public List<String> chestOrder;
    }

    @Expose public Map<String, Entry> milestones;
    @Expose public VaultOfVaultsEntry vaultOfVaults;
    @Expose public DedicatedLooterEntry dedicatedLooter;

    @Override
    public String getName() {
        return "greed_milestones";
    }

    @Override
    protected void reset() {
    }

    @Nullable
    public Entry getMilestone(String id) {
        return this.milestones == null ? null : this.milestones.get(id);
    }

    @Nullable
    @Override
    public String validate() {
        if (this.milestones == null || this.milestones.isEmpty()) {
            return "it defines no milestones";
        }
        for (Map.Entry<String, Entry> milestone : this.milestones.entrySet()) {
            String problem = milestone.getValue() == null ? "is empty" : milestone.getValue().validate();
            if (problem != null) {
                return "milestone '" + milestone.getKey() + "' " + problem;
            }
        }
        if (this.vaultOfVaults == null) {
            return "vaultOfVaults is missing";
        }
        if (this.dedicatedLooter == null || this.dedicatedLooter.perChestType <= 0L
                || this.dedicatedLooter.chestOrder == null || this.dedicatedLooter.chestOrder.isEmpty()) {
            return "dedicatedLooter needs a positive perChestType and a chest order";
        }
        for (String name : this.dedicatedLooter.chestOrder) {
            try {
                VaultChestType.valueOf(name.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                return "dedicatedLooter.chestOrder names '" + name + "', which is not a vault chest type";
            }
        }
        return null;
    }
}
