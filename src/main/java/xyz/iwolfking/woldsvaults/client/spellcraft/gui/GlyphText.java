package xyz.iwolfking.woldsvaults.client.spellcraft.gui;

import iskallia.vault.gear.etching.EtchingHelper;
import iskallia.vault.util.TextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.client.spellcraft.stats.GlyphRelevance;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.spellcraft.ability.SpellAbilities;
import xyz.iwolfking.woldsvaults.spellcraft.gear.ConvertedEtchings;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphStacking;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphType;
import xyz.iwolfking.woldsvaults.spellcraft.source.GlyphQuality;
import xyz.iwolfking.woldsvaults.spellcraft.spell.PlacedGlyph;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public final class GlyphText {
    private static final String LABEL_TAG = "<gray>";
    private static final Set<GlyphType> MISSING_DESCRIPTIONS = EnumSet.noneOf(GlyphType.class);
    private static final Set<String> UNKNOWN_STATS = new HashSet<>();
    private static boolean noConfigWarned;

    private GlyphText() {
    }

    /** A tooltip line saying why a glyph does nothing right now, empty for an active glyph. */
    public static String inactiveReason(@Nullable SpellcraftConfig config, PlacedGlyph glyph) {
        if (glyph.dependent()) {
            return SpellcraftNames.tr("glyph_state.dependent");
        }
        return switch (glyph.state()) {
            case ACTIVE -> "";
            case DORMANT -> SpellcraftNames.tr(spellWide(config, glyph.type()) ? "glyph_state.dormant_spell" : "glyph_state.dormant");
            case INACTIVE_SOURCE -> SpellcraftNames.tr("glyph_state.source", glyph.type().displayName());
            case INACTIVE_USES -> SpellcraftNames.tr("glyph_state.uses", glyph.type().displayName());
            case INACTIVE_BUDGET -> SpellcraftNames.tr("glyph_state.budget");
        };
    }

    private static boolean spellWide(@Nullable SpellcraftConfig config, GlyphType type) {
        if (config == null) {
            if (!noConfigWarned) {
                noConfigWarned = true;
                WoldLogger.error("Spellcraft UI: no spellcraft table; a dormant {} is described as not fitting its step", type.id());
            }
            return false;
        }
        return config.glyph(type).spellWide();
    }

    /**
     * An etching glyph shows its etching's own text and every other glyph fills its table template. A glyph with
     * neither logs an error once and shows its potency text.
     */
    public static MutableComponent describe(SpellcraftConfig config, GlyphType type, float potency, float quality) {
        String text = ConvertedEtchings.description(type, potency, quality);
        if (text == null) {
            String template = config.glyph(type).description;
            if (template != null && !template.isBlank()) {
                text = EtchingHelper.formatDescription(fillLinked(config, type, template, quality), Optional.of(potency));
            }
        }
        if (text == null) {
            if (MISSING_DESCRIPTIONS.add(type)) {
                WoldLogger.error("Spellcraft: glyph {} has no description; its tooltip shows the potency text", type.id());
            }
            return new TextComponent(potency(config, type, potency)).withStyle(ChatFormatting.GRAY);
        }
        return TextUtil.applyColorTags(SpellcraftNames.capitalize(text));
    }

    private static String fillLinked(SpellcraftConfig config, GlyphType type, String template, float quality) {
        SpellcraftConfig.GlyphDefinition glyph = config.glyph(type);
        if (glyph.potency == null || glyph.potency.linked == null) {
            return template;
        }
        String out = template;
        for (SpellcraftConfig.Linked linked : glyph.potency.linked) {
            float value = GlyphQuality.linkedValue(linked, quality);
            out = out.replace("{" + linked.name + "}", Integer.toString(Math.round(value * 100.0F)));
        }
        return out;
    }

    public static String potency(SpellcraftConfig config, GlyphType type, float p) {
        return SpellcraftNames.capitalize(rawPotency(config, type, p));
    }

    /**
     * What a glyph fits: the names of its abilities when it lists any, else each trait set it requires, joined with
     * plus signs.
     */
    public static List<String> fits(SpellcraftConfig.GlyphDefinition glyph) {
        List<String> fits = new ArrayList<>();
        if (glyph.specs != null && !glyph.specs.isEmpty()) {
            for (String spec : glyph.specs) {
                String name = SpellAbilities.displayName(spec);
                if (!fits.contains(name)) {
                    fits.add(name);
                }
            }
            return fits;
        }
        for (List<String> set : glyph.requires) {
            List<String> words = new ArrayList<>();
            set.forEach(trait -> words.add(SpellcraftNames.capitalize(trait)));
            if (!words.isEmpty()) {
                fits.add(String.join(" + ", words));
            }
        }
        return fits;
    }

    private static String rawPotency(SpellcraftConfig config, GlyphType type, float p) {
        SpellcraftConfig.Potency potency = config.glyph(type).potency;
        return switch (potency.format) {
            case "pct" -> "+" + Math.round(p * 100) + "% " + potency.label;
            case "neg_pct" -> "-" + Math.round(p * 100) + "% " + potency.label;
            case "pct_plain" -> Math.round(p * 100) + "% " + potency.label;
            case "seconds" -> potency.label + " " + StatText.trim(p) + "s";
            case "blocks" -> potency.label + " " + Math.round(p);
            case "deg_s" -> potency.label + " " + Math.round(p) + "°/s";
            case "none" -> potency.label;
            default -> potency.label + " " + StatText.trim(p);
        };
    }

    /**
     * The glyph's side effects at a potency, skipping the potency's own stat (the effect with k of plus or minus 1 and
     * no base). Effects that add into the step's pool read as a percentage and the rest as a factor.
     */
    public static String sideEffects(SpellcraftConfig config, GlyphType type, float p) {
        return sideEffects(config, type, p, GlyphRelevance.ALL);
    }

    public static String sideEffects(SpellcraftConfig config, GlyphType type, float p, GlyphRelevance.Filter relevance) {
        SpellcraftConfig.GlyphDefinition glyph = config.glyph(type);
        List<String> parts = new ArrayList<>();
        List<SpellcraftConfig.Effect> effects = new ArrayList<>(glyph.step);
        effects.addAll(glyph.spell);
        for (SpellcraftConfig.Effect effect : effects) {
            if (effect.k != null && effect.base == null && Math.abs(Math.abs(effect.k) - 1.0F) < 1.0E-6F) {
                continue;
            }
            boolean pooled = GlyphStacking.pooled(glyph, effect);
            if (!relevance.applies(effect.stat, pooled)) {
                continue;
            }
            float factor = effect.factor(p);
            String stat = statName(effect.stat);
            if (effect.k == null && effect.fixed == null && effect.perUse != null) {
                stat = SpellcraftNames.tr("stat.per_use", stat);
            }
            String tag = statTag(effect.stat);
            String part = pooled ? percent(factor - 1.0F, stat, tag) : times(factor, stat, tag);
            if (part != null) {
                parts.add(part);
            }
        }
        return SpellcraftNames.capitalize(String.join(LABEL_TAG + ", ", parts));
    }

    /**
     * A step's stacked glyph stats, each stat's pooled upsides first and then each stat's multiplied part, listing only
     * the stats {@code relevance} says apply. Empty when no glyph moves a stat that applies.
     */
    public static String poolSummary(GlyphStacking.Pool pool, GlyphRelevance.Filter relevance) {
        List<String> pooled = new ArrayList<>();
        List<String> multiplied = new ArrayList<>();
        for (GlyphStacking.Stat stat : pool.stats().values()) {
            String name = statName(stat.name());
            String tag = statTag(stat.name());
            if (stat.pool() != 0.0F && relevance.applies(stat.name(), true)) {
                float change = GlyphStacking.lowerIsBetter(stat.name()) ? 1.0F / (1.0F + stat.pool()) - 1.0F : stat.pool();
                String part = percent(change, name, tag);
                if (part != null) {
                    pooled.add(part);
                }
            }
            if (relevance.applies(stat.name(), false)) {
                String part = times(stat.perCopy() * stat.special(), name, tag);
                if (part != null) {
                    multiplied.add(part);
                }
            }
        }
        String separator = LABEL_TAG + ", ";
        if (pooled.isEmpty() || multiplied.isEmpty()) {
            pooled.addAll(multiplied);
            return String.join(separator, pooled);
        }
        return String.join(separator, pooled) + LABEL_TAG + "; " + String.join(separator, multiplied);
    }

    public static MutableComponent tagged(String text) {
        return TextUtil.applyColorTags(LABEL_TAG + text);
    }

    private static String statTag(String stat) {
        String tag = SpellcraftPalette.STAT_TAGS.get(stat);
        if (tag == null) {
            if (UNKNOWN_STATS.add(stat)) {
                WoldLogger.error("Spellcraft UI: glyph stat '{}' has no colour; its numbers show yellow", stat);
            }
            return "<yellow>";
        }
        return tag;
    }

    private static String statName(String stat) {
        return SpellcraftNames.tr("stat." + stat.toLowerCase(Locale.ROOT));
    }

    @Nullable
    private static String percent(float change, String name, String tag) {
        int pct = Math.round(change * 100.0F);
        return pct == 0 ? null : tag + (pct > 0 ? "+" : "") + pct + "%" + LABEL_TAG + " " + name;
    }

    @Nullable
    private static String times(float factor, String name, String tag) {
        String text = StatText.trim(factor);
        return "1".equals(text) ? null : tag + text + "x" + LABEL_TAG + " " + name;
    }
}
