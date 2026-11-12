package xyz.iwolfking.woldsvaults.client.spellcraft;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.atlas.TextureAtlasRegion;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.helper.FontHelper;
import iskallia.vault.gear.etching.EtchingHelper;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModTextureAtlases;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SpecializedSkill;
import iskallia.vault.skill.tree.AbilityTree;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.augments.AugmentType;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.augments.spell.GlyphRelevance;
import xyz.iwolfking.woldsvaults.augments.spell.GlyphStacking;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import iskallia.vault.util.TextUtil;
import xyz.iwolfking.woldsvaults.spellcraft.gear.EtchingGlyphs;

/**
 * Shared drawing for the spellcraft screens, the skill menu's spell bar and the focus tooltip: the H-menu window,
 * inset and slot textures, glyph runes (category tinted, 16 px textures drawn at any size), ability icons, bordered
 * small text, the arcana bar and the weave's starfield, plus the text helpers (translations, key labels, glyph potency).
 */
public final class SpellcraftGui {
    public static final int ARCANE_DEEP = 0xFF0B0714;
    public static final int ARCANE_LINE = 0xFF6C4AA8;
    public static final int ARCANE_GLOW = 0xFFB48CFF;
    public static final int ARCANA = 0xFFC08CFF;
    public static final int ARCANA_DIM = 0xFF4A2F78;
    public static final int ARCANA_TEXT = 0xFF5A2A9A;
    public static final int WHITE = 0xFFFFFFFF;
    public static final int GREY = 0xFFAAAAAA;
    public static final int DARK = 0xFF555555;
    public static final int MUTED = 0xFF9A9A9A;
    public static final int LABEL = 0xFF000000;
    public static final int GOOD = 0xFF6EF08C;
    public static final int BAD = 0xFFFF6A6A;
    public static final int BAD_TEXT = 0xFFB01010;
    public static final int GOLD = 0xFFFFE34D;
    public static final int NAME_GOLD = 0xFFFFF7C7;
    public static final int BORDER = 0xFF101010;
    public static final int BAR_TICK = 0x60FFFFFF;
    public static final int OVER_TINT = 0x80FF3030;
    public static final int STAT_DAMAGE = 0xFFE0564A;
    public static final int STAT_AREA = 0xFF5FD068;
    public static final int STAT_COOLDOWN = 0xFF4AC6E0;
    public static final int STAT_MANA = 0xFF6FA8FF;
    public static final int STAT_INTENSITY = 0xFFFFAA00;
    public static final int STAT_YELLOW = 0xFFFFFF55;
    public static final int STAT_PLAIN = 0xFFD9C98A;
    public static final String LABEL_TAG = "<gray>";

    private static final String LANG = "woldsvaults.spellcraft.";
    private static final Map<String, Integer> CATEGORY_COLORS = Map.of(
            "power", 0xFFE0564A, "flow", 0xFF4AC6E0, "path", 0xFF5FD068, "form", 0xFFE8B84A, "chaos", 0xFFB46CF0,
            "unique", 0xFFF0912E);
    private static final Map<AugmentType, ResourceLocation> RUNES = new EnumMap<>(AugmentType.class);
    private static final Set<String> MISSING_ICONS = new HashSet<>();
    private static final Set<AugmentType> MISSING_DESCRIPTIONS = EnumSet.noneOf(AugmentType.class);
    private static final Set<String> UNKNOWN_STATS = new HashSet<>();
    private static boolean noConfigWarned;
    private static final Map<String, String> STAT_TAGS = Map.ofEntries(
            Map.entry("damage", "<#E0564A>"), Map.entry("area", "<#5FD068>"), Map.entry("cooldown", "<#4AC6E0>"),
            Map.entry("haste", "<#4AC6E0>"), Map.entry("mana", "<#6FA8FF>"), Map.entry("efficiency", "<#6FA8FF>"),
            Map.entry("heal_mana", "<#6FA8FF>"), Map.entry("intensity", "<gold>"), Map.entry("speed", "<yellow>"),
            Map.entry("scale", "<yellow>"), Map.entry("duration", "<yellow>"), Map.entry("drain", "<yellow>"));

