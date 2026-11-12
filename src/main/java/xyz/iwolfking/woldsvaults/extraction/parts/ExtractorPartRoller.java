package xyz.iwolfking.woldsvaults.extraction.parts;

import iskallia.vault.gear.VaultGearRarity;
import xyz.iwolfking.woldsvaults.config.ExtractorPartsConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * Turns a blueprint (part type, rarity, quality) into a finished {@link PartData}: picks the series or
 * drill class allowed at that rarity, rolls every workbook range, scales by quality and adds affixes.
 */
public final class ExtractorPartRoller {
    private ExtractorPartRoller() {
    }

    private static ExtractorPartsConfig config() {
        return ModConfigs.EXTRACTOR_PARTS;
    }

    public static VaultGearRarity rollDropRarity(Random random) {
        ExtractorPartsConfig cfg = config();
        int total = 0;
        for (VaultGearRarity rarity : VaultGearRarity.values()) {
            total += Math.max(0, cfg.getRarityDropWeight(rarity));
        }
        if (total <= 0) {
            WoldLogger.error("extractor_parts.json RARITY_DROP_WEIGHTS sum to zero; falling back to scrappy blueprints.");
            return VaultGearRarity.SCRAPPY;
        }
        int pick = random.nextInt(total);
        for (VaultGearRarity rarity : VaultGearRarity.values()) {
            pick -= Math.max(0, cfg.getRarityDropWeight(rarity));
            if (pick < 0) {
                return rarity;
            }
        }
        return VaultGearRarity.SCRAPPY;
    }

    @Nullable
    public static String rollSeries(VaultGearRarity rarity, Random random) {
        List<String> allowed = new ArrayList<>();
        for (String series : config().getSeriesFor(rarity)) {
            if (config().getSeries(series) != null) {
                allowed.add(series);
            }
        }
        if (allowed.isEmpty()) {
            WoldLogger.error("No extractor series is configured for rarity {}; check SERIES_BY_RARITY in extractor_parts.json.", rarity);
            return null;
        }
        return allowed.get(random.nextInt(allowed.size()));
    }

    /** Drill classes that have both a class entry and a stat row; the rest are not rollable yet. */
    public static List<String> rollableDrillClasses(VaultGearRarity rarity) {
        List<String> classes = new ArrayList<>();
        config().DRILL_CLASSES.forEach((id, entry) -> {
            VaultGearRarity min = ExtractorPartsConfig.rarityOf(entry.minRarity);
            boolean rarityOk = min == null || rarity.ordinal() >= min.ordinal();
            if (rarityOk && config().getRoll(ExtractorPartType.DRILL, id) != null) {
                classes.add(id);
            }
        });
        return classes;
    }

    public static float drillMinQuality(String drillClass) {
        ExtractorPartsConfig.DrillClassEntry entry = config().DRILL_CLASSES.get(drillClass);
        return entry == null ? 0.0F : entry.minQuality;
    }

    /**
     * The rollable drill classes whose {@code minQuality} the given quality reaches. When it reaches none, the
     * lowest-gated classes are returned instead, so a blueprint always identifies into something.
     */
    public static List<String> eligibleDrillClasses(VaultGearRarity rarity, float quality) {
        List<String> rollable = rollableDrillClasses(rarity);
        List<String> eligible = new ArrayList<>();
        float lowest = Float.MAX_VALUE;
        for (String id : rollable) {
            float gate = drillMinQuality(id);
            if (quality >= gate) {
                eligible.add(id);
            }
            lowest = Math.min(lowest, gate);
        }
        if (eligible.isEmpty()) {
            for (String id : rollable) {
                if (drillMinQuality(id) <= lowest) {
                    eligible.add(id);
                }
            }
        }
        return eligible;
    }

