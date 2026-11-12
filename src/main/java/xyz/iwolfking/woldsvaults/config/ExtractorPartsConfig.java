package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import iskallia.vault.gear.VaultGearRarity;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorPartType;
import xyz.iwolfking.woldsvaults.extraction.parts.ExtractorStat;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Every number behind extractor parts, blueprints and assembly, generated as
 * {@code vault_configs/extraction/parts/extractor_parts.json}: series, per-series stat ranges, drill classes,
 * attachments, affix pools, the quality curve, the stat scaling families and attachment slots. Crafting and repair
 * costs are deliberately absent; they live in {@link ExtractorCostsConfig}.
 */
public class ExtractorPartsConfig extends Config implements WoldsTable {
    public static final String SERIES_WOODEN = "wooden";
    public static final String SERIES_CHROMATIC = "chromatic";
    public static final String SERIES_EXPERIMENTAL = "experimental";
    public static final String SERIES_ARMORED = "armored";
    public static final String SERIES_ECHOSTEEL = "echosteel";
    public static final List<String> RARITIES = List.of("scrappy", "common", "rare", "epic", "omega");
    public static final List<String> AFFIX_POOL_NAMES = List.of("hardware_prefix", "hardware_suffix", "mechanical_prefix", "mechanical_suffix");

    @Expose public LinkedHashMap<String, SeriesEntry> SERIES;
    @Expose public LinkedHashMap<String, List<String>> SERIES_BY_RARITY;
    @Expose public LinkedHashMap<String, Integer> RARITY_DROP_WEIGHTS;
    @Expose public LinkedHashMap<String, LinkedHashMap<String, PartRollEntry>> PARTS;
    @Expose public LinkedHashMap<String, DrillClassEntry> DRILL_CLASSES;
    @Expose public LinkedHashMap<String, AttachmentEntry> ATTACHMENTS;
    @Expose public LinkedHashMap<String, List<AffixEntry>> AFFIX_POOLS;
    @Expose public LinkedHashMap<String, Integer> AFFIX_COUNT;
    @Expose public QualityEntry QUALITY;
    @Expose public ScalesEntry SCALES;
    @Expose public SlotsEntry SLOTS;
    @Expose public float CELL_RESTORE_BASE;

    @Override
    public String getName() {
        return "extractor_parts";
    }

    public static String rarityKey(VaultGearRarity rarity) {
        return rarity.name().toLowerCase(Locale.ROOT);
    }

    @Nullable
    public static VaultGearRarity rarityOf(String key) {
        if (key == null) {
            return null;
        }
        for (VaultGearRarity rarity : VaultGearRarity.values()) {
            if (rarity.name().equalsIgnoreCase(key)) {
                return rarity;
            }
        }
        return null;
    }

    @Nullable
    public SeriesEntry getSeries(String id) {
        return SERIES.get(id);
    }

    public List<String> getSeriesFor(VaultGearRarity rarity) {
        List<String> allowed = SERIES_BY_RARITY.get(rarityKey(rarity));
        return allowed == null ? new ArrayList<>() : allowed;
    }

    @Nullable
    public PartRollEntry getRoll(ExtractorPartType type, String seriesOrClass) {
        LinkedHashMap<String, PartRollEntry> rolls = PARTS.get(type.getId());
        return rolls == null ? null : rolls.get(seriesOrClass);
    }

    public int getAffixCount(VaultGearRarity rarity) {
        return AFFIX_COUNT.getOrDefault(rarityKey(rarity), 0);
    }

    public int getRarityDropWeight(VaultGearRarity rarity) {
        return RARITY_DROP_WEIGHTS.getOrDefault(rarityKey(rarity), 0);
    }

    public static class Range {
        @Expose public float min;
        @Expose public float max;

        public Range() {
        }

        public Range(float min, float max) {
            this.min = min;
            this.max = max;
        }

        public float lerp(float u) {
            return this.min + (this.max - this.min) * u;
        }
    }

    public static class SeriesEntry {
        @Expose public String name;
        @Expose public String rigSuffix;
        @Expose public LinkedHashMap<String, String> partNames = new LinkedHashMap<>();
        @Expose public LinkedHashMap<String, String> rigBones = new LinkedHashMap<>();

        public SeriesEntry() {
        }

        public SeriesEntry(String name, String rigSuffix, String chassis, String core, String legs, String shield, String radiator, String scanner) {
            this.name = name;
            this.rigSuffix = rigSuffix;
            this.rigBones.put(ExtractorPartType.CHASSIS.getId(), chassis);
            this.rigBones.put(ExtractorPartType.POWER_CORE.getId(), core);
            this.rigBones.put(ExtractorPartType.LEGS.getId(), legs);
            this.rigBones.put(ExtractorPartType.SHIELD_ARRAY.getId(), shield);
            this.rigBones.put(ExtractorPartType.RADIATOR.getId(), radiator);
            this.rigBones.put(ExtractorPartType.SCANNER.getId(), scanner);
        }

