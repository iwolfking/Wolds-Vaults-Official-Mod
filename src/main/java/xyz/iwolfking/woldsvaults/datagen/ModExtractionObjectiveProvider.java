package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.config.ExtractionObjectiveConfig;
import xyz.iwolfking.woldsvaults.config.ExtractionObjectiveConfig.Count;
import xyz.iwolfking.woldsvaults.config.ExtractionObjectiveConfig.FloorAnchor;
import xyz.iwolfking.woldsvaults.config.ExtractionObjectiveConfig.JobEntry;
import xyz.iwolfking.woldsvaults.config.ExtractionObjectiveConfig.ModifierSource;
import xyz.iwolfking.woldsvaults.config.ExtractionObjectiveConfig.PayoutEntry;
import xyz.iwolfking.woldsvaults.config.ExtractionObjectiveConfig.TierEntry;
import xyz.iwolfking.woldsvaults.config.ExtractionObjectiveConfig.TierWindow;
import xyz.iwolfking.woldsvaults.config.ExtractionObjectiveConfig.TimedWaveEntry;
import xyz.iwolfking.woldsvaults.config.ExtractionObjectiveConfig.WeightedId;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractWoldsTableProvider;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * The extraction objective: the default rig, wear and deployment, every extraction job and its payout, the wave
 * tiers sites run, cell drop chances, site placement, the Greed Infuser, fluid payouts and the Ancient Crystal payout.
 */
public class ModExtractionObjectiveProvider extends AbstractWoldsTableProvider {

    public ModExtractionObjectiveProvider(DataGenerator generator) {
        super(generator, "objectives/extraction");
    }

    @Override
    public void registerConfigs() {
        ExtractionObjectiveConfig table = new ExtractionObjectiveConfig();
        table.DEFAULT_EXTRACTOR = defaultExtractor();
        table.DURABILITY = durability();
        table.DEPLOYMENT = new ExtractionObjectiveConfig.DeploymentEntry();
        table.DEPLOYMENT.initialEnergyFraction = 0.4F;
        table.DEPLOYMENT.healFractionOnJobComplete = 0.33F;
        table.JOBS = jobs();
        table.WAVES = waves();
        table.THUMPER = thumper();
        table.CACHE = cache();
        table.CELLS = cells();
        table.SITES = new ExtractionObjectiveConfig.SitesEntry();
        table.SITES.count = 3;
        table.SITES.ringMinRooms = 4;
        table.SITES.ringMaxRooms = 9;
        table.SITES.minSeparationRooms = 3;
        table.GREED_INFUSER = greedInfuser();
        table.FLUID_PAYOUT = new ExtractionObjectiveConfig.FluidPayoutEntry();
        table.FLUID_PAYOUT.pickupLockSeconds = 45;
        table.ANCIENT_CRYSTAL = ancientCrystal();
        this.table("extraction_objective", table);
    }

    private static ExtractionObjectiveConfig.DefaultExtractorEntry defaultExtractor() {
        ExtractionObjectiveConfig.DefaultExtractorEntry rig = new ExtractionObjectiveConfig.DefaultExtractorEntry();
        rig.health = 10000.0F;
        rig.shield = 2000.0F;
        rig.armor = 10.0F;
        rig.shieldToughness = 0.0F;
        rig.shieldRegenPerTick = 5.0F;
        rig.power = 50;
        rig.speed = 100;
        rig.energyCapacity = 1000.0F;
        rig.energyCostPerSecond = 15.0F;
        rig.energyPerCell = 200.0F;
        rig.drillQuantity = 0.0F;
        rig.drillRarity = 0.0F;
        rig.durability = 100000;
        rig.job = "arcane_essence";
        return rig;
    }

    private static ExtractionObjectiveConfig.DurabilityEntry durability() {
        ExtractionObjectiveConfig.DurabilityEntry wear = new ExtractionObjectiveConfig.DurabilityEntry();
        wear.wearCoefficient = 1.0F;
        wear.wearPowerExponent = 0.65F;
        wear.attachmentDrainPerSecondMining = 20.0F;
        wear.destructionLossFraction = 0.45F;
        wear.hpDamagePerDurabilityPoint = 2.0F;
        return wear;
    }