    static {
        for (AugmentType type : AugmentType.values()) {
            RUNES.put(type, WoldsVaults.id("textures/gui/spellcraft/glyph/" + type.id() + ".png"));
        }
    }

    private SpellcraftGui() {
    }

    /** A translated UI string under {@code woldsvaults.spellcraft.}. */
    public static String tr(String key, Object... args) {
        return I18n.get(LANG + key, args);
    }

    public static int categoryColor(@Nullable FocusAugmentsConfig config, AugmentType type) {
        if (config == null || config.glyph(type) == null) {
            return GREY;
        }
        return CATEGORY_COLORS.getOrDefault(config.glyph(type).category, GREY);
    }

    public static void window(PoseStack pose, int x0, int y0, int x1, int y1) {
        ScreenTextures.DEFAULT_WINDOW_BACKGROUND.blit(pose, x0, y0, 0, x1 - x0, y1 - y0);
    }

    public static void insetBlack(PoseStack pose, int x0, int y0, int x1, int y1) {
        ScreenTextures.INSET_BLACK_BACKGROUND.blit(pose, x0, y0, 0, x1 - x0, y1 - y0);
    }

    public static void insetGrey(PoseStack pose, int x0, int y0, int x1, int y1) {
        ScreenTextures.INSET_GREY_BACKGROUND.blit(pose, x0, y0, 0, x1 - x0, y1 - y0);
    }

    public static void insetDarkGrey(PoseStack pose, int x0, int y0, int x1, int y1) {
        ScreenTextures.INSET_DARK_GREY_BACKGROUND.blit(pose, x0, y0, 0, x1 - x0, y1 - y0);
    }

