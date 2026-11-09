package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import iskallia.vault.core.util.WeightedList;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * The extraction objective, generated as {@code vault_configs/objectives/extraction/extraction_objective.json}: the
 * default rig, wear and deployment rules, every extraction job and its payout, the wave tiers sites run, cell drop
 * chances, site placement, the Greed Infuser, fluid payouts and the Ancient Crystal payout.
 */
public class ExtractionObjectiveConfig extends Config implements WoldsTable {
    @Expose public DefaultExtractorEntry DEFAULT_EXTRACTOR;
    @Expose public DurabilityEntry DURABILITY;
    @Expose public DeploymentEntry DEPLOYMENT;
    @Expose public LinkedHashMap<String, JobEntry> JOBS;
    @Expose public WavesEntry WAVES;
    @Expose public ThumperEntry THUMPER;
    @Expose public CacheEntry CACHE;
    @Expose public CellsEntry CELLS;
    @Expose public SitesEntry SITES;
    @Expose public GreedInfuserEntry GREED_INFUSER;
    @Expose public FluidPayoutEntry FLUID_PAYOUT;
    @Expose public AncientCrystalEntry ANCIENT_CRYSTAL;

    @Override
    public String getName() {
        return "extraction_objective";
    }

    @Override
    protected void reset() {
    }

    /** A config with no jobs, no wave tiers and zeroed rules: no extraction job can be started on it. */
    public static ExtractionObjectiveConfig inert() {
        ExtractionObjectiveConfig config = new ExtractionObjectiveConfig();
        config.DEFAULT_EXTRACTOR = new DefaultExtractorEntry();
        config.DEFAULT_EXTRACTOR.job = "";
        config.DURABILITY = new DurabilityEntry();
        config.DEPLOYMENT = new DeploymentEntry();
        config.JOBS = new LinkedHashMap<>();
        config.WAVES = new WavesEntry();
        config.WAVES.statFloor = new StatFloorEntry();
        config.WAVES.boss = new BossEntry();
        config.WAVES.elite = new EliteEntry();
        config.WAVES.carrier = new CarrierEntry();
        config.WAVES.carrier.baseEntity = "";
        config.WAVES.lanes = new LanesEntry();
        config.WAVES.ram = new RamEntry();
        config.WAVES.ram.entity = "";
        config.WAVES.ram.hpReference = "";
        config.WAVES.focusOrder = new ArrayList<>();
        config.THUMPER = new ThumperEntry();
        config.CACHE = new CacheEntry();
        config.CELLS = new CellsEntry();
        config.SITES = new SitesEntry();
        config.GREED_INFUSER = new GreedInfuserEntry();
        config.GREED_INFUSER.powerPerExtraSlot = 1;
        config.GREED_INFUSER.classOrder = new ArrayList<>();
        config.GREED_INFUSER.cardPoolPrefix = "";
        config.GREED_INFUSER.legacyCardPool = "";
        config.GREED_INFUSER.traderPoolPrefix = "";
        config.FLUID_PAYOUT = new FluidPayoutEntry();
        config.ANCIENT_CRYSTAL = new AncientCrystalEntry();
        config.ANCIENT_CRYSTAL.objectivePool = "";
        config.ANCIENT_CRYSTAL.excludedObjectiveTypes = new ArrayList<>();
        return config;
    }

