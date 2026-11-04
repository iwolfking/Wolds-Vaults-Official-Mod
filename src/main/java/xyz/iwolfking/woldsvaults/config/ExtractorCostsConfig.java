package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorPartType;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fabrication, repair and Vault Fabricator costs for extractor parts, generated as
 * {@code vault_configs/extraction/costs/extractor_costs.json}: the quality curve, the rarity factors, the repair
 * rule, the Extraction Forge's power and time rule, every part, drill and attachment recipe, and the Vault
 * Fabricator component recipes.
 */
public class ExtractorCostsConfig extends Config implements WoldsTable {
    public static final String SCALING_MAIN = "main";
    public static final String SCALING_FABRICATOR = "fabricator";
    public static final String SCALING_GATED = "gated";
    public static final List<String> RARITIES = List.of("scrappy", "common", "rare", "epic", "omega");
    public static final List<String> SERIES = List.of(ExtractorPartsConfig.SERIES_WOODEN, ExtractorPartsConfig.SERIES_CHROMATIC,
            ExtractorPartsConfig.SERIES_EXPERIMENTAL, ExtractorPartsConfig.SERIES_ARMORED, ExtractorPartsConfig.SERIES_ECHOSTEEL);
    public static final int GRID_SIZE = 5;

    @Expose public CurveEntry CURVE;
    @Expose public LinkedHashMap<String, Float> RARITY;
    @Expose public RepairEntry REPAIR;
    @Expose public ForgeEntry FORGE;
    @Expose public LinkedHashMap<String, LinkedHashMap<String, List<Ingredient>>> PARTS;
    @Expose public LinkedHashMap<String, List<Ingredient>> DRILLS;
    @Expose public LinkedHashMap<String, List<Ingredient>> ATTACHMENTS;
    @Expose public LinkedHashMap<String, ComponentEntry> COMPONENTS;

    @Override
    public String getName() {
        return "extractor_costs";
    }

    /** The quality curve: main rows scale as (1 + Q/4)^exponent, steeper past accelQuality; gated and Fabricator rows at reduced rates. */
    public static class CurveEntry {
        @Expose public double exponent;
        @Expose public double gatedRate;
        @Expose public double fabricatorRate;
        @Expose public double accelQuality;
        @Expose public double accelFactor;
    }

    /** Repairs charge {@code fraction} of the parts' mid-roll cost plus Repair Cores from {@code coreBase}. */
    public static class RepairEntry {
        @Expose public double fraction;
        @Expose public String coreItem;
        @Expose public double coreBase;
    }

    /**
     * The Extraction Forge's fabrication run: it draws {@code fePerTickPerQuality} × quality FE every tick for
     * {@code secondsPerQuality} × quality seconds, from a buffer of {@code energyCapacity} FE filled at up to
     * {@code energyInput} FE per tick.
     */
    public static class ForgeEntry {
        @Expose public double fePerTickPerQuality;
        @Expose public double secondsPerQuality;
        @Expose public int energyCapacity;
        @Expose public int energyInput;

        public int fePerTick(double quality) {
            return (int) Math.round(this.fePerTickPerQuality * Math.max(0.0D, quality));
        }

        public int ticks(double quality) {
            return Math.max(1, (int) Math.round(this.secondsPerQuality * Math.max(0.0D, quality) * 20.0D));
        }
    }

    /** One cost row: an item or fluid id, its roll range at the row's gate, the gate, and its scaling class. */
    public static class Ingredient {
        @Expose public String id;
        @Expose public boolean fluid;
        @Expose public double min;
        @Expose public double max;
        @Expose public double gate;
        @Expose public String scaling;
    }

    /** A Vault Fabricator recipe: a 5x5 grid of symbols, what each symbol is, an optional fluid, energy and time. */
    public static class ComponentEntry {
        @Expose public String item;
        @Expose public String tier;
        @Expose public List<String> grid;
        @Expose public LinkedHashMap<String, String> legend;
        @Expose public String fluid;
        @Expose public int fluidAmount;
        @Expose public int fe;
        @Expose public int ticks;

        /** The item id for grid cell {@code index} (row-major, 0..24), or null for an empty cell. */
        @Nullable
        public String cell(int index) {
            int row = index / GRID_SIZE;
            int col = index % GRID_SIZE;
            if (row >= this.grid.size()) {
                return null;
            }
            String[] symbols = this.grid.get(row).trim().split("\\s+");
            if (col >= symbols.length || ".".equals(symbols[col])) {
                return null;
            }
            return this.legend.get(symbols[col]);
        }
    }

    @Nullable
    public List<Ingredient> partRecipe(String partType, String series) {
        LinkedHashMap<String, List<Ingredient>> bySeries = this.PARTS.get(partType);
        return bySeries == null ? null : bySeries.get(series);
    }

    @Nullable
    public ComponentEntry componentFor(String key) {
        return this.COMPONENTS.get(key);
    }

    /** A config with no recipes, unit rarity factors and a zeroed curve: every part on it costs nothing. */
    public static ExtractorCostsConfig inert() {
        ExtractorCostsConfig config = new ExtractorCostsConfig();
        config.CURVE = new CurveEntry();
        config.RARITY = new LinkedHashMap<>();
        for (String rarity : RARITIES) {
            config.RARITY.put(rarity, 1.0F);
        }
        config.REPAIR = new RepairEntry();
        config.REPAIR.coreItem = "the_vault:repair_core";
        config.FORGE = new ForgeEntry();
        config.PARTS = new LinkedHashMap<>();
        config.DRILLS = new LinkedHashMap<>();
        config.ATTACHMENTS = new LinkedHashMap<>();
        config.COMPONENTS = new LinkedHashMap<>();
        return config;
    }

