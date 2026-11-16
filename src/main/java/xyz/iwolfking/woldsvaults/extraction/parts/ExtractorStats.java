package xyz.iwolfking.woldsvaults.extraction.parts;

import iskallia.vault.gear.VaultGearRarity;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.config.ExtractorPartsConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The composition formula: flat part values sum, attachment and affix percentages multiply the sum,
 * multiplier stats compound across every source. Attachments that have worn through their own pool
 * are skipped: they stay bolted on and rendered, but contribute nothing until repaired. Pure, so
 * client previews and server assembly agree. Also the one place rig bone names are built.
 */
public final class ExtractorStats {
    /** The chassis has exactly three attachment mounts; no table may grant more hardpoints than this. */
    public static final int MAX_HARDPOINTS = 3;
    private static final String DEFAULT_RIG_SUFFIX = "s1";
    private static final String ATTACH_PREFIX = "attach_";
    private static final ExtractorPartType[] BONE_ORDER = {ExtractorPartType.CHASSIS, ExtractorPartType.LEGS, ExtractorPartType.POWER_CORE,
            ExtractorPartType.RADIATOR, ExtractorPartType.SHIELD_ARRAY, ExtractorPartType.SCANNER, ExtractorPartType.DRILL};
    private static final Map<ExtractorPartType, String> DEFAULT_BONES = new EnumMap<>(ExtractorPartType.class);
    private static ExtractorPartsConfig boneSource;
    private static Set<String> boneCache = Collections.emptySet();

    static {
        DEFAULT_BONES.put(ExtractorPartType.CHASSIS, "chassis_s1_wooden");
        DEFAULT_BONES.put(ExtractorPartType.LEGS, "legs_s1_wooden");
        DEFAULT_BONES.put(ExtractorPartType.POWER_CORE, "core_s1_combustion");
        DEFAULT_BONES.put(ExtractorPartType.RADIATOR, "radiator_s1_grating");
        DEFAULT_BONES.put(ExtractorPartType.SHIELD_ARRAY, "shield_s1_projector");
        DEFAULT_BONES.put(ExtractorPartType.SCANNER, "scanner_s1_probe");
        DEFAULT_BONES.put(ExtractorPartType.DRILL, "drill_standard");
    }

    private ExtractorStats() {
    }

    public static class Block {
        public final Map<ExtractorStat, Float> values = new EnumMap<>(ExtractorStat.class);
        public int slots;
        public String drillClass = "";
        /** The majority series of the six series parts (chassis on ties); the tooltip frame and the item name follow it. */
        public String frameSeries = "";
        /**
         * The rig bones to unhide, in render order: one per structural slot, then one per installed attachment. An
         * attachment's position among the attachment entries is its chassis mount index.
         */
        public final List<String> variantBones = new ArrayList<>();

        public float get(ExtractorStat stat) {
            return this.values.getOrDefault(stat, stat.getKind() == ExtractorStat.Kind.MULTIPLIER ? 1.0F : 0.0F);
        }
    }

    public static int slotsFor(@Nullable PartData chassis, @Nullable PartData core) {
        ExtractorPartsConfig.SlotsEntry slots = ModConfigs.EXTRACTOR_PARTS.SLOTS;
        int count = 0;
        if (chassis != null) {
            Integer granted = slots.chassis.get(ExtractorPartsConfig.rarityKey(chassis.rarity));
            if (granted == null) {
                WoldLogger.errorOnce("extractor_stats.slots." + chassis.rarity,
                        "extractor_parts SLOTS has no chassis entry for {}; that chassis grants no hardpoints.", ExtractorPartsConfig.rarityKey(chassis.rarity));
            } else {
                count = granted;
            }
        }
        if (core != null && core.rarity == VaultGearRarity.OMEGA) {
            count += slots.omegaCoreBonus;
        }
        return Math.min(Math.min(slots.max, MAX_HARDPOINTS), count);
    }

    @Nullable
    public static PartData find(List<PartData> parts, ExtractorPartType type) {
        for (PartData part : parts) {
            if (part.type == type) {
                return part;
            }
        }
        return null;
    }