    @Nullable
    @Override
    public String validate() {
        if (this.DEFAULT_EXTRACTOR == null || this.DURABILITY == null || this.DEPLOYMENT == null || this.CELLS == null
                || this.SITES == null || this.FLUID_PAYOUT == null) {
            return "a DEFAULT_EXTRACTOR, DURABILITY, DEPLOYMENT, CELLS, SITES or FLUID_PAYOUT block is missing";
        }
        if (this.JOBS == null || this.JOBS.isEmpty()) {
            return "it defines no JOBS";
        }
        for (Map.Entry<String, JobEntry> entry : this.JOBS.entrySet()) {
            String problem = checkJob(entry.getKey(), entry.getValue());
            if (problem != null) {
                return problem;
            }
        }
        if (!this.JOBS.containsKey(this.DEFAULT_EXTRACTOR.job)) {
            return "DEFAULT_EXTRACTOR names job '" + this.DEFAULT_EXTRACTOR.job + "', which JOBS does not define";
        }
        String problem = checkWaves(this.WAVES);
        if (problem != null) {
            return problem;
        }
        if (this.THUMPER == null || this.THUMPER.healthFraction <= 0.0F || this.THUMPER.progressLossPerThumper < 0.0F
                || this.THUMPER.progressLossPerThumper * 2.0F >= 1.0F) {
            return "THUMPER is missing, has no positive healthFraction, or its progress loss would stop a rig with two thumpers lost";
        }
        CacheEntry cache = this.CACHE;
        if (cache == null || cache.intervalTicks <= 0 || cache.batchMin < 0 || cache.batchMax < cache.batchMin || cache.cap < 0
                || cache.cellChance < 0.0D || cache.cellChance > 1.0D) {
            return "CACHE is missing, has no positive interval, an inverted batch size, a negative cap or a cell chance outside 0..1";
        }
        if (this.SITES.count <= 0 || this.SITES.ringMinRooms > this.SITES.ringMaxRooms) {
            return "SITES has no sites or an inverted ring";
        }
        GreedInfuserEntry infuser = this.GREED_INFUSER;
        if (infuser == null || infuser.powerPerExtraSlot <= 0 || infuser.classOrder == null || infuser.classOrder.isEmpty()
                || infuser.cardPoolPrefix == null || infuser.legacyCardPool == null || infuser.traderPoolPrefix == null) {
            return "GREED_INFUSER is missing, has no positive powerPerExtraSlot, no classOrder or a missing pool name";
        }
        AncientCrystalEntry crystal = this.ANCIENT_CRYSTAL;
        if (crystal == null || crystal.strongPositive == null || crystal.strongNegative == null || crystal.randomPositive == null
                || crystal.randomNegative == null || crystal.sigils == null || crystal.sigils.isEmpty()
                || crystal.excludedObjectiveTypes == null || crystal.objectivePool == null || crystal.objectivePool.isEmpty()
                || crystal.capacityMin > crystal.capacityMax) {
            return "ANCIENT_CRYSTAL is missing a modifier source list, its sigils or objective pool, or has an inverted capacity";
        }
        return null;
    }

    @Nullable
    private static String checkJob(String id, @Nullable JobEntry job) {
        if (job == null || job.drillClass == null || job.drillClass.isEmpty() || job.baseDurationTicks <= 0) {
            return "job " + id + " is missing, names no drill class or has no positive duration";
        }
        PayoutEntry payout = job.payout;
        if (payout == null || payout.type == null || payout.type.isEmpty() || payout.item == null || payout.itemNbtId == null
                || payout.powerScaling == null || payout.quantScaling == null || payout.itemRarityDampedKey == null) {
            return "job " + id + " has no payout or a payout with a missing field";
        }
        if (payout.countMin < 0.0F || payout.countMax < payout.countMin) {
            return "job " + id + " has a negative or inverted payout count";
        }
        if (("item".equals(payout.type) || "fluid".equals(payout.type)) && payout.item.isEmpty()) {
            return "job " + id + " pays out " + payout.type + " but names no item";
        }
        if (payout.rarityWeights == null || payout.rarityPerExtraPower == null || payout.rarityPerExtraPowerSquared == null
                || payout.tierWindows == null) {
            return "job " + id + " has a missing rarity or tier window table";
        }
        return null;
    }

