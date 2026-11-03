package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.woldsvaults.config.ExtractorPartsConfig;
import xyz.iwolfking.woldsvaults.config.ExtractorPartsConfig.AffixEntry;
import xyz.iwolfking.woldsvaults.config.ExtractorPartsConfig.AttachmentEntry;
import xyz.iwolfking.woldsvaults.config.ExtractorPartsConfig.BucketEntry;
import xyz.iwolfking.woldsvaults.config.ExtractorPartsConfig.DrillClassEntry;
import xyz.iwolfking.woldsvaults.config.ExtractorPartsConfig.EffectEntry;
import xyz.iwolfking.woldsvaults.config.ExtractorPartsConfig.PartRollEntry;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractWoldsTableProvider;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorPartType;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorStat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Every number behind extractor parts, blueprints and assembly: series and their part names, which series each rarity
 * can roll, per-series and per-drill-class stat ranges, drill classes, attachments, affix pools, the blueprint quality
 * curve, the stat scaling families, attachment slots and the power cell restore base. Mirrors the "Extractor Parts and
 * Rolls" workbook tab.
 */
public class ModExtractorPartsProvider extends AbstractWoldsTableProvider {

    public ModExtractorPartsProvider(DataGenerator generator) {
        super(generator, "extraction/parts");
    }

