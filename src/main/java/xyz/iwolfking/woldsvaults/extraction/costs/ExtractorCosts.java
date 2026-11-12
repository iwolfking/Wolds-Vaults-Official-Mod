package xyz.iwolfking.woldsvaults.extraction.costs;

import iskallia.vault.gear.VaultGearRarity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.woldsvaults.config.ExtractorCostsConfig;
import xyz.iwolfking.woldsvaults.config.ExtractorPartsConfig;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorPartType;
import xyz.iwolfking.woldsvaults.extraction.parts.PartData;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * Turns a part's series or class, rarity and quality into what it costs, from {@code extractor_costs.json}.
 * {@code amount = floor(roll x scale(Q) x rarity)}, where the roll is uniform in the row's range and the scale
 * depends on the row's class: main rows grow as {@code (1 + Q/4)^K} (the exponent multiplied by
 * {@code accelFactor} past {@code accelQuality}), Fabricator rows at {@code fabricatorRate x K}, and gated rows
 * at {@code gatedRate x K} measured from their gate, where they enter at exactly the listed amount.
 */
public final class ExtractorCosts {
    private static final Set<String> WARNED = new HashSet<>();

    private ExtractorCosts() {
    }

    /** One owed ingredient: an item or fluid registry id and how many items or millibuckets. */
    public record CostLine(String id, boolean fluid, long amount) {
        public CompoundTag write() {
            CompoundTag tag = new CompoundTag();
            tag.putString("Id", this.id);
            tag.putBoolean("Fluid", this.fluid);
            tag.putLong("Amount", this.amount);
            return tag;
        }

        public static CostLine read(CompoundTag tag) {
            return new CostLine(tag.getString("Id"), tag.getBoolean("Fluid"), tag.getLong("Amount"));
        }

        /** The key the paid ledger files this line under; items and fluids never collide. */
        public String key() {
            return (this.fluid ? "fluid:" : "item:") + this.id;
        }

        @Nullable
        public Item item() {
            return this.fluid ? null : resolveItem(this.id);
        }

        @Nullable
        public Fluid fluidType() {
            return this.fluid ? resolveFluid(this.id) : null;
        }

        public Component displayName() {
            if (this.fluid) {
                Fluid fluid = this.fluidType();
                return fluid == null ? new TextComponent(this.id) : new FluidStack(fluid, 1000).getDisplayName();
            }
            Item item = this.item();
            return item == null ? new TextComponent(this.id) : new ItemStack(item).getHoverName();
        }
    }

    public static ListTag writeLines(List<CostLine> lines) {
        ListTag list = new ListTag();
        lines.forEach(line -> list.add(line.write()));
        return list;
    }