    @Nullable
    private static String checkWaves(@Nullable WavesEntry waves) {
        if (waves == null || waves.statFloor == null || waves.statFloor.anchors == null || waves.boss == null
                || waves.boss.pool == null || waves.elite == null || waves.carrier == null) {
            return "the WAVES block or one of its statFloor, boss, elite or carrier entries is missing";
        }
        if (waves.carrier.baseEntity == null || waves.carrier.baseEntity.isEmpty()) {
            return "WAVES carrier names no base entity";
        }
        if (waves.lanes == null || waves.lanes.repathTicks <= 0 || waves.lanes.lookahead <= 0 || waves.lanes.stuckTicks <= 0) {
            return "WAVES lanes is missing or has a non-positive repath interval, lookahead or stuck time";
        }
        if (waves.roomCountMultiplier <= 0.0F || waves.packetSize <= 0 || waves.liveMobCapPerRoom <= 0 || waves.liveMobCapPerVault <= 0
                || waves.focusOrder == null) {
            return "WAVES has a non-positive count multiplier, packet size or live mob cap, or no focusOrder list";
        }
        RamEntry ram = waves.ram;
        if (ram == null || ram.entity == null || ram.hpReference == null || ram.maxPerRoom < 0 || ram.maxPerTarget < 0
                || (ram.maxPerRoom > 0 && (ram.entity.isEmpty() || ram.killSeconds <= 0.0F || ram.hpEliteMultiple <= 0.0F || ram.speedMultiplier <= 0.0F))) {
            return "WAVES ram is missing, has negative caps, or is enabled without an entity, kill time, HP multiple or speed";
        }
        if (waves.tiers == null || waves.tiers.isEmpty()) {
            return "WAVES defines no tiers";
        }
        for (TierEntry tier : waves.tiers) {
            if (tier == null || tier.name == null) {
                return "WAVES has an unnamed tier";
            }
            for (TimedWaveEntry wave : new TimedWaveEntry[]{tier.trickle, tier.horde, tier.combat, tier.assault, tier.siege}) {
                if (wave == null || wave.intervalTicks <= 0 || wave.horde == null || wave.assassins == null || wave.tanks == null
                        || wave.elites == null || wave.bosses == null) {
                    return "WAVES tier " + tier.name + " has a missing wave, a wave with no positive interval, or a missing count";
                }
            }
            if (tier.hordePool == null || tier.assassinPool == null || tier.tankPool == null || tier.elitePool == null) {
                return "WAVES tier " + tier.name + " is missing a mob pool";
            }
            if (waves.ram.maxPerRoom > 0 && tier.ramIntervalTicks <= 0) {
                return "WAVES tier " + tier.name + " has no positive ramIntervalTicks while rams are enabled";
            }
        }
        return null;
    }

    public JobEntry getJob(String id) {
        return JOBS.get(id);
    }

    public static class DefaultExtractorEntry {
        @Expose public float health;
        @Expose public float shield;
        @Expose public float armor;
        @Expose public float shieldToughness;
        @Expose public float shieldRegenPerTick;
        @Expose public int power;
        @Expose public int speed;
        @Expose public float energyCapacity;
        @Expose public float energyCostPerSecond;
        @Expose public float energyPerCell;
        @Expose public float drillQuantity;
        @Expose public float drillRarity;
        @Expose public int durability;
        @Expose public String job;
    }

    /**
     * Passive drilling wear is {@code wearCoefficient x max(1, Power)^wearPowerExponent} per second,
     * so a stronger rig burns its hull faster and a series' durability edge shows through. Installed
     * attachments wear from their own pools at {@code attachmentDrainPerSecondMining} instead.
     */
    public static class DurabilityEntry {
        @Expose public float wearCoefficient;
        @Expose public float wearPowerExponent;
        @Expose public float attachmentDrainPerSecondMining;
        @Expose public float destructionLossFraction;
        @Expose public float hpDamagePerDurabilityPoint;
    }

    public static class DeploymentEntry {
        @Expose public float initialEnergyFraction;
        @Expose public float healFractionOnJobComplete;
    }