    /** An item-slot cell whose 16x16 content area starts at (x, y); the frame covers x-1 to x+17. */
    public static void slot(PoseStack pose, int x, int y, boolean disabled) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        (disabled ? ScreenTextures.INSET_DISABLED_ITEM_SLOT_BACKGROUND : ScreenTextures.INSET_ITEM_SLOT_BACKGROUND).blit(pose, x - 1, y - 1);
    }

    public static void ring(PoseStack pose, int x0, int y0, int x1, int y1, int color) {
        GuiComponent.fill(pose, x0, y0, x1, y0 + 1, color);
        GuiComponent.fill(pose, x0, y1 - 1, x1, y1, color);
        GuiComponent.fill(pose, x0, y0, x0 + 1, y1, color);
        GuiComponent.fill(pose, x1 - 1, y0, x1, y1, color);
    }

    /** A glyph rune at any size; dimmed runes are greyed and faded (spent or dormant). */
    public static void rune(PoseStack pose, AugmentType type, int x, int y, int size, boolean dim) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, RUNES.get(type));
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (dim) {
            RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 0.6F);
        } else {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
        GuiComponent.blit(pose, x, y, size, size, 0.0F, 0.0F, 16, 16, 16, 16);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /** The icon of an ability group's specialization, or of the group's first specialization. */
    @Nullable
    public static TextureAtlasRegion abilityIcon(@Nullable String specOrGroup) {
        if (specOrGroup == null || specOrGroup.isEmpty()) {
            return null;
        }
        ResourceLocation icon = ModConfigs.ABILITIES_GUI.getIconOrFirst(specOrGroup);
        if (icon == null) {
            if (MISSING_ICONS.add(specOrGroup)) {
                WoldsVaults.LOGGER.error("Spellcraft UI: no ability icon for {}; drawing an empty cell", specOrGroup);
            }
            return null;
        }
        return TextureAtlasRegion.of(ModTextureAtlases.ABILITIES, icon);
    }

    public static void ability(PoseStack pose, @Nullable String specOrGroup, int x, int y, int size, float alpha) {
        TextureAtlasRegion icon = abilityIcon(specOrGroup);
        if (icon == null) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        icon.blit(pose, x, y, 0, size, size);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /** Bordered text at a scale, drawn like a stack count; returns its scaled width. */
    public static float smallText(PoseStack pose, Font font, String text, float x, float y, int color, float scale) {
        pose.pushPose();
        pose.translate(x, y, 0.0);
        pose.scale(scale, scale, 1.0F);
        FontHelper.drawStringWithBorder(pose, text, 0.0F, 0.0F, color, BORDER);
        pose.popPose();
        return font.width(text) * scale;
    }

    /** The inset arcana bar: committed arcana dim, the edited spell bright, red when over budget. */
    public static void arcanaBar(PoseStack pose, int x, int y, int w, int h, float used, float pending, float total) {
        insetBlack(pose, x, y, x + w, y + h);
        int inner = w - 2;
        float budget = Math.max(1.0F, total);
        int u = (int) (inner * Math.min(used, total) / budget);
        int p = (int) (inner * Math.min(used + pending, total) / budget) - u;
        if (u > 0) {
            GuiComponent.fill(pose, x + 1, y + 1, x + 1 + u, y + h - 1, ARCANA_DIM);
        }
        if (p > 0) {
            GuiComponent.fill(pose, x + 1 + u, y + 1, x + 1 + u + p, y + h - 1, ARCANA);
        }
        for (int k = 1; k < 10; k++) {
            int tx = x + 1 + inner * k / 10;
            GuiComponent.fill(pose, tx, y + 1, tx + 1, y + 2, BAR_TICK);
        }
        if (Math.round(used + pending) > total) {
            GuiComponent.fill(pose, x + 1, y + 1, x + w - 1, y + h - 1, OVER_TINT);
        }
    }

    /** The weave inset: deep violet with faint stars, laid out from a fixed seed so it doesn't flicker. */
    public static void starfield(PoseStack pose, int x0, int y0, int x1, int y1) {
        GuiComponent.fill(pose, x0, y0, x1, y1, ARCANE_DEEP);
        Random random = new Random(11L);
        int count = (x1 - x0) * (y1 - y0) / 80;
        int[] colors = {0xFFFFFF, 0xD8C8FF, 0xB8E0FF};
        int[] alphas = {0x50, 0x80, 0xC0};
        for (int i = 0; i < count; i++) {
            int x = x0 + 1 + random.nextInt(Math.max(1, x1 - x0 - 3));
            int y = y0 + 1 + random.nextInt(Math.max(1, y1 - y0 - 3));
            int color = (alphas[random.nextInt(alphas.length)] << 24) | colors[random.nextInt(colors.length)];
            GuiComponent.fill(pose, x, y, x + 1, y + 1, color);
        }
    }

    /** The corner star of an augmented spell's cell. */
    public static void augmentStar(PoseStack pose, int x, int y) {
        GuiComponent.fill(pose, x + 11, y, x + 16, y + 5, 0xFF2A1840);
        GuiComponent.fill(pose, x + 13, y, x + 14, y + 5, ARCANE_GLOW);
        GuiComponent.fill(pose, x + 11, y + 2, x + 16, y + 3, ARCANE_GLOW);
    }

    public static void redX(PoseStack pose, int x, int y) {
        for (int d = 0; d < 10; d++) {
            GuiComponent.fill(pose, x + 3 + d, y + 3 + d, x + 5 + d, y + 4 + d, BAD);
            GuiComponent.fill(pose, x + 12 - d, y + 3 + d, x + 14 - d, y + 4 + d, BAD);
        }
    }

    public static void strike(PoseStack pose, int x, int y) {
        GuiComponent.fill(pose, x, y, x + 16, y + 16, 0x90200000);
        for (int d = 0; d < 16; d++) {
            GuiComponent.fill(pose, x + d, y + 15 - d, x + d + 1, y + 16 - d, BAD);
        }
    }

    /**
     * A short label for a spell slot key, at most {@code room} font pixels wide: mouse buttons as {@code M4}, other keys
     * by their name, cut to fit; empty when unbound.
     */
    public static String keyLabel(int slot, Font font, int room) {
        KeyMapping key = SpellBarKeys.KEYS[slot];
        if (key == null || key.isUnbound()) {
            return "";
        }
        String label = key.getKey().getType() == com.mojang.blaze3d.platform.InputConstants.Type.MOUSE
                ? "M" + (key.getKey().getValue() + 1)
                : key.getTranslatedKeyMessage().getString();
        return font.plainSubstrByWidth(label, room);
    }

    /** The key's full name for tooltips, or empty when unbound. */
    public static String keyName(int slot) {
        KeyMapping key = SpellBarKeys.KEYS[slot];
        if (key == null || key.isUnbound()) {
            return "";
        }
        return key.getTranslatedKeyMessage().getString();
    }

    /** An ability group as players read it: its current specialization's name, else the id with spaces. */
    public static String abilityName(@Nullable AbilityTree tree, String group) {
        if (tree != null) {
            Skill skill = tree.getForId(group).orElse(null);
            if (skill instanceof SpecializedSkill specialized && specialized.getSpecialization() != null
                    && specialized.getSpecialization().getName() != null) {
                return specialized.getSpecialization().getName();
            }
        }
        return group.replace('_', ' ');
    }

    /** The group's own name (no specialization), for compact lists such as "Stonefall + Nova". */
    public static String groupName(@Nullable AbilityTree tree, String group) {
        if (tree != null) {
            Skill skill = tree.getForId(group).orElse(null);
            if (skill != null && skill.getName() != null) {
                return skill.getName();
            }
        }
        return group.replace('_', ' ');
    }

    /** A line's potency in its table format (percent, seconds, blocks), labelled from the table. */
    /** Why a glyph does nothing right now, for tooltips; a dormant spell-wide glyph fits no step of the spell. */
    public static String inactiveReason(@Nullable FocusAugmentsConfig config, xyz.iwolfking.woldsvaults.augments.spell.SpellModel.Glyph glyph) {
        if (glyph.dependent()) {
            return tr("glyph_state.dependent");
        }
        return switch (glyph.state()) {
            case ACTIVE -> "";
            case DORMANT -> tr(spellWide(config, glyph.type()) ? "glyph_state.dormant_spell" : "glyph_state.dormant");
            case INACTIVE_SOURCE -> tr("glyph_state.source", glyph.type().displayName());
            case INACTIVE_USES -> tr("glyph_state.uses", glyph.type().displayName());
            case INACTIVE_BUDGET -> tr("glyph_state.budget");
        };
    }

    private static boolean spellWide(@Nullable FocusAugmentsConfig config, AugmentType type) {
        if (config == null) {
            if (!noConfigWarned) {
                noConfigWarned = true;
                WoldsVaults.LOGGER.error("Spellcraft UI: no focus augments table; a dormant {} is described as not fitting its step", type.id());
            }
            return false;
        }
        return config.glyph(type).spellWide();
    }

    /**
     * A glyph's description at a potency and quality, coloured with VH's tags: an etching glyph shows its etching's own
     * text; every other glyph fills its table template. Without either (logged once) it falls back to the potency text.
     */
    public static MutableComponent describe(FocusAugmentsConfig config, AugmentType type, float potency, float quality) {
        String text = EtchingGlyphs.description(type, potency, quality);
        if (text == null) {
            String template = config.glyph(type).description;
            if (template != null && !template.isBlank()) {
                text = EtchingHelper.formatDescription(template, Optional.of(potency));
            }
        }
        if (text == null) {
            if (MISSING_DESCRIPTIONS.add(type)) {
                WoldsVaults.LOGGER.error("Spellcraft: glyph {} has no description; its tooltip shows the potency text", type.id());
            }
            return new TextComponent(potency(config, type, potency)).withStyle(ChatFormatting.GRAY);
        }
        return TextUtil.applyColorTags(capitalize(text));
    }

    /** The text with its first letter upper-cased, skipping leading colour tags, signs and digits. */
    public static String capitalize(String text) {
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == '<') {
                int close = text.indexOf('>', i);
                if (close < 0) {
                    break;
                }
                i = close + 1;
                continue;
            }
            if (Character.isLetter(c)) {
                return text.substring(0, i) + Character.toUpperCase(c) + text.substring(i + 1);
            }
            if (Character.isDigit(c) || c == '+' || c == '-' || c == ' ' || c == '%' || c == '.') {
                return text;
            }
            i++;
        }
        return text;
    }

    public static String potency(FocusAugmentsConfig config, AugmentType type, float p) {
        return capitalize(rawPotency(config, type, p));
    }

    private static String rawPotency(FocusAugmentsConfig config, AugmentType type, float p) {
        FocusAugmentsConfig.Potency potency = config.glyph(type).potency;
        return switch (potency.format) {
            case "pct" -> "+" + Math.round(p * 100) + "% " + potency.label;
            case "neg_pct" -> "-" + Math.round(p * 100) + "% " + potency.label;
            case "pct_plain" -> Math.round(p * 100) + "% " + potency.label;
            case "seconds" -> potency.label + " " + trim(p) + "s";
            case "blocks" -> potency.label + " " + Math.round(p);
            case "deg_s" -> potency.label + " " + Math.round(p) + "°/s";
            case "none" -> potency.label;
            default -> potency.label + " " + trim(p);
        };
    }

    /**
     * The glyph's side effects at a potency, e.g. "+18% dmg, 0.93x area, 1.13x mana", every number coloured by its stat
     * and every label grey (VH colour tags, for {@link #tagged}): each step and spell effect except the potency's own
     * stat (the effect with k = +-1 and no base). Common upsides, which add into the step's pool, read as a percentage;
     * everything that multiplies reads as a factor. Without an ability, every effect is listed.
     */
    public static String sideEffects(FocusAugmentsConfig config, AugmentType type, float p) {
        return sideEffects(config, type, p, GlyphRelevance.ALL);
    }

    /** The glyph's side effects that apply where {@code relevance} says, coloured as {@link #sideEffects}. */
    public static String sideEffects(FocusAugmentsConfig config, AugmentType type, float p, GlyphRelevance.Filter relevance) {
        FocusAugmentsConfig.Glyph glyph = config.glyph(type);
        List<String> parts = new ArrayList<>();
        List<FocusAugmentsConfig.Effect> effects = new ArrayList<>(glyph.step);
        effects.addAll(glyph.spell);
        for (FocusAugmentsConfig.Effect effect : effects) {
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
                stat = tr("stat.per_use", stat);
            }
            String tag = statTag(effect.stat);
            String part = pooled ? percent(factor - 1.0F, stat, tag) : times(factor, stat, tag);
            if (part != null) {
                parts.add(part);
            }
        }
        return capitalize(String.join(LABEL_TAG + ", ", parts));
    }

    /**
     * A step's stacked glyph stats, e.g. "+48% dmg, +22% area; 0.86x area", coloured as {@link #sideEffects}: first each
     * stat's pool of common upsides, then each stat's multiplied part (downsides, costs and special factors), each only
     * where {@code relevance} says it applies. Empty when no glyph moves a stat that applies.
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

    /** Text with VH colour tags as a component; untagged text starts grey. */
    public static MutableComponent tagged(String text) {
        return TextUtil.applyColorTags(LABEL_TAG + text);
    }

    /** The colour tag of a glyph stat's numbers, matching the glyph descriptions and the spell bar's stat lines. */
    public static String statTag(String stat) {
        String tag = STAT_TAGS.get(stat);
        if (tag == null) {
            if (UNKNOWN_STATS.add(stat)) {
                WoldsVaults.LOGGER.warn("Spellcraft UI: glyph stat '{}' has no colour; its numbers show yellow", stat);
            }
            return "<yellow>";
        }
        return tag;
    }

    private static String statName(String stat) {
        return tr("stat." + stat.toLowerCase(Locale.ROOT));
    }

    @Nullable
    private static String percent(float change, String name, String tag) {
        int pct = Math.round(change * 100.0F);
        return pct == 0 ? null : tag + (pct > 0 ? "+" : "") + pct + "%" + LABEL_TAG + " " + name;
    }

    @Nullable
    private static String times(float factor, String name, String tag) {
        String text = trim(factor);
        return "1".equals(text) ? null : tag + text + "x" + LABEL_TAG + " " + name;
    }

    public static String trim(float value) {
        String text = String.format(Locale.ROOT, "%.2f", value);
        text = text.replaceAll("0+$", "");
        return text.endsWith(".") ? text.substring(0, text.length() - 1) : text;
    }

    public static String seconds(float ticks) {
        return String.format(Locale.ROOT, "%.1f", ticks / 20.0F);
    }

    /** The ability groups a spell casts (its tokens that are not glyphs), in order. */
    public static List<String> groups(SpellcraftItemData.Spell spell) {
        List<String> groups = new ArrayList<>();
        for (String token : spell.tokens()) {
            if (AugmentType.byId(token) == null && !groups.contains(token)) {
                groups.add(token);
            }
        }
        return groups;
    }
}