    @Override
    public void registerConfigs() {
        ExtractorPartsConfig table = new ExtractorPartsConfig();
        table.SERIES = new LinkedHashMap<>();
        table.SERIES_BY_RARITY = new LinkedHashMap<>();
        table.RARITY_DROP_WEIGHTS = new LinkedHashMap<>();
        table.PARTS = new LinkedHashMap<>();
        table.DRILL_CLASSES = new LinkedHashMap<>();
        table.ATTACHMENTS = new LinkedHashMap<>();
        table.AFFIX_POOLS = new LinkedHashMap<>();
        table.AFFIX_COUNT = new LinkedHashMap<>();
        table.QUALITY = quality();
        table.SCALES = scales();
        table.SLOTS = slots();
        table.CELL_RESTORE_BASE = 200.0F;

        ExtractorPartsConfig.SeriesEntry wooden = new ExtractorPartsConfig.SeriesEntry("Wooden", "s1",
                "chassis_s1_wooden", "core_s1_combustion", "legs_s1_wooden", "shield_s1_projector", "radiator_s1_grating", "scanner_s1_probe");
        ExtractorPartsConfig.SeriesEntry chromatic = new ExtractorPartsConfig.SeriesEntry("Chromatic Steel", "s2",
                "chassis_s2_chromatic", "core_s2_turbine", "legs_s2_chromatic", "shield_s2_barrier", "radiator_s2_silkvent", "scanner_s2_calibrator");
        ExtractorPartsConfig.SeriesEntry experimental = new ExtractorPartsConfig.SeriesEntry("Experimental", "s3",
                "chassis_s3_suspension", "core_s3_fusion", "legs_s3_suspension", "shield_s3_voidbender", "radiator_s3_painite", "scanner_s3_wutodite");
        ExtractorPartsConfig.SeriesEntry armored = new ExtractorPartsConfig.SeriesEntry("Armored", "s4",
                "chassis_s4_armored", "core_s4_etg", "legs_s4_armored", "shield_s4_impact", "radiator_s4_heatsink", "scanner_s4_vaulterite");
        ExtractorPartsConfig.SeriesEntry echosteel = new ExtractorPartsConfig.SeriesEntry("Echosteel", "s5",
                "chassis_s5_echosteel", "core_s5_echofluid", "legs_s5_echosteel", "shield_s5_echoflare", "radiator_s5_evaporator", "scanner_s5_echolocator");
        names(wooden, "Wooden Chassis", "Combustion Engine", "Wooden Supports", "Shield Projector", "Iron Grating", "Radio Probe");
        names(chromatic, "Chromatic Steel Chassis", "Essence Turbine", "Chromatic Steel Legs", "Essence Barrier", "Silk Vents", "Chromatic Calibrator");
        names(experimental, "Suspension Chassis", "Fusion Core", "Suspension Rig", "Void Bender", "Liquid Painite Cycler", "Quantum Wutodite Sensor");
        names(armored, "Armored Chassis", "Echoreactive Thermoisotope Generator", "Armored Legs", "Impact Shield", "Heat Sink", "Vaulterite Detector");
        names(echosteel, "Echosteel Chassis", "Echofluid Reactor", "Echosteel Legs", "Echoflare Deflector", "Echo Evaporator", "Echolocator");
        table.SERIES.put(ExtractorPartsConfig.SERIES_WOODEN, wooden);
        table.SERIES.put(ExtractorPartsConfig.SERIES_CHROMATIC, chromatic);
        table.SERIES.put(ExtractorPartsConfig.SERIES_EXPERIMENTAL, experimental);
        table.SERIES.put(ExtractorPartsConfig.SERIES_ARMORED, armored);
        table.SERIES.put(ExtractorPartsConfig.SERIES_ECHOSTEEL, echosteel);

        table.SERIES_BY_RARITY.put("scrappy", new ArrayList<>(List.of(ExtractorPartsConfig.SERIES_WOODEN, ExtractorPartsConfig.SERIES_CHROMATIC)));
        table.SERIES_BY_RARITY.put("common", new ArrayList<>(List.of(ExtractorPartsConfig.SERIES_WOODEN, ExtractorPartsConfig.SERIES_CHROMATIC, ExtractorPartsConfig.SERIES_ARMORED)));
        table.SERIES_BY_RARITY.put("rare", new ArrayList<>(List.of(ExtractorPartsConfig.SERIES_WOODEN, ExtractorPartsConfig.SERIES_CHROMATIC, ExtractorPartsConfig.SERIES_ARMORED, ExtractorPartsConfig.SERIES_EXPERIMENTAL)));
        table.SERIES_BY_RARITY.put("epic", new ArrayList<>(List.of(ExtractorPartsConfig.SERIES_WOODEN, ExtractorPartsConfig.SERIES_CHROMATIC, ExtractorPartsConfig.SERIES_ARMORED, ExtractorPartsConfig.SERIES_EXPERIMENTAL, ExtractorPartsConfig.SERIES_ECHOSTEEL)));
        table.SERIES_BY_RARITY.put("omega", new ArrayList<>(List.of(ExtractorPartsConfig.SERIES_WOODEN, ExtractorPartsConfig.SERIES_CHROMATIC, ExtractorPartsConfig.SERIES_ARMORED, ExtractorPartsConfig.SERIES_EXPERIMENTAL, ExtractorPartsConfig.SERIES_ECHOSTEEL)));

        table.RARITY_DROP_WEIGHTS.put("scrappy", 45);
        table.RARITY_DROP_WEIGHTS.put("common", 25);
        table.RARITY_DROP_WEIGHTS.put("rare", 15);
        table.RARITY_DROP_WEIGHTS.put("epic", 10);
        table.RARITY_DROP_WEIGHTS.put("omega", 5);

        LinkedHashMap<String, PartRollEntry> chassis = new LinkedHashMap<>();
        chassis.put(ExtractorPartsConfig.SERIES_WOODEN, new PartRollEntry().stat(ExtractorStat.HEALTH, 3000, 5000).stat(ExtractorStat.ARMOR, 2, 5).stat(ExtractorStat.DRAIN, 1, 2).stat(ExtractorStat.DURABILITY, 40000, 50000));
        chassis.put(ExtractorPartsConfig.SERIES_CHROMATIC, new PartRollEntry().stat(ExtractorStat.HEALTH, 3500, 6000).stat(ExtractorStat.ARMOR, 3, 8).stat(ExtractorStat.DRAIN, 2, 3).stat(ExtractorStat.DURABILITY, 45000, 55000));
        chassis.put(ExtractorPartsConfig.SERIES_EXPERIMENTAL, new PartRollEntry().stat(ExtractorStat.HEALTH, 2000, 3500).stat(ExtractorStat.ARMOR, 1, 3).stat(ExtractorStat.DRAIN, 4, 6).stat(ExtractorStat.DURABILITY, 30000, 40000)
                .special(ExtractorStat.QUANTITY, 10).special(ExtractorStat.RARITY, 10));
        chassis.put(ExtractorPartsConfig.SERIES_ARMORED, new PartRollEntry().stat(ExtractorStat.HEALTH, 4000, 8000).stat(ExtractorStat.ARMOR, 5, 10).stat(ExtractorStat.DRAIN, 3, 4).stat(ExtractorStat.DURABILITY, 50000, 60000));
        chassis.put(ExtractorPartsConfig.SERIES_ECHOSTEEL, new PartRollEntry().stat(ExtractorStat.HEALTH, 3500, 6000).stat(ExtractorStat.ARMOR, 3, 8).stat(ExtractorStat.DRAIN, 2, 3).stat(ExtractorStat.DURABILITY, 60000, 70000));
        table.PARTS.put(ExtractorPartType.CHASSIS.getId(), chassis);

        LinkedHashMap<String, PartRollEntry> cores = new LinkedHashMap<>();
        cores.put(ExtractorPartsConfig.SERIES_WOODEN, new PartRollEntry().stat(ExtractorStat.POWER, 30, 40).stat(ExtractorStat.SPEED, 40, 60).stat(ExtractorStat.DRAIN, 4, 6).stat(ExtractorStat.ENERGY_CAPACITY, 700, 900).stat(ExtractorStat.ENERGY_PER_CELL, 15, 30).stat(ExtractorStat.DURABILITY, 10000, 15000));
        cores.put(ExtractorPartsConfig.SERIES_CHROMATIC, new PartRollEntry().stat(ExtractorStat.POWER, 35, 50).stat(ExtractorStat.SPEED, 50, 75).stat(ExtractorStat.DRAIN, 8, 10).stat(ExtractorStat.ENERGY_CAPACITY, 900, 1100).stat(ExtractorStat.ENERGY_PER_CELL, 75, 100).stat(ExtractorStat.DURABILITY, 15000, 20000));
        cores.put(ExtractorPartsConfig.SERIES_EXPERIMENTAL, new PartRollEntry().stat(ExtractorStat.POWER, 50, 65).stat(ExtractorStat.SPEED, 55, 80).stat(ExtractorStat.DRAIN, 12, 15).stat(ExtractorStat.ENERGY_CAPACITY, 900, 1100).stat(ExtractorStat.ENERGY_PER_CELL, 50, 75).stat(ExtractorStat.DURABILITY, 8000, 10000));
        cores.put(ExtractorPartsConfig.SERIES_ARMORED, new PartRollEntry().stat(ExtractorStat.POWER, 35, 50).stat(ExtractorStat.SPEED, 50, 75).stat(ExtractorStat.DRAIN, 9, 11).stat(ExtractorStat.ENERGY_CAPACITY, 1400, 1800).stat(ExtractorStat.ENERGY_PER_CELL, 120, 150).stat(ExtractorStat.DURABILITY, 25000, 30000));
        cores.put(ExtractorPartsConfig.SERIES_ECHOSTEEL, new PartRollEntry().stat(ExtractorStat.POWER, 45, 55).stat(ExtractorStat.SPEED, 50, 75).stat(ExtractorStat.DRAIN, 10, 12).stat(ExtractorStat.ENERGY_CAPACITY, 1200, 1500).stat(ExtractorStat.ENERGY_PER_CELL, 100, 130).stat(ExtractorStat.DURABILITY, 25000, 30000));
        table.PARTS.put(ExtractorPartType.POWER_CORE.getId(), cores);

        LinkedHashMap<String, PartRollEntry> drills = new LinkedHashMap<>();
        drills.put("standard", new PartRollEntry().stat(ExtractorStat.POWER, 10, 15).stat(ExtractorStat.SPEED, 30, 60).stat(ExtractorStat.DRAIN, 5, 7).mult(ExtractorStat.DRAIN_MULT, 1.0F, 1.0F).stat(ExtractorStat.DURABILITY, 10000, 15000));
        drills.put("laser", new PartRollEntry().stat(ExtractorStat.POWER, 5, 10).stat(ExtractorStat.SPEED, 35, 70).stat(ExtractorStat.DRAIN, 6, 8).mult(ExtractorStat.DRAIN_MULT, 1.1F, 1.1F).stat(ExtractorStat.DURABILITY, 10000, 15000));
        drills.put("greed_infuser", new PartRollEntry().stat(ExtractorStat.POWER, 10, 15).stat(ExtractorStat.SPEED, 30, 60).stat(ExtractorStat.DRAIN, 5, 7).mult(ExtractorStat.DRAIN_MULT, 1.0F, 1.0F).stat(ExtractorStat.DURABILITY, 10000, 15000));
        drills.put("archeology", new PartRollEntry().stat(ExtractorStat.POWER, 5, 10).stat(ExtractorStat.SPEED, 35, 70).stat(ExtractorStat.DRAIN, 6, 8).mult(ExtractorStat.DRAIN_MULT, 1.1F, 1.1F).stat(ExtractorStat.DURABILITY, 10000, 15000));
        drills.put("excavator", new PartRollEntry().stat(ExtractorStat.POWER, 5, 10).stat(ExtractorStat.SPEED, 30, 60).stat(ExtractorStat.DRAIN, 6, 8).mult(ExtractorStat.DRAIN_MULT, 1.05F, 1.05F).stat(ExtractorStat.DURABILITY, 10000, 15000));
        drills.put("refiner", new PartRollEntry().stat(ExtractorStat.POWER, 10, 15).stat(ExtractorStat.SPEED, 30, 60).stat(ExtractorStat.DRAIN, 6, 8).mult(ExtractorStat.DRAIN_MULT, 1.1F, 1.1F).stat(ExtractorStat.DURABILITY, 10000, 15000));
        drills.put("heavy", new PartRollEntry().stat(ExtractorStat.POWER, 10, 15).stat(ExtractorStat.SPEED, 35, 70).stat(ExtractorStat.DRAIN, 7, 9).mult(ExtractorStat.DRAIN_MULT, 1.15F, 1.15F).stat(ExtractorStat.DURABILITY, 10000, 15000));
        drills.put("juicer", new PartRollEntry().stat(ExtractorStat.POWER, 5, 10).stat(ExtractorStat.SPEED, 40, 80).stat(ExtractorStat.DRAIN, 6, 8).mult(ExtractorStat.DRAIN_MULT, 1.1F, 1.1F).stat(ExtractorStat.DURABILITY, 10000, 15000));
        drills.put("glue_derrick", new PartRollEntry().stat(ExtractorStat.POWER, 15, 20).stat(ExtractorStat.SPEED, 25, 50).stat(ExtractorStat.DRAIN, 5, 7).mult(ExtractorStat.DRAIN_MULT, 1.05F, 1.05F).stat(ExtractorStat.DURABILITY, 10000, 15000));
        drills.put("crystal", new PartRollEntry().stat(ExtractorStat.POWER, 10, 15).stat(ExtractorStat.SPEED, 30, 60).stat(ExtractorStat.DRAIN, 5, 7).mult(ExtractorStat.DRAIN_MULT, 1.05F, 1.05F).stat(ExtractorStat.DURABILITY, 10000, 15000));
        drills.put("containment", new PartRollEntry().stat(ExtractorStat.POWER, 10, 15).stat(ExtractorStat.SPEED, 35, 75).stat(ExtractorStat.DRAIN, 6, 8).mult(ExtractorStat.DRAIN_MULT, 1.1F, 1.1F).stat(ExtractorStat.DURABILITY, 10000, 15000));
        table.PARTS.put(ExtractorPartType.DRILL.getId(), drills);

        LinkedHashMap<String, PartRollEntry> legs = new LinkedHashMap<>();
        legs.put(ExtractorPartsConfig.SERIES_WOODEN, new PartRollEntry().stat(ExtractorStat.HEALTH, 1000, 2000).stat(ExtractorStat.ARMOR, 0, 2).stat(ExtractorStat.DRAIN, -1, 0).stat(ExtractorStat.DURABILITY, 15000, 20000));
        legs.put(ExtractorPartsConfig.SERIES_CHROMATIC, new PartRollEntry().stat(ExtractorStat.HEALTH, 1500, 2500).stat(ExtractorStat.ARMOR, 1, 3).stat(ExtractorStat.DRAIN, 1, 2).stat(ExtractorStat.DURABILITY, 20000, 25000));
        legs.put(ExtractorPartsConfig.SERIES_EXPERIMENTAL, new PartRollEntry().stat(ExtractorStat.HEALTH, 500, 1000).stat(ExtractorStat.ARMOR, 0, 1).stat(ExtractorStat.DRAIN, 4, 6).stat(ExtractorStat.DURABILITY, 10000, 15000)
                .special(ExtractorStat.QUANTITY, 10).special(ExtractorStat.RARITY, 10));
        legs.put(ExtractorPartsConfig.SERIES_ARMORED, new PartRollEntry().stat(ExtractorStat.HEALTH, 2000, 3000).stat(ExtractorStat.ARMOR, 3, 5).stat(ExtractorStat.DRAIN, 2, 3).stat(ExtractorStat.DURABILITY, 25000, 30000));
        legs.put(ExtractorPartsConfig.SERIES_ECHOSTEEL, new PartRollEntry().stat(ExtractorStat.HEALTH, 1250, 1750).stat(ExtractorStat.ARMOR, 1, 3).stat(ExtractorStat.DRAIN, 1, 2).stat(ExtractorStat.DURABILITY, 30000, 35000));
        table.PARTS.put(ExtractorPartType.LEGS.getId(), legs);

        LinkedHashMap<String, PartRollEntry> shields = new LinkedHashMap<>();
        shields.put(ExtractorPartsConfig.SERIES_WOODEN, new PartRollEntry().stat(ExtractorStat.SHIELD, 600, 1200).stat(ExtractorStat.SHIELD_TOUGHNESS, 0, 1).stat(ExtractorStat.SHIELD_REGEN, 60, 80).stat(ExtractorStat.DRAIN, 3, 5).stat(ExtractorStat.DURABILITY, 10000, 15000));
        shields.put(ExtractorPartsConfig.SERIES_CHROMATIC, new PartRollEntry().stat(ExtractorStat.SHIELD, 750, 1500).stat(ExtractorStat.SHIELD_TOUGHNESS, 0, 1).stat(ExtractorStat.SHIELD_REGEN, 70, 90).stat(ExtractorStat.DRAIN, 4, 6).stat(ExtractorStat.DURABILITY, 12000, 17000));
        shields.put(ExtractorPartsConfig.SERIES_EXPERIMENTAL, new PartRollEntry().stat(ExtractorStat.SHIELD, 1500, 2500).stat(ExtractorStat.SHIELD_TOUGHNESS, 2, 3).stat(ExtractorStat.SHIELD_REGEN, 125, 175).stat(ExtractorStat.DRAIN, 8, 12).stat(ExtractorStat.DURABILITY, 8000, 10000));
        shields.put(ExtractorPartsConfig.SERIES_ARMORED, new PartRollEntry().stat(ExtractorStat.SHIELD, 1000, 1750).stat(ExtractorStat.SHIELD_TOUGHNESS, 1, 2).stat(ExtractorStat.SHIELD_REGEN, 60, 80).stat(ExtractorStat.DRAIN, 5, 7).stat(ExtractorStat.DURABILITY, 15000, 20000));
        shields.put(ExtractorPartsConfig.SERIES_ECHOSTEEL, new PartRollEntry().stat(ExtractorStat.SHIELD, 800, 1600).stat(ExtractorStat.SHIELD_TOUGHNESS, 1, 2).stat(ExtractorStat.SHIELD_REGEN, 75, 100).stat(ExtractorStat.DRAIN, 5, 7).stat(ExtractorStat.DURABILITY, 16000, 20000));
        table.PARTS.put(ExtractorPartType.SHIELD_ARRAY.getId(), shields);

        LinkedHashMap<String, PartRollEntry> radiators = new LinkedHashMap<>();
        radiators.put(ExtractorPartsConfig.SERIES_WOODEN, new PartRollEntry().stat(ExtractorStat.DRAIN, -2, -1).stat(ExtractorStat.POWER, 5, 10).stat(ExtractorStat.SPEED, 3, 5).mult(ExtractorStat.DRAIN_MULT, 0.95F, 0.95F).stat(ExtractorStat.DURABILITY, 5000, 10000));
        radiators.put(ExtractorPartsConfig.SERIES_CHROMATIC, new PartRollEntry().stat(ExtractorStat.DRAIN, -3, -2).stat(ExtractorStat.POWER, 4, 8).stat(ExtractorStat.SPEED, 5, 10).mult(ExtractorStat.DRAIN_MULT, 0.95F, 0.95F).stat(ExtractorStat.DURABILITY, 8000, 16000));
        radiators.put(ExtractorPartsConfig.SERIES_EXPERIMENTAL, new PartRollEntry().stat(ExtractorStat.DRAIN, -8, -6).stat(ExtractorStat.POWER, 5, 10).stat(ExtractorStat.SPEED, 0, 1).mult(ExtractorStat.DRAIN_MULT, 1.0F, 1.0F).mult(ExtractorStat.DURABILITY_DRAIN_MULT, 1.5F, 1.5F).stat(ExtractorStat.DURABILITY, 3000, 6000));
        radiators.put(ExtractorPartsConfig.SERIES_ARMORED, new PartRollEntry().stat(ExtractorStat.DRAIN, -1, 0).stat(ExtractorStat.POWER, 3, 6).stat(ExtractorStat.SPEED, 3, 5).mult(ExtractorStat.DRAIN_MULT, 0.85F, 0.85F).stat(ExtractorStat.DURABILITY, 10000, 20000));
        radiators.put(ExtractorPartsConfig.SERIES_ECHOSTEEL, new PartRollEntry().stat(ExtractorStat.DRAIN, -4, -2).stat(ExtractorStat.POWER, 4, 8).stat(ExtractorStat.SPEED, 4, 6).mult(ExtractorStat.DRAIN_MULT, 0.9F, 0.9F).stat(ExtractorStat.DURABILITY, 12000, 18000));
        table.PARTS.put(ExtractorPartType.RADIATOR.getId(), radiators);

        LinkedHashMap<String, PartRollEntry> scanners = new LinkedHashMap<>();
        scanners.put(ExtractorPartsConfig.SERIES_WOODEN, new PartRollEntry().stat(ExtractorStat.SPEED, 10, 15).stat(ExtractorStat.QUANTITY, 10, 15).stat(ExtractorStat.RARITY, 10, 25).stat(ExtractorStat.DRAIN, 1, 2).stat(ExtractorStat.DURABILITY, 5000, 10000));
        scanners.put(ExtractorPartsConfig.SERIES_CHROMATIC, new PartRollEntry().stat(ExtractorStat.SPEED, 15, 20).stat(ExtractorStat.QUANTITY, 15, 25).stat(ExtractorStat.RARITY, 10, 15).stat(ExtractorStat.DRAIN, 1, 2).stat(ExtractorStat.DURABILITY, 8000, 16000));
        scanners.put(ExtractorPartsConfig.SERIES_EXPERIMENTAL, new PartRollEntry().stat(ExtractorStat.SPEED, 20, 25).stat(ExtractorStat.QUANTITY, 30, 40).stat(ExtractorStat.RARITY, 15, 25).stat(ExtractorStat.DRAIN, 4, 6).stat(ExtractorStat.DURABILITY, 3000, 6000));
        scanners.put(ExtractorPartsConfig.SERIES_ARMORED, new PartRollEntry().stat(ExtractorStat.SPEED, 0, 5).stat(ExtractorStat.QUANTITY, 10, 15).stat(ExtractorStat.RARITY, 30, 40).stat(ExtractorStat.DRAIN, 2, 3).stat(ExtractorStat.DURABILITY, 10000, 20000));
        scanners.put(ExtractorPartsConfig.SERIES_ECHOSTEEL, new PartRollEntry().stat(ExtractorStat.SPEED, 15, 20).stat(ExtractorStat.QUANTITY, 20, 30).stat(ExtractorStat.RARITY, 20, 30).stat(ExtractorStat.DRAIN, 2, 3).stat(ExtractorStat.DURABILITY, 12000, 18000));
        table.PARTS.put(ExtractorPartType.SCANNER.getId(), scanners);

        table.DRILL_CLASSES.put("standard", new DrillClassEntry("Standard", "arcane_essence", "drill_standard", "scrappy", 0.0F));
        table.DRILL_CLASSES.put("laser", new DrillClassEntry("Laser", "maps", "drill_laser", "scrappy", 0.0F));
        table.DRILL_CLASSES.put("greed_infuser", new DrillClassEntry("Greed Infuser", "greed_card_upgrade", "drill_greed", "scrappy", 1.46F));
        table.DRILL_CLASSES.put("archeology", new DrillClassEntry("Archeology", "mythic_charms", "drill_archeology", "scrappy", 2.00F));
        table.DRILL_CLASSES.put("excavator", new DrillClassEntry("Excavator", "ancient_uniques", "drill_excavator", "scrappy", 4.00F));
        table.DRILL_CLASSES.put("refiner", new DrillClassEntry("Refiner", "power_sealing", "drill_refiner", "scrappy", 5.10F));
        table.DRILL_CLASSES.put("heavy", new DrillClassEntry("Heavy", "shiny_card_packs", "drill_heavy", "scrappy", 5.10F));
        table.DRILL_CLASSES.put("juicer", new DrillClassEntry("Juicer", "card_juice", "drill_juicer", "scrappy", 10.00F));
        table.DRILL_CLASSES.put("glue_derrick", new DrillClassEntry("Glue Derrick", "prismatic_glue", "drill_glue", "scrappy", 9.34F));
        table.DRILL_CLASSES.put("crystal", new DrillClassEntry("Crystal", "ancient_crystals", "drill_crystal", "scrappy", 21.46F));
        table.DRILL_CLASSES.put("containment", new DrillClassEntry("Containment", "nullite", "drill_containment", "scrappy", 32.37F));

        table.ATTACHMENTS.put("auxiliary_radiator", attachment("Auxiliary Radiator", "aux_radiator", -2, -1, 20000, 30000,
                new EffectEntry(ExtractorStat.SPEED, 5, 10, true), new EffectEntry(ExtractorStat.POWER, 2, 5, true)));
        table.ATTACHMENTS.put("secondary_shield_charger", attachment("Secondary Shield Charger", "shield_charger", 2, 4, 20000, 30000,
                new EffectEntry(ExtractorStat.SHIELD, 10, 15, true), new EffectEntry(ExtractorStat.SHIELD_REGEN, 20, 30, true)));
        table.ATTACHMENTS.put("driver_override", attachment("Driver Override", "driver_override", 8, 12, 10000, 15000,
                new EffectEntry(ExtractorStat.SPEED, 20, 30, true), new EffectEntry(ExtractorStat.POWER, -10, -5, true)));
        table.ATTACHMENTS.put("extra_plating", attachment("Extra Plating", "extra_plating", 1, 2, 20000, 30000,
                new EffectEntry(ExtractorStat.HEALTH, 10, 15, true), new EffectEntry(ExtractorStat.ARMOR, 10, 15, true)));
        table.ATTACHMENTS.put("fuel_injector", attachment("Fuel Injector", "fuel_injector", -1, 0, 20000, 30000,
                new EffectEntry(ExtractorStat.ENERGY_PER_CELL, 20, 30, true), new EffectEntry(ExtractorStat.DRAIN_MULT, 0.97F, 0.97F, false)));
        table.ATTACHMENTS.put("battery_array", attachment("Battery Array", "battery_array", -1, 0, 20000, 30000,
                new EffectEntry(ExtractorStat.ENERGY_CAPACITY, 20, 30, true), new EffectEntry(ExtractorStat.POWER, 2, 5, true)));
        table.ATTACHMENTS.put("ore_cleaner", attachment("Ore Cleaner", "ore_cleaner", 2, 4, 20000, 30000,
                new EffectEntry(ExtractorStat.QUANTITY, 15, 20, true), new EffectEntry(ExtractorStat.RARITY, 15, 20, true)));
        table.ATTACHMENTS.put("defense_turret", attachment("Defense Turret", "defense_turret", 4, 8, 20000, 30000,
                new EffectEntry(ExtractorStat.TURRET, 1, 1, false)));
        table.ATTACHMENTS.put("system_reinforcement", attachment("System Reinforcement", "system_reinforcement", 3, 5, 40000, 50000,
                new EffectEntry(ExtractorStat.DURABILITY_DRAIN_MULT, 0.85F, 0.90F, false)));
        table.ATTACHMENTS.put("regenerative_nanobots", attachment("Regenerative Nanobots", "nanobots", 4, 8, 20000, 30000,
                new EffectEntry(ExtractorStat.HEALTH_REGEN, 3, 5, false), new EffectEntry(ExtractorStat.HEALTH, 5, 10, true)));

        table.AFFIX_POOLS.put("hardware_prefix", new ArrayList<>(List.of(
                new AffixEntry("reinforced", ExtractorStat.HEALTH, 2, 5, true),
                new AffixEntry("plated", ExtractorStat.ARMOR, 1, 2, false),
                new AffixEntry("sturdy", ExtractorStat.HEALTH, 200, 400, false),
                new AffixEntry("charged", ExtractorStat.POWER, 1, 3, false),
                new AffixEntry("lasting", ExtractorStat.DURABILITY, 4000, 8000, false))));
        table.AFFIX_POOLS.put("hardware_suffix", new ArrayList<>(List.of(
                new AffixEntry("of_recharging", ExtractorStat.ENERGY_PER_CELL, 20, 40, false),
                new AffixEntry("of_warding", ExtractorStat.EFFECT_RESISTANCE, 10, 20, true),
                new AffixEntry("of_defiance", ExtractorStat.ELITE_RESISTANCE, 5, 10, true),
                new AffixEntry("of_surging", ExtractorStat.POWER, 1, 2, true))));
        table.AFFIX_POOLS.put("mechanical_prefix", new ArrayList<>(List.of(
                new AffixEntry("driven", ExtractorStat.POWER, 2, 4, false),
                new AffixEntry("swift", ExtractorStat.SPEED, 5, 10, false),
                new AffixEntry("hardened", ExtractorStat.DURABILITY, 3000, 6000, false),
                new AffixEntry("conductive", ExtractorStat.ENERGY_PER_CELL, 30, 60, false),
                new AffixEntry("shielded", ExtractorStat.SHIELD, 5, 10, true))));
        table.AFFIX_POOLS.put("mechanical_suffix", new ArrayList<>(List.of(
                new AffixEntry("of_plenty", ExtractorStat.QUANTITY, 5, 10, false),
                new AffixEntry("of_fortune", ExtractorStat.RARITY, 5, 10, false),
                new AffixEntry("of_mending", ExtractorStat.HEALING_EFFICIENCY, 10, 15, true),
                new AffixEntry("of_recovery", ExtractorStat.SHIELD_REGEN, 5, 10, true))));

        table.AFFIX_COUNT.put("scrappy", 0);
        table.AFFIX_COUNT.put("common", 1);
        table.AFFIX_COUNT.put("rare", 1);
        table.AFFIX_COUNT.put("epic", 2);
        table.AFFIX_COUNT.put("omega", 2);
        this.table("extractor_parts", table);
    }

