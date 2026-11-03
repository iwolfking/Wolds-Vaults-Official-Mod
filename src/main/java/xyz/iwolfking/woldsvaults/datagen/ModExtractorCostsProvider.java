package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.woldsvaults.config.ExtractorCostsConfig;
import xyz.iwolfking.woldsvaults.config.ExtractorCostsConfig.ComponentEntry;
import xyz.iwolfking.woldsvaults.config.ExtractorCostsConfig.Ingredient;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractWoldsTableProvider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Extractor part costs. A row costs {@code floor(roll * scale(Q) * rarity)}, where the roll is uniform in the row's
 * range and {@code scale} follows the curve: {@code (1 + Q/4)^K} with {@code K} putting Q80 at 500x, accelerating past
 * Q60 for main rows; Fabricator components scale at a reduced rate, and gated rows (absent below their gate) scale
 * from their gate at half rate. A part's recipe is its series' base material at the part type's share, the series'
 * signature ingredients for that part type, and the part type's shared ingredients. Drills add a housing and an apex
 * row around their class recipe; attachments a housing and two POG. Vault Fabricator components have fixed recipes.
 */
public class ModExtractorCostsProvider extends AbstractWoldsTableProvider {
    private static final double K = Math.log(500.0D) / Math.log(21.0D);
    private static final double GATE_ADV = 15.0D;
    private static final double GATE_APEX = 35.0D;
    private static final double RATE_GATED = 0.5D;
    private static final double RATE_FABRICATOR = 0.66D;
    private static final double ACCEL_Q = 60.0D;
    private static final double ACCEL_FACTOR = 1.5D;

    private static final Map<String, String> ITEMS = new LinkedHashMap<>();
    private static final Map<String, String> FLUIDS = new LinkedHashMap<>();
    private static final Map<String, Component> COMPONENTS = new LinkedHashMap<>();
    private static final List<String> PART_TYPES = List.of("chassis", "legs", "power_core", "shield_array", "radiator", "scanner");
    private static final Map<String, Double> SHARE = new LinkedHashMap<>();
    private static final Map<String, List<Row>> BASE = new LinkedHashMap<>();
    private static final Map<String, Row> BASE_OVERRIDE = new LinkedHashMap<>();
    private static final Map<String, Map<String, List<Row>>> SIGNATURE = new LinkedHashMap<>();
    private static final Map<String, List<Row>> UNIVERSAL = new LinkedHashMap<>();
    private static final Map<String, Row> UNIVERSAL_OVERRIDE = new LinkedHashMap<>();
    private static final Row DRILL_HOUSING = row("chromatic_steel", 12, 15);
    private static final Row DRILL_APEX = gated("prismatic_glue", 50, 50, GATE_APEX);
    private static final Map<String, List<Row>> DRILLS = new LinkedHashMap<>();
    private static final Row ATTACHMENT_HOUSING = row("chromatic_steel", 8, 12);
    private static final Row ATTACHMENT_POG = row("pog", 2, 2);
    private static final Map<String, List<Row>> ATTACHMENTS = new LinkedHashMap<>();

