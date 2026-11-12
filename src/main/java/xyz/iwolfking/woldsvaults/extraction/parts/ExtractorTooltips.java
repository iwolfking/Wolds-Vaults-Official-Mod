package xyz.iwolfking.woldsvaults.extraction.parts;

import iskallia.vault.gear.VaultGearRarity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import xyz.iwolfking.woldsvaults.config.ExtractorPartsConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The shared vocabulary of every extractor tooltip: names composed from affixes, stat lines in the
 * stat's colour, series colours, and the implicit / prefix / suffix layout that mirrors vault gear.
 * Base values print without a sign; modifiers (percent effects, affixes, multipliers) keep theirs.
 */
public final class ExtractorTooltips {
    public static final int QUALITY_COLOR = 0xB0C0FA;
    public static final int SLOT_COLOR = 0xE2E28A;
    public static final int DRILL_CLASS_COLOR = 0xD9A6FF;
    public static final int BLUEPRINT_COLOR = 0x8FD3FF;
    public static final String FRAME_TAG = "TooltipFrame";
    public static final String FRAME_BLUEPRINT = "blueprint";
    public static final String[] GROUP_NAMES = {"Core", "Defensive", "Energy", "Misc"};
    /** The four stat groups every finished extractor is read in; the last group also takes whatever else is non-zero. */
    public static final ExtractorStat[][] GROUPS = {
            {ExtractorStat.POWER, ExtractorStat.SPEED, ExtractorStat.HEALTH, ExtractorStat.ARMOR},
            {ExtractorStat.SHIELD, ExtractorStat.SHIELD_TOUGHNESS, ExtractorStat.SHIELD_REGEN},
            {ExtractorStat.ENERGY_CAPACITY, ExtractorStat.DRAIN, ExtractorStat.ENERGY_PER_CELL},
            {ExtractorStat.QUANTITY, ExtractorStat.RARITY, ExtractorStat.DURABILITY_DRAIN_MULT, ExtractorStat.HEALTH_REGEN,
                    ExtractorStat.TURRET, ExtractorStat.EFFECT_RESISTANCE, ExtractorStat.ELITE_RESISTANCE, ExtractorStat.HEALING_EFFICIENCY}};
    private static final int EMPTY_AFFIX_COLOR = 0x555555;
    private static final int DEPLETED_COLOR = 0xFF5A5A;
    private static final Map<String, Integer> SERIES_COLORS = Map.of(
            ExtractorPartsConfig.SERIES_WOODEN, 0xE0B573,
            ExtractorPartsConfig.SERIES_CHROMATIC, 0xC1BFD0,
            ExtractorPartsConfig.SERIES_EXPERIMENTAL, 0x6FD8F0,
            ExtractorPartsConfig.SERIES_ARMORED, 0xD98A2B,
            ExtractorPartsConfig.SERIES_ECHOSTEEL, 0xB36BFF);

    private ExtractorTooltips() {
    }

    public static boolean hasShiftDown() {
        return DistExecutor.unsafeRunForDist(() -> Screen::hasShiftDown, () -> () -> false);
    }

    public static int seriesColor(String series) {
        return SERIES_COLORS.getOrDefault(series, 0xFFFFFF);
    }

    public static String seriesName(String series) {
        ExtractorPartsConfig.SeriesEntry entry = ModConfigs.EXTRACTOR_PARTS.getSeries(series);
        return entry == null || entry.name == null ? series : entry.name;
    }

    public static String drillClassName(String drillClass) {
        ExtractorPartsConfig.DrillClassEntry entry = ModConfigs.EXTRACTOR_PARTS.DRILL_CLASSES.get(drillClass);
        return entry == null || entry.name == null ? drillClass : entry.name;
    }

    public static String jobName(String drillClass) {
        ExtractorPartsConfig.DrillClassEntry entry = ModConfigs.EXTRACTOR_PARTS.DRILL_CLASSES.get(drillClass);
        return entry == null || entry.job == null ? "" : titleCase(entry.job);
    }

