package xyz.iwolfking.woldsvaults.extraction.parts;

import iskallia.vault.gear.VaultGearRarity;
import xyz.iwolfking.woldsvaults.config.ExtractorPartsConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import java.util.Random;

/**
 * Quality: an unbounded value in hundredths centred on the chest opener's item rarity, nudged by blueprint rarity,
 * and the per-stat multipliers it drives (linear or geometric with a linear tail, per the table's scale families).
 */
public final class ExtractorQuality {
    private ExtractorQuality() {
    }

    private static ExtractorPartsConfig config() {
        return ModConfigs.EXTRACTOR_PARTS;
    }

    public static double center(double itemRarity) {
        ExtractorPartsConfig.QualityEntry q = config().QUALITY;
        double ir = Math.max(0.0D, itemRarity);
        double knee = q.centerA * Math.pow(q.kneeIr / q.centerIrScale, q.centerB);
        if (ir <= q.kneeIr) {
            return q.centerA * Math.pow(ir / q.centerIrScale, q.centerB);
        }
        return knee + q.kneeLogK * Math.log(ir / q.kneeIr);
    }

    /** Rolls a grade for a blueprint of the given rarity opened at the given item rarity. */
    public static float roll(double itemRarity, VaultGearRarity rarity, Random random) {
        ExtractorPartsConfig.QualityEntry q = config().QUALITY;
        String key = ExtractorPartsConfig.rarityKey(rarity);
        double center = center(itemRarity);
        double mult = q.rarityMult.getOrDefault(key, 1.0F);
        double flat = q.rarityFlat.getOrDefault(key, 0.0F);
        double width = q.rarityWidth.getOrDefault(key, 1.0F) * (q.widthBase + q.widthSlope * center);
        double value = center * mult + flat + (random.nextDouble() * 2.0D - 1.0D) * width;
        if (q.rollNoise > 0.0F) {
            value += random.nextGaussian() * q.rollNoise;
        }
        return Math.max(0.0F, Math.round(value * 100.0D) / 100.0F);
    }

    /** Multiplier a rolled base value receives at the given grade for the given stat. */
    public static double statMultiplier(ExtractorStat stat, double quality) {
        ExtractorPartsConfig.ScalesEntry scales = config().SCALES;
        Float geometric = scales.geometric.get(stat.getId());
        if (geometric != null) {
            double capped = Math.min(quality, scales.geometricTailQuality);
            double tail = Math.max(0.0D, quality - scales.geometricTailQuality);
            return Math.pow(1.0D + geometric, capped) * (1.0D + scales.geometricTailSlope * tail);
        }
        Float linear = scales.linear.get(stat.getId());
        if (linear != null) {
            return 1.0D + linear * quality;
        }
        return 1.0D;
    }
}