    static {
        items("driftwood", "the_vault:driftwood", "infused_driftwood", "woldsvaults:infused_driftwood",
                "wooden_chunk", "the_vault:wooden_chunk", "living_rock", "the_vault:living_rock",
                "chromatic_steel", "the_vault:chromatic_steel_ingot", "chromatic_iron", "the_vault:chromatic_iron_ingot",
                "chromatic_gold", "woldsvaults:chromatic_gold_ingot", "vault_ingot", "the_vault:vault_ingot",
                "vault_plating", "the_vault:vault_plating", "vaulterite", "the_vault:vaulterite_ingot",
                "vault_alloy", "the_vault:vault_alloy", "bcs", "the_vault:black_chromatic_steel_ingot",
                "carbon", "the_vault:carbon", "carbon_nugget", "the_vault:carbon_nugget",
                "wutodie", "the_vault:gem_wutodie", "vault_diamond", "the_vault:vault_diamond", "echo", "the_vault:gem_echo",
                "perfect_painite", "the_vault:perfect_painite", "extraordinary_painite", "the_vault:extraordinary_painite",
                "larimar", "the_vault:gem_larimar", "perfect_larimar", "the_vault:perfect_larimar",
                "extraordinary_larimar", "the_vault:extraordinary_larimar",
                "benitoite", "the_vault:gem_benitoite", "perfect_benitoite", "the_vault:perfect_benitoite",
                "alexandrite", "the_vault:gem_alexandrite",
                "perfect_black_opal", "the_vault:perfect_black_opal", "extraordinary_black_opal", "the_vault:extraordinary_black_opal",
                "pog", "the_vault:gem_pog", "echo_pog", "the_vault:echo_pog", "omega_pog", "the_vault:omega_pog",
                "pogominium", "woldsvaults:pogominium_ingot", "vault_essence", "the_vault:vault_essence",
                "knowledge_shard", "the_vault:knowledge_star_shard", "knowledge_star", "the_vault:knowledge_star",
                "magic_silk", "the_vault:magic_silk", "soot", "the_vault:soot", "ornate_chain", "the_vault:ornate_chain",
                "mossy_bone", "the_vault:mossy_bone", "eternal_soul", "the_vault:eternal_soul",
                "catalyst_fragment", "the_vault:vault_catalyst_fragment",
                "nullite_fragment", "woldsvaults:nullite_fragment", "nullite_crystal", "woldsvaults:nullite_crystal",
                "smashed_cluster", "woldsvaults:smashed_vault_gem_cluster");
        items("sturdy_sheet", "create:sturdy_sheet", "andesite_alloy", "create:andesite_alloy",
                "copper_sheet", "create:copper_sheet", "fluid_pipe", "create:fluid_pipe", "encased_fan", "create:encased_fan",
                "mechanical_pump", "create:mechanical_pump",
                "rf_coil", "thermal:rf_coil", "invar", "thermal:invar_ingot", "signalum", "thermal:signalum_ingot",
                "cured_rubber", "thermal:cured_rubber",
                "silicon", "ae2:silicon", "logic_processor", "ae2:logic_processor",
                "calculation_processor", "ae2:calculation_processor", "engineering_processor", "ae2:engineering_processor",
                "formation_core", "ae2:formation_core", "annihilation_core", "ae2:annihilation_core", "singularity", "ae2:singularity",
                "capacitor_basic", "powah:capacitor_basic", "crystal_niotic", "powah:crystal_niotic",
                "snowball", "minecraft:snowball", "glass", "minecraft:glass", "spyglass", "minecraft:spyglass");
        items("blast_chamber", "woldsvaults:blast_chamber", "vpu", "woldsvaults:vault_processing_unit",
                "field_emitter", "woldsvaults:field_emitter", "heat_exchanger", "woldsvaults:heat_exchanger",
                "servo_actuator", "woldsvaults:servo_actuator", "spine", "woldsvaults:structural_spine",
                "containment_ring", "woldsvaults:containment_ring", "phase_lattice", "woldsvaults:phase_lattice",
                "cryo_exchanger", "woldsvaults:cryogenic_exchanger", "qpu", "woldsvaults:quantum_processing_unit");

        fluids("molten_painite", "woldsvaults:molten_painite", "molten_echo", "woldsvaults:molten_echo",
                "molten_larimar", "woldsvaults:molten_larimar", "coolant", "woldsvaults:coolant",
                "prismatic_glue", "woldsvaults:prismatic_glue", "destabilized_redstone", "thermal:redstone",
                "energized_glowstone", "thermal:glowstone", "water", "minecraft:water");

        component("blast_chamber", "basic", "", 0, 40_000, 100,
                grid("D P P P D", "P o c o P", "P c R c P", "P o c o P", "D P P P D"),
                legend("D", "vault_diamond", "P", "vault_plating", "c", "carbon", "o", "soot", "R", "rf_coil"));
        component("vpu", "basic", "destabilized_redstone", 250, 50_000, 100,
                grid(". D . D .", "s . L . s", "K s C s K", "s . E . s", ". . . . ."),
                legend("D", "vault_diamond", "K", "knowledge_shard", "s", "silicon", "L", "logic_processor",
                        "C", "calculation_processor", "E", "engineering_processor"));
        component("field_emitter", "basic", "energized_glowstone", 500, 60_000, 120,
                grid("e s e s e", "L D F D L", "e . V . e", "L P A . L", "e s e s e"),
                legend("s", "signalum", "e", "vault_essence", "L", "perfect_larimar", "D", "vault_diamond",
                        "P", "pog", "F", "formation_core", "A", "annihilation_core", "V", "vpu"));
        component("heat_exchanger", "basic", "water", 1000, 30_000, 100,
                grid("c f c f c", "f I k I f", "L k S k L", "S I k I S", "c f c S c"),
                legend("c", "copper_sheet", "f", "fluid_pipe", "I", "invar", "k", "chromatic_steel",
                        "L", "perfect_larimar", "S", "snowball"));
        component("servo_actuator", "basic", "", 0, 20_000, 100,
                grid("a k a k a", "k i . i k", "a . . . a", "k i . i k", "a k a k a"),
                legend("a", "andesite_alloy", "k", "ornate_chain", "i", "chromatic_iron"));
        component("spine", "advanced", "", 0, 200_000, 300,
                grid("A . A . A", ". c X c .", "A . D . A", ". c X c .", "A . A . A"),
                legend("A", "vault_alloy", "c", "carbon", "X", "servo_actuator", "D", "vault_diamond"));
        component("containment_ring", "advanced", "", 0, 400_000, 400,
                grid(". K . K .", ". n . n .", ". . B . .", ". n B n .", ". K . K ."),
                legend("B", "blast_chamber", "K", "bcs", "n", "crystal_niotic"));
        component("phase_lattice", "advanced", "", 0, 400_000, 400,
                grid(". g . g .", "o . F . o", ". . X . .", "o . F . o", ". g . g ."),
                legend("g", "chromatic_gold", "o", "perfect_black_opal", "F", "field_emitter", "X", "echo_pog"));
        component("cryo_exchanger", "advanced", "coolant", 250, 300_000, 300,
                grid(". A . A .", ". . H . .", "L P . P L", ". . H . .", ". A . A ."),
                legend("A", "vault_alloy", "H", "heat_exchanger", "L", "extraordinary_larimar", "P", "pog"));
        component("qpu", "advanced", "destabilized_redstone", 500, 500_000, 400,
                grid(". . . . .", ". V . V .", ". . X . .", ". V I V .", ". . . . ."),
                legend("V", "vpu", "X", "singularity", "I", "pogominium"));

        SHARE.put("chassis", 1.00D);
        SHARE.put("legs", 0.60D);
        SHARE.put("power_core", 0.35D);
        SHARE.put("shield_array", 0.25D);
        SHARE.put("radiator", 0.20D);
        SHARE.put("scanner", 0.15D);

        BASE.put("wooden", List.of(row("driftwood", 1500, 2000)));
        BASE.put("chromatic", List.of(row("chromatic_steel", 113, 150)));
        BASE.put("experimental", List.of(row("wutodie", 500, 700), row("vault_diamond", 600, 800)));
        BASE.put("armored", List.of(row("vault_plating", 400, 600), row("vaulterite", 90, 150)));
        BASE.put("echosteel", List.of(row("echo", 60, 100)));
        BASE_OVERRIDE.put(slot("scanner", "experimental", "wutodie"), row("wutodie", 180, 220));

        signature("chassis", "wooden", row("infused_driftwood", 32, 40), row("carbon_nugget", 200, 300));
        signature("chassis", "chromatic", row("chromatic_iron", 300, 400), row("alexandrite", 24, 32));
        signature("chassis", "experimental", row("perfect_painite", 56, 72), row("chromatic_gold", 12, 18));
        signature("chassis", "armored", row("carbon", 90, 114), row("vault_ingot", 16, 24));
        signature("chassis", "echosteel", row("chromatic_steel", 10, 14), row("vault_diamond", 120, 160));
        signature("legs", "wooden", row("living_rock", 300, 360));
        signature("legs", "chromatic", row("chromatic_gold", 24, 32));
        signature("legs", "experimental", row("magic_silk", 140, 170));
        signature("legs", "armored", row("carbon", 44, 52));
        signature("legs", "echosteel", row("chromatic_steel", 6, 9));
        signature("power_core", "wooden", row("wooden_chunk", 820, 1000));
        signature("power_core", "chromatic", row("vault_essence", 2000, 3000));
        signature("power_core", "experimental", row("molten_painite", 38_000, 48_000), row("molten_echo", 2_000, 3_000));
        signature("power_core", "armored", row("echo", 16, 24), row("coolant", 600, 800));
        signature("power_core", "echosteel", row("molten_echo", 15_000, 22_000));
        signature("shield_array", "wooden", row("perfect_larimar", 90, 110));
        signature("shield_array", "chromatic", row("vault_essence", 1200, 1800));
        signature("shield_array", "experimental", row("extraordinary_black_opal", 4, 8));
        signature("shield_array", "armored", row("extraordinary_painite", 3, 4));
        signature("shield_array", "echosteel", row("molten_echo", 6_000, 9_000), row("molten_larimar", 10_000, 14_000));
        signature("radiator", "wooden", row("chromatic_iron", 520, 680));
        signature("radiator", "chromatic", row("magic_silk", 500, 700));
        signature("radiator", "experimental", row("molten_painite", 14_000, 18_000));
        signature("radiator", "armored", row("chromatic_gold", 12, 18));
        signature("radiator", "echosteel", row("molten_echo", 2_500, 3_100));
        signature("scanner", "wooden", row("benitoite", 110, 140));
        signature("scanner", "chromatic", row("alexandrite", 140, 180));
        signature("scanner", "experimental", row("echo_pog", 1, 1));
        signature("scanner", "armored", row("vault_ingot", 3, 5));
        signature("scanner", "echosteel", row("knowledge_star", 3, 5));

        UNIVERSAL.put("chassis", List.of(row("servo_actuator", 1, 2),
                gated("spine", 6, 6, GATE_ADV), gated("prismatic_glue", 200, 200, GATE_APEX)));
        UNIVERSAL.put("legs", List.of(row("servo_actuator", 3, 4), row("cured_rubber", 16, 24),
                gated("spine", 4, 4, GATE_ADV), gated("prismatic_glue", 100, 100, GATE_APEX)));
        UNIVERSAL.put("power_core", List.of(row("blast_chamber", 3, 4), row("rf_coil", 10, 15),
                gated("containment_ring", 4, 4, GATE_ADV), gated("nullite_crystal", 2, 2, GATE_APEX)));
        UNIVERSAL.put("shield_array", List.of(row("field_emitter", 2, 3), row("capacitor_basic", 2, 3),
                gated("phase_lattice", 4, 4, GATE_ADV), gated("omega_pog", 4, 4, GATE_APEX)));
        UNIVERSAL.put("radiator", List.of(row("heat_exchanger", 3, 4), row("encased_fan", 2, 3),
                gated("cryo_exchanger", 3, 3, GATE_ADV), gated("prismatic_glue", 50, 50, GATE_APEX)));
        UNIVERSAL.put("scanner", List.of(row("vpu", 3, 4), row("spyglass", 1, 2),
                gated("qpu", 2, 2, GATE_ADV), gated("echo_pog", 10, 10, GATE_APEX)));
        UNIVERSAL_OVERRIDE.put(slot("scanner", "wooden", "spyglass"), row("glass", 10, 15));

        DRILLS.put("standard", List.of(row("vault_diamond", 22, 28), row("servo_actuator", 1, 2), row("knowledge_shard", 5, 10),
                gated("spine", 2, 2, GATE_ADV)));
        DRILLS.put("laser", List.of(row("perfect_benitoite", 44, 52), row("vpu", 1, 1), row("rf_coil", 3, 4),
                gated("qpu", 1, 1, GATE_ADV)));
        DRILLS.put("greed_infuser", List.of(row("chromatic_gold", 20, 24), row("field_emitter", 1, 1), row("vpu", 1, 1),
                gated("phase_lattice", 2, 2, GATE_ADV)));
        DRILLS.put("archeology", List.of(row("mossy_bone", 112, 144), row("magic_silk", 64, 80), row("servo_actuator", 1, 1),
                gated("qpu", 1, 1, GATE_ADV)));
        DRILLS.put("excavator", List.of(row("vault_diamond", 22, 28), row("servo_actuator", 2, 3), row("vault_ingot", 5, 8),
                gated("spine", 2, 2, GATE_ADV)));
        DRILLS.put("refiner", List.of(row("molten_painite", 3000, 5000), row("blast_chamber", 1, 1), row("heat_exchanger", 1, 1),
                gated("containment_ring", 2, 2, GATE_ADV)));
        DRILLS.put("heavy", List.of(row("vault_plating", 200, 240), row("blast_chamber", 1, 1), row("servo_actuator", 2, 2),
                gated("spine", 2, 2, GATE_ADV)));
        DRILLS.put("juicer", List.of(row("vault_essence", 500, 600), row("mechanical_pump", 4, 6), row("servo_actuator", 1, 1),
                gated("cryo_exchanger", 2, 2, GATE_ADV)));
        DRILLS.put("glue_derrick", List.of(row("ornate_chain", 100, 130), row("mechanical_pump", 3, 4), row("servo_actuator", 1, 1),
                gated("spine", 2, 2, GATE_ADV)));
        DRILLS.put("crystal", List.of(row("catalyst_fragment", 6, 10), row("vpu", 1, 1), row("field_emitter", 1, 1),
                gated("qpu", 1, 1, GATE_ADV)));
        DRILLS.put("containment", List.of(row("nullite_fragment", 2, 4), row("field_emitter", 2, 2),
                gated("containment_ring", 2, 2, GATE_ADV)));

        ATTACHMENTS.put("auxiliary_radiator", List.of(row("heat_exchanger", 2, 4), row("encased_fan", 2, 4), row("larimar", 192, 256),
                gated("cryo_exchanger", 2, 2, GATE_ADV)));
        ATTACHMENTS.put("secondary_shield_charger", List.of(row("field_emitter", 2, 2), row("capacitor_basic", 4, 6),
                row("vault_essence", 128, 192), gated("phase_lattice", 2, 2, GATE_ADV)));
        ATTACHMENTS.put("driver_override", List.of(row("blast_chamber", 2, 2), row("vpu", 2, 2), row("rf_coil", 4, 6),
                gated("qpu", 2, 2, GATE_ADV)));
        ATTACHMENTS.put("extra_plating", List.of(row("vault_plating", 192, 256), row("sturdy_sheet", 24, 32), row("carbon", 32, 48),
                gated("spine", 2, 2, GATE_ADV)));
        ATTACHMENTS.put("fuel_injector", List.of(row("blast_chamber", 2, 2), row("mechanical_pump", 4, 6),
                row("molten_painite", 8_000, 12_000), gated("containment_ring", 2, 2, GATE_ADV)));
        ATTACHMENTS.put("battery_array", List.of(row("capacitor_basic", 8, 12), row("rf_coil", 6, 8), row("vault_essence", 128, 192),
                gated("containment_ring", 2, 2, GATE_ADV)));
        ATTACHMENTS.put("ore_cleaner", List.of(row("encased_fan", 4, 6), row("vpu", 2, 2), row("smashed_cluster", 16, 24),
                gated("qpu", 2, 2, GATE_ADV)));
        ATTACHMENTS.put("defense_turret", List.of(row("servo_actuator", 2, 2), row("vpu", 2, 2), row("blast_chamber", 2, 2),
                gated("qpu", 2, 2, GATE_ADV)));
        ATTACHMENTS.put("system_reinforcement", List.of(row("vault_alloy", 4, 6), row("servo_actuator", 2, 2),
                row("sturdy_sheet", 24, 32), gated("spine", 2, 2, GATE_ADV)));
        ATTACHMENTS.put("regenerative_nanobots", List.of(row("vpu", 2, 2), row("eternal_soul", 12, 16), row("field_emitter", 2, 2),
                gated("phase_lattice", 2, 2, GATE_ADV)));
    }

