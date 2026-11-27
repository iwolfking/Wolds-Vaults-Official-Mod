package xyz.iwolfking.woldsvaults.client.spellcraft.editor;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.skill.tree.AbilityTree;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftDraw;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftNames;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftPalette;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.StatText;
import xyz.iwolfking.woldsvaults.client.spellcraft.state.ClientSpellBar;
import xyz.iwolfking.woldsvaults.client.spellcraft.stats.SlotCostStats;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.spellcraft.bar.BarMode;
import xyz.iwolfking.woldsvaults.spellcraft.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.spellcraft.item.StoredSpell;
import xyz.iwolfking.woldsvaults.spellcraft.spell.SpellModel;
import xyz.iwolfking.woldsvaults.spellcraft.spell.SpellcraftRules;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The stats box under the weave: the spell's own numbers and placement on the left, the per-ability column on the
 * right, and the save status at the bottom left.
 */
final class StatsPanel {
    static final int COOLDOWN_TEXT = 0xFF6A3A00;
    private static final int HIDES_TEXT = 0xFF8A1A1A;
    private static final int SLOT_TEXT = 0xFF3A2A80;
    private static final int PRIORITY_ON = 0xFF6C4AA8;
    private static final int PRIORITY_OFF = 0xFF6E6E6E;
    private static final int PRIORITY_EDGE = 0xFF373737;
    private static final int BOX = 7;
    private static final int TOOLTIP_WRAP = 200;

    enum StatRow {
        MANA("screen.stat.mana"),
        COOLDOWN("screen.stat.cooldown"),
        ARCANA("screen.stat.arcana"),
        SLOT("screen.stat.slot"),
        PRIORITY("screen.stat.priority");

        final String key;

        StatRow(String key) {
            this.key = key;
        }
    }

    private final SpellcraftScreen screen;
    private final SpellDrafts drafts;
    private final AbilityColumn abilities;
    private final Map<StatColumn.Line, StatRow> rowOf = new IdentityHashMap<>();
    private List<StatColumn.Placed> left = List.of();
    private int lx0;
    private int lx1;
    private int statusY;
    private String status = "";
    private boolean statusCut;
    private float escalation;
    private int priorities;

    StatsPanel(SpellcraftScreen screen, SpellDrafts drafts) {
        this.screen = screen;
        this.drafts = drafts;
        this.abilities = new AbilityColumn(screen);
    }

    /** The height the spell column and the status row need inside the box. */
    static int spellColumnHeight() {
        List<StatColumn.Line> template = new ArrayList<>();
        for (StatRow row : StatRow.values()) {
            if (row == StatRow.SLOT) {
                template.add(StatColumn.Line.BREAK);
            }
            template.add(StatColumn.Line.row("", "", 0, null));
        }
        return 2 + StatColumn.height(template) + 2 * StatColumn.SECTION_GAP + StatColumn.ROW_H;
    }