    private static ExtractorPartsConfig.QualityEntry quality() {
        ExtractorPartsConfig.QualityEntry quality = new ExtractorPartsConfig.QualityEntry();
        quality.centerA = 2.16F;
        quality.centerB = 0.85F;
        quality.centerIrScale = 200.0F;
        quality.kneeIr = 3000.0F;
        quality.kneeLogK = 21.0F;
        quality.widthBase = 0.5F;
        quality.widthSlope = 0.15F;
        quality.costExponent = 1.6F;
        quality.costTailRate = 1.12F;
        quality.rollNoise = 0.0F;
        quality.rarityMult.put("scrappy", 1.00F);
        quality.rarityMult.put("common", 1.03F);
        quality.rarityMult.put("rare", 1.07F);
        quality.rarityMult.put("epic", 1.12F);
        quality.rarityMult.put("omega", 1.18F);
        quality.rarityFlat.put("scrappy", 0.0F);
        quality.rarityFlat.put("common", 0.0F);
        quality.rarityFlat.put("rare", 1.0F);
        quality.rarityFlat.put("epic", 1.0F);
        quality.rarityFlat.put("omega", 2.0F);
        quality.rarityWidth.put("scrappy", 1.0F);
        quality.rarityWidth.put("common", 1.1F);
        quality.rarityWidth.put("rare", 1.25F);
        quality.rarityWidth.put("epic", 1.4F);
        quality.rarityWidth.put("omega", 1.6F);
        quality.buckets.add(new BucketEntry(3, "Early"));
        quality.buckets.add(new BucketEntry(8, "Mid"));
        quality.buckets.add(new BucketEntry(20, "Late"));
        quality.buckets.add(new BucketEntry(Integer.MAX_VALUE, "Endgame"));
        return quality;
    }

