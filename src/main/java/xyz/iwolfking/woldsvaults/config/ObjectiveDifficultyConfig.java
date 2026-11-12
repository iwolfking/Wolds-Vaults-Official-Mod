package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.TreeMap;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * How a vault's difficulty multiplier D = (1 + sigil difficulty) x (1 + medallion objective bonus) is turned
 * into objective requirements, generated as {@code vault_configs/objectives/difficulty/objective_difficulty.json}. Every objective raises its base
 * requirement by D to the power listed under {@code exponents}; bingo tiles use the class exponents instead.
 * Threshold maps are keyed by the difficulty increase x = D - 1 as a string ("3.0") and the entry with the
 * highest key not above x applies.
 */
public class ObjectiveDifficultyConfig extends Config implements WoldsTable {
    public static final String LINEAR = "linear";

    @Expose public Map<String, Double> exponents;
    @Expose public Map<String, Double> bingoTileClasses;
    @Expose public Map<String, String> bingoTileClassByName;
    @Expose public double stackLinearFactor;
    @Expose public double stackBreakpoint;
    @Expose public double stackOffset;
    @Expose public double stackPower;
    @Expose public Map<String, Integer> crateTiersBySigilDifficulty;
    @Expose public Map<String, Integer> scavengerGoalFloorLowLevel;
    @Expose public int scavengerGoalFloorHighLevelFrom;
    @Expose public Map<String, Integer> scavengerGoalFloorHighLevel;
    @Expose public Map<String, Integer> ballisticGridBonus;
    @Expose public int ballisticGridCap;
    @Expose public int survivalReservedWaveGroups;

    private transient TreeMap<Double, Integer> crateTiers;
    private transient TreeMap<Double, Integer> goalFloorLow;
    private transient TreeMap<Double, Integer> goalFloorHigh;
    private transient TreeMap<Double, Integer> gridBonus;

    @Override
    public String getName() {
        return "objective_difficulty";
    }

    @Override
    protected void reset() {
    }

    @Nullable
    @Override
    public String validate() {
        if (this.exponents == null || this.exponents.isEmpty() || this.bingoTileClasses == null || this.bingoTileClassByName == null) {
            return "the exponent tables are missing";
        }
        if (this.crateTiersBySigilDifficulty == null || this.scavengerGoalFloorLowLevel == null
                || this.scavengerGoalFloorHighLevel == null || this.ballisticGridBonus == null) {
            return "a threshold map is missing";
        }
        return null;
    }

    public double getExponent(String objective) {
        Double exponent = this.exponents == null ? null : this.exponents.get(objective);
        if (exponent == null) {
            WoldLogger.error("objective_difficulty.json has no exponent for '{}', scaling it linearly", objective);
            return 1.0D;
        }
        return exponent;
    }

    public double getTileExponent(String rendererName) {
        String tileClass = rendererName == null || this.bingoTileClassByName == null ? null : this.bingoTileClassByName.get(rendererName);
        if (tileClass == null) {
            tileClass = LINEAR;
        }
        Double exponent = this.bingoTileClasses == null ? null : this.bingoTileClasses.get(tileClass);
        if (exponent == null) {
            WoldLogger.error("objective_difficulty.json bingo tile class '{}' (tile '{}') has no exponent, scaling it linearly", tileClass, rendererName);
            return 1.0D;
        }
        return exponent;
    }

    public double getStackLinearFactor() {
        return this.stackLinearFactor;
    }

    public double getStackBreakpoint() {
        return this.stackBreakpoint;
    }

    public double getStackOffset() {
        return this.stackOffset;
    }

    public double getStackPower() {
        return this.stackPower;
    }

    public int getCrateTiers(double sigilDifficulty) {
        if (this.crateTiers == null) {
            this.crateTiers = toTree(this.crateTiersBySigilDifficulty, "crateTiersBySigilDifficulty");
        }
        return floor(this.crateTiers, sigilDifficulty, 0);
    }

    public int getScavengerGoalFloor(double increase, int level) {
        if (level >= this.scavengerGoalFloorHighLevelFrom) {
            if (this.goalFloorHigh == null) {
                this.goalFloorHigh = toTree(this.scavengerGoalFloorHighLevel, "scavengerGoalFloorHighLevel");
            }
            return floor(this.goalFloorHigh, increase, 0);
        }
        if (this.goalFloorLow == null) {
            this.goalFloorLow = toTree(this.scavengerGoalFloorLowLevel, "scavengerGoalFloorLowLevel");
        }
        return floor(this.goalFloorLow, increase, 0);
    }

    public int getBallisticGridBonus(double increase) {
        if (this.gridBonus == null) {
            this.gridBonus = toTree(this.ballisticGridBonus, "ballisticGridBonus");
        }
        return floor(this.gridBonus, increase, 0);
    }

    public int getBallisticGridCap() {
        return this.ballisticGridCap;
    }

    public int getSurvivalReservedWaveGroups() {
        return this.survivalReservedWaveGroups;
    }

    private static int floor(TreeMap<Double, Integer> tree, double key, int fallback) {
        Map.Entry<Double, Integer> entry = tree.floorEntry(key);
        return entry == null ? fallback : entry.getValue();
    }

    private static TreeMap<Double, Integer> toTree(Map<String, Integer> raw, String field) {
        TreeMap<Double, Integer> tree = new TreeMap<>();
        if (raw == null) {
            WoldLogger.error("objective_difficulty.json is missing '{}', treating it as empty", field);
            return tree;
        }
        raw.forEach((key, value) -> {
            try {
                tree.put(Double.parseDouble(key), value);
            } catch (NumberFormatException e) {
                WoldLogger.error("objective_difficulty.json '{}' has non-numeric threshold '{}', ignoring it", field, key);
            }
        });
        return tree;
    }
}