    void render(PoseStack pose, SpellcraftConfig config, AbilityTree tree, @Nullable SpellDrafts.Draft draft,
                @Nullable SpellcraftRules.ItemCheck check, @Nullable SpellModel model, int x0, int y0, int x1, int y1) {
        Font font = this.screen.font();
        SpellcraftDraw.insetGrey(pose, x0, y0, x1, y1);
        this.priorities = config.rules.priorities;
        List<StatColumn.Line> lines = spellLines(config, tree, draft, model);
        int mid = x0 + Math.min((x1 - x0) / 2, columnWidth(font, lines));
        GuiComponent.fill(pose, mid, y0 + 3, mid + 1, y1 - 3, StatColumn.DARK_LINE);
        this.lx0 = x0 + 5;
        this.lx1 = mid - 5;
        this.statusY = y1 - 1 - StatColumn.ROW_H;
        this.left = StatColumn.draw(pose, font, lines, this.lx0, this.lx1, y0 + 1, this.statusY - 2 * StatColumn.SECTION_GAP);
        if (!this.left.isEmpty()) {
            int end = this.left.get(this.left.size() - 1).y() + StatColumn.ROW_H;
            GuiComponent.fill(pose, this.lx0 - 2, end - 1 + StatColumn.SECTION_GAP, this.lx1 + 2, end + StatColumn.SECTION_GAP, StatColumn.DARK_LINE);
        }
        StatColumn.Placed priority = placed(StatRow.PRIORITY);
        if (priority != null && draft != null && this.drafts.stored(draft) != null) {
            int chosen = SpellcraftRules.priority(config, this.drafts.stored(draft));
            int bx = boxesX();
            for (int i = 0; i < this.priorities; i++) {
                int x = bx + i * (BOX + 1);
                int y = priority.y() + StatColumn.CAP_Y;
                GuiComponent.fill(pose, x, y, x + BOX, y + BOX, PRIORITY_EDGE);
                GuiComponent.fill(pose, x + 1, y + 1, x + BOX - 1, y + BOX - 1, i < chosen ? PRIORITY_ON : PRIORITY_OFF);
            }
        }
        if (draft != null) {
            SpellDrafts.Status state = this.drafts.status(draft, check);
            this.status = state.text();
            String shown = font.plainSubstrByWidth(this.status, this.lx1 - this.lx0);
            this.statusCut = !shown.equals(this.status);
            font.draw(pose, shown, this.lx0, this.statusY + StatColumn.CAP_Y, state.color());
        } else {
            this.status = "";
            this.statusCut = false;
        }
        this.abilities.render(pose, config, tree, draft == null ? 0 : draft.id, model, this.drafts.errors(draft, check), mid + 5, y0 + 1, x1 - 3, y1 - 1);
    }

    private List<StatColumn.Line> spellLines(SpellcraftConfig config, AbilityTree tree, @Nullable SpellDrafts.Draft draft, @Nullable SpellModel model) {
        SlotCostStats.Numbers numbers = model == null || model.groups().isEmpty() ? null : SlotCostStats.spell(config, tree, model);
        this.escalation = model == null ? 1.0F : model.escalation();
        this.rowOf.clear();
        List<StatColumn.Line> lines = new ArrayList<>();
        for (StatRow row : StatRow.values()) {
            String value = "-";
            String base = null;
            int color = SpellcraftPalette.LABEL;
            switch (row) {
                case MANA -> {
                    color = SpellcraftPalette.onGrey(SpellcraftPalette.STAT_MANA);
                    if (numbers != null && numbers.hasMana()) {
                        value = StatText.compact(Math.round(numbers.manaAfter()));
                        base = StatText.compact(Math.round(numbers.manaBefore()));
                    }
                }
                case COOLDOWN -> {
                    color = SpellcraftPalette.onGrey(SpellcraftPalette.STAT_COOLDOWN);
                    if (numbers != null) {
                        value = SpellcraftNames.tr("fmt.seconds", StatText.seconds(numbers.cooldownAfter()));
                        base = SpellcraftNames.tr("fmt.seconds", StatText.seconds(numbers.cooldownBefore()));
                    }
                }
                case ARCANA -> {
                    color = SpellcraftPalette.ARCANA_TEXT;
                    value = model == null ? "-" : String.valueOf(Math.round(model.arcana()));
                }
                case SLOT -> {
                    color = SLOT_TEXT;
                    value = slotText(draft);
                    lines.add(StatColumn.Line.BREAK);
                }
                case PRIORITY -> value = "";
            }
            String label = row == StatRow.SLOT && classic() ? "screen.stat.active" : row.key;
            StatColumn.Line line = StatColumn.Line.row(SpellcraftNames.tr(label), value, color, base);
            this.rowOf.put(line, row);
            lines.add(line);
        }
        return lines;
    }