    private static ExtractorPartsConfig.ScalesEntry scales() {
        ExtractorPartsConfig.ScalesEntry scales = new ExtractorPartsConfig.ScalesEntry();
        scales.geometricTailQuality = 28.0F;
        scales.geometricTailSlope = 0.04F;
        scales.linear.put(ExtractorStat.POWER.getId(), 0.25F);
        scales.linear.put(ExtractorStat.SPEED.getId(), 0.12F);
        scales.linear.put(ExtractorStat.ENERGY_CAPACITY.getId(), 0.15F);
        scales.linear.put(ExtractorStat.DURABILITY.getId(), 0.15F);
        scales.linear.put(ExtractorStat.ARMOR.getId(), 0.10F);
        scales.linear.put(ExtractorStat.SHIELD_TOUGHNESS.getId(), 0.10F);
        scales.linear.put(ExtractorStat.QUANTITY.getId(), 0.10F);
        scales.linear.put(ExtractorStat.RARITY.getId(), 0.10F);
        scales.linear.put(ExtractorStat.DRAIN.getId(), 0.10F);
        scales.linear.put(ExtractorStat.ENERGY_PER_CELL.getId(), 0.10F);
        scales.linear.put(ExtractorStat.HEALTH_REGEN.getId(), 0.15F);
        scales.geometric.put(ExtractorStat.HEALTH.getId(), 0.15F);
        scales.geometric.put(ExtractorStat.SHIELD.getId(), 0.15F);
        scales.geometric.put(ExtractorStat.SHIELD_REGEN.getId(), 0.15F);
        return scales;
    }

