package xyz.iwolfking.woldsvaults.extraction.jobs;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.woldsvaults.config.ExtractionObjectiveConfig;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorStat;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorStats;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * What a finished extractor would earn: the job its drill selects, how long a run takes, the energy
 * and power cells it needs, the payout range and the rarity odds. Every number is the deterministic
 * part of the formulas in {@link ExtractionPayouts} and {@link ExtractionObjectiveConfig.JobEntry}.
 */
public final class ExtractorJobPreview {
    private ExtractorJobPreview() {
    }

    public static class Result {
        public String jobId = "";
        public ExtractionObjectiveConfig.JobEntry job;
        public String drillClass = "";
        public int totalPower;
        public int speed;
        public boolean deployable;
        public int ticks;
        public double energy;
        public int cells;
        public double yieldMin;
        public double yieldMax;
        public String unit = "";
        public boolean pending;
        public final Map<String, Double> shares = new LinkedHashMap<>();
        public int tierMin;
        public int tierMax;
    }

    @Nullable
    public static Result of(ExtractorStats.Block block) {
        return of(block.drillClass, block.get(ExtractorStat.POWER), block.get(ExtractorStat.SPEED), block.get(ExtractorStat.QUANTITY),
                block.get(ExtractorStat.RARITY), block.get(ExtractorStat.DRAIN), block.get(ExtractorStat.ENERGY_CAPACITY), block.get(ExtractorStat.ENERGY_PER_CELL));
    }

    /** Null when the drill class names no job, which is the only way a preview cannot be computed. */
    @Nullable
    public static Result of(String drillClass, float power, float speed, float quantity, float itemRarity, float drainPerSecond, float energyCapacity, float energyPerCell) {
        String jobId = ExtractorStats.jobFor(drillClass);
        ExtractionObjectiveConfig.JobEntry job = jobId == null ? null : ModConfigs.EXTRACTION_OBJECTIVE.getJob(jobId);
        if (job == null) {
            return null;
        }
        Result result = new Result();
        result.jobId = jobId;
        result.job = job;
        result.drillClass = drillClass;
        result.totalPower = Math.round(power);
        result.speed = Math.round(speed);
        result.deployable = result.totalPower >= job.minPower;
        result.ticks = job.durationTicks(result.totalPower, result.speed);
        result.energy = drainPerSecond * result.ticks / 20.0D;
        double initial = ModConfigs.EXTRACTION_OBJECTIVE.DEPLOYMENT.initialEnergyFraction * energyCapacity;
        result.cells = energyPerCell <= 0.0F ? 0 : (int) Math.max(0L, (long) Math.ceil((result.energy - initial) / energyPerCell));
        ExtractionObjectiveConfig.PayoutEntry spec = job.payout;
        double factor = ExtractionPayouts.countFactor(spec, job, result.totalPower, quantity);
        result.yieldMin = spec.countMin * factor;
        result.yieldMax = spec.countMax * factor;
        String type = spec.type == null ? "item" : spec.type.toLowerCase(Locale.ROOT);
        result.pending = type.equals("seal");
        result.unit = unitFor(type, spec);
        if (type.equals("cards")) {
            int slots = ModConfigs.EXTRACTION_OBJECTIVE.GREED_INFUSER.slotsFor(result.totalPower, job.minPower);
            result.yieldMin = slots;
            result.yieldMax = slots;
        } else if (type.equals("crystals")) {
            result.yieldMin = 0.0D;
            result.yieldMax = Math.ceil(AncientCrystals.tries(quantity));
        }
        int extraPower = Math.max(0, result.totalPower - job.minPower);
        Map<String, Double> weights = ExtractionPayouts.rarityWeights(spec, extraPower, itemRarity);
        double total = 0.0D;
        for (double weight : weights.values()) {
            total += Math.max(0.0D, weight);
        }
        if (total > 0.0D) {
            for (Map.Entry<String, Double> entry : weights.entrySet()) {
                result.shares.put(entry.getKey(), Math.max(0.0D, entry.getValue()) / total);
            }
        }
        for (ExtractionObjectiveConfig.TierWindow window : spec.tierWindows) {
            if (result.totalPower >= window.minPower) {
                result.tierMin = window.tierMin;
                result.tierMax = window.tierMax;
            }
        }
        return result;
    }

    private static String unitFor(String type, ExtractionObjectiveConfig.PayoutEntry spec) {
        switch (type) {
            case "maps":
                return "Vault Maps";
            case "charms":
                return "Unidentified Charms";
            case "uniques":
                return "Unidentified Uniques";
            case "cards":
                return "Greed Card Slots";
            case "seal":
                return "Gear Sealed";
            case "fluid":
                return "mB Prismatic Glue";
            case "crystals":
                return String.format("Ancient Crystals (%.0f%% per roll)", ModConfigs.EXTRACTION_OBJECTIVE.ANCIENT_CRYSTAL.crystalChance * 100.0F);
            default:
                break;
        }
        if (spec.itemNbtId != null && !spec.itemNbtId.isEmpty()) {
            return ExtractorJobUnits.humanize(spec.itemNbtId);
        }
        Item item = spec.item == null || spec.item.isEmpty() ? null : ForgeRegistries.ITEMS.getValue(new ResourceLocation(spec.item));
        return item == null ? ExtractorJobUnits.humanize(spec.item) : new ItemStack(item).getHoverName().getString();
    }

    /** {@code m:ss} of a tick count. */
    public static String formatTime(int ticks) {
        int seconds = Math.max(0, ticks / 20);
        return String.format("%d:%02d", seconds / 60, seconds % 60);
    }

    static final class ExtractorJobUnits {
        private ExtractorJobUnits() {
        }

        static String humanize(String id) {
            if (id == null || id.isEmpty()) {
                return "Items";
            }
            String path = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
            StringBuilder out = new StringBuilder();
            for (String word : path.split("_")) {
                if (word.isEmpty()) {
                    continue;
                }
                if (out.length() > 0) {
                    out.append(' ');
                }
                out.append(word.substring(0, 1).toUpperCase(Locale.ROOT)).append(word.substring(1));
            }
            return out.toString();
        }
    }
}