    private static LinkedHashMap<String, JobEntry> jobs() {
        LinkedHashMap<String, JobEntry> jobs = new LinkedHashMap<>();

        JobEntry arcane = job(50, 0, "standard", 2400);
        arcane.payout = item("woldsvaults:arcane_essence", 50, 100, "linear", 50, "sqrt");
        jobs.put("arcane_essence", arcane);

        JobEntry maps = job(50, 3, "laser", 3600);
        maps.timePowerReference = 75;
        maps.payout = payout("maps", 2, 4, "sqrt", 50, "sqrt");
        maps.payout.rarityWeights.put("rare", 70.0F);
        maps.payout.rarityWeights.put("epic", 20.0F);
        maps.payout.rarityWeights.put("omega", 10.0F);
        maps.payout.rarityPerExtraPower.put("epic", 0.2F);
        maps.payout.rarityPerExtraPowerSquared.put("omega", 0.002F);
        maps.payout.itemRarityDampedKey = "rare";
        maps.payout.itemRarityDampK = 500.0F;
        maps.payout.tierWindows.add(new TierWindow(0, 1, 3));
        maps.payout.tierWindows.add(new TierWindow(100, 2, 4));
        maps.payout.tierWindows.add(new TierWindow(175, 3, 5));
        maps.payout.tierWindows.add(new TierWindow(300, 4, 6));
        jobs.put("maps", maps);

        JobEntry cards = job(75, 4, "greed_infuser", 2400);
        cards.requiresArming = true;
        cards.timePowerRoot = 4.0F;
        cards.timeShrinksWithPower = true;
        cards.payout = payout("cards", 3, 3, "none", 75, "none");
        jobs.put("greed_card_upgrade", cards);

        JobEntry charms = job(75, 4, "archeology", 4800);
        charms.payout = payout("charms", 3, 4, "sqrt", 75, "sqrt");
        charms.payout.rarityWeights.put("omega", 70.0F);
        charms.payout.rarityWeights.put("mythic", 30.0F);
        charms.payout.rarityPerExtraPowerSquared.put("mythic", 0.0004F);
        charms.payout.itemRarityDampedKey = "omega";
        charms.payout.itemRarityDampK = 1000.0F;
        jobs.put("mythic_charms", charms);

        JobEntry uniques = job(100, 7, "excavator", 3600);
        uniques.payout = payout("uniques", 2, 3, "sqrt", 100, "cbrt");
        uniques.payout.rarityWeights.put("unique", 95.0F);
        uniques.payout.rarityWeights.put("ancient", 5.0F);
        uniques.payout.rarityPerExtraPower.put("ancient", 0.06F);
        uniques.payout.itemRarityDampedKey = "unique";
        uniques.payout.itemRarityDampK = 1000.0F;
        jobs.put("ancient_uniques", uniques);

        JobEntry sealing = job(125, 5, "refiner", 6000);
        sealing.payout = payout("seal", 1, 1, "none", 125, "none");
        jobs.put("power_sealing", sealing);

        JobEntry packs = job(125, 4, "heavy", 3600);
        packs.payout = item("the_vault:booster_pack", 3, 5, "sqrt", 75, "sqrt");
        packs.payout.itemNbtId = "the_vault:shiny_pack";
        jobs.put("shiny_card_packs", packs);

        JobEntry juice = job(175, 10, "juicer", 4800);
        juice.payout = item("the_vault:card_juice", 50, 100, "sqrt", 100, "sqrt");
        jobs.put("card_juice", juice);

        JobEntry glue = job(200, 8, "glue_derrick", 4800);
        glue.payout = payout("fluid", 100, 300, "sqrt", 200, "sqrt");
        glue.payout.item = "woldsvaults:prismatic_glue";
        jobs.put("prismatic_glue", glue);

        JobEntry crystals = job(350, 10, "crystal", 6000);
        crystals.payout = payout("crystals", 2, 2, "none", 350, "none");
        jobs.put("ancient_crystals", crystals);

        JobEntry nullite = job(500, 13, "containment", 12000);
        nullite.speedK = 500.0F;
        nullite.speedBase = 400.0F;
        nullite.payout = item("woldsvaults:nullite_crystal", 12, 24, "linear", 500, "sqrt");
        jobs.put("nullite", nullite);
        return jobs;
    }