    private int columnWidth(Font font, List<StatColumn.Line> lines) {
        int need = 0;
        for (StatColumn.Line line : lines) {
            if (line.kind() != StatColumn.Kind.ROW) {
                continue;
            }
            int value = this.rowOf.get(line) == StatRow.PRIORITY ? this.priorities * (BOX + 1) - 1 : font.width(line.value());
            need = Math.max(need, font.width(line.label()) + 6 + value);
        }
        return need + 10;
    }

    private int boxesX() {
        return this.lx1 - (this.priorities * (BOX + 1) - 1);
    }

    @Nullable
    private StatColumn.Placed placed(StatRow row) {
        for (StatColumn.Placed placed : this.left) {
            if (this.rowOf.get(placed.line()) == row) {
                return placed;
            }
        }
        return null;
    }

    @Nullable
    private StatRow rowAt(double mouseX, double mouseY) {
        StatColumn.Placed placed = StatColumn.at(this.left, this.lx0, this.lx1, mouseX, mouseY);
        return placed == null ? null : this.rowOf.get(placed.line());
    }

    private String slotText(@Nullable SpellDrafts.Draft draft) {
        StoredSpell stored = this.drafts.stored(draft);
        if (stored == null) {
            return "-";
        }
        if (classic()) {
            return SpellcraftNames.tr(stored.assigned() ? "screen.active_on" : "screen.active_off");
        }
        return stored.assigned() ? String.valueOf(stored.slot() + 1) : SpellcraftNames.tr("screen.slot_none");
    }

    private static boolean classic() {
        return ClientSpellBar.mode() == BarMode.CLASSIC;
    }

    /** The default-bar slots this spell would hide: the slots other than its own that hold an ability it casts. */
    private String hides(AbilityTree tree, @Nullable SpellDrafts.Draft draft, SpellModel model) {
        StoredSpell stored = this.drafts.stored(draft);
        int own = stored == null ? -1 : stored.slot();
        List<String> hidden = new ArrayList<>();
        for (int i = 0; i < SpellcraftItemData.SLOTS; i++) {
            String group = ClientSpellBar.defaultSlot(i);
            if (i != own && !group.isEmpty() && model.groups().contains(group)) {
                hidden.add(SpellcraftNames.tr("screen.slot_ref", SpellcraftNames.groupName(tree, group), i + 1));
            }
        }
        if (hidden.isEmpty()) {
            return "-";
        }
        return hidden.size() == 1 ? hidden.get(0) : SpellcraftNames.tr("screen.and_more", hidden.get(0), hidden.size() - 1);
    }

    @Nullable
    List<Component> tooltipAt(@Nullable SpellDrafts.Draft draft, @Nullable SpellcraftRules.ItemCheck check, int mouseX, int mouseY) {
        StatColumn.Placed placed = StatColumn.at(this.left, this.lx0, this.lx1, mouseX, mouseY);
        StatRow row = placed == null ? null : this.rowOf.get(placed.line());
        if (row == StatRow.SLOT || row == StatRow.PRIORITY) {
            return placementTooltip(draft, check, row == StatRow.SLOT);
        }
        List<Component> lines = new ArrayList<>();
        if (row == StatRow.ARCANA && !placed.line().value().equals("-")) {
            lines.add(SpellcraftNames.colored(placed.line().label() + ": " + placed.line().value(), SpellcraftPalette.WHITE));
            lines.add(SpellcraftNames.colored(SpellcraftNames.tr("screen.arcana_escalation", String.format(Locale.ROOT, "%.2f", this.escalation)),
                    SpellcraftPalette.GREY));
            if (draft != null && check != null && !check.charged().contains(draft.id > 0 ? draft.id : StoredSpell.UNSAVED_ID)) {
                lines.add(SpellcraftNames.colored(SpellcraftNames.tr("screen.arcana_off_bar"), SpellcraftPalette.GREY));
            }
            return lines;
        }
        if (row != null && placed.line().changed()) {
            lines.add(SpellcraftNames.colored(placed.line().label() + ": " + placed.line().value(), SpellcraftPalette.WHITE));
            lines.add(SpellcraftNames.colored(SpellcraftNames.tr("screen.without_glyphs", placed.line().base()), SpellcraftPalette.GREY));
            return lines;
        }
        if (this.statusCut && SpellcraftDraw.inside(mouseX, mouseY, this.lx0, this.statusY, this.lx1 - this.lx0, StatColumn.ROW_H)) {
            lines.add(new TextComponent(this.status));
            return lines;
        }
        return this.abilities.tooltipAt(mouseX, mouseY);
    }