    /**
     * One extraction job. Duration = baseDurationTicks × (TP / timePowerReference)^(±1/timePowerRoot)
     * × speedK / (speedBase + speed); payouts follow the {@link PayoutEntry}.
     */
    public static class JobEntry {
        @Expose public int minPower;
        @Expose public int minRank;
        @Expose public String drillClass;
        @Expose public boolean requiresArming;
        @Expose public int baseDurationTicks;
        @Expose public float timePowerRoot;
        @Expose public float timePowerReference;
        @Expose public boolean timeShrinksWithPower;
        @Expose public float speedK;
        @Expose public float speedBase;
        @Expose public PayoutEntry payout;

        public JobEntry() {
        }

        public JobEntry(int minPower, int minRank, String drillClass, int baseDurationTicks) {
            this.minPower = minPower;
            this.minRank = minRank;
            this.drillClass = drillClass;
            this.baseDurationTicks = baseDurationTicks;
        }

        public int durationTicks(int totalPower, int speed) {
            float reference = this.timePowerReference > 0.0F ? this.timePowerReference : Math.max(1, this.minPower);
            double powerTerm = 1.0D;
            if (this.timePowerRoot > 0.0F && totalPower > 0) {
                double exponent = (this.timeShrinksWithPower ? -1.0D : 1.0D) / this.timePowerRoot;
                powerTerm = Math.pow(Math.max(totalPower, 1) / (double) reference, exponent);
            }
            double speedTerm = this.speedK <= 0.0F ? 1.0D : this.speedK / (this.speedBase + Math.max(0, speed));
            return Math.max(20, (int) Math.round(this.baseDurationTicks * powerTerm * speedTerm));
        }
    }

    public static class TierWindow {
        @Expose public int minPower;
        @Expose public int tierMin;
        @Expose public int tierMax;

        public TierWindow() {
        }

        public TierWindow(int minPower, int tierMin, int tierMax) {
            this.minPower = minPower;
            this.tierMin = tierMin;
            this.tierMax = tierMax;
        }
    }

    /**
     * Payout shape. count = uniform[countMin, countMax] × powerScaling(TP / powerReference) × quantScaling(1 + quant / 100);
     * rarity weights shift by extra power (TP − minPower) and the item-rarity stat damps one key by K / (K + IR).
     */
    public static class PayoutEntry {
        @Expose public String type;
        @Expose public String item;
        @Expose public String itemNbtId;
        @Expose public float countMin;
        @Expose public float countMax;
        @Expose public String powerScaling;
        @Expose public float powerReference;
        @Expose public String quantScaling;
        @Expose public LinkedHashMap<String, Float> rarityWeights = new LinkedHashMap<>();
        @Expose public LinkedHashMap<String, Float> rarityPerExtraPower = new LinkedHashMap<>();
        @Expose public LinkedHashMap<String, Float> rarityPerExtraPowerSquared = new LinkedHashMap<>();
        @Expose public String itemRarityDampedKey;
        @Expose public float itemRarityDampK;
        @Expose public List<TierWindow> tierWindows = new ArrayList<>();

        public PayoutEntry() {
        }

    }

    public static class Count {
        @Expose public int min;
        @Expose public int max;

        public Count() {
        }

        public Count(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public static Count of(int fixed) {
            return new Count(fixed, fixed);
        }

        public int roll(Random random) {
            if (this.max <= this.min) {
                return Math.max(0, this.min);
            }
            return this.min + random.nextInt(this.max - this.min + 1);
        }
    }

    /** One wave on its own cooldown; every class count it does not use stays at zero. */
    public static class TimedWaveEntry {
        @Expose public int intervalTicks;
        @Expose public Count horde;
        @Expose public Count assassins;
        @Expose public Count tanks;
        @Expose public Count elites;
        @Expose public Count bosses;

        public TimedWaveEntry() {
        }

        public TimedWaveEntry(int intervalTicks) {
            this.intervalTicks = intervalTicks;
        }
    }