        public String partName(ExtractorPartType type, String fallback) {
            return this.partNames.getOrDefault(type.getId(), fallback);
        }
    }

    /** One row of the workbook: flat stat ranges, multiplier ranges and fixed specials for a part in a series. */
    public static class PartRollEntry {
        @Expose public LinkedHashMap<String, Range> stats = new LinkedHashMap<>();
        @Expose public LinkedHashMap<String, Range> multipliers = new LinkedHashMap<>();
        @Expose public LinkedHashMap<String, Float> specials = new LinkedHashMap<>();

        public PartRollEntry() {
        }

        public PartRollEntry stat(ExtractorStat stat, float min, float max) {
            this.stats.put(stat.getId(), new Range(min, max));
            return this;
        }

        public PartRollEntry mult(ExtractorStat stat, float min, float max) {
            this.multipliers.put(stat.getId(), new Range(min, max));
            return this;
        }

        public PartRollEntry special(ExtractorStat stat, float value) {
            this.specials.put(stat.getId(), value);
            return this;
        }
    }

    /**
     * One drill head. {@code minQuality} is the lowest blueprint quality this class can roll at: the grade where a
     * wooden rig built at that quality with average rolls reaches the job's minimum power.
     */
    public static class DrillClassEntry {
        @Expose public String name;
        @Expose public String job;
        @Expose public String rigBone;
        @Expose public String minRarity;
        @Expose public float minQuality;

        public DrillClassEntry() {
        }

        public DrillClassEntry(String name, String job, String rigBone, String minRarity, float minQuality) {
            this.name = name;
            this.job = job;
            this.rigBone = rigBone;
            this.minRarity = minRarity;
            this.minQuality = minQuality;
        }
    }

    public static class EffectEntry {
        @Expose public String stat;
        @Expose public float min;
        @Expose public float max;
        @Expose public boolean percent;

        public EffectEntry() {
        }

        public EffectEntry(ExtractorStat stat, float min, float max, boolean percent) {
            this.stat = stat.getId();
            this.min = min;
            this.max = max;
            this.percent = percent;
        }
    }

    public static class AttachmentEntry {
        @Expose public String name;
        @Expose public String rigBone;
        @Expose public Range drain;
        @Expose public Range durability;
        @Expose public List<EffectEntry> effects = new ArrayList<>();
        @Expose public boolean stackable;

        public AttachmentEntry() {
        }

        public AttachmentEntry(String name, String rigBone, float drainMin, float drainMax, float duraMin, float duraMax, EffectEntry... effects) {
            this.name = name;
            this.rigBone = rigBone;
            this.drain = new Range(drainMin, drainMax);
            this.durability = new Range(duraMin, duraMax);
            this.effects.addAll(Arrays.asList(effects));
        }
    }

    public static class AffixEntry {
        @Expose public String id;
        @Expose public String stat;
        @Expose public float min;
        @Expose public float max;
        @Expose public boolean percent;

        public AffixEntry() {
        }

        public AffixEntry(String id, ExtractorStat stat, float min, float max, boolean percent) {
            this.id = id;
            this.stat = stat.getId();
            this.min = min;
            this.max = max;
            this.percent = percent;
        }
    }

    public static class QualityEntry {
        @Expose public float centerA;
        @Expose public float centerB;
        @Expose public float centerIrScale;
        @Expose public float kneeIr;
        @Expose public float kneeLogK;
        @Expose public LinkedHashMap<String, Float> rarityMult = new LinkedHashMap<>();
        @Expose public LinkedHashMap<String, Float> rarityFlat = new LinkedHashMap<>();
        @Expose public LinkedHashMap<String, Float> rarityWidth = new LinkedHashMap<>();
        @Expose public float widthBase;
        @Expose public float widthSlope;
        @Expose public float costExponent;
        @Expose public float costTailRate;
        @Expose public float rollNoise;
        @Expose public List<BucketEntry> buckets = new ArrayList<>();
    }

    public static class BucketEntry {
        @Expose public int maxQuality;
        @Expose public String label;

        public BucketEntry() {
        }

        public BucketEntry(int maxQuality, String label) {
            this.maxQuality = maxQuality;
            this.label = label;
        }
    }

    public static class ScalesEntry {
        @Expose public LinkedHashMap<String, Float> linear = new LinkedHashMap<>();
        @Expose public LinkedHashMap<String, Float> geometric = new LinkedHashMap<>();
        @Expose public float geometricTailQuality;
        @Expose public float geometricTailSlope;
    }