    public ModExtractorCostsProvider(DataGenerator generator) {
        super(generator, "extraction/costs");
    }

    @Override
    public void registerConfigs() {
        ExtractorCostsConfig table = new ExtractorCostsConfig();
        table.CURVE = new ExtractorCostsConfig.CurveEntry();
        table.CURVE.exponent = K;
        table.CURVE.gatedRate = RATE_GATED;
        table.CURVE.fabricatorRate = RATE_FABRICATOR;
        table.CURVE.accelQuality = ACCEL_Q;
        table.CURVE.accelFactor = ACCEL_FACTOR;

        table.RARITY = new LinkedHashMap<>();
        table.RARITY.put("scrappy", 1.0F);
        table.RARITY.put("common", 1.1F);
        table.RARITY.put("rare", 1.25F);
        table.RARITY.put("epic", 1.35F);
        table.RARITY.put("omega", 1.5F);

        table.REPAIR = new ExtractorCostsConfig.RepairEntry();
        table.REPAIR.fraction = 0.20D;
        table.REPAIR.coreItem = "the_vault:repair_core";
        table.REPAIR.coreBase = 2.0D;

        table.PARTS = new LinkedHashMap<>();
        for (String part : PART_TYPES) {
            LinkedHashMap<String, List<Ingredient>> bySeries = new LinkedHashMap<>();
            for (String series : ExtractorCostsConfig.SERIES) {
                bySeries.put(series, export(partRecipe(part, series)));
            }
            table.PARTS.put(part, bySeries);
        }

        table.DRILLS = new LinkedHashMap<>();
        DRILLS.forEach((drill, rows) -> {
            List<Row> recipe = new ArrayList<>();
            recipe.add(DRILL_HOUSING);
            recipe.addAll(rows);
            recipe.add(DRILL_APEX);
            table.DRILLS.put(drill, export(recipe));
        });

        table.ATTACHMENTS = new LinkedHashMap<>();
        ATTACHMENTS.forEach((attachment, rows) -> {
            List<Row> recipe = new ArrayList<>();
            recipe.add(ATTACHMENT_HOUSING);
            recipe.add(ATTACHMENT_POG);
            recipe.addAll(rows);
            table.ATTACHMENTS.put(attachment, export(recipe));
        });

        table.COMPONENTS = new LinkedHashMap<>();
        COMPONENTS.forEach((key, component) -> table.COMPONENTS.put(key, component.export(key)));
        this.table("extractor_costs", table);
    }

