package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.MonolithConfig;

public class HauntedBraziersConfig extends MonolithConfig {

    @Expose
    private double ghostDrainPerSecond;
    @Expose
    private double ghostOutsideDrainMultiplier;
    @Expose
    private double heatTierBiasPerHeat;

    @Expose
    private int ghostDrainGraceTicks;

    public HauntedBraziersConfig() {
    }

    @Override
    public String getName() {
        return "haunted_braziers";
    }

    @Override
    protected void reset() {
        super.reset();
        this.ghostDrainPerSecond = 0.35D;
        this.ghostOutsideDrainMultiplier = 0.5D;
        this.heatTierBiasPerHeat = 0.05D;
        this.ghostDrainGraceTicks = 15;
    }

    public double getGhostDrainPerSecond() {
        return Math.max(0.0D, this.ghostDrainPerSecond);
    }

    public double getGhostOutsideDrainMultiplier() {
        return Math.max(0.0D, this.ghostOutsideDrainMultiplier);
    }

    public double getHeatTierBiasPerHeat() {
        return Math.max(0.0D, this.heatTierBiasPerHeat);
    }

    public int getGhostDrainGraceTicks() {
        return Math.max(0, this.ghostDrainGraceTicks);
    }
}