    /** A job on the standard duration curve: cube root of power over minPower, speed term 300 / (200 + speed). */
    private static JobEntry job(int minPower, int minRank, String drillClass, int baseDurationTicks) {
        JobEntry job = new JobEntry(minPower, minRank, drillClass, baseDurationTicks);
        job.requiresArming = false;
        job.timePowerRoot = 3.0F;
        job.timePowerReference = 0.0F;
        job.timeShrinksWithPower = false;
        job.speedK = 300.0F;
        job.speedBase = 200.0F;
        return job;
    }

    /** A payout with no item, no rarity table and the default item-rarity damping. */
    private static PayoutEntry payout(String type, float countMin, float countMax, String powerScaling, float powerReference, String quantScaling) {
        PayoutEntry payout = new PayoutEntry();
        payout.type = type;
        payout.item = "";
        payout.itemNbtId = "";
        payout.countMin = countMin;
        payout.countMax = countMax;
        payout.powerScaling = powerScaling;
        payout.powerReference = powerReference;
        payout.quantScaling = quantScaling;
        payout.itemRarityDampedKey = "";
        payout.itemRarityDampK = 1000.0F;
        return payout;
    }

    private static PayoutEntry item(String item, float countMin, float countMax, String powerScaling, float powerReference, String quantScaling) {
        PayoutEntry payout = payout("item", countMin, countMax, powerScaling, powerReference, quantScaling);
        payout.item = item;
        return payout;
    }

