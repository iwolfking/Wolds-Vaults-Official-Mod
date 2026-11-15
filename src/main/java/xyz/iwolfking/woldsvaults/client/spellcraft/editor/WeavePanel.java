package xyz.iwolfking.woldsvaults.client.spellcraft.editor;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import xyz.iwolfking.woldsvaults.client.spellcraft.bar.SpellBarStrip;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.GlyphText;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftDraw;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftNames;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftPalette;
import xyz.iwolfking.woldsvaults.client.spellcraft.stats.GlyphRelevance;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.spellcraft.ability.AbilityLookup;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphType;
import xyz.iwolfking.woldsvaults.spellcraft.item.StoredSpell;
import xyz.iwolfking.woldsvaults.spellcraft.source.GlyphViewLine;
import xyz.iwolfking.woldsvaults.spellcraft.spell.PlacedGlyph;
import xyz.iwolfking.woldsvaults.spellcraft.spell.SpellModel;
import xyz.iwolfking.woldsvaults.spellcraft.spell.SpellcraftRules;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

final class WeavePanel {
    private static final int PAYLOAD = 0xFFB46CF0;
    private static final int THREAD_DIM = 0xFF3A2A55;
    private static final int PLUS = 0xFF8A8A8A;
    private static final int CELL_EDGE = 0xFF5A4A80;
    private static final int CELL_EDGE_DIM = 0xFF3A3050;
    private static final int CELL_FILL = 0xFF1A1426;
    private static final int HOVER = 0x80FFFFFF;

    private final SpellcraftScreen screen;
    private final SpellDrafts drafts;
    private final SpellBarStrip strip;

    WeavePanel(SpellcraftScreen screen, SpellDrafts drafts, SpellBarStrip strip) {
        this.screen = screen;
        this.drafts = drafts;
        this.strip = strip;
    }

    private static int payloadStart(SpellDrafts.Draft draft) {
        int bundle = draft.tokens.indexOf(GlyphType.BUNDLED_SPELL.id());
        if (bundle < 0) {
            return -1;
        }
        for (int i = bundle + 1; i < draft.tokens.size(); i++) {
            if (GlyphType.byId(draft.tokens.get(i)) == null) {
                return i;
            }
        }
        return -1;
    }

    private int tokens() {
        SpellDrafts.Draft draft = this.drafts.draft();
        return draft == null ? 0 : draft.tokens.size();
    }

    /** The weave rows on screen for the current draft. */
    int shownRows() {
        return EditorLayout.shownWeaveRows(this.screen.width, this.screen.height, tokens());
    }

    private List<EditorLayout.Cell> cells(int x0, int y0, int x1) {
        int shown = shownRows();
        int rows = EditorLayout.weaveRows(tokens(), EditorLayout.perRow(x0, x1));
        this.drafts.setWeaveScroll(Math.min(this.drafts.weaveScroll(), Math.max(0, rows - shown)));
        return EditorLayout.cells(tokens(), this.drafts.weaveScroll(), shown, x0, y0, x1);
    }