    /** Base material at the part type's share (or an override range), then the signature rows, then the shared rows. */
    private static List<Row> partRecipe(String part, String series) {
        List<Row> rows = new ArrayList<>();
        for (Row base : BASE.get(series)) {
            Row override = BASE_OVERRIDE.get(slot(part, series, base.key()));
            double share = SHARE.get(part);
            rows.add(override != null ? override : new Row(base.key(), base.lo() * share, base.hi() * share, 0.0D));
        }
        rows.addAll(SIGNATURE.get(part).get(series));
        for (Row shared : UNIVERSAL.get(part)) {
            Row override = UNIVERSAL_OVERRIDE.get(slot(part, series, shared.key()));
            rows.add(override != null ? new Row(override.key(), override.lo(), override.hi(), shared.gate()) : shared);
        }
        return rows;
    }

    private static List<Ingredient> export(List<Row> rows) {
        List<Ingredient> out = new ArrayList<>();
        for (Row row : rows) {
            Ingredient ingredient = new Ingredient();
            ingredient.id = id(row.key());
            ingredient.fluid = !ITEMS.containsKey(row.key());
            ingredient.min = round4(row.lo());
            ingredient.max = round4(row.hi());
            ingredient.gate = row.gate();
            ingredient.scaling = scaling(row.key(), row.gate());
            out.add(ingredient);
        }
        return out;
    }