    private static ExtractionObjectiveConfig.WavesEntry waves() {
        ExtractionObjectiveConfig.WavesEntry waves = new ExtractionObjectiveConfig.WavesEntry();
        waves.roomCountMultiplier = 1.2F;
        waves.rampAtFullProgress = 1.0F;
        waves.packetSize = 4;
        waves.packetStaggerTicks = 5;
        waves.liveMobCapPerRoom = 600;
        waves.liveMobCapPerVault = 1200;
        waves.focusOrder = new ArrayList<>(List.of(1, 0, 2));
        waves.surgeSiege = true;
        waves.spawnDebugLog = true;

        waves.lanes = new ExtractionObjectiveConfig.LanesEntry();
        waves.lanes.engageSteps = 6;
        waves.lanes.rangedEngageBlocks = 12.0F;
        waves.lanes.repathTicks = 10;
        waves.lanes.lookahead = 12;
        waves.lanes.stuckTicks = 200;

        waves.ram = new ExtractionObjectiveConfig.RamEntry();
        waves.ram.entity = "minecraft:ravager";
        waves.ram.hpReference = "the_vault:elite_zombie";
        waves.ram.hpEliteMultiple = 2.0F;
        waves.ram.speedMultiplier = 0.6F;
        waves.ram.killSeconds = 60.0F;
        waves.ram.firstTicks = 900;
        waves.ram.maxPerRoom = 2;
        waves.ram.maxPerTarget = 1;
        waves.ram.cellDrop = true;

        waves.statFloor = new ExtractionObjectiveConfig.StatFloorEntry();
        waves.statFloor.enabled = true;
        waves.statFloor.anchors.add(new FloorAnchor(0, 4));
        waves.statFloor.anchors.add(new FloorAnchor(110, 8));
        waves.statFloor.anchors.add(new FloorAnchor(200, 11));
        waves.statFloor.anchors.add(new FloorAnchor(420, 13));
        waves.statFloor.anchors.add(new FloorAnchor(700, 15));

        waves.boss = new ExtractionObjectiveConfig.BossEntry();
        waves.boss.pool.add(vh("elite_zombie"), 20);
        waves.boss.pool.add(vh("elite_husk"), 20);
        waves.boss.pool.add(vh("elite_skeleton"), 20);
        waves.boss.pool.add(vh("elite_stray"), 20);
        waves.boss.pool.add(vh("elite_drowned"), 20);
        waves.boss.pool.add(vh("elite_wither_skeleton"), 20);
        waves.boss.pool.add(vh("elite_witch"), 20);
        waves.boss.pool.add(vh("boogieman"), 10);
        waves.boss.pool.add(vh("blue_blaze"), 10);
        waves.boss.speedMultiplier = 0.65F;
        waves.boss.knockbackResistance = 1.0F;
        waves.boss.infernalModifierCount = 6;
        waves.boss.spawnSearchRadius = 5;

        waves.elite = new ExtractionObjectiveConfig.EliteEntry();
        waves.elite.knockbackResistance = 1.0F;

        waves.carrier = new ExtractionObjectiveConfig.CarrierEntry();
        waves.carrier.baseEntity = "the_vault:blood_tank";
        waves.carrier.intervalTicks = 400;
        waves.carrier.healthMultiplier = 2.0F;
        waves.carrier.damageMultiplier = 1.5F;
        waves.carrier.auraRadius = 8;
        waves.carrier.auraIntervalTicks = 100;
        waves.carrier.auraEffectDurationTicks = 120;

        TierEntry salvage = new TierEntry("salvage", 0);
        salvage.trickle = horde(80, 2, 3);
        salvage.horde = horde(200, 8, 12);
        salvage.combat = assassins(320, 3);
        salvage.assault = mixed(500, 0, 6, 1, 0, 0);
        salvage.siege = mixed(1400, 12, 5, 2, 0, 0);
        salvage.hordePool.add(new ResourceLocation("minecraft", "zombie"), 3);
        salvage.hordePool.add(vh("t1_zombie"), 3);
        salvage.hordePool.add(vh("t1_husk"), 2);
        salvage.hordePool.add(vh("t1_drowned"), 2);
        salvage.hordePool.add(vh("t2_zombie"), 2);
        salvage.hordePool.add(vh("blood_horde_t1"), 2);
        salvage.hordePool.add(vh("miner_zombie_t1"), 1);
        salvage.assassinPool.add(vh("t1_skeleton"), 3);
        salvage.assassinPool.add(vh("vault_spider"), 3);
        salvage.assassinPool.add(vh("t1_stray"), 2);
        salvage.assassinPool.add(vh("t1_cave_skeleton"), 1);
        salvage.assassinPool.add(vh("blood_skeleton_t1"), 1);
        salvage.tankPool.add(vh("blood_tank"), 3);
        salvage.tankPool.add(vh("overgrown_tank"), 2);
        salvage.tankPool.add(vh("t1_wither_skeleton"), 1);
        addElitePool(salvage);
        salvage.ramIntervalTicks = 1200;
        waves.tiers.add(salvage);

        TierEntry industrial = new TierEntry("industrial", 110);
        industrial.trickle = horde(70, 2, 3);
        industrial.horde = horde(180, 8, 12);
        industrial.combat = assassins(300, 4);
        industrial.assault = mixed(450, 0, 7, 1, 0, 0);
        industrial.siege = mixed(1300, 13, 6, 2, 0, 0);
        industrial.hordePool.add(vh("t2_zombie"), 3);
        industrial.hordePool.add(vh("t2_husk"), 3);
        industrial.hordePool.add(vh("t2_drowned"), 3);
        industrial.hordePool.add(vh("blood_horde_t2"), 2);
        industrial.hordePool.add(vh("miner_zombie_t2"), 2);
        industrial.hordePool.add(vh("overgrown_zombie_t2"), 1);
        industrial.hordePool.add(vh("skeleton_pirate_t2"), 1);
        industrial.assassinPool.add(vh("t2_skeleton"), 3);
        industrial.assassinPool.add(vh("vault_spider"), 3);
        industrial.assassinPool.add(vh("t2_stray"), 2);
        industrial.assassinPool.add(vh("blood_skeleton_t2"), 2);
        industrial.assassinPool.add(new ResourceLocation("quark", "wraith"), 1);
        industrial.tankPool.add(vh("blood_tank"), 3);
        industrial.tankPool.add(vh("overgrown_tank"), 2);
        industrial.tankPool.add(vh("t2_wither_skeleton"), 1);
        addElitePool(industrial);
        industrial.ramIntervalTicks = 1200;
        waves.tiers.add(industrial);

        TierEntry fortified = new TierEntry("fortified", 200);
        fortified.trickle = horde(60, 2, 4);
        fortified.horde = horde(160, 9, 13);
        fortified.combat = assassins(280, 5);
        fortified.assault = mixed(400, 0, 8, 1, 0, 0);
        fortified.siege = mixed(1200, 15, 8, 3, 1, 0);
        fortified.hordePool.add(vh("t2_zombie"), 2);
        fortified.hordePool.add(vh("t2_husk"), 2);
        fortified.hordePool.add(vh("t2_drowned"), 2);
        fortified.hordePool.add(vh("blood_horde_t3"), 3);
        fortified.hordePool.add(vh("miner_zombie_t3"), 2);
        fortified.hordePool.add(vh("overgrown_zombie_t3"), 2);
        fortified.hordePool.add(vh("winterwalker_t3"), 1);
        fortified.hordePool.add(vh("mummy_t1"), 1);
        fortified.assassinPool.add(vh("t2_skeleton"), 3);
        fortified.assassinPool.add(vh("vault_spider"), 2);
        fortified.assassinPool.add(vh("t2_stray"), 2);
        fortified.assassinPool.add(vh("blood_skeleton_t3"), 2);
        fortified.assassinPool.add(vh("t2_cave_skeleton"), 1);
        fortified.assassinPool.add(vh("vault_wraith_white"), 1);
        fortified.assassinPool.add(new ResourceLocation("quark", "wraith"), 1);
        fortified.tankPool.add(vh("blood_tank"), 3);
        fortified.tankPool.add(vh("overgrown_tank"), 2);
        fortified.tankPool.add(vh("t2_wither_skeleton"), 1);
        fortified.tankPool.add(vh("plastic_tank"), 1);
        addElitePool(fortified);
        fortified.ramIntervalTicks = 1200;
        waves.tiers.add(fortified);

        TierEntry siege = new TierEntry("siege", 420);
        siege.trickle = horde(50, 3, 4);
        siege.horde = horde(140, 10, 14);
        siege.combat = assassins(260, 5);
        siege.assault = mixed(350, 0, 9, 2, 1, 0);
        siege.siege = mixed(1100, 18, 9, 4, 1, 1);
        siege.hordePool.add(vh("t3_zombie"), 3);
        siege.hordePool.add(vh("t3_husk"), 3);
        siege.hordePool.add(vh("t3_drowned"), 2);
        siege.hordePool.add(vh("blood_horde_t4"), 3);
        siege.hordePool.add(vh("miner_zombie_t4"), 2);
        siege.hordePool.add(vh("overgrown_zombie_t4"), 2);
        siege.hordePool.add(vh("winterwalker_t4"), 1);
        siege.hordePool.add(vh("mummy_t2"), 1);
        siege.hordePool.add(vh("skeleton_pirate_t4"), 1);
        siege.assassinPool.add(vh("t3_skeleton"), 3);
        siege.assassinPool.add(vh("t3_stray"), 2);
        siege.assassinPool.add(vh("blood_skeleton_t4"), 2);
        siege.assassinPool.add(vh("t3_cave_skeleton"), 2);
        siege.assassinPool.add(vh("vault_wraith_yellow"), 1);
        siege.assassinPool.add(vh("deep_dark_skeleton"), 1);
        siege.tankPool.add(vh("blood_tank"), 2);
        siege.tankPool.add(vh("overgrown_tank"), 2);
        siege.tankPool.add(vh("t3_wither_skeleton"), 2);
        siege.tankPool.add(vh("pirate_guardian_tank"), 1);
        addElitePool(siege);
        siege.ramIntervalTicks = 900;
        waves.tiers.add(siege);

        TierEntry cataclysm = new TierEntry("cataclysm", 700);
        cataclysm.trickle = horde(40, 3, 4);
        cataclysm.horde = horde(120, 10, 15);
        cataclysm.combat = assassins(240, 6);
        cataclysm.assault = mixed(300, 0, 10, 2, 1, 0);
        cataclysm.siege = mixed(1000, 20, 10, 5, 1, 1);
        cataclysm.hordePool.add(vh("t3_zombie"), 3);
        cataclysm.hordePool.add(vh("t3_husk"), 3);
        cataclysm.hordePool.add(vh("t3_drowned"), 2);
        cataclysm.hordePool.add(vh("blood_horde_t5"), 3);
        cataclysm.hordePool.add(vh("miner_zombie_t5"), 2);
        cataclysm.hordePool.add(vh("overgrown_zombie_t5"), 2);
        cataclysm.hordePool.add(vh("winterwalker_t5"), 1);
        cataclysm.hordePool.add(vh("mummy_t2"), 1);
        cataclysm.hordePool.add(vh("skeleton_pirate_t5"), 1);
        cataclysm.assassinPool.add(vh("t3_skeleton"), 3);
        cataclysm.assassinPool.add(vh("t3_stray"), 2);
        cataclysm.assassinPool.add(vh("blood_skeleton_t5"), 3);
        cataclysm.assassinPool.add(vh("t4_cave_skeleton"), 2);
        cataclysm.assassinPool.add(vh("vault_wraith_yellow"), 1);
        cataclysm.assassinPool.add(vh("deep_dark_skeleton"), 1);
        cataclysm.tankPool.add(vh("blood_tank"), 2);
        cataclysm.tankPool.add(vh("overgrown_tank"), 2);
        cataclysm.tankPool.add(vh("t3_wither_skeleton"), 2);
        cataclysm.tankPool.add(vh("pirate_guardian_tank"), 2);
        addElitePool(cataclysm);
        cataclysm.ramIntervalTicks = 900;
        waves.tiers.add(cataclysm);
        return waves;
    }

