package xyz.iwolfking.woldsvaults.client.spellcraft.editor;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.skill.tree.AbilityTree;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftDraw;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftNames;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftPalette;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.StatText;
import xyz.iwolfking.woldsvaults.client.spellcraft.stats.StepStatRows;
import xyz.iwolfking.woldsvaults.client.spellcraft.stats.StepStats;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.spellcraft.spell.SpellModel;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The stats box's right column: one section per ability step and Bundled Spell orb with every stat it has, then the
 * whole spell's damage. It scrolls when the sections don't fit, and shows the draft's errors in their place.
 */
final class AbilityColumn {
    static final int SCROLL_W = 3;
    private static final int SCROLL_TRACK = 0xFF6E6E6E;
    private static final int SCROLL_THUMB = 0xFF373737;
    private static final String CASTS = "screen.stat.casts";

    private final SpellcraftScreen screen;
    private List<StatColumn.Placed> placed = List.of();
    private int x0;
    private int x1;
    private int y0;
    private int y1;
    private int scroll;
    private int hidden;
    private int lastDraft = Integer.MIN_VALUE;
    private List<String> errors = List.of();

    AbilityColumn(SpellcraftScreen screen) {
        this.screen = screen;
    }

    void render(PoseStack pose, SpellcraftConfig config, AbilityTree tree, int draftId, @Nullable SpellModel model, List<String> errors,
                int x0, int y0, int x1, int y1) {
        Font font = this.screen.font();
        this.x0 = x0;
        this.x1 = x1 - SCROLL_W - 2;
        this.y0 = y0;
        this.y1 = y1;
        if (draftId != this.lastDraft) {
            this.lastDraft = draftId;
            this.scroll = 0;
        }
        this.errors = errors;
        if (!errors.isEmpty()) {
            this.placed = List.of();
            this.hidden = 0;
            renderErrors(pose, font, errors);
            return;
        }
        List<StatColumn.Line> lines = model == null ? List.of() : lines(config, tree, model);
        int total = (int) lines.stream().filter(line -> line.kind() != StatColumn.Kind.BREAK).count();
        this.scroll = Math.max(0, Math.min(this.scroll, total - 1));
        List<StatColumn.Line> shown = skip(lines, this.scroll);
        this.placed = StatColumn.draw(pose, font, shown, this.x0, this.x1, y0, y1);
        this.hidden = total - this.scroll - this.placed.size();
        if (this.scroll > 0 || this.hidden > 0) {
            int track = y1 - y0 - 4;
            int thumb = Math.max(6, track * this.placed.size() / Math.max(1, total));
            int top = y0 + 2 + (track - thumb) * this.scroll / Math.max(1, this.scroll + this.hidden);
            GuiComponent.fill(pose, x1 - SCROLL_W, y0 + 2, x1, y1 - 2, SCROLL_TRACK);
            GuiComponent.fill(pose, x1 - SCROLL_W, top, x1, top + thumb, SCROLL_THUMB);
        }
    }

    private void renderErrors(PoseStack pose, Font font, List<String> errors) {
        List<String> wrapped = new ArrayList<>();
        for (String error : errors) {
            font.getSplitter().splitLines(error, this.x1 - this.x0, Style.EMPTY).forEach(line -> wrapped.add(line.getString()));
        }
        int room = (this.y1 - this.y0) / StatColumn.ROW_H;
        boolean overflow = wrapped.size() > room;
        int shown = overflow ? room - 1 : wrapped.size();
        for (int i = 0; i < shown; i++) {
            font.draw(pose, wrapped.get(i), this.x0, this.y0 + StatColumn.CAP_Y + i * StatColumn.ROW_H, SpellcraftPalette.BAD_TEXT);
        }
        if (overflow && room > 0) {
            font.draw(pose, SpellcraftNames.tr("screen.more_errors", wrapped.size() - shown), this.x0, this.y0 + StatColumn.CAP_Y + shown * StatColumn.ROW_H,
                    SpellcraftPalette.DARK);
        }
    }

    private static List<StatColumn.Line> skip(List<StatColumn.Line> lines, int rows) {
        int skipped = 0;
        int i = 0;
        while (i < lines.size() && skipped < rows) {
            if (lines.get(i).kind() != StatColumn.Kind.BREAK) {
                skipped++;
            }
            i++;
        }
        while (i < lines.size() && lines.get(i).kind() == StatColumn.Kind.BREAK) {
            i++;
        }
        return lines.subList(i, lines.size());
    }