    /** Gated rows never compound; Fabricator components scale at the reduced Fabricator rate; everything else is main. */
    private static String scaling(String key, double gate) {
        if (gate > 0.0D) {
            return ExtractorCostsConfig.SCALING_GATED;
        }
        return COMPONENTS.containsKey(key) ? ExtractorCostsConfig.SCALING_FABRICATOR : ExtractorCostsConfig.SCALING_MAIN;
    }

    private static String id(String key) {
        String id = ITEMS.containsKey(key) ? ITEMS.get(key) : FLUIDS.get(key);
        if (id == null) {
            throw new IllegalStateException("No registry id for extractor cost ingredient '" + key + "'");
        }
        return id;
    }

    /** Rounds half-even to four decimals on the exact binary value, as Python's {@code round(x, 4)} does. */
    private static double round4(double value) {
        return new BigDecimal(value).setScale(4, RoundingMode.HALF_EVEN).doubleValue();
    }

    private static Row row(String key, double lo, double hi) {
        return new Row(key, lo, hi, 0.0D);
    }

    private static Row gated(String key, double lo, double hi, double gate) {
        return new Row(key, lo, hi, gate);
    }

    private static String slot(String part, String series, String key) {
        return part + "/" + series + "/" + key;
    }

    private static void signature(String part, String series, Row... rows) {
        SIGNATURE.computeIfAbsent(part, p -> new LinkedHashMap<>()).put(series, List.of(rows));
    }

