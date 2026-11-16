package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Ancient uniques, generated as {@code vault_configs/gear/ancients/ancients.json}: the ancient name of every
 * ancient-capable unique, the identify chance breakpoints by rank, and the numbers of the ancient-only effects.
 */
public class AncientsConfig extends Config implements WoldsTable {
    @Expose public Map<String, String> names;
    @Expose public List<Breakpoint> breakpoints;
    @Expose public Effects effects;

    /** The identify chance of every rank at or above {@code minRank}, up to the next breakpoint. */
    public static class Breakpoint {
        @Expose public int minRank;
        @Expose public float chance;

        public Breakpoint() {
        }

        public Breakpoint(int minRank, float chance) {
            this.minRank = minRank;
            this.chance = chance;
        }
    }

    /** The numbers of the effects only ancient uniques roll. */
    public static class Effects {
        @Expose public float luckyHitCleavePercent;
        @Expose public String quakingHitAbility;
        @Expose public float crashwaveReferenceSpeed;
        @Expose public float crashwaveDamageMultiplier;
        @Expose public int crashwaveRetriggerTicks;
        @Expose public double crashwaveMinLookahead;
        @Expose public double crashwaveLookaheadFactor;
        @Expose public int radiationScanIntervalTicks;
        @Expose public int radiationEscalationTicks;
        @Expose public int radiationMaxAmplifier;
        @Expose public int radiationSlowDurationTicks;
    }

    @Override
    public String getName() {
        return "ancients";
    }

    @Override
    protected void reset() {
    }

    @Nullable
    @Override
    public String validate() {
        if (this.names == null || this.names.isEmpty()) {
            return "no ancient names";
        }
        for (Map.Entry<String, String> entry : this.names.entrySet()) {
            if (ResourceLocation.tryParse(entry.getKey()) == null) {
                return "ancient name key " + entry.getKey() + " is not a unique id";
            }
            if (entry.getValue() == null || entry.getValue().isBlank()) {
                return "unique " + entry.getKey() + " has a blank ancient name";
            }
        }
        if (this.breakpoints == null || this.breakpoints.isEmpty()) {
            return "no identify chance breakpoints";
        }
        int previousRank = 0;
        for (Breakpoint breakpoint : this.breakpoints) {
            if (breakpoint == null || breakpoint.minRank <= previousRank) {
                return "breakpoint ranks are not positive and ascending";
            }
            if (!(breakpoint.chance > 0.0F && breakpoint.chance <= 1.0F)) {
                return "breakpoint chance " + breakpoint.chance + " at rank " + breakpoint.minRank + " is outside (0, 1]";
            }
            previousRank = breakpoint.minRank;
        }
        return this.validateEffects();
    }

    @Nullable
    private String validateEffects() {
        Effects e = this.effects;
        if (e == null) {
            return "no effects section";
        }
        if (!(e.luckyHitCleavePercent > 0.0F)) {
            return "lucky hit cleave percent is not positive";
        }
        if (e.quakingHitAbility == null || e.quakingHitAbility.isBlank()) {
            return "quaking hit ability is blank";
        }
        if (!(e.crashwaveReferenceSpeed > 0.0F) || e.crashwaveDamageMultiplier < 0.0F || e.crashwaveRetriggerTicks < 0) {
            return "a crashwave speed, damage or retrigger value is out of range";
        }
        if (!(e.crashwaveMinLookahead > 0.0) || !(e.crashwaveLookaheadFactor > 0.0)) {
            return "a crashwave lookahead value is not positive";
        }
        if (e.radiationScanIntervalTicks < 1 || e.radiationEscalationTicks < 1 || e.radiationMaxAmplifier < 0
                || e.radiationSlowDurationTicks < 1) {
            return "a radiation wave value is out of range";
        }
        return null;
    }
}