    private static List<StatColumn.Line> lines(SpellcraftConfig config, AbilityTree tree, SpellModel model) {
        List<StatColumn.Line> lines = new ArrayList<>();
        StepStats.SpellStats stats = StepStats.spell(config, tree, model);
        if (stats == null) {
            return lines;
        }
        for (StepStats.Row row : stats.rows()) {
            if (!lines.isEmpty()) {
                lines.add(StatColumn.Line.BREAK);
            }
            if (row instanceof StepStats.Step step) {
                lines.add(StatColumn.Line.header(SpellcraftNames.groupName(tree, step.group()), SpellcraftNames.tier(tree, step.group())));
                StepStats.Step plain = StepStats.plain(tree, step.group());
                Map<String, String> base = new HashMap<>();
                if (plain != null) {
                    values(plain).forEach(value -> base.put(value.key(), value.text()));
                }
                for (StepStatRows.Value value : values(step)) {
                    String before = base.getOrDefault(value.key(), value.key().equals(CASTS) ? "1" : "-");
                    lines.add(StatColumn.Line.row(SpellcraftNames.label(value.key()), value.text(), SpellcraftPalette.onGrey(value.color()), before));
                }
            } else if (row instanceof StepStats.Orb orb) {
                lines.add(StatColumn.Line.header(SpellcraftNames.tr("bar.stat.orb_step"), ""));
                if (orb.copies() > 1) {
                    lines.add(StatColumn.Line.row(SpellcraftNames.label("bar.stat.orbs"), String.valueOf(orb.copies()),
                            SpellcraftPalette.onGrey(SpellcraftPalette.STAT_PROJECTILES), null));
                }
                lines.add(StatColumn.Line.row(SpellcraftNames.label("bar.stat.velocity"), SpellcraftNames.tr("fmt.blocks_per_second", StatText.compact(orb.velocity())),
                        SpellcraftPalette.onGrey(SpellcraftPalette.STAT_YELLOW), null));
                lines.add(StatColumn.Line.row(SpellcraftNames.label("bar.stat.flight"), SpellcraftNames.tr("fmt.seconds", StatText.trim(orb.flightSeconds())),
                        SpellcraftPalette.onGrey(SpellcraftPalette.STAT_YELLOW), null));
            }
        }
        if (stats.total() != null) {
            lines.add(StatColumn.Line.BREAK);
            lines.add(StatColumn.Line.row(SpellcraftNames.label("bar.stat.spell_total"), StatText.hit(stats.total()),
                    SpellcraftPalette.onGrey(SpellcraftPalette.STAT_DAMAGE), null));
        }
        return lines;
    }

    /** A step's rows: its hit, charged hit, projectiles, echo and total, then the effect rows the slot tooltip also lists. */
    private static List<StepStatRows.Value> values(StepStats.Step step) {
        List<StepStatRows.Value> values = new ArrayList<>();
        if (step.hit() != null) {
            values.add(new StepStatRows.Value("screen.stat.damage", StatText.hit(step.hit()), SpellcraftPalette.STAT_DAMAGE));
        }
        if (step.charged() != null) {
            values.add(new StepStatRows.Value("screen.stat.charged", StatText.hit(step.charged()), SpellcraftPalette.STAT_DAMAGE));
        }
        if (step.casts() > 1) {
            values.add(new StepStatRows.Value(CASTS, String.valueOf(step.casts()), SpellcraftPalette.STAT_PROJECTILES));
        }
        if (step.echoPower() > 0.0F) {
            values.add(new StepStatRows.Value("screen.stat.echo", SpellcraftNames.tr("fmt.percent", Math.round(step.echoPower() * 100.0F)),
                    SpellcraftPalette.STAT_INTENSITY));
        }
        if (step.total() != null) {
            values.add(new StepStatRows.Value("bar.stat.total", StatText.hit(step.total()), SpellcraftPalette.STAT_DAMAGE));
        }
        if (step.chargedTotal() != null) {
            values.add(new StepStatRows.Value("screen.stat.charged_total", StatText.hit(step.chargedTotal()), SpellcraftPalette.STAT_DAMAGE));
        }
        values.addAll(StepStatRows.effects(step));
        return values;
    }

    /** The full label and the value without glyphs of the row under the mouse, when the row cuts or changes them. */
    @Nullable
    List<Component> tooltipAt(double mouseX, double mouseY) {
        if (!this.errors.isEmpty() && over(mouseX, mouseY)) {
            List<Component> lines = new ArrayList<>();
            this.errors.forEach(error -> lines.add(SpellcraftNames.colored(error, SpellcraftPalette.BAD)));
            return lines;
        }
        StatColumn.Placed row = StatColumn.at(this.placed, this.x0, this.x1, mouseX, mouseY);
        if (row == null || !row.cut() && !row.line().changed()) {
            return null;
        }
        List<Component> lines = new ArrayList<>();
        StatColumn.Line line = row.line();
        if (line.kind() == StatColumn.Kind.HEADER) {
            lines.add(SpellcraftNames.colored(line.label(), SpellcraftPalette.NAME_GOLD));
            return lines;
        }
        lines.add(SpellcraftNames.colored(line.label() + ": " + line.value(), SpellcraftPalette.WHITE));
        if (line.changed()) {
            lines.add(SpellcraftNames.colored(SpellcraftNames.tr("screen.without_glyphs", line.base()), SpellcraftPalette.GREY));
        }
        return lines;
    }

    boolean over(double mouseX, double mouseY) {
        return SpellcraftDraw.inside(mouseX, mouseY, this.x0, this.y0, this.x1 + SCROLL_W + 2 - this.x0, this.y1 - this.y0);
    }

    boolean mouseScrolled(double mouseX, double mouseY, int step) {
        if (!over(mouseX, mouseY)) {
            return false;
        }
        this.scroll = Math.max(0, Math.min(this.scroll + step, this.scroll + this.hidden));
        return true;
    }
}