    @Override
    protected void reset() {
    }

    @Nullable
    @Override
    public String validate() {
        if (this.CURVE == null || this.CURVE.exponent <= 0.0D || this.CURVE.gatedRate <= 0.0D || this.CURVE.fabricatorRate <= 0.0D
                || this.CURVE.accelQuality <= 0.0D || this.CURVE.accelFactor <= 0.0D) {
            return "the CURVE block is missing or has a non-positive value";
        }
        if (this.RARITY == null) {
            return "the RARITY block is missing";
        }
        for (String rarity : RARITIES) {
            Float factor = this.RARITY.get(rarity);
            if (factor == null || factor <= 0.0F) {
                return "RARITY has no positive factor for " + rarity;
            }
        }
        if (this.REPAIR == null || this.REPAIR.coreItem == null || this.REPAIR.coreItem.isEmpty()
                || this.REPAIR.fraction <= 0.0D || this.REPAIR.fraction > 1.0D || this.REPAIR.coreBase <= 0.0D) {
            return "the REPAIR block is missing, has no core item, or has a fraction outside (0, 1] or a non-positive core base";
        }
        if (this.FORGE == null || this.FORGE.fePerTickPerQuality < 0.0D || this.FORGE.secondsPerQuality < 0.0D
                || this.FORGE.energyCapacity <= 0 || this.FORGE.energyInput <= 0) {
            return "the FORGE block is missing, has a negative rate, or has no energy capacity or input";
        }
        if (this.PARTS == null) {
            return "the PARTS block is missing";
        }
        for (ExtractorPartType type : ExtractorPartType.values()) {
            if (type == ExtractorPartType.DRILL || type == ExtractorPartType.ATTACHMENT) {
                continue;
            }
            for (String series : SERIES) {
                String problem = checkRows("PARTS " + type.getId() + "/" + series, this.partRecipe(type.getId(), series));
                if (problem != null) {
                    return problem;
                }
            }
        }
        String problem = checkRecipes("DRILLS", this.DRILLS);
        if (problem == null) {
            problem = checkRecipes("ATTACHMENTS", this.ATTACHMENTS);
        }
        if (problem != null) {
            return problem;
        }
        if (this.COMPONENTS == null || this.COMPONENTS.isEmpty()) {
            return "it defines no Vault Fabricator COMPONENTS";
        }
        for (Map.Entry<String, ComponentEntry> entry : this.COMPONENTS.entrySet()) {
            problem = checkComponent(entry.getKey(), entry.getValue());
            if (problem != null) {
                return problem;
            }
        }
        return null;
    }

    @Nullable
    private static String checkRecipes(String block, @Nullable Map<String, List<Ingredient>> recipes) {
        if (recipes == null || recipes.isEmpty()) {
            return block + " is missing or empty";
        }
        for (Map.Entry<String, List<Ingredient>> entry : recipes.entrySet()) {
            String problem = checkRows(block + " " + entry.getKey(), entry.getValue());
            if (problem != null) {
                return problem;
            }
        }
        return null;
    }

    @Nullable
    private static String checkRows(String where, @Nullable List<Ingredient> rows) {
        if (rows == null || rows.isEmpty()) {
            return where + " has no recipe";
        }
        for (Ingredient row : rows) {
            if (row == null || row.id == null || row.id.isEmpty()) {
                return where + " has a row with no id";
            }
            if (row.min < 0.0D || row.max < row.min || row.gate < 0.0D) {
                return where + " row " + row.id + " has a negative or inverted range or gate";
            }
            boolean gated = SCALING_GATED.equals(row.scaling);
            if (!gated && !SCALING_MAIN.equals(row.scaling) && !SCALING_FABRICATOR.equals(row.scaling)) {
                return where + " row " + row.id + " has unknown scaling '" + row.scaling + "'";
            }
            if (gated != row.gate > 0.0D) {
                return where + " row " + row.id + " is " + (gated ? "gated with no gate" : "not gated but has a gate");
            }
        }
        return null;
    }

    @Nullable
    private static String checkComponent(String key, @Nullable ComponentEntry component) {
        if (component == null || component.item == null || component.item.isEmpty()) {
            return "component " + key + " names no item";
        }
        if (component.grid == null || component.grid.size() != GRID_SIZE || component.legend == null) {
            return "component " + key + " does not have a " + GRID_SIZE + "-row grid and a legend";
        }
        for (String line : component.grid) {
            String[] symbols = line.trim().split("\\s+");
            if (symbols.length != GRID_SIZE) {
                return "component " + key + " grid row '" + line + "' does not have " + GRID_SIZE + " cells";
            }
            for (String symbol : symbols) {
                if (!".".equals(symbol) && !component.legend.containsKey(symbol)) {
                    return "component " + key + " grid uses '" + symbol + "', which the legend does not define";
                }
            }
        }
        boolean hasFluid = component.fluid != null && !component.fluid.isEmpty();
        if (hasFluid != component.fluidAmount > 0) {
            return "component " + key + " has a fluid without an amount or an amount without a fluid";
        }
        if (component.fe < 0 || component.ticks <= 0) {
            return "component " + key + " has negative energy or no craft time";
        }
        return null;
    }
}