    private static ExtractorPartsConfig.SlotsEntry slots() {
        ExtractorPartsConfig.SlotsEntry slots = new ExtractorPartsConfig.SlotsEntry();
        slots.omegaCoreBonus = 1;
        slots.max = 3;
        slots.chassis.put("scrappy", 0);
        slots.chassis.put("common", 1);
        slots.chassis.put("rare", 1);
        slots.chassis.put("epic", 2);
        slots.chassis.put("omega", 2);
        return slots;
    }

    /** An attachment that does not stack: one per rig. */
    private static AttachmentEntry attachment(String name, String rigBone, float drainMin, float drainMax, float duraMin, float duraMax, EffectEntry... effects) {
        AttachmentEntry attachment = new AttachmentEntry(name, rigBone, drainMin, drainMax, duraMin, duraMax, effects);
        attachment.stackable = false;
        return attachment;
    }

    private static void names(ExtractorPartsConfig.SeriesEntry series, String chassis, String core, String legs, String shield, String radiator, String scanner) {
        series.partNames.put(ExtractorPartType.CHASSIS.getId(), chassis);
        series.partNames.put(ExtractorPartType.POWER_CORE.getId(), core);
        series.partNames.put(ExtractorPartType.LEGS.getId(), legs);
        series.partNames.put(ExtractorPartType.SHIELD_ARRAY.getId(), shield);
        series.partNames.put(ExtractorPartType.RADIATOR.getId(), radiator);
        series.partNames.put(ExtractorPartType.SCANNER.getId(), scanner);
    }
}