    public static String rarityName(VaultGearRarity rarity) {
        return titleCase(ExtractorPartsConfig.rarityKey(rarity));
    }

    /** {@code of_recharging} becomes {@code of Recharging}; {@code reinforced} becomes {@code Reinforced}. */
    public static String affixName(String id) {
        StringBuilder out = new StringBuilder();
        for (String word : id.split("_")) {
            if (word.isEmpty()) {
                continue;
            }
            if (out.length() > 0) {
                out.append(' ');
            }
            out.append(word.equals("of") ? word : capitalize(word));
        }
        return out.toString();
    }

    public static String titleCase(String id) {
        StringBuilder out = new StringBuilder();
        for (String word : id.split("_")) {
            if (word.isEmpty()) {
                continue;
            }
            if (out.length() > 0) {
                out.append(' ');
            }
            out.append(capitalize(word));
        }
        return out.toString();
    }

    private static String capitalize(String word) {
        return word.substring(0, 1).toUpperCase(Locale.ROOT) + word.substring(1);
    }

    /** {@code [Prefix] base [Suffix]} from the rolled affixes; attachments and affix-less parts keep the base name. */
    public static String composeName(PartData part) {
        String base = part.baseName == null || part.baseName.isEmpty() ? part.name : part.baseName;
        String prefix = "";
        String suffix = "";
        for (PartData.Affix affix : part.affixes) {
            if (affix.prefix && prefix.isEmpty()) {
                prefix = affixName(affix.id);
            } else if (!affix.prefix && suffix.isEmpty()) {
                suffix = affixName(affix.id);
            }
        }
        StringBuilder name = new StringBuilder();
        if (!prefix.isEmpty()) {
            name.append(prefix).append(' ');
        }
        name.append(base);
        if (!suffix.isEmpty()) {
            name.append(' ').append(suffix);
        }
        return name.toString();
    }

    public static String extractorName(String frameSeries) {
        return frameSeries == null || frameSeries.isEmpty() ? "Extractor" : seriesName(frameSeries) + " Extractor";
    }

    /** Thousands separators and no decimals from 1,000 up; at most one decimal below that. Negative values keep their sign. */
    public static String formatNumber(double value) {
        double abs = Math.abs(value);
        if (abs >= 1000.0D) {
            return String.format("%,.0f", value);
        }
        if (Math.abs(value - Math.rint(value)) < 0.05D) {
            return String.format("%.0f", value);
        }
        return String.format("%.1f", value);
    }

    public static String formatSigned(double value) {
        return (value >= 0.0D ? "+" : "-") + formatNumber(Math.abs(value));
    }

    public static Component text(String value, int color) {
        return new TextComponent(value).withStyle(Style.EMPTY.withColor(color));
    }

    public static MutableComponent labelValue(String label, String value, int color) {
        return new TextComponent(label).withStyle(ChatFormatting.WHITE).append(text(value, color));
    }

    public static Component quality(float quality) {
        return labelValue("Quality: ", String.format("%.2f", quality), QUALITY_COLOR);
    }

    public static Component seriesLine(String series) {
        return labelValue("Series: ", seriesName(series), seriesColor(series));
    }

    /**
     * The readout of a stat value: {@code 402 Power}, {@code +3.4% Health}, {@code 1.5/s Energy Drain}.
     * {@code signed} adds the leading plus that marks a modifier rather than a base value.
     */
    public static String statText(ExtractorStat stat, float value, boolean percent, boolean signed) {
        if (stat.getUnit() == ExtractorStat.Unit.FLAG) {
            return stat.getDisplayName();
        }
        String number = signed ? formatSigned(value) : formatNumber(value);
        if (percent || stat.getUnit() == ExtractorStat.Unit.PERCENT) {
            return number + "% " + stat.getDisplayName();
        }
        if (stat.getUnit() == ExtractorStat.Unit.RATE) {
            return number + "/s " + stat.getDisplayName();
        }
        return number + " " + stat.getDisplayName();
    }

    public static Component statLine(ExtractorStat stat, float value, boolean percent, boolean signed) {
        return text(statText(stat, value, percent, signed), stat.getColor());
    }