    /** Majority series over the series parts present; ties go to the chassis; empty when no series part is present. */
    public static String frameSeries(List<PartData> parts) {
        Map<String, Integer> votes = new LinkedHashMap<>();
        PartData chassis = null;
        for (PartData part : parts) {
            if (part.type == ExtractorPartType.DRILL || part.isAttachment() || part.series == null || part.series.isEmpty()) {
                continue;
            }
            if (part.type == ExtractorPartType.CHASSIS) {
                chassis = part;
            }
            votes.merge(part.series, 1, Integer::sum);
        }
        String best = "";
        int bestVotes = 0;
        for (Map.Entry<String, Integer> entry : votes.entrySet()) {
            if (entry.getValue() > bestVotes) {
                best = entry.getKey();
                bestVotes = entry.getValue();
            }
        }
        if (chassis != null && votes.getOrDefault(chassis.series, 0) == bestVotes) {
            return chassis.series;
        }
        return best;
    }

    public static Block compose(List<PartData> parts, List<PartData> attachments) {
        ExtractorPartsConfig cfg = ModConfigs.EXTRACTOR_PARTS;
        Map<ExtractorStat, Float> flat = new EnumMap<>(ExtractorStat.class);
        Map<ExtractorStat, Float> percent = new EnumMap<>(ExtractorStat.class);
        Map<ExtractorStat, Float> multiplier = new EnumMap<>(ExtractorStat.class);
        for (PartData part : parts) {
            accumulate(part, flat, percent, multiplier);
        }
        for (PartData attachment : attachments) {
            if (attachment.isDepleted()) {
                continue;
            }
            accumulate(attachment, flat, percent, multiplier);
        }
        PartData chassis = find(parts, ExtractorPartType.CHASSIS);
        PartData core = find(parts, ExtractorPartType.POWER_CORE);
        PartData drill = find(parts, ExtractorPartType.DRILL);
        if (core != null) {
            flat.merge(ExtractorStat.ENERGY_PER_CELL, (float) (cfg.CELL_RESTORE_BASE * ExtractorQuality.statMultiplier(ExtractorStat.ENERGY_PER_CELL, core.quality)), Float::sum);
        }

        Block block = new Block();
        for (ExtractorStat stat : ExtractorStat.values()) {
            switch (stat.getKind()) {
                case FLAT -> {
                    float value = flat.getOrDefault(stat, 0.0F) * (1.0F + percent.getOrDefault(stat, 0.0F) / 100.0F);
                    block.values.put(stat, value);
                }
                case MULTIPLIER -> block.values.put(stat, multiplier.getOrDefault(stat, 1.0F));
                case INERT -> block.values.put(stat, flat.getOrDefault(stat, 0.0F) + percent.getOrDefault(stat, 0.0F));
            }
        }
        block.values.put(ExtractorStat.DRAIN, Math.max(0.0F, block.get(ExtractorStat.DRAIN)) * block.get(ExtractorStat.DRAIN_MULT));
        block.slots = slotsFor(chassis, core);
        block.drillClass = drill == null ? "" : drill.drillClass;
        block.frameSeries = frameSeries(parts);
        for (ExtractorPartType slot : BONE_ORDER) {
            block.variantBones.add(slotBone(find(parts, slot), slot));
        }
        block.variantBones.addAll(attachmentBones(chassis, attachments, block.slots));
        return block;
    }

    private static void accumulate(PartData part, Map<ExtractorStat, Float> flat, Map<ExtractorStat, Float> percent, Map<ExtractorStat, Float> multiplier) {
        part.stats.forEach((stat, value) -> flat.merge(stat, value, Float::sum));
        part.percents.forEach((stat, value) -> percent.merge(stat, value, Float::sum));
        part.multipliers.forEach((stat, value) -> multiplier.merge(stat, value, (a, b) -> a * b));
    }

    /** The wooden or standard bone a structural slot shows when nothing is installed in it. */
    public static String defaultBone(ExtractorPartType slot) {
        String bone = DEFAULT_BONES.get(slot);
        if (bone == null) {
            WoldLogger.errorOnce("extractor_stats.default_bone." + slot.getId(),
                    "Extractor slot '{}' has no default rig bone; showing the default chassis.", slot.getId());
            return DEFAULT_BONES.get(ExtractorPartType.CHASSIS);
        }
        return bone;
    }

    /** The default bone of every structural slot, in render order: what an extractor with no parts shows. */
    public static List<String> defaultVariantBones() {
        List<String> bones = new ArrayList<>();
        for (ExtractorPartType slot : BONE_ORDER) {
            bones.add(DEFAULT_BONES.get(slot));
        }
        return bones;
    }