    private static void addElitePool(TierEntry tier) {
        tier.elitePool.add(vh("elite_zombie"), 3);
        tier.elitePool.add(vh("elite_husk"), 3);
        tier.elitePool.add(vh("elite_skeleton"), 3);
        tier.elitePool.add(vh("elite_stray"), 2);
        tier.elitePool.add(vh("elite_drowned"), 2);
        tier.elitePool.add(vh("elite_wither_skeleton"), 2);
        tier.elitePool.add(vh("vault_fighter_4"), 2);
        tier.elitePool.add(vh("bruiser_guardian"), 1);
        tier.elitePool.add(vh("arbalist_guardian"), 1);
    }

    private static ResourceLocation vh(String path) {
        return new ResourceLocation("the_vault", path);
    }

    private static TimedWaveEntry wave(int intervalTicks, Count horde, Count assassins, Count tanks, Count elites, Count bosses) {
        TimedWaveEntry wave = new TimedWaveEntry(intervalTicks);
        wave.horde = horde;
        wave.assassins = assassins;
        wave.tanks = tanks;
        wave.elites = elites;
        wave.bosses = bosses;
        return wave;
    }

    private static TimedWaveEntry horde(int intervalTicks, int min, int max) {
        return wave(intervalTicks, new Count(min, max), Count.of(0), Count.of(0), Count.of(0), Count.of(0));
    }