    private List<Component> placementTooltip(@Nullable SpellDrafts.Draft draft, @Nullable SpellcraftRules.ItemCheck check, boolean slot) {
        List<Component> lines = new ArrayList<>();
        if (this.drafts.stored(draft) == null) {
            lines.add(SpellcraftNames.colored(SpellcraftNames.tr("screen.place_unsaved"), SpellcraftPalette.GREY));
            return lines;
        }
        String hint = SpellcraftNames.tr(!slot ? "screen.priority_hint" : classic() ? "screen.active_hint" : "screen.slot_hint");
        this.screen.font().getSplitter().splitLines(hint, TOOLTIP_WRAP, Style.EMPTY)
                .forEach(line -> lines.add(SpellcraftNames.colored(line.getString(), SpellcraftPalette.WHITE)));
        if (ClientSpellBar.source(SpellcraftItemData.key(this.drafts.stack())) == null) {
            lines.add(SpellcraftNames.colored(SpellcraftNames.tr("screen.not_equipped"), SpellcraftPalette.BAD));
        }
        if (slot && classic()) {
            lines.add(new TextComponent(SpellcraftNames.tr("screen.toggle_hint")).withStyle(ChatFormatting.DARK_GRAY));
        } else if (slot) {
            SpellModel model = check == null ? null : check.models().get(draft.id > 0 ? draft.id : StoredSpell.UNSAVED_ID);
            if (model != null) {
                lines.add(SpellcraftNames.colored(SpellcraftNames.tr("screen.stat.hides") + ": " + hides(this.screen.tree(), draft, model), HIDES_TEXT));
            }
            lines.add(new TextComponent(SpellcraftNames.tr("screen.stepper_hint")).withStyle(ChatFormatting.DARK_GRAY));
        }
        return lines;
    }

    boolean mouseClicked(double mouseX, double mouseY, int button) {
        StatRow row = rowAt(mouseX, mouseY);
        if (row == StatRow.SLOT && classic() && button == 0) {
            this.drafts.toggleActive();
            return true;
        }
        if (row == StatRow.SLOT && !classic() && (button == 0 || button == 1)) {
            this.drafts.stepPlacement(true, button == 0 ? 1 : -1);
            return true;
        }
        StatColumn.Placed priority = placed(StatRow.PRIORITY);
        if (row == StatRow.PRIORITY && button == 0 && priority != null) {
            int bx = boxesX();
            for (int i = 0; i < this.priorities; i++) {
                if (SpellcraftDraw.inside(mouseX, mouseY, bx + i * (BOX + 1), priority.y() + StatColumn.CAP_Y - 1, BOX + 1, BOX + 2)) {
                    this.drafts.setPriority(i + 1);
                    return true;
                }
            }
        }
        return false;
    }

    boolean mouseScrolled(double mouseX, double mouseY, int step) {
        StatRow row = rowAt(mouseX, mouseY);
        if (row == StatRow.SLOT && classic()) {
            return true;
        }
        if (row == StatRow.SLOT || row == StatRow.PRIORITY) {
            this.drafts.stepPlacement(row == StatRow.SLOT, -step);
            return true;
        }
        return this.abilities.mouseScrolled(mouseX, mouseY, step);
    }
}