    public static class SlotsEntry {
        @Expose public LinkedHashMap<String, Integer> chassis = new LinkedHashMap<>();
        @Expose public int omegaCoreBonus;
        @Expose public int max;
    }

    @Override
    protected void reset() {
    }

    /** A config with no series, parts, drills, attachments or affixes: nothing can be rolled or assembled from it. */
    public static ExtractorPartsConfig inert() {
        ExtractorPartsConfig config = new ExtractorPartsConfig();
        config.SERIES = new LinkedHashMap<>();
        config.SERIES_BY_RARITY = new LinkedHashMap<>();
        config.RARITY_DROP_WEIGHTS = new LinkedHashMap<>();
        config.PARTS = new LinkedHashMap<>();
        config.DRILL_CLASSES = new LinkedHashMap<>();
        config.ATTACHMENTS = new LinkedHashMap<>();
        config.AFFIX_POOLS = new LinkedHashMap<>();
        config.AFFIX_COUNT = new LinkedHashMap<>();
        config.QUALITY = new QualityEntry();
        config.SCALES = new ScalesEntry();
        config.SLOTS = new SlotsEntry();
        return config;
    }

    @Nullable
    @Override
    public String validate() {
        String problem = this.checkSeries();
        if (problem == null) {
            problem = this.checkParts();
        }
        if (problem == null) {
            problem = this.checkAttachmentsAndAffixes();
        }
        if (problem == null) {
            problem = this.checkCurves();
        }
        return problem;
    }

    @Nullable
    private String checkSeries() {
        if (this.SERIES == null || this.SERIES_BY_RARITY == null || this.RARITY_DROP_WEIGHTS == null) {
            return "the SERIES, SERIES_BY_RARITY or RARITY_DROP_WEIGHTS block is missing";
        }
        for (String id : List.of(SERIES_WOODEN, SERIES_CHROMATIC, SERIES_EXPERIMENTAL, SERIES_ARMORED, SERIES_ECHOSTEEL)) {
            SeriesEntry series = this.SERIES.get(id);
            if (series == null || series.name == null || series.rigSuffix == null || series.partNames == null || series.rigBones == null) {
                return "series " + id + " is missing or has no name, rig suffix, part names or rig bones";
            }
            for (ExtractorPartType type : hardwareTypes()) {
                if (!series.partNames.containsKey(type.getId()) || !series.rigBones.containsKey(type.getId())) {
                    return "series " + id + " has no part name or rig bone for " + type.getId();
                }
            }
        }
        int totalWeight = 0;
        for (String rarity : RARITIES) {
            List<String> allowed = this.SERIES_BY_RARITY.get(rarity);
            if (allowed == null || allowed.isEmpty()) {
                return "SERIES_BY_RARITY lists no series for " + rarity;
            }
            for (String id : allowed) {
                if (!this.SERIES.containsKey(id)) {
                    return "SERIES_BY_RARITY " + rarity + " names series '" + id + "', which SERIES does not define";
                }
            }
            Integer weight = this.RARITY_DROP_WEIGHTS.get(rarity);
            if (weight == null || weight < 0) {
                return "RARITY_DROP_WEIGHTS has no non-negative weight for " + rarity;
            }
            totalWeight += weight;
        }
        return totalWeight > 0 ? null : "RARITY_DROP_WEIGHTS sums to zero";
    }

    @Nullable
    private String checkParts() {
        if (this.PARTS == null || this.DRILL_CLASSES == null || this.DRILL_CLASSES.isEmpty()) {
            return "the PARTS block is missing or DRILL_CLASSES is missing or empty";
        }
        for (ExtractorPartType type : hardwareTypes()) {
            for (String series : this.SERIES.keySet()) {
                String problem = checkRoll(type.getId() + "/" + series, this.getRoll(type, series));
                if (problem != null) {
                    return problem;
                }
            }
        }
        for (Map.Entry<String, DrillClassEntry> entry : this.DRILL_CLASSES.entrySet()) {
            DrillClassEntry drill = entry.getValue();
            if (drill == null || isBlank(drill.name) || isBlank(drill.job) || isBlank(drill.rigBone) || rarityOf(drill.minRarity) == null
                    || drill.minQuality < 0.0F) {
                return "drill class " + entry.getKey() + " is missing a name, job or rig bone, or has an unknown rarity or negative quality";
            }
            String problem = checkRoll(ExtractorPartType.DRILL.getId() + "/" + entry.getKey(), this.getRoll(ExtractorPartType.DRILL, entry.getKey()));
            if (problem != null) {
                return problem;
            }
        }
        return null;
    }