    /** {@code 15% Reduced Energy Drain}; null for a x1.00 multiplier, which changes nothing. */
    @Nullable
    public static String multiplierText(ExtractorStat stat, float multiplier) {
        float delta = (multiplier - 1.0F) * 100.0F;
        if (Math.abs(delta) < 0.05F) {
            return null;
        }
        return formatNumber(Math.abs(delta)) + "% " + (delta < 0.0F ? "Reduced " : "Increased ") + stat.getDisplayName();
    }

    @Nullable
    public static Component multiplierLine(ExtractorStat stat, float multiplier) {
        String line = multiplierText(stat, multiplier);
        return line == null ? null : text(line, stat.getColor());
    }

    /** One composed stat as the panel and the extractor tooltip print it; null when the stat is at its neutral value. */
    @Nullable
    public static String blockStatText(ExtractorStats.Block block, ExtractorStat stat) {
        float value = block.get(stat);
        if (stat.getKind() == ExtractorStat.Kind.MULTIPLIER) {
            return multiplierText(stat, value);
        }
        if (stat.getUnit() == ExtractorStat.Unit.FLAG) {
            return value >= 0.5F ? stat.getDisplayName() : null;
        }
        return Math.abs(value) < 0.05F ? null : statText(stat, value, false, false);
    }

