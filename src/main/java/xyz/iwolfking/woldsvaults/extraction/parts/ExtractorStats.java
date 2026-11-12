package xyz.iwolfking.woldsvaults.extraction.parts;

import iskallia.vault.gear.VaultGearRarity;
import xyz.iwolfking.woldsvaults.config.ExtractorPartsConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * The composition formula: flat part values sum, attachment and affix percentages multiply the sum,
 * multiplier stats compound across every source. Attachments that have worn through their own pool
 * are skipped -- they stay bolted on and rendered, but contribute nothing until repaired. Pure, so
 * client previews and server assembly agree.
 */
public final class ExtractorStats {
    /** The chassis reserves exactly three hardpoints; a fourth attachment has nowhere to go. */
    public static final int MAX_RENDERED_ATTACHMENTS = 3;
    private static final Map<ExtractorPartType, String> DEFAULT_BONES = new EnumMap<>(ExtractorPartType.class);

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
         * The rig bones to unhide, in render order: one per structural slot, then one per
         * installed attachment. An attachment's POSITION among the attachment entries is
         * its chassis mount index, which is how the renderer knows where to put it and
         * which blank to hide. See ExtractorGeoModel.
         */
        public final List<String> variantBones = new ArrayList<>();

        public float get(ExtractorStat stat) {
            return this.values.getOrDefault(stat, stat.getKind() == ExtractorStat.Kind.MULTIPLIER ? 1.0F : 0.0F);
        }
    }

    public static int slotsFor(@Nullable PartData chassis, @Nullable PartData core) {
        ExtractorPartsConfig.SlotsEntry slots = ModConfigs.EXTRACTOR_PARTS.SLOTS;
        int count = chassis == null ? 0 : slots.chassis.getOrDefault(ExtractorPartsConfig.rarityKey(chassis.rarity), 0);
        if (core != null && core.rarity == VaultGearRarity.OMEGA) {
            count += slots.omegaCoreBonus;
        }
        return Math.min(slots.max, count);
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
        block.variantBones.add(slotBone(chassis, ExtractorPartType.CHASSIS));
        block.variantBones.add(slotBone(find(parts, ExtractorPartType.LEGS), ExtractorPartType.LEGS));
        block.variantBones.add(slotBone(core, ExtractorPartType.POWER_CORE));
        block.variantBones.add(slotBone(find(parts, ExtractorPartType.RADIATOR), ExtractorPartType.RADIATOR));
        block.variantBones.add(slotBone(find(parts, ExtractorPartType.SHIELD_ARRAY), ExtractorPartType.SHIELD_ARRAY));
        block.variantBones.add(slotBone(find(parts, ExtractorPartType.SCANNER), ExtractorPartType.SCANNER));
        block.variantBones.add(slotBone(drill, ExtractorPartType.DRILL));
        block.variantBones.addAll(attachmentBones(chassis, attachments, block.slots));
        return block;
    }

    private static void accumulate(PartData part, Map<ExtractorStat, Float> flat, Map<ExtractorStat, Float> percent, Map<ExtractorStat, Float> multiplier) {
        part.stats.forEach((stat, value) -> flat.merge(stat, value, Float::sum));
        part.percents.forEach((stat, value) -> percent.merge(stat, value, Float::sum));
        part.multipliers.forEach((stat, value) -> multiplier.merge(stat, value, (a, b) -> a * b));
    }

    /** The wooden or standard bone a slot shows when nothing is installed in it. */
    public static String defaultBone(ExtractorPartType slot) {
        return DEFAULT_BONES.getOrDefault(slot, "chassis_s1_wooden");
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
                WoldLogger.error("Extractor drill class '{}' has no rig bone; using {}.", part.drillClass, fallback);
                return fallback;
            }
            return drillClass.rigBone;
        }
        ExtractorPartsConfig.SeriesEntry series = cfg.getSeries(part.series);
        if (series == null || !series.rigBones.containsKey(slot.getId())) {
            WoldLogger.error("Extractor series '{}' has no rig bone for slot '{}'; using {}.", part.series, slot.getId(), fallback);
            return fallback;
        }
        return series.rigBones.get(slot.getId());
    }

    /**
     * The bone for each installed attachment, in mount order. Attachments have no series
     * of their own -- they are authored once per role and stamped in every palette -- so
     * the bone takes the CHASSIS's rig suffix, which is what makes a bolted-on module
     * match the machine it is bolted to.
     */
    public static List<String> attachmentBones(@Nullable PartData chassis, List<PartData> attachments, int slots) {
        ExtractorPartsConfig cfg = ModConfigs.EXTRACTOR_PARTS;
        List<String> bones = new ArrayList<>();
        ExtractorPartsConfig.SeriesEntry series = chassis == null ? null : cfg.getSeries(chassis.series);
        String suffix = series == null || series.rigSuffix == null ? "s1" : series.rigSuffix;
        for (PartData attachment : attachments) {
            if (bones.size() >= Math.min(slots, MAX_RENDERED_ATTACHMENTS)) {
                break;
            }
            ExtractorPartsConfig.AttachmentEntry entry = cfg.ATTACHMENTS.get(attachment.attachmentId);
            if (entry == null || entry.rigBone == null) {
                WoldLogger.error("Extractor attachment '{}' has no rig bone; it will not be rendered.", attachment.attachmentId);
                continue;
            }
            bones.add("attach_" + entry.rigBone + "_" + suffix);
        }
        return bones;
    }

    @Nullable
    public static String jobFor(String drillClass) {
        ExtractorPartsConfig.DrillClassEntry entry = ModConfigs.EXTRACTOR_PARTS.DRILL_CLASSES.get(drillClass);
        return entry == null ? null : entry.job;
    }
}