    /**
     * The wave table one site runs, picked once when its extractor starts drilling: the highest tier whose
     * {@code minPower} the extractor's power reaches. Pools are per tier so composition can shift with power.
     */
    public static class TierEntry {
        @Expose public String name;
        @Expose public int minPower;
        @Expose public TimedWaveEntry trickle;
        @Expose public TimedWaveEntry horde;
        @Expose public TimedWaveEntry combat;
        @Expose public TimedWaveEntry assault;
        @Expose public TimedWaveEntry siege;
        @Expose public WeightedList<ResourceLocation> hordePool = new WeightedList<>();
        @Expose public WeightedList<ResourceLocation> assassinPool = new WeightedList<>();
        @Expose public WeightedList<ResourceLocation> tankPool = new WeightedList<>();
        @Expose public WeightedList<ResourceLocation> elitePool = new WeightedList<>();
        @Expose public int ramIntervalTicks;

        public TierEntry() {
        }

        public TierEntry(String name, int minPower) {
            this.name = name;
            this.minPower = minPower;
        }
    }

    public static class FloorAnchor {
        @Expose public int power;
        @Expose public int medallionRank;

        public FloorAnchor() {
        }

        public FloorAnchor(int power, int medallionRank) {
            this.power = power;
            this.medallionRank = medallionRank;
        }
    }

    /**
     * Wave mobs are raised to the health and damage they would have under the medallion rank a site's extractor
     * power maps to, interpolated exponentially between anchors and flat past the last one; a vault whose own
     * medallion already meets the floor is left alone.
     */
    public static class StatFloorEntry {
        @Expose public boolean enabled;
        @Expose public List<FloorAnchor> anchors = new ArrayList<>();
    }

    public static class BossEntry {
        @Expose public WeightedList<ResourceLocation> pool = new WeightedList<>();
        @Expose public float speedMultiplier;
        @Expose public float knockbackResistance;
        @Expose public int infernalModifierCount;
        @Expose public int spawnSearchRadius;
    }

    public static class EliteEntry {
        @Expose public float knockbackResistance;
    }

    public static class CarrierEntry {
        @Expose public String baseEntity;
        @Expose public int intervalTicks;
        @Expose public float healthMultiplier;
        @Expose public float damageMultiplier;
        @Expose public int auraRadius;
        @Expose public int auraIntervalTicks;
        @Expose public int auraEffectDurationTicks;
    }

    /**
     * Baked lane pathing for wave mobs. A mob follows its rig's flow field until it is within {@code engageSteps}
     * lane steps of the pad, or, if ranged, sees the rig within {@code rangedEngageBlocks}; it re-aims every
     * {@code repathTicks} at the farthest lane cell in straight-line view within {@code lookahead} steps, and is nudged
     * one lane cell forward after {@code stuckTicks} without progress.
     */
    public static class LanesEntry {
        @Expose public int engageSteps;
        @Expose public float rangedEngageBlocks;
        @Expose public int repathTicks;
        @Expose public int lookahead;
        @Expose public int stuckTicks;
    }

    /**
     * The battering ram: a slow, single-minded mob that walks a 2-wide lane to one rig and deals level-independent
     * true damage to it. Max HP = {@code hpEliteMultiple} x the max HP {@code hpReference} rolls in the same vault; each
     * hit deals the rig's shield regen per second plus (max shield + max HP) / {@code killSeconds}.
     */
    public static class RamEntry {
        @Expose public String entity;
        @Expose public String hpReference;
        @Expose public float hpEliteMultiple;
        @Expose public float speedMultiplier;
        @Expose public float killSeconds;
        @Expose public int firstTicks;
        @Expose public int maxPerRoom;
        @Expose public int maxPerTarget;
        @Expose public boolean cellDrop;
    }