    /** Every bone the installed parts table and the defaults can produce; rebuilt when the table is reinstalled. */
    public static Collection<String> allBones() {
        ExtractorPartsConfig cfg = ModConfigs.EXTRACTOR_PARTS;
        if (cfg != boneSource) {
            Set<String> bones = new LinkedHashSet<>(defaultVariantBones());
            Set<String> suffixes = new LinkedHashSet<>();
            suffixes.add(DEFAULT_RIG_SUFFIX);
            cfg.SERIES.values().forEach(series -> {
                bones.addAll(series.rigBones.values());
                suffixes.add(series.rigSuffix);
            });
            cfg.DRILL_CLASSES.values().forEach(drill -> bones.add(drill.rigBone));
            cfg.ATTACHMENTS.values().forEach(attachment -> suffixes.forEach(suffix -> bones.add(attachmentBone(attachment.rigBone, suffix))));
            boneCache = Collections.unmodifiableSet(bones);
            boneSource = cfg;
        }
        return boneCache;
    }

    public static boolean isKnownBone(String bone) {
        return allBones().contains(bone);
    }

    /** The rig bone for the part in a slot, or the slot's default when the part is missing or unknown to the config. */
    public static String slotBone(@Nullable PartData part, ExtractorPartType slot) {
        ExtractorPartsConfig cfg = ModConfigs.EXTRACTOR_PARTS;
        String fallback = defaultBone(slot);
        if (part == null) {
            return fallback;
        }
        if (slot == ExtractorPartType.DRILL) {
            ExtractorPartsConfig.DrillClassEntry drillClass = cfg.DRILL_CLASSES.get(part.drillClass);
            if (drillClass == null || drillClass.rigBone == null) {
                WoldLogger.errorOnce("extractor_stats.drill_bone." + part.drillClass,
                        "Extractor drill class '{}' has no rig bone; using {}.", part.drillClass, fallback);
                return fallback;
            }
            return drillClass.rigBone;
        }
        ExtractorPartsConfig.SeriesEntry series = cfg.getSeries(part.series);
        if (series == null || !series.rigBones.containsKey(slot.getId())) {
            WoldLogger.errorOnce("extractor_stats.series_bone." + part.series + "." + slot.getId(),
                    "Extractor series '{}' has no rig bone for slot '{}'; using {}.", part.series, slot.getId(), fallback);
            return fallback;
        }
        return series.rigBones.get(slot.getId());
    }

    /**
     * The bone for each installed attachment, in mount order. Attachments are authored once per role and stamped in
     * every series palette, so the bone takes the chassis's rig suffix; with no chassis it takes the first palette.
     */
    public static List<String> attachmentBones(@Nullable PartData chassis, List<PartData> attachments, int slots) {
        ExtractorPartsConfig cfg = ModConfigs.EXTRACTOR_PARTS;
        List<String> bones = new ArrayList<>();
        String suffix = DEFAULT_RIG_SUFFIX;
        if (chassis != null) {
            ExtractorPartsConfig.SeriesEntry series = cfg.getSeries(chassis.series);
            if (series == null || series.rigSuffix == null) {
                WoldLogger.errorOnce("extractor_stats.rig_suffix." + chassis.series,
                        "Extractor series '{}' has no rig suffix; its attachments use the {} palette.", chassis.series, DEFAULT_RIG_SUFFIX);
            } else {
                suffix = series.rigSuffix;
            }
        }
        for (PartData attachment : attachments) {
            if (bones.size() >= Math.min(slots, MAX_HARDPOINTS)) {
                break;
            }
            ExtractorPartsConfig.AttachmentEntry entry = cfg.ATTACHMENTS.get(attachment.attachmentId);
            if (entry == null || entry.rigBone == null) {
                WoldLogger.errorOnce("extractor_stats.attachment_bone." + attachment.attachmentId,
                        "Extractor attachment '{}' has no rig bone; it will not be rendered.", attachment.attachmentId);
                continue;
            }
            bones.add(attachmentBone(entry.rigBone, suffix));
        }
        return bones;
    }

    private static String attachmentBone(String rigBone, String suffix) {
        return ATTACH_PREFIX + rigBone + "_" + suffix;
    }

    @Nullable
    public static String jobFor(String drillClass) {
        ExtractorPartsConfig.DrillClassEntry entry = ModConfigs.EXTRACTOR_PARTS.DRILL_CLASSES.get(drillClass);
        return entry == null ? null : entry.job;
    }
}
