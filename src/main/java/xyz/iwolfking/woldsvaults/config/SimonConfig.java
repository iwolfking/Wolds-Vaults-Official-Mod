package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tuning for the Simon's Set set mechanic, generated as {@code vault_configs/gear/simon/simon.json}. Simon's Set
 * tuning. Planting (simon_crystals grant = planted crystals at once): right click shoots a prism seed seedSpeed blocks
 * a tick, out to seedRange, once per plantCooldownTicks; it plants a Seed-stage crystal where it lands, replacing the
 * oldest planted one. Damage the wearer deals to an enemy within growRadius of a crystal feeds it (once a tick) damage
 * / attack damage points, at most maxGrowthPerHit, and growthPerStage points grow it a stage (Seed, Shard, Cluster,
 * Geode); a crystal unfed for shrinkTicks loses a stage, and a budded Seed vanishes. Crystals also vanish when their
 * owner is more than leashRange away. Brittle: each crystal costs brittleResistance Resistance. Budding (simon_budding
 * grant = most crystals at once, planted, budded and incoming seeds together): a Lucky Hit from the wearer's own swing
 * near any of their crystals (within growRadius) makes their planted crystal throw a new seed on to open ground it can
 * see, up to budRange from it and at least budSpacing from every other crystal and incoming seed. Resonate
 * (simon_sequence grant = burst damage per detonation value, times ability power): sneak right click with
 * resonateMinCrystals Shard-or-bigger crystals lights them one by one, biggest first, each for showTicks with
 * showGapTicks between; then the wearer has inputBaseTicks plus inputTicksPerCrystal per crystal to aim and left click
 * them (within clickRange) in that order. The sequence shows every crystal once, then repeats, until it is one step
 * longer than the last sequence got right, for as long as the streak lasts; each sequence in the streak adds
 * streakDamage to every explosion. Each correct click bursts that crystal on every enemy within burstRadius plus
 * burstRadiusPerValue per detonation value (detonationValues by stage), shattering it on its last turn; a wrong click
 * or running out of time bursts the rest for failDamage of their damage and ends the streak. Facets (simon_facets
 * grant = most facets): a Lucky Hit from a swing near a Geode gives it a facet: 1 = facetDamage more burst damage, 2 =
 * also slows for facetSlowTicks, 3 = also leaves a shard field of shardFieldRadius for shardFieldTicks, hitting every
 * half second for shardFieldDamage of the burst. simon_prismatic = the chance a budded seed is Prismatic (stands in
 * for the next crystal in a sequence); simon_fracture = each correct click rolls the wearer's Lucky Hit chance to
 * burst twice; simon_finale = a finished chain collapses into one blast at its middle for ability power times this
 * times the chain's total detonation value, on a radius of finaleRadius plus finaleRadiusPerValue per value, at most
 * finaleMaxRadius. Arcane beams (Arcane, Arcane Rail and Arcane: Prism) that enter a crystal (a column as wide as
 * reflectRadius for a Geode, less for smaller stages) bounce from its middle to the nearest crystal within
 * reflectRange that they haven't visited, then on to the nearest enemy, dealing arcaneReflectDamage of the beam's
 * damage; where the beam hits the crystal nudges the bounce, landing that far off centre on the next crystal's face
 * (or the enemy's body). Other abilities don't interact with crystals. A wearer's Prism beams reach prismReach times
 * as far. prismaticTestRound is a testing aid (0 = off): in that round (1 = the first sequence, 2 = the one after a
 * sequence got right, and so on), the wearer's first budded seed is Prismatic whatever the chance, unless one of their
 * crystals or seeds already is.
 */
public class SimonConfig extends Config implements WoldsTable {
    private static final AtomicBoolean WARNED = new AtomicBoolean();

    @Expose public int plantCooldownTicks;
    @Expose public float seedSpeed;
    @Expose public float seedRange;
    @Expose public float growRadius;
    @Expose public float growthPerStage;
    @Expose public float maxGrowthPerHit;
    @Expose public int shrinkTicks;
    @Expose public float leashRange;
    @Expose public float budRange;
    @Expose public float budSpacing;
    @Expose public int resonateMinCrystals;
    @Expose public int showTicks;
    @Expose public int showGapTicks;
    @Expose public int inputBaseTicks;
    @Expose public int inputTicksPerCrystal;
    @Expose public float clickRange;
    @Expose public float[] detonationValues;
    @Expose public float burstRadius;
    @Expose public float burstRadiusPerValue;
    @Expose public float failDamage;
    @Expose public float streakDamage;
    @Expose public float facetDamage;
    @Expose public int facetSlowTicks;
    @Expose public int shardFieldTicks;
    @Expose public float shardFieldRadius;
    @Expose public float shardFieldDamage;
    @Expose public float finaleRadius;
    @Expose public float finaleRadiusPerValue;
    @Expose public float finaleMaxRadius;
    @Expose public float brittleResistance;
    @Expose public float reflectRadius;
    @Expose public float reflectRange;
    @Expose public float arcaneReflectDamage;
    @Expose public float prismReach;
    @Expose public int prismaticTestRound;

    /** The installed table, or null (logged once) until it is installed; callers treat null as the mechanic being off. */
    @Nullable
    public static SimonConfig installed() {
        SimonConfig config = ModConfigs.SIMON;
        if (config == null && WARNED.compareAndSet(false, true)) {
            WoldLogger.error("Simon's Set was used before the gear/simon/simon table was installed; it does nothing until it is");
        }
        return config;
    }

    @Override
    public String getName() {
        return "simon";
    }

    @Override
    protected void reset() {
    }

    @Nullable
    @Override
    public String validate() {
        if (this.plantCooldownTicks <= 0 || this.detonationValues == null || this.detonationValues.length < 4) {
            return "the plant cooldown is not positive or the detonation values are missing a stage";
        }
        return null;
    }
}
