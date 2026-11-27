package xyz.iwolfking.woldsvaults.client.spellcraft.editor;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SpecializedSkill;
import iskallia.vault.skill.tree.AbilityTree;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.GlyphText;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftDraw;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftNames;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftPalette;
import xyz.iwolfking.woldsvaults.client.spellcraft.stats.GlyphRelevance;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.spellcraft.ability.AbilityLookup;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphType;
import xyz.iwolfking.woldsvaults.spellcraft.source.GlyphViewLine;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class PouchPanel {
    private static final int SCROLL_W = 3;
    private static final int SCROLL_TRACK = 0xFF2A2A2A;
    private static final int SCROLL_THUMB = 0xFF8B8B8B;
    private static final int BORROWED_MARK = 0xFFB46CF0;
    private static final int TRANSFER_MARK = 0xFF5FD87E;
    private static final int FITS_TEXT = 0xFFE3CC7A;

    private final SpellcraftScreen screen;
    private final SpellDrafts drafts;
    private boolean abilitiesTab;
    private int pouchScroll;

    PouchPanel(SpellcraftScreen screen, SpellDrafts drafts) {
        this.screen = screen;
        this.drafts = drafts;
    }

    private List<String> pouchAbilities() {
        List<String> groups = new ArrayList<>();
        for (Skill skill : this.screen.tree().skills) {
            if (skill instanceof SpecializedSkill specialized && specialized.isUnlocked()) {
                groups.add(specialized.getId());
            }
        }
        return groups;
    }

    private int pouchSize() {
        SpellcraftConfig config = ModConfigs.SPELLCRAFT;
        return this.abilitiesTab ? pouchAbilities().size() : config == null ? 0 : this.drafts.view(config).lines().size();
    }

    private List<GlyphViewLine> pouchGlyphs(SpellcraftConfig config) {
        return new ArrayList<>(this.drafts.view(config).lines());
    }

    void render(PoseStack pose, SpellcraftConfig config) {
        Font font = this.screen.font();
        int x0 = 21;
        int y0 = EditorLayout.bodyTop();
        int x1 = x0 + EditorLayout.POUCH_W;
        SpellcraftDraw.window(pose, x0, y0, x1, EditorLayout.bodyBottom(this.screen.height));
        int tw = EditorLayout.pouchTabWidth();
        String[] labels = {SpellcraftNames.tr("screen.tab.glyphs"), SpellcraftNames.tr("screen.tab.abilities")};
        for (int k = 0; k < 2; k++) {
            int tx = EditorLayout.pouchTabX(k);
            boolean on = (k == 1) == this.abilitiesTab;
            if (on) {
                SpellcraftDraw.insetGrey(pose, tx, y0 + 4, tx + tw, y0 + 16);
            } else {
                SpellcraftDraw.insetDarkGrey(pose, tx, y0 + 4, tx + tw, y0 + 16);
            }
            String label = font.plainSubstrByWidth(labels[k], tw - 4);
            font.drawShadow(pose, label, tx + (tw - font.width(label)) / 2.0F, y0 + 6, on ? SpellcraftPalette.WHITE : SpellcraftPalette.MUTED);
        }
        int lx0 = EditorLayout.pouchListX0();
        int lx1 = EditorLayout.pouchListX1();
        int ly0 = EditorLayout.pouchListTop();
        int ly1 = EditorLayout.pouchListBottom(this.screen.height);
        SpellcraftDraw.insetBlack(pose, lx0, ly0, lx1, ly1);
        int fit = EditorLayout.pouchFit(this.screen.height);
        int size = pouchSize();
        this.pouchScroll = Math.max(0, Math.min(this.pouchScroll, size - fit));
        int right = lx1 - 3 - SCROLL_W;
        if (this.abilitiesTab) {
            List<String> groups = pouchAbilities();
            SpellDrafts.Draft draft = this.drafts.draft();
            for (int r = 0; r < Math.min(fit, groups.size() - this.pouchScroll); r++) {
                renderAbilityRow(pose, config, groups.get(r + this.pouchScroll), draft, lx0, ly0 + 2 + r * EditorLayout.ROW, right);
            }
        } else {
            List<GlyphViewLine> lines = pouchGlyphs(config);
            for (int r = 0; r < Math.min(fit, lines.size() - this.pouchScroll); r++) {
                renderGlyphRow(pose, config, lines.get(r + this.pouchScroll), lx0, ly0 + 2 + r * EditorLayout.ROW, right);
            }
        }
        if (size > fit) {
            int track = ly1 - ly0 - 4;
            int thumb = Math.max(6, track * fit / size);
            int top = ly0 + 2 + (track - thumb) * this.pouchScroll / Math.max(1, size - fit);
            GuiComponent.fill(pose, lx1 - SCROLL_W, ly0 + 2, lx1 - 1, ly1 - 2, SCROLL_TRACK);
            GuiComponent.fill(pose, lx1 - SCROLL_W, top, lx1 - 1, top + thumb, SCROLL_THUMB);
        }
    }

    private int usesLeft(GlyphViewLine line) {
        return line.limit() - usedInDraft(line.type());
    }

    private int usedInDraft(GlyphType type) {
        SpellDrafts.Draft draft = this.drafts.draft();
        return draft == null ? 0 : (int) draft.tokens.stream().filter(token -> token.equals(type.id())).count();
    }

    private void renderGlyphRow(PoseStack pose, SpellcraftConfig config, GlyphViewLine line, int lx0, int y, int right) {
        Font font = this.screen.font();
        int left = usesLeft(line);
        boolean spent = left <= 0;
        SpellcraftDraw.slot(pose, lx0 + 3, y + 1, false);
        SpellcraftDraw.rune(pose, line.type(), lx0 + 3, y + 1, 16, spent);
        if (line.borrowed()) {
            GuiComponent.fill(pose, lx0 + 15, y + 1, lx0 + 19, y + 5, BORROWED_MARK);
        } else if (line.transferable()) {
            GuiComponent.fill(pose, lx0 + 15, y + 1, lx0 + 19, y + 5, TRANSFER_MARK);
        }
        int tx = lx0 + 23;
        int room = right - tx;
        String uses = Math.max(0, left) + "/" + line.limit();
        String name = font.plainSubstrByWidth(line.type().displayName(), room - font.width(uses) - 3);
        font.drawShadow(pose, name, tx, y + 1, spent ? SpellcraftPalette.DARK : SpellcraftPalette.categoryColor(config, line.type()));
        font.drawShadow(pose, uses, right - font.width(uses), y + 1, spent ? SpellcraftPalette.DARK : SpellcraftPalette.WHITE);
        SpellcraftConfig.GlyphDefinition definition = config.glyph(line.type());
        String cost = definition.isUnique() ? "0/" + config.rules.uniqueMixCost : String.valueOf(line.cost());
        int cw = font.width(cost);
        List<String> fits = definition.isUnique() ? GlyphText.fits(definition) : List.of();
        if (fits.isEmpty()) {
            String potency = font.plainSubstrByWidth(GlyphText.potency(config, line.type(), line.potency()), room - cw - 9);
            font.drawShadow(pose, potency, tx, y + 10, SpellcraftPalette.GREY);
        } else {
            font.drawShadow(pose, fitsText(font, fits, room - cw - 9), tx, y + 10, spent ? SpellcraftPalette.DARK : FITS_TEXT);
        }
        GuiComponent.fill(pose, right - cw - 7, y + 12, right - cw - 3, y + 16, SpellcraftPalette.ARCANA);
        font.drawShadow(pose, cost, right - cw, y + 10, SpellcraftPalette.ARCANA);
    }

    /** The abilities a unique glyph fits, cut to the first name and a count of the rest when they don't fit in {@code room}. */
    private static String fitsText(Font font, List<String> fits, int room) {
        String all = String.join(", ", fits);
        if (font.width(all) <= room || fits.size() == 1) {
            return font.plainSubstrByWidth(all, room);
        }
        String more = " +" + (fits.size() - 1);
        return font.plainSubstrByWidth(fits.get(0), room - font.width(more)) + more;
    }

    private boolean spellable(SpellcraftConfig config, String spec) {
        List<String> traits = config.abilityTraits.get(spec);
        return traits != null && !config.isDisabled(spec) && traits.stream().noneMatch(t -> t.equalsIgnoreCase("hold"));
    }

    private void renderAbilityRow(PoseStack pose, SpellcraftConfig config, String group, @Nullable SpellDrafts.Draft draft, int lx0, int y, int right) {
        Font font = this.screen.font();
        AbilityTree tree = this.screen.tree();
        String spec = AbilityLookup.specOf(tree).apply(group);
        boolean usable = spec != null && spellable(config, spec) && (draft == null || !draft.tokens.contains(group));
        SpellcraftDraw.slot(pose, lx0 + 3, y + 1, false);
        SpellcraftDraw.ability(pose, spec != null ? spec : group, lx0 + 3, y + 1, 16, usable ? 1.0F : 0.4F);
        int tx = lx0 + 23;
        int room = right - tx;
        String tier = SpellcraftNames.tier(tree, group);
        String name = font.plainSubstrByWidth(SpellcraftNames.groupName(tree, group), room - font.width(tier) - 3);
        font.drawShadow(pose, name, tx, y + 1, usable ? SpellcraftPalette.NAME_GOLD : SpellcraftPalette.DARK);
        font.drawShadow(pose, tier, right - font.width(tier), y + 1, usable ? SpellcraftPalette.WHITE : SpellcraftPalette.DARK);
        String traits = spec == null ? "" : traitText(config, spec);
        font.drawShadow(pose, font.plainSubstrByWidth(traits, room), tx, y + 10, SpellcraftPalette.GREY);
    }

    private String traitText(SpellcraftConfig config, String spec) {
        List<String> traits = config.abilityTraits.get(spec);
        if (traits == null || config.isDisabled(spec)) {
            return SpellcraftNames.tr("screen.no_spellcraft");
        }
        List<String> names = new ArrayList<>();
        traits.forEach(trait -> names.add(trait.toLowerCase(Locale.ROOT)));
        return String.join(" ", names);
    }

    @Nullable
    List<Component> tooltipAt(SpellcraftConfig config, int mouseX, int mouseY) {
        if (!EditorLayout.overPouchList(mouseX, mouseY, this.screen.height)) {
            return null;
        }
        List<Component> lines = new ArrayList<>();
        int ly0 = EditorLayout.pouchListTop();
        int row = (mouseY - ly0 - 2) / EditorLayout.ROW + this.pouchScroll;
        if (this.abilitiesTab) {
            List<String> groups = pouchAbilities();
            if (row < groups.size()) {
                String group = groups.get(row);
                String spec = AbilityLookup.specOf(this.screen.tree()).apply(group);
                lines.add(SpellcraftNames.colored(SpellcraftNames.abilityName(this.screen.tree(), group), SpellcraftPalette.NAME_GOLD));
                if (spec != null) {
                    lines.add(SpellcraftNames.colored(traitText(config, spec), SpellcraftPalette.GREY));
                }
                lines.add(new TextComponent(SpellcraftNames.tr("screen.pouch_hint")).withStyle(ChatFormatting.DARK_GRAY));
            }
        } else {
            List<GlyphViewLine> all = pouchGlyphs(config);
            if (row < all.size()) {
                GlyphViewLine line = all.get(row);
                EditorTooltip.glyphLines(config, line.type(), line.potency(), line.quality(), GlyphRelevance.ALL, lines);
                lines.add(SpellcraftNames.colored(SpellcraftNames.tr("screen.uses_left", Math.max(0, usesLeft(line)), line.limit()), SpellcraftPalette.WHITE));
                if (config.glyph(line.type()).isUnique()) {
                    lines.add(SpellcraftNames.colored(SpellcraftNames.tr("screen.unique_cost", config.rules.uniqueMixCost), SpellcraftPalette.ARCANA));
                } else {
                    lines.add(SpellcraftNames.colored(SpellcraftNames.tr("screen.cost", line.cost()), SpellcraftPalette.ARCANA));
                }
                lines.add(SpellcraftNames.colored(SpellcraftNames.tr("screen.potency_from", line.ownPotency() ? SpellcraftNames.tr("screen.this_item")
                        : line.potencySource()), line.ownPotency() ? SpellcraftPalette.GREY : BORROWED_MARK));
                lines.add(SpellcraftNames.colored(SpellcraftNames.tr("screen.uses_from", line.limit(), line.ownUses() ? SpellcraftNames.tr("screen.this_item")
                        : line.usesSource()), line.ownUses() ? SpellcraftPalette.GREY : BORROWED_MARK));
                if (line.transferable()) {
                    lines.add(SpellcraftNames.colored(SpellcraftNames.tr("screen.transferable"), TRANSFER_MARK));
                }
                lines.add(new TextComponent(SpellcraftNames.tr("screen.pouch_hint")).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
        return lines;
    }

    boolean mouseClicked(SpellcraftConfig config, double mouseX, double mouseY, int button) {
        int y0 = EditorLayout.bodyTop();
        int tw = EditorLayout.pouchTabWidth();
        for (int k = 0; k < 2; k++) {
            if (SpellcraftDraw.inside(mouseX, mouseY, EditorLayout.pouchTabX(k), y0 + 4, tw, 12)) {
                this.abilitiesTab = k == 1;
                this.pouchScroll = 0;
                SpellcraftDraw.click();
                return true;
            }
        }
        int ly0 = EditorLayout.pouchListTop();
        if (button == 0 && EditorLayout.overPouchList(mouseX, mouseY, this.screen.height)) {
            int row = (int) ((mouseY - ly0 - 2) / EditorLayout.ROW) + this.pouchScroll;
            if (this.abilitiesTab) {
                List<String> groups = pouchAbilities();
                if (row < groups.size()) {
                    this.drafts.insert(groups.get(row));
                }
            } else {
                List<GlyphViewLine> lines = pouchGlyphs(config);
                if (row < lines.size()) {
                    this.drafts.insert(lines.get(row).type().id());
                }
            }
            return true;
        }
        return false;
    }

    boolean mouseScrolled(double mouseX, double mouseY, int step) {
        if (EditorLayout.overPouch(mouseX, mouseY, this.screen.height)) {
            this.pouchScroll = Math.max(0, Math.min(Math.max(0, pouchSize() - EditorLayout.pouchFit(this.screen.height)), this.pouchScroll + step));
            return true;
        }
        return false;
    }
}