    void render(PoseStack pose, @Nullable SpellDrafts.Draft draft, @Nullable SpellModel model, int x0, int y0, int x1, int y1, int mouseX,
                int mouseY) {
        Font font = this.screen.font();
        int icon = EditorLayout.ICON;
        SpellcraftDraw.starfield(pose, x0, y0, x1, y1);
        List<EditorLayout.Cell> cells = cells(x0, y0, x1);
        int size = draft == null ? 0 : draft.tokens.size();
        for (int k = 1; k < cells.size(); k++) {
            EditorLayout.Cell a = cells.get(k - 1);
            EditorLayout.Cell b = cells.get(k);
            if (a.y() != b.y()) {
                continue;
            }
            boolean toAdd = b.index() == size;
            int mid = a.y() + icon / 2;
            GuiComponent.fill(pose, a.x() + icon + 1, mid - 1, b.x() - 1, mid + 1, toAdd ? THREAD_DIM : SpellcraftPalette.ARCANE_LINE);
            GuiComponent.fill(pose, a.x() + icon + 1, mid - 1, b.x() - 1, mid, toAdd ? THREAD_DIM : SpellcraftPalette.ARCANE_GLOW);
        }
        int payload = draft == null ? -1 : payloadStart(draft);
        if (payload >= 0) {
            int minX = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int rowY = Integer.MIN_VALUE;
            for (EditorLayout.Cell cell : cells) {
                if (cell.index() < payload) {
                    continue;
                }
                if (cell.y() != rowY) {
                    if (rowY != Integer.MIN_VALUE) {
                        SpellcraftDraw.ring(pose, minX - 4, rowY - 4, maxX + icon + 4, rowY + icon + 4, PAYLOAD);
                    }
                    rowY = cell.y();
                    minX = cell.x();
                }
                maxX = cell.x();
            }
            if (rowY != Integer.MIN_VALUE) {
                SpellcraftDraw.ring(pose, minX - 4, rowY - 4, maxX + icon + 4, rowY + icon + 4, PAYLOAD);
            }
            EditorLayout.Cell first = cells.stream().filter(cell -> cell.index() == payload).findFirst().orElse(null);
            if (first != null && first.y() == y0 + EditorLayout.WEAVE_TOP) {
                SpellcraftDraw.smallText(pose, font, SpellcraftNames.tr("screen.payload"), first.x() - 1, first.y() - 11, PAYLOAD, SpellBarStrip.KEY_SCALE);
            }
        }
        for (EditorLayout.Cell cell : cells) {
            int x = cell.x();
            int y = cell.y();
            if (cell.index() == size) {
                cell(pose, x, y, true);
                int m = icon / 2;
                GuiComponent.fill(pose, x + m - 3, y + m - 1, x + m + 3, y + m + 1, PLUS);
                GuiComponent.fill(pose, x + m - 1, y + m - 3, x + m + 1, y + m + 3, PLUS);
            } else {
                String token = draft.tokens.get(cell.index());
                GlyphType type = GlyphType.byId(token);
                cell(pose, x, y, false);
                if (type != null) {
                    PlacedGlyph glyph = model == null ? null : model.glyphAt(cell.index());
                    SpellcraftDraw.rune(pose, type, x, y, icon, glyph != null && !glyph.active());
                } else {
                    String spec = AbilityLookup.specOf(this.screen.tree()).apply(token);
                    SpellcraftDraw.ability(pose, spec != null ? spec : token, x, y, icon, 1.0F);
                }
            }
            boolean marked = this.drafts.insertAt() == SpellDrafts.END ? cell.index() == size : cell.index() == this.drafts.insertAt() - 1;
            if (marked) {
                SpellcraftDraw.ring(pose, x - 3, y - 3, x + icon + 3, y + icon + 3, SpellcraftPalette.ARCANE_GLOW);
            }
            if (SpellcraftDraw.inside(mouseX, mouseY, x, y, icon, icon) && !this.strip.popupOpen()) {
                GuiComponent.fill(pose, x, y, x + icon, y + icon, HOVER);
            }
        }
        int rows = EditorLayout.weaveRows(size, EditorLayout.perRow(x0, x1));
        int shown = shownRows();
        if (rows > shown) {
            String more = SpellcraftNames.tr("screen.weave_rows", this.drafts.weaveScroll() + 1, Math.min(rows, this.drafts.weaveScroll() + shown), rows);
            float w = font.width(more) * SpellBarStrip.KEY_SCALE;
            SpellcraftDraw.smallText(pose, font, more, x1 - 4 - w, y0 + 2, SpellcraftPalette.MUTED, SpellBarStrip.KEY_SCALE);
        }
    }

    /** A flat cell: one even border around a flat fill, with (x, y) the top left of the icon area. */
    private static void cell(PoseStack pose, int x, int y, boolean empty) {
        int icon = EditorLayout.ICON;
        SpellcraftDraw.ring(pose, x - 1, y - 1, x + icon + 1, y + icon + 1, empty ? CELL_EDGE_DIM : CELL_EDGE);
        GuiComponent.fill(pose, x, y, x + icon, y + icon, CELL_FILL);
    }