    @Nullable
    private String checkAttachmentsAndAffixes() {
        if (this.ATTACHMENTS == null || this.ATTACHMENTS.isEmpty()) {
            return "ATTACHMENTS is missing or empty";
        }
        for (Map.Entry<String, AttachmentEntry> entry : this.ATTACHMENTS.entrySet()) {
            AttachmentEntry attachment = entry.getValue();
            if (attachment == null || isBlank(attachment.name) || isBlank(attachment.rigBone) || !isRange(attachment.drain)
                    || !isRange(attachment.durability) || attachment.effects == null) {
                return "attachment " + entry.getKey() + " is missing a name, rig bone, drain, durability or effect list";
            }
            for (EffectEntry effect : attachment.effects) {
                if (effect == null || ExtractorStat.byId(effect.stat) == null || effect.max < effect.min) {
                    return "attachment " + entry.getKey() + " has an effect with an unknown stat or an inverted range";
                }
            }
        }
        if (this.AFFIX_POOLS == null || this.AFFIX_COUNT == null) {
            return "the AFFIX_POOLS or AFFIX_COUNT block is missing";
        }
        for (String name : AFFIX_POOL_NAMES) {
            List<AffixEntry> pool = this.AFFIX_POOLS.get(name);
            if (pool == null || pool.isEmpty()) {
                return "affix pool " + name + " is missing or empty";
            }
            for (AffixEntry affix : pool) {
                if (affix == null || isBlank(affix.id) || ExtractorStat.byId(affix.stat) == null || affix.max < affix.min) {
                    return "affix pool " + name + " has an affix with no id, an unknown stat or an inverted range";
                }
            }
        }
        for (String rarity : RARITIES) {
            Integer count = this.AFFIX_COUNT.get(rarity);
            if (count == null || count < 0) {
                return "AFFIX_COUNT has no non-negative count for " + rarity;
            }
        }
        return null;
    }

    @Nullable
    private String checkCurves() {
        QualityEntry quality = this.QUALITY;
        if (quality == null || quality.buckets == null || quality.buckets.isEmpty()
                || quality.rarityMult == null || quality.rarityFlat == null || quality.rarityWidth == null) {
            return "the QUALITY block is missing, has no buckets or is missing a rarity table";
        }
        for (String rarity : RARITIES) {
            if (!quality.rarityMult.containsKey(rarity) || !quality.rarityFlat.containsKey(rarity) || !quality.rarityWidth.containsKey(rarity)) {
                return "QUALITY has no multiplier, flat bonus or width for " + rarity;
            }
        }
        for (BucketEntry bucket : quality.buckets) {
            if (bucket == null || bucket.label == null) {
                return "QUALITY has an unlabelled bucket";
            }
        }
        if (this.SCALES == null || this.SCALES.linear == null || this.SCALES.geometric == null) {
            return "the SCALES block or one of its families is missing";
        }
        if (this.SLOTS == null || this.SLOTS.chassis == null || this.SLOTS.max < 0) {
            return "the SLOTS block is missing or has a negative maximum";
        }
        for (String rarity : RARITIES) {
            if (!this.SLOTS.chassis.containsKey(rarity)) {
                return "SLOTS has no chassis slot count for " + rarity;
            }
        }
        return this.CELL_RESTORE_BASE > 0.0F ? null : "CELL_RESTORE_BASE is not positive";
    }

    @Nullable
    private static String checkRoll(String where, @Nullable PartRollEntry roll) {
        if (roll == null || roll.stats == null || roll.multipliers == null || roll.specials == null) {
            return "PARTS has no roll entry for " + where;
        }
        for (Map<String, Range> ranges : List.of(roll.stats, roll.multipliers)) {
            for (Map.Entry<String, Range> entry : ranges.entrySet()) {
                if (ExtractorStat.byId(entry.getKey()) == null || !isRange(entry.getValue())) {
                    return "PARTS " + where + " rolls '" + entry.getKey() + "', which is an unknown stat or has an inverted range";
                }
            }
        }
        for (String stat : roll.specials.keySet()) {
            if (ExtractorStat.byId(stat) == null) {
                return "PARTS " + where + " has a special for unknown stat '" + stat + "'";
            }
        }
        return null;
    }

    private static boolean isRange(@Nullable Range range) {
        return range != null && range.min <= range.max;
    }

    private static boolean isBlank(@Nullable String value) {
        return value == null || value.isEmpty();
    }

    private static List<ExtractorPartType> hardwareTypes() {
        List<ExtractorPartType> types = new ArrayList<>();
        for (ExtractorPartType type : ExtractorPartType.values()) {
            if (type != ExtractorPartType.DRILL && type != ExtractorPartType.ATTACHMENT) {
                types.add(type);
            }
        }
        return types;
    }

    public Map<String, Float> getSpecials(ExtractorPartType type, String key) {
        PartRollEntry entry = getRoll(type, key);
        return entry == null ? new LinkedHashMap<>() : entry.specials;
    }
}
