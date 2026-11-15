package xyz.iwolfking.woldsvaults.client.spellcraft.gui;

import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphStats;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphType;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Map;

public final class SpellcraftPalette {
    static final int ARCANE_DEEP = 0xFF0B0714;
    public static final int ARCANE_LINE = 0xFF6C4AA8;
    public static final int ARCANE_GLOW = 0xFFB48CFF;
    public static final int ARCANA = 0xFFC08CFF;
    static final int ARCANA_DIM = 0xFF4A2F78;
    public static final int ARCANA_TEXT = 0xFF5A2A9A;
    public static final int WHITE = 0xFFFFFFFF;
    public static final int GREY = 0xFFAAAAAA;
    public static final int DARK = 0xFF555555;
    public static final int MUTED = 0xFF9A9A9A;
    public static final int LABEL = 0xFF000000;
    public static final int BAD = 0xFFFF6A6A;
    public static final int BAD_TEXT = 0xFFB01010;
    public static final int GOLD = 0xFFFFE34D;
    public static final int NAME_GOLD = 0xFFFFF7C7;
    static final int BORDER = 0xFF101010;
    static final int BAR_TICK = 0x60FFFFFF;
    static final int OVER_TINT = 0x80FF3030;
    public static final int STAT_DAMAGE = 0xFFE0564A;
    public static final int STAT_AREA = 0xFF5FD068;
    public static final int STAT_COOLDOWN = 0xFF4AC6E0;
    public static final int STAT_MANA = 0xFF6FA8FF;
    public static final int STAT_INTENSITY = 0xFFFFAA00;
    public static final int STAT_YELLOW = 0xFFFFFF55;
    public static final int STAT_PLAIN = 0xFFD9C98A;
    public static final int STAT_PROJECTILES = 0xFFE070E0;
    private static final Map<Integer, Integer> ON_GREY = Map.of(
            STAT_DAMAGE, 0xFF9E1B12, STAT_AREA, 0xFF1F6E2A, STAT_COOLDOWN, 0xFF0F5E72, STAT_MANA, 0xFF1F4FA0, STAT_INTENSITY, 0xFF8E4A00,
            STAT_YELLOW, 0xFF6E5200, STAT_PLAIN, 0xFF5C4A12, STAT_PROJECTILES, 0xFF7A1F7A);

    private static final Map<String, Integer> CATEGORY_COLORS = Map.of(
            "power", STAT_DAMAGE, "flow", STAT_COOLDOWN, "path", STAT_AREA, "form", 0xFFE8B84A, "chaos", 0xFFB46CF0,
            "unique", 0xFFF0912E);
    static final Map<String, String> STAT_TAGS = Map.ofEntries(
            Map.entry(GlyphStats.DAMAGE, hexTag(STAT_DAMAGE)), Map.entry(GlyphStats.AREA, hexTag(STAT_AREA)), Map.entry(GlyphStats.COOLDOWN, hexTag(STAT_COOLDOWN)),
            Map.entry(GlyphStats.HASTE, hexTag(STAT_COOLDOWN)), Map.entry(GlyphStats.MANA, hexTag(STAT_MANA)), Map.entry(GlyphStats.EFFICIENCY, hexTag(STAT_MANA)),
            Map.entry(GlyphStats.HEAL_MANA, hexTag(STAT_MANA)), Map.entry(GlyphStats.INTENSITY, "<gold>"), Map.entry(GlyphStats.SPEED, "<yellow>"),
            Map.entry(GlyphStats.SCALE, "<yellow>"), Map.entry(GlyphStats.DURATION, "<yellow>"), Map.entry(GlyphStats.DRAIN, "<yellow>"));

    private SpellcraftPalette() {
    }

    private static String hexTag(int color) {
        return "<#" + String.format(Locale.ROOT, "%06X", color & 0xFFFFFF) + ">";
    }

    /** A stat colour made for dark tooltips, darkened to read on a grey inset. */
    public static int onGrey(int color) {
        Integer dark = ON_GREY.get(color);
        if (dark == null) {
            WoldLogger.errorOnce("spellcraft.on_grey." + Integer.toHexString(color),
                    "Spellcraft UI: stat colour {} has no grey-inset variant; its numbers show black", Integer.toHexString(color));
            return LABEL;
        }
        return dark;
    }

    public static int categoryColor(@Nullable SpellcraftConfig config, GlyphType type) {
        if (config == null || config.glyph(type) == null) {
            return GREY;
        }
        return CATEGORY_COLORS.getOrDefault(config.glyph(type).category, GREY);
    }
}