    private static TimedWaveEntry assassins(int intervalTicks, int count) {
        return wave(intervalTicks, Count.of(0), Count.of(count), Count.of(0), Count.of(0), Count.of(0));
    }

    private static TimedWaveEntry mixed(int intervalTicks, int horde, int assassins, int tanks, int elites, int bosses) {
        return wave(intervalTicks, Count.of(horde), Count.of(assassins), Count.of(tanks), Count.of(elites), Count.of(bosses));
    }

    private static ExtractionObjectiveConfig.ThumperEntry thumper() {
        ExtractionObjectiveConfig.ThumperEntry thumper = new ExtractionObjectiveConfig.ThumperEntry();
        thumper.healthFraction = 0.6F;
        thumper.shieldFraction = 0.6F;
        thumper.shieldRegenFraction = 0.6F;
        thumper.healthRegenFraction = 0.6F;
        thumper.armorFraction = 1.0F;
        thumper.shieldToughnessFraction = 1.0F;
        thumper.progressLossPerThumper = 0.25F;
        thumper.destructionPenaltyFraction = 0.10F;
        return thumper;
    }

    private static ExtractionObjectiveConfig.CacheEntry cache() {
        ExtractionObjectiveConfig.CacheEntry cache = new ExtractionObjectiveConfig.CacheEntry();
        cache.intervalTicks = 600;
        cache.batchMin = 4;
        cache.batchMax = 8;
        cache.cap = 40;
        cache.cellChance = 0.05D;
        cache.mobMargin = 2;
        cache.pedestalClearance = 6;
        cache.minSpacing = 2;
        cache.playerClearance = 4;
        cache.cluster = true;
        cache.clusterRadius = 4;
        cache.clusterSpacing = 10;
        return cache;
    }