    /**
     * One wave state per site room, run from the main rig's tier while it drills. Spread waves go out in packets of at
     * most {@code packetSize} from a shuffle bag of the room's caves, each to the rig its cave faces; assault waves focus
     * one rig at a time in {@code focusOrder} (pad indices); siege waves surge from every cave when {@code surgeSiege}.
     * Counts are multiplied by {@code roomCountMultiplier}, ramping linearly to x{@code rampAtFullProgress} at the
     * end of the job.
     */
    public static class WavesEntry {
        @Expose public float roomCountMultiplier;
        @Expose public float rampAtFullProgress;
        @Expose public int packetSize;
        @Expose public int packetStaggerTicks;
        @Expose public int liveMobCapPerRoom;
        @Expose public int liveMobCapPerVault;
        @Expose public List<Integer> focusOrder = new ArrayList<>();
        @Expose public boolean surgeSiege;
        @Expose public boolean spawnDebugLog;
        @Expose public LanesEntry lanes;
        @Expose public RamEntry ram;
        @Expose public StatFloorEntry statFloor;
        @Expose public BossEntry boss;
        @Expose public EliteEntry elite;
        @Expose public CarrierEntry carrier;
        @Expose public List<TierEntry> tiers = new ArrayList<>();

        /** The highest tier whose {@code minPower} the given power reaches, or null when no tier is configured. */
        public TierEntry tierFor(int power) {
            TierEntry best = null;
            for (TierEntry tier : this.tiers) {
                if (tier.minPower <= power && (best == null || tier.minPower >= best.minPower)) {
                    best = tier;
                }
            }
            return best;
        }
    }

    /**
     * Thumpers, the two secondary rigs of a site room. Their pools are fractions of the keyed extractor's stats, taken
     * when placed. Each wrecked thumper cuts the main rig's progress rate by {@code progressLossPerThumper} and charges
     * {@code destructionPenaltyFraction} of the main rig's own destruction durability loss.
     */
    public static class ThumperEntry {
        @Expose public float healthFraction;
        @Expose public float shieldFraction;
        @Expose public float shieldRegenFraction;
        @Expose public float healthRegenFraction;
        @Expose public float armorFraction;
        @Expose public float shieldToughnessFraction;
        @Expose public float progressLossPerThumper;
        @Expose public float destructionPenaltyFraction;
    }

    /**
     * Cache chests placed in a site room while its main rig drills: every {@code intervalTicks} a batch of
     * {@code batchMin..batchMax} chests, at most {@code cap} standing at once. Spots are standable, player-reachable cells
     * at least {@code mobMargin} columns from any mob lane and {@code pedestalClearance} from pads and room walls. In
     * cluster mode a batch is one pile within {@code clusterRadius} of a centre that keeps {@code clusterSpacing} from
     * other chests; chests stay {@code minSpacing} apart and centres {@code playerClearance} from players. A player break
     * drops one power cell with chance {@code cellChance}.
     */
    public static class CacheEntry {
        @Expose public int intervalTicks;
        @Expose public int batchMin;
        @Expose public int batchMax;
        @Expose public int cap;
        @Expose public double cellChance;
        @Expose public int mobMargin;
        @Expose public int pedestalClearance;
        @Expose public int minSpacing;
        @Expose public int playerClearance;
        @Expose public boolean cluster;
        @Expose public int clusterRadius;
        @Expose public int clusterSpacing;
    }

    public static class CellsEntry {
        @Expose public double hordeMobDropChance;
        @Expose public double assassinMobDropChance;
        @Expose public double eliteMobDropChance;
        @Expose public double bossMobDropChance;
        @Expose public double otherMobDropChance;
        @Expose public double chestDropChance;
    }

    public static class SitesEntry {
        @Expose public int count;
        @Expose public int ringMinRooms;
        @Expose public int ringMaxRooms;
        @Expose public int minSeparationRooms;
    }