    private static void items(String... pairs) {
        for (int i = 0; i < pairs.length; i += 2) {
            ITEMS.put(pairs[i], pairs[i + 1]);
        }
    }

    private static void fluids(String... pairs) {
        for (int i = 0; i < pairs.length; i += 2) {
            FLUIDS.put(pairs[i], pairs[i + 1]);
        }
    }

    private static List<String> grid(String... rows) {
        return List.of(rows);
    }

    private static Map<String, String> legend(String... pairs) {
        Map<String, String> legend = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            legend.put(pairs[i], pairs[i + 1]);
        }
        return legend;
    }

    private static void component(String key, String tier, String fluid, int fluidAmount, int fe, int ticks,
                                  List<String> grid, Map<String, String> legend) {
        COMPONENTS.put(key, new Component(tier, fluid, fluidAmount, fe, ticks, grid, legend));
    }

    /** One cost row by ingredient key: its roll range at the gate and the gate (0 for ungated rows). */
    private record Row(String key, double lo, double hi, double gate) {
    }

    /** A Fabricator recipe with its legend and fluid still named by ingredient key. */
    private record Component(String tier, String fluid, int fluidAmount, int fe, int ticks, List<String> grid, Map<String, String> legend) {

        ComponentEntry export(String key) {
            ComponentEntry entry = new ComponentEntry();
            entry.item = id(key);
            entry.tier = this.tier;
            entry.grid = new ArrayList<>(this.grid);
            entry.legend = new LinkedHashMap<>();
            this.legend.forEach((symbol, ingredient) -> entry.legend.put(symbol, id(ingredient)));
            entry.fluid = this.fluid.isEmpty() ? "" : id(this.fluid);
            entry.fluidAmount = this.fluidAmount;
            entry.fe = this.fe;
            entry.ticks = this.ticks;
            return entry;
        }
    }
}