    @Nullable
    public static String rollDrillClass(VaultGearRarity rarity, float quality, Random random) {
        List<String> classes = eligibleDrillClasses(rarity, quality);
        if (classes.isEmpty()) {
            WoldLogger.error("No drill class has both a DRILL_CLASSES entry and a PARTS.drill stat row; drills cannot roll.");
            return null;
        }
        String pick = classes.get(random.nextInt(classes.size()));
        if (quality < drillMinQuality(pick)) {
            WoldLogger.error("Quality {} reaches no drill class's minQuality at rarity {}; fell back to the lowest-gated class '{}'. Check DRILL_CLASSES minQuality in extractor_parts.json.",
                    quality, rarity, pick);
        }
        return pick;
    }

    @Nullable
    public static String rollAttachmentId(Random random) {
        List<String> ids = new ArrayList<>(config().ATTACHMENTS.keySet());
        if (ids.isEmpty()) {
            WoldLogger.error("extractor_parts.json has no ATTACHMENTS; attachment blueprints cannot roll.");
            return null;
        }
        return ids.get(random.nextInt(ids.size()));
    }

    /**
     * Rolls a finished part. {@code preset} forces the series (or drill class / attachment id) instead of rolling it.
     */
    @Nullable
    public static PartData roll(ExtractorPartType type, VaultGearRarity rarity, float quality, @Nullable String preset, Random random) {
        if (type == ExtractorPartType.ATTACHMENT) {
            return rollAttachment(rarity, quality, preset, random);
        }
        PartData data = new PartData();
        data.type = type;
        data.rarity = rarity;
        data.quality = quality;
        String key;
        if (type == ExtractorPartType.DRILL) {
            key = preset != null ? preset : rollDrillClass(rarity, quality, random);
            if (key == null) {
                return null;
            }
            data.drillClass = key;
            data.series = preset != null && config().getSeries(preset) != null ? preset : defaultSeries(rarity, random);
            ExtractorPartsConfig.DrillClassEntry drillClass = config().DRILL_CLASSES.get(key);
            data.name = drillClass == null ? key + " Drill" : drillClass.name + " Drill";
        } else {
            key = preset != null ? preset : rollSeries(rarity, random);
            if (key == null) {
                return null;
            }
            data.series = key;
            ExtractorPartsConfig.SeriesEntry series = config().getSeries(key);
            data.name = series == null ? key + " " + type.getDisplayName() : series.partName(type, series.name + " " + type.getDisplayName());
        }
        ExtractorPartsConfig.PartRollEntry roll = config().getRoll(type, key);
        if (roll == null) {
            WoldLogger.error("extractor_parts.json has no PARTS.{} row for '{}'; the part rolled with no stats.", type.getId(), key);
            return data;
        }
        for (Map.Entry<String, ExtractorPartsConfig.Range> entry : roll.stats.entrySet()) {
            ExtractorStat stat = ExtractorStat.byId(entry.getKey());
            if (stat == null) {
                WoldLogger.error("Unknown extractor stat '{}' in PARTS.{}.{}; skipped.", entry.getKey(), type.getId(), key);
                continue;
            }
            float base = entry.getValue().lerp(random.nextFloat());
            data.addStat(stat, (float) (base * ExtractorQuality.statMultiplier(stat, quality)));
        }
        for (Map.Entry<String, ExtractorPartsConfig.Range> entry : roll.multipliers.entrySet()) {
            ExtractorStat stat = ExtractorStat.byId(entry.getKey());
            if (stat == null) {
                WoldLogger.error("Unknown extractor multiplier stat '{}' in PARTS.{}.{}; skipped.", entry.getKey(), type.getId(), key);
                continue;
            }
            data.multiply(stat, entry.getValue().lerp(random.nextFloat()));
        }
        for (Map.Entry<String, Float> entry : roll.specials.entrySet()) {
            ExtractorStat stat = ExtractorStat.byId(entry.getKey());
            if (stat != null) {
                data.addStat(stat, (float) (entry.getValue() * ExtractorQuality.statMultiplier(stat, quality)));
            }
        }
        data.baseName = data.name;
        rollAffixes(data, random);
        data.name = ExtractorTooltips.composeName(data);
        return data;
    }