    @Nullable
    List<Component> tooltipAt(SpellcraftConfig config, @Nullable SpellDrafts.Draft draft, @Nullable SpellcraftRules.ItemCheck check, int mouseX, int mouseY) {
        int icon = EditorLayout.ICON;
        for (EditorLayout.Cell cell : cells(EditorLayout.innerX0(), EditorLayout.weaveTop(), EditorLayout.innerX1(this.screen.width))) {
            if (!SpellcraftDraw.inside(mouseX, mouseY, cell.x(), cell.y(), icon, icon)) {
                continue;
            }
            List<Component> lines = new ArrayList<>();
            if (draft == null || cell.index() >= draft.tokens.size()) {
                lines.add(new TextComponent(SpellcraftNames.tr("screen.add_hint")).withStyle(ChatFormatting.GRAY));
            } else {
                String token = draft.tokens.get(cell.index());
                GlyphType type = GlyphType.byId(token);
                if (type != null) {
                    SpellModel model = check == null ? null : check.models().get(draft.id > 0 ? draft.id : StoredSpell.UNSAVED_ID);
                    PlacedGlyph glyph = model == null ? null : model.glyphAt(cell.index());
                    GlyphViewLine line = this.drafts.view(config).get(type);
                    GlyphRelevance.Filter relevance = model == null ? GlyphRelevance.ALL : GlyphRelevance.forToken(config, model, cell.index());
                    EditorTooltip.glyphLines(config, type, line == null ? 0.0F : line.potency(), line == null ? 0.0F : line.quality(), relevance, lines);
                    if (glyph != null && !glyph.active()) {
                        lines.add(SpellcraftNames.colored(GlyphText.inactiveReason(config, glyph), SpellcraftPalette.BAD));
                    }
                } else {
                    lines.add(SpellcraftNames.colored(SpellcraftNames.abilityName(this.screen.tree(), token), SpellcraftPalette.NAME_GOLD));
                    SpellModel model = check == null ? null : check.models().get(draft.id > 0 ? draft.id : StoredSpell.UNSAVED_ID);
                    SpellModel.Cast cast = model == null ? null : castOf(model.steps(), token);
                    String summary = cast == null ? "" : GlyphText.poolSummary(model.stepPool(cast), GlyphRelevance.of(config, model, cast));
                    if (!summary.isEmpty()) {
                        lines.add(SpellcraftNames.colored(SpellcraftNames.tr("bar.stat.glyphs"), SpellcraftPalette.GREY).copy().append(GlyphText.tagged(summary)));
                    }
                }
                int payload = payloadStart(draft);
                if (payload >= 0 && cell.index() >= payload && (type == null || !config.glyph(type).spellWide())) {
                    lines.add(SpellcraftNames.colored(SpellcraftNames.tr("screen.in_payload"), PAYLOAD));
                }
                lines.add(new TextComponent(SpellcraftNames.tr("screen.cell_hint")).withStyle(ChatFormatting.DARK_GRAY));
            }
            return lines;
        }
        return null;
    }

    @Nullable
    private static SpellModel.Cast castOf(List<SpellModel.Step> steps, String group) {
        for (SpellModel.Step step : steps) {
            if (step instanceof SpellModel.Cast cast && cast.group().equals(group)) {
                return cast;
            }
            if (step instanceof SpellModel.Bundle bundle) {
                SpellModel.Cast inner = castOf(bundle.payload(), group);
                if (inner != null) {
                    return inner;
                }
            }
        }
        return null;
    }

    boolean mouseClicked(double mouseX, double mouseY, int button) {
        SpellDrafts.Draft draft = this.drafts.draft();
        int icon = EditorLayout.ICON;
        for (EditorLayout.Cell cell : cells(EditorLayout.innerX0(), EditorLayout.weaveTop(), EditorLayout.innerX1(this.screen.width))) {
            if (!SpellcraftDraw.inside(mouseX, mouseY, cell.x(), cell.y(), icon, icon)) {
                continue;
            }
            int size = draft == null ? 0 : draft.tokens.size();
            if (button == 0) {
                this.drafts.setInsertAt(cell.index() >= size ? SpellDrafts.END : cell.index() + 1);
                SpellcraftDraw.click();
            } else if (button == 1 && draft != null && cell.index() < size) {
                this.drafts.removeToken(draft, cell.index());
                SpellcraftDraw.click();
            }
            return true;
        }
        return false;
    }

    boolean mouseScrolled(double mouseX, double mouseY, int step) {
        int shown = shownRows();
        if (EditorLayout.overWeave(mouseX, mouseY, this.screen.width, shown)) {
            int rows = EditorLayout.weaveRows(tokens(), EditorLayout.perRow(EditorLayout.innerX0(), EditorLayout.innerX1(this.screen.width)));
            this.drafts.setWeaveScroll(Math.max(0, Math.min(Math.max(0, rows - shown), this.drafts.weaveScroll() + step)));
            return true;
        }
        return false;
    }
}