    /**
     * The Greed Infuser's card slots and class ladder. Slots = {@code baseSlots} + one per {@code powerPerExtraSlot}
     * above the job's minimum power, capped at {@code maxSlots}. A card's class is found in the card modifier pool
     * {@code cardPoolPrefix + class}; the placer's class is the highest one whose greed trader pool
     * {@code traderPoolPrefix + class} their greed rank can see. {@code classOrder} runs weakest to strongest.
     */
    public static class GreedInfuserEntry {
        public static final int SLOT_LIMIT = 27;

        @Expose public int baseSlots;
        @Expose public int powerPerExtraSlot;
        @Expose public int maxSlots;
        @Expose public List<String> classOrder;
        @Expose public String cardPoolPrefix;
        @Expose public String legacyCardPool;
        @Expose public String traderPoolPrefix;

        public int slotsFor(int power, int minPower) {
            int step = Math.max(1, this.powerPerExtraSlot);
            int slots = this.baseSlots + Math.max(0, power - minPower) / step;
            return Math.max(1, Math.min(Math.min(this.maxSlots, SLOT_LIMIT), slots));
        }
    }

    /** A finished fluid job keeps its fluid on board; while any is left the rig refuses pickup for this long. */
    public static class FluidPayoutEntry {
        @Expose public int pickupLockSeconds;
    }

    /** One weighted source of crystal modifiers: a vault modifier pool ({@code "vault"}) or a greed trader modifier list ({@code "greed_trader"}). */
    public static class ModifierSource {
        @Expose public String type;
        @Expose public String pool;
        @Expose public float weight;

        public ModifierSource() {
        }

        public ModifierSource(String type, String pool, float weight) {
            this.type = type;
            this.pool = pool;
            this.weight = weight;
        }
    }

    public static class WeightedId {
        @Expose public String id;
        @Expose public float weight;

        public WeightedId() {
        }

        public WeightedId(String id, float weight) {
            this.id = id;
            this.weight = weight;
        }
    }

    /**
     * The Ancient Crystal payout. {@code crystalRolls} tries at {@code crystalChance}, plus one try per
     * {@code quantityPerExtraRoll} quantity (the remainder is a partial try). Each crystal gets the strong rolls, then
     * {@code randomRolls} + power / {@code powerPerRandomRoll} + rarity / {@code rarityPerRandomRoll} random rolls that
     * are positive with weight {@code positiveWeight} + {@code positiveWeightPerRarity} x rarity against
     * {@code negativeWeight}. Every roll picks one source by weight, then one modifier from it. Capacity is
     * {@code capacityMin..capacityMax} + power / {@code powerPerCapacity} + rarity / {@code rarityPerCapacity} plus the
     * placer's own crystal-capacity bonuses. The objective comes from {@code objectivePool} at the vault's level minus
     * {@code excludedObjectiveTypes}; the sigil is drawn from {@code sigils}.
     */
    public static class AncientCrystalEntry {
        @Expose public int crystalRolls;
        @Expose public float crystalChance;
        @Expose public float quantityPerExtraRoll;
        @Expose public int strongNegativeRolls;
        @Expose public int strongPositiveRolls;
        @Expose public int randomRolls;
        @Expose public float powerPerRandomRoll;
        @Expose public float rarityPerRandomRoll;
        @Expose public float positiveWeight;
        @Expose public float negativeWeight;
        @Expose public float positiveWeightPerRarity;
        @Expose public int capacityMin;
        @Expose public int capacityMax;
        @Expose public float powerPerCapacity;
        @Expose public float rarityPerCapacity;
        @Expose public boolean vaultRandomModifiers;
        @Expose public List<ModifierSource> strongPositive = new ArrayList<>();
        @Expose public List<ModifierSource> strongNegative = new ArrayList<>();
        @Expose public List<ModifierSource> randomPositive = new ArrayList<>();
        @Expose public List<ModifierSource> randomNegative = new ArrayList<>();
        @Expose public String objectivePool;
        @Expose public List<String> excludedObjectiveTypes;
        @Expose public List<WeightedId> sigils = new ArrayList<>();
    }
}