    private static ExtractionObjectiveConfig.CellsEntry cells() {
        ExtractionObjectiveConfig.CellsEntry cells = new ExtractionObjectiveConfig.CellsEntry();
        cells.hordeMobDropChance = 0.001D;
        cells.assassinMobDropChance = 0.01D;
        cells.eliteMobDropChance = 0.05D;
        cells.bossMobDropChance = 1.0D;
        cells.otherMobDropChance = 0.001D;
        cells.chestDropChance = 0.01D;
        return cells;
    }

    private static ExtractionObjectiveConfig.GreedInfuserEntry greedInfuser() {
        ExtractionObjectiveConfig.GreedInfuserEntry infuser = new ExtractionObjectiveConfig.GreedInfuserEntry();
        infuser.baseSlots = 3;
        infuser.powerPerExtraSlot = 100;
        infuser.maxSlots = 18;
        infuser.classOrder = new ArrayList<>(List.of("weak", "medium", "strong", "elite"));
        infuser.cardPoolPrefix = "greed_";
        infuser.legacyCardPool = "greed";
        infuser.traderPoolPrefix = "greed_cards_";
        return infuser;
    }

    private static ExtractionObjectiveConfig.AncientCrystalEntry ancientCrystal() {
        ExtractionObjectiveConfig.AncientCrystalEntry crystal = new ExtractionObjectiveConfig.AncientCrystalEntry();
        crystal.crystalRolls = 2;
        crystal.crystalChance = 0.25F;
        crystal.quantityPerExtraRoll = 250.0F;
        crystal.strongNegativeRolls = 10;
        crystal.strongPositiveRolls = 10;
        crystal.randomRolls = 15;
        crystal.powerPerRandomRoll = 30.0F;
        crystal.rarityPerRandomRoll = 50.0F;
        crystal.positiveWeight = 60.0F;
        crystal.negativeWeight = 40.0F;
        crystal.positiveWeightPerRarity = 0.012F;
        crystal.capacityMin = 140;
        crystal.capacityMax = 175;
        crystal.powerPerCapacity = 10.0F;
        crystal.rarityPerCapacity = 10.0F;
        crystal.vaultRandomModifiers = false;
        crystal.strongPositive.add(new ModifierSource("greed_trader", "normal_modifiers", 14.0F));
        crystal.strongPositive.add(new ModifierSource("greed_trader", "treasure_modifiers", 1.5F));
        crystal.strongPositive.add(new ModifierSource("greed_trader", "temporal_modifiers", 1.0F));
        crystal.strongNegative.add(new ModifierSource("vault", "woldsvaults:concealed_chaos_backfire", 1.0F));
        crystal.randomPositive.add(new ModifierSource("greed_trader", "normal_modifiers", 14.0F));
        crystal.randomPositive.add(new ModifierSource("greed_trader", "treasure_modifiers", 1.5F));
        crystal.randomPositive.add(new ModifierSource("greed_trader", "temporal_modifiers", 1.0F));
        crystal.randomPositive.add(new ModifierSource("vault", "the_vault:omega_positive", 16.5F));
        crystal.randomNegative.add(new ModifierSource("vault", "woldsvaults:concealed_chaos_backfire", 1.0F));
        crystal.randomNegative.add(new ModifierSource("vault", "the_vault:alchemy_strong_negative", 1.0F));
        crystal.objectivePool = "the_vault:default";
        crystal.excludedObjectiveTypes = new ArrayList<>(List.of("monolith"));
        for (String step : List.of("70", "75", "80", "85", "90", "95", "100")) {
            crystal.sigils.add(new WeightedId("ancient_" + step, 1.0F));
        }
        return crystal;
    }
}