    private static String defaultSeries(VaultGearRarity rarity, Random random) {
        String series = rollSeries(rarity, random);
        return series == null ? ExtractorPartsConfig.SERIES_WOODEN : series;
    }

    @Nullable
    private static PartData rollAttachment(VaultGearRarity rarity, float quality, @Nullable String preset, Random random) {
        String id = preset != null ? preset : rollAttachmentId(random);
        if (id == null) {
            return null;
        }
        ExtractorPartsConfig.AttachmentEntry entry = config().ATTACHMENTS.get(id);
        if (entry == null) {
            WoldLogger.error("Unknown extractor attachment '{}' requested; nothing rolled.", id);
            return null;
        }
        PartData data = new PartData();
        data.type = ExtractorPartType.ATTACHMENT;
        data.attachmentId = id;
        data.series = defaultSeries(rarity, random);
        data.rarity = rarity;
        data.quality = quality;
        data.name = entry.name;
        data.baseName = entry.name;
        data.addStat(ExtractorStat.DRAIN, (float) (entry.drain.lerp(random.nextFloat()) * ExtractorQuality.statMultiplier(ExtractorStat.DRAIN, quality)));
        data.maxDurability = Math.max(0, Math.round((float) (entry.durability.lerp(random.nextFloat()) * ExtractorQuality.statMultiplier(ExtractorStat.DURABILITY, quality))));
        data.durability = data.maxDurability;
        for (ExtractorPartsConfig.EffectEntry effect : entry.effects) {
            ExtractorStat stat = ExtractorStat.byId(effect.stat);
            if (stat == null) {
                WoldLogger.error("Unknown extractor stat '{}' on attachment '{}'; skipped.", effect.stat, id);
                continue;
            }
            if (stat == ExtractorStat.DURABILITY) {
                WoldLogger.error("Attachment '{}' has a durability effect; attachments carry their own wear pool and never add to the extractor's, so it was skipped.", id);
                continue;
            }
            float value = effect.min + (effect.max - effect.min) * random.nextFloat();
            if (effect.percent) {
                data.addPercent(stat, value);
            } else if (stat.getKind() == ExtractorStat.Kind.MULTIPLIER) {
                data.multiply(stat, value);
            } else {
                data.addStat(stat, (float) (value * ExtractorQuality.statMultiplier(stat, quality)));
            }
        }
        return data;
    }

    private static void rollAffixes(PartData data, Random random) {
        int count = config().getAffixCount(data.rarity);
        if (count <= 0) {
            return;
        }
        String prefixPool = data.type.isStructural() ? "mechanical_prefix" : "hardware_prefix";
        String suffixPool = data.type.isStructural() ? "mechanical_suffix" : "hardware_suffix";
        String[] pools = {prefixPool, suffixPool};
        for (int i = 0; i < Math.min(count, pools.length); i++) {
            List<ExtractorPartsConfig.AffixEntry> pool = config().AFFIX_POOLS.get(pools[i]);
            if (pool == null || pool.isEmpty()) {
                WoldLogger.error("Affix pool '{}' is missing or empty in extractor_parts.json; the {} rolled without it.", pools[i], data.type.getId());
                continue;
            }
            ExtractorPartsConfig.AffixEntry affix = pool.get(random.nextInt(pool.size()));
            ExtractorStat stat = ExtractorStat.byId(affix.stat);
            if (stat == null) {
                WoldLogger.error("Affix '{}' names unknown stat '{}'; skipped.", affix.id, affix.stat);
                continue;
            }
            float value = affix.min + (affix.max - affix.min) * random.nextFloat();
            boolean prefix = i == 0;
            if (affix.percent) {
                data.addPercent(stat, value);
                data.affixes.add(new PartData.Affix(affix.id, stat, value, true, prefix));
            } else {
                float scaled = (float) (value * ExtractorQuality.statMultiplier(stat, data.quality));
                data.addStat(stat, scaled);
                data.affixes.add(new PartData.Affix(affix.id, stat, scaled, false, prefix));
            }
        }
    }
}