    /** The part's rolled base values with every affix contribution subtracted, in the config's stat order. */
    public static List<Component> implicits(PartData part) {
        Map<ExtractorStat, Float> flat = new EnumMap<>(ExtractorStat.class);
        Map<ExtractorStat, Float> percent = new EnumMap<>(ExtractorStat.class);
        part.stats.forEach((stat, value) -> flat.put(stat, value));
        part.percents.forEach((stat, value) -> percent.put(stat, value));
        for (PartData.Affix affix : part.affixes) {
            Map<ExtractorStat, Float> target = affix.percent ? percent : flat;
            target.computeIfPresent(affix.stat, (stat, value) -> value - affix.value);
        }
        List<Component> lines = new ArrayList<>();
        for (Map.Entry<ExtractorStat, Float> entry : part.stats.entrySet()) {
            float value = flat.getOrDefault(entry.getKey(), 0.0F);
            if (Math.abs(value) >= 0.05F || entry.getKey().getUnit() == ExtractorStat.Unit.FLAG) {
                lines.add(statLine(entry.getKey(), value, false, false));
            }
        }
        for (Map.Entry<ExtractorStat, Float> entry : part.percents.entrySet()) {
            float value = percent.getOrDefault(entry.getKey(), 0.0F);
            if (Math.abs(value) >= 0.05F) {
                lines.add(statLine(entry.getKey(), value, true, true));
            }
        }
        for (Map.Entry<ExtractorStat, Float> entry : part.multipliers.entrySet()) {
            Component line = multiplierLine(entry.getKey(), entry.getValue());
            if (line != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    /**
     * Prefix then suffix lines. With {@code detail} the grey {@code Prefixes:} / {@code Suffixes:}
     * headers and the empty-slot placeholders are added, exactly like vault gear under Shift.
     */
    public static List<Component> affixGroup(PartData part, boolean prefix, boolean detail) {
        List<Component> lines = new ArrayList<>();
        List<Component> rolled = new ArrayList<>();
        for (PartData.Affix affix : part.affixes) {
            if (affix.prefix == prefix) {
                rolled.add(statLine(affix.stat, affix.value, affix.percent, true));
            }
        }
        if (detail) {
            lines.add(new TextComponent(prefix ? "Prefixes:" : "Suffixes:").withStyle(ChatFormatting.GRAY));
            if (rolled.isEmpty()) {
                lines.add(text(prefix ? "■ Empty Prefix" : "■ Empty Suffix", EMPTY_AFFIX_COLOR));
            }
        }
        lines.addAll(rolled);
        return lines;
    }

    /** How many prefix / suffix slots the rarity grants: 1 = prefix only, 2 = prefix and suffix. */
    public static int affixSlots(PartData part) {
        if (part.isAttachment()) {
            return 0;
        }
        return Math.min(2, ModConfigs.EXTRACTOR_PARTS.getAffixCount(part.rarity));
    }

    /**
     * An attachment's remaining charge as {@code (99%)}, for printing right after its name wherever it is
     * listed on something else. Empty for a part that has no pool of its own.
     */
    public static Component attachmentCharge(PartData attachment) {
        if (attachment.maxDurability <= 0) {
            return TextComponent.EMPTY;
        }
        float fraction = attachment.durabilityFraction();
        int percent = attachment.durability > 0 ? Math.max(1, (int) Math.floor(fraction * 100.0F)) : 0;
        return text(" (" + percent + "%)", percent <= 0 ? DEPLETED_COLOR : ExtractorStat.DURABILITY.getColor());
    }

    /** An attachment's own wear pool, which never touches the extractor's durability. */
    public static Component attachmentDurability(PartData attachment) {
        boolean spent = attachment.isDepleted();
        MutableComponent line = labelValue("Durability: ", String.format("%,d/%,d", attachment.durability, attachment.maxDurability),
                spent ? DEPLETED_COLOR : ExtractorStat.DURABILITY.getColor());
        return spent ? line.append(text("  (provides nothing until repaired)", DEPLETED_COLOR)) : line;
    }

    /** Everything under the title for a part, attachment or identified blueprint. */
    public static void appendPartBody(PartData part, List<Component> tooltip, boolean detail) {
        tooltip.add(quality(part.quality));
        if (part.type == ExtractorPartType.DRILL) {
            tooltip.add(labelValue("Drill Class: ", drillClassName(part.drillClass), DRILL_CLASS_COLOR));
            tooltip.add(labelValue("Job: ", jobName(part.drillClass), SLOT_COLOR));
        } else if (!part.isAttachment()) {
            tooltip.add(seriesLine(part.series));
        }
        tooltip.add(labelValue("Slot: ", part.type.getDisplayName(), SLOT_COLOR));
        if (part.isAttachment() && part.maxDurability > 0) {
            tooltip.add(attachmentDurability(part));
        }
        if (detail) {
            tooltip.add(new TextComponent("Rarity: ").withStyle(ChatFormatting.WHITE)
                    .append(new TextComponent(rarityName(part.rarity)).withStyle(PartData.style(part.rarity))));
        }
        tooltip.add(TextComponent.EMPTY);
        tooltip.addAll(implicits(part));
        int slots = affixSlots(part);
        if (slots <= 0) {
            return;
        }
        tooltip.add(TextComponent.EMPTY);
        tooltip.addAll(affixGroup(part, true, detail));
        if (slots >= 2) {
            tooltip.addAll(affixGroup(part, false, detail));
        }
    }

    /** The composed stats of a finished extractor in their four groups, each under a grey header, blank lines between. */
    public static void appendGroupedStats(ExtractorStats.Block block, List<Component> tooltip) {
        boolean first = true;
        for (int group = 0; group < GROUPS.length; group++) {
            List<Component> lines = new ArrayList<>();
            for (ExtractorStat stat : GROUPS[group]) {
                String line = blockStatText(block, stat);
                if (line != null) {
                    lines.add(text(line, stat.getColor()));
                }
            }
            if (lines.isEmpty()) {
                continue;
            }
            if (!first) {
                tooltip.add(TextComponent.EMPTY);
            }
            first = false;
            tooltip.add(new TextComponent(GROUP_NAMES[group] + ":").withStyle(ChatFormatting.GRAY));
            tooltip.addAll(lines);
        }
    }

    public static Component hint(String hint) {
        return new TextComponent(hint).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
    }

    public static Component partSummary(PartData part) {
        return new TextComponent(part.type.getDisplayName() + ": ").withStyle(ChatFormatting.GRAY)
                .append(new TextComponent(part.name).withStyle(PartData.style(part.rarity)))
                .append(text(String.format("  Q%.1f", part.quality), QUALITY_COLOR));
    }
}