    public static List<CostLine> readLines(ListTag list) {
        List<CostLine> lines = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            lines.add(CostLine.read(list.getCompound(i)));
        }
        return lines;
    }

    public static List<CostLine> readLines(CompoundTag tag, String key) {
        return readLines(tag.getList(key, Tag.TAG_COMPOUND));
    }

    @Nullable
    public static Item resolveItem(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        Item item = rl == null ? null : ForgeRegistries.ITEMS.getValue(rl);
        if (item == null || (rl != null && !ForgeRegistries.ITEMS.containsKey(rl))) {
            errorOnce(id, "item");
            return null;
        }
        return item;
    }

    @Nullable
    public static Fluid resolveFluid(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null || !ForgeRegistries.FLUIDS.containsKey(rl)) {
            errorOnce(id, "fluid");
            return null;
        }
        return ForgeRegistries.FLUIDS.getValue(rl);
    }

    private static void errorOnce(String id, String kind) {
        if (WARNED.add(kind + ":" + id)) {
            WoldLogger.error("extractor_costs.json names the {} '{}', which is not registered; that cost line is skipped.", kind, id);
        }
    }

    public static ExtractorCostsConfig config() {
        return ModConfigs.EXTRACTOR_COSTS;
    }

    public static double rarityFactor(VaultGearRarity rarity) {
        Float factor = config().RARITY.get(ExtractorPartsConfig.rarityKey(rarity));
        if (factor == null) {
            WoldLogger.error("extractor_costs.json has no RARITY factor for '{}'; using 1.0.", ExtractorPartsConfig.rarityKey(rarity));
            return 1.0D;
        }
        return factor;
    }

    /** The multiplier a row of the given class and gate reaches at quality {@code q}. */
    public static double scale(String scaling, double gate, double q) {
        ExtractorCostsConfig.CurveEntry curve = config().CURVE;
        double p = 1.0D + 0.25D * q;
        if (ExtractorCostsConfig.SCALING_GATED.equals(scaling) || gate > 0.0D) {
            return Math.pow(p / (1.0D + 0.25D * gate), curve.exponent * curve.gatedRate);
        }
        if (ExtractorCostsConfig.SCALING_FABRICATOR.equals(scaling)) {
            return Math.pow(p, curve.exponent * curve.fabricatorRate);
        }
        if (q <= curve.accelQuality) {
            return Math.pow(p, curve.exponent);
        }
        double pa = 1.0D + 0.25D * curve.accelQuality;
        return Math.pow(pa, curve.exponent) * Math.pow(p / pa, curve.exponent * curve.accelFactor);
    }

    @Nullable
    public static List<ExtractorCostsConfig.Ingredient> recipeFor(PartData part) {
        ExtractorCostsConfig cfg = config();
        List<ExtractorCostsConfig.Ingredient> rows;
        if (part.type == ExtractorPartType.DRILL) {
            rows = cfg.DRILLS.get(part.drillClass);
        } else if (part.type == ExtractorPartType.ATTACHMENT) {
            rows = cfg.ATTACHMENTS.get(part.attachmentId);
        } else {
            rows = cfg.partRecipe(part.type.getId(), part.series);
        }
        if (rows == null) {
            WoldLogger.error("extractor_costs.json has no recipe for {} ({}); it costs nothing.", part.name, part.describeOrigin());
        }
        return rows;
    }

    /** Rolls a fresh cost for a part; each row rolls independently inside its range. */
    public static List<CostLine> roll(PartData part, Random random) {
        return compute(part, random, 1.0D);
    }

    /** The cost at mid rolls, times {@code factor}; used for repairs, which charge a share of what the parts cost. */
    public static List<CostLine> midpoint(PartData part, double factor) {
        return compute(part, null, factor);
    }

    private static List<CostLine> compute(PartData part, @Nullable Random random, double factor) {
        List<ExtractorCostsConfig.Ingredient> rows = recipeFor(part);
        Map<String, CostLine> merged = new LinkedHashMap<>();
        if (rows == null) {
            return new ArrayList<>();
        }
        double rarity = rarityFactor(part.rarity);
        for (ExtractorCostsConfig.Ingredient row : rows) {
            if (part.quality < row.gate) {
                continue;
            }
            double u = random == null ? 0.5D : random.nextDouble();
            double roll = row.min + (row.max - row.min) * u;
            long amount = (long) Math.floor(roll * scale(row.scaling, row.gate, part.quality) * rarity * factor + 1.0E-9D);
            if (amount <= 0) {
                continue;
            }
            CostLine line = new CostLine(row.id, row.fluid, amount);
            merged.merge(line.key(), line, (a, b) -> new CostLine(a.id(), a.fluid(), a.amount() + b.amount()));
        }
        return new ArrayList<>(merged.values());
    }

    /**
     * What it costs to repair {@code missingFraction} of an extractor's durability: {@code REPAIR.fraction} of each
     * part's mid-roll cost, plus Repair Cores at {@code coreBase} scaled like a main ingredient at the parts' mean
     * quality and rarity, all multiplied by the missing fraction. Attachments carry their own wear and are not repaired.
     */
    public static List<CostLine> repairCost(List<PartData> parts, double missingFraction) {
        ExtractorCostsConfig.RepairEntry repair = config().REPAIR;
        Map<String, CostLine> merged = new LinkedHashMap<>();
        if (parts.isEmpty() || missingFraction <= 0.0D) {
            return new ArrayList<>();
        }
        double qualitySum = 0.0D;
        double raritySum = 0.0D;
        for (PartData part : parts) {
            qualitySum += part.quality;
            raritySum += rarityFactor(part.rarity);
            for (CostLine line : midpoint(part, repair.fraction * missingFraction)) {
                merged.merge(line.key(), line, (a, b) -> new CostLine(a.id(), a.fluid(), a.amount() + b.amount()));
            }
        }
        double meanQ = qualitySum / parts.size();
        double meanR = raritySum / parts.size();
        long cores = (long) Math.max(1.0D, Math.floor(repair.coreBase * scale(ExtractorCostsConfig.SCALING_MAIN, 0.0D, meanQ) * meanR * missingFraction));
        CostLine core = new CostLine(repair.coreItem, false, cores);
        merged.merge(core.key(), core, (a, b) -> new CostLine(a.id(), a.fluid(), a.amount() + b.amount()));
        return new ArrayList<>(merged.values());
    }
}
