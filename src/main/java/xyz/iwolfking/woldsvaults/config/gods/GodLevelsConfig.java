package xyz.iwolfking.woldsvaults.config.gods;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import xyz.iwolfking.woldsvaults.config.WoldsTable;

import javax.annotation.Nullable;
import java.io.File;

/**
 * The god alignment progression curve, generated as {@code vault_configs/gods/levels/god_levels.json}: cumulative
 * XP thresholds, the linear tail past the last defined level, god points per level and per reputation, the
 * reputation that charts a constellation, the levels that unlock a minor-transfer slot or the ultimate, and the
 * base experience one god altar pays.
 */
public class GodLevelsConfig extends Config implements WoldsTable {
    @Expose public long[] cumulativeXp;
    @Expose public int maxDefinedLevel;
    @Expose public long xpPerLevelPastMax;
    @Expose public int ultimateUnlockLevel;
    @Expose public int pointsAtLevelOne;
    @Expose public int pointsPerLevelToMax;
    @Expose public int pointsPerLevelPastMax;
    @Expose public int[] minorTransferSlotLevels;
    @Expose public int chartingReputation;
    @Expose public int reputationPerGodPoint;
    @Expose public double baseAltarXp;

    @Override
    public String getName() {
        return "gods" + File.separator + "god_levels";
    }

    @Override
    protected void reset() {
    }

    @Nullable
    @Override
    public String validate() {
        if (this.cumulativeXp == null || this.cumulativeXp.length == 0) {
            return "cumulativeXp is empty";
        }
        if (this.maxDefinedLevel < 1 || this.maxDefinedLevel > this.cumulativeXp.length) {
            return "maxDefinedLevel " + this.maxDefinedLevel + " is outside the " + this.cumulativeXp.length + "-level XP table";
        }
        if (this.xpPerLevelPastMax <= 0L) {
            return "xpPerLevelPastMax must be positive";
        }
        if (this.minorTransferSlotLevels == null) {
            return "minorTransferSlotLevels is missing";
        }
        if (this.chartingReputation < 0) {
            return "chartingReputation is negative";
        }
        if (this.reputationPerGodPoint <= 0) {
            return "reputationPerGodPoint must be positive";
        }
        if (this.baseAltarXp < 0.0D) {
            return "baseAltarXp is negative";
        }
        return null;
    }
}
