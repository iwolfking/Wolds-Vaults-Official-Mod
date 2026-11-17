package xyz.iwolfking.woldsvaults.client.spellcraft.bar;

import iskallia.vault.skill.tree.AbilityTree;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.GlyphText;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftDraw;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftNames;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftPalette;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.StatText;
import xyz.iwolfking.woldsvaults.client.spellcraft.state.ClientSpellBar;
import xyz.iwolfking.woldsvaults.client.spellcraft.stats.GlyphRelevance;
import xyz.iwolfking.woldsvaults.client.spellcraft.stats.SlotCostStats;
import xyz.iwolfking.woldsvaults.client.spellcraft.stats.StepStatRows;
import xyz.iwolfking.woldsvaults.client.spellcraft.stats.StepStats;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphStacking;
import xyz.iwolfking.woldsvaults.spellcraft.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.spellcraft.play.SpellCooldowns;

import java.util.ArrayList;
import java.util.List;

final class SlotTooltip {
    private static final int WRAP = 200;

    private SlotTooltip() {
    }

    static List<Component> tooltip(SpellBarStrip strip, int mouseX, int mouseY) {
        List<Component> lines = new ArrayList<>();
        if (SpellcraftDraw.inside(mouseX, mouseY, strip.pickX(0), strip.y0() + 2, SpellBarStrip.PICK, SpellBarStrip.PICK)) {
            lines.add(new TextComponent(SpellcraftNames.tr("bar.default")));
            return lines;
        }
        SpellBarView item = strip.host().effectiveView();
        if (item != null && SpellcraftDraw.inside(mouseX, mouseY, strip.pickX(1), strip.y0() + 2, SpellBarStrip.PICK, SpellBarStrip.PICK)) {
            lines.add(new TextComponent(SpellcraftNames.tr("bar.item"))
                    .withStyle(style -> style.withColor(TextColor.fromRgb(SpellcraftPalette.ARCANE_GLOW & 0xFFFFFF))));
            return lines;
        }
        if (SpellcraftDraw.inside(mouseX, mouseY, strip.keysX(), strip.y0() + 4, strip.keysW(), SpellBarStrip.KEYS_H)) {
            wrapped(lines, SpellcraftNames.tr(strip.bindMode() ? "bar.keys_hint_on" : "bar.keys_hint"), SpellcraftPalette.WHITE);
            return lines;
        }
        for (int i = 0; i < SpellcraftItemData.SLOTS; i++) {
            if (SpellcraftDraw.inside(mouseX, mouseY, SpellBarStrip.slotX(strip.x0(), i), strip.y0() + 4, 16, 16)) {
                return strip.bindMode() ? bindTooltip(strip, i) : slotTooltip(strip.host(), strip.lastView(), i);
            }
        }
        return lines;
    }

    private static List<Component> bindTooltip(SpellBarStrip strip, int index) {
        List<Component> lines = new ArrayList<>();
        lines.add(SpellcraftNames.colored(keyLine(index), SpellcraftPalette.WHITE));
        if (strip.armedSlot() == index) {
            wrapped(lines, SpellcraftNames.tr("bar.bind_armed", index + 1), SpellcraftPalette.STAT_YELLOW);
        } else {
            lines.add(new TextComponent(SpellcraftNames.tr("bar.hint_bind")).withStyle(ChatFormatting.DARK_GRAY));
        }
        return lines;
    }

    private static String keyLine(int index) {
        String key = SpellcraftNames.keyName(index);
        return key.isEmpty() ? SpellcraftNames.tr("bar.no_key", index + 1) : SpellcraftNames.tr("bar.key", index + 1, key);
    }

    private static List<Component> slotTooltip(SpellBarStrip.Host host, SpellBarView view, int index) {
        List<Component> lines = new ArrayList<>();
        AbilityTree tree = host.tree();
        SpellcraftConfig config = ModConfigs.SPELLCRAFT;
        SpellBarView.Slot slot = view.slot(index);
        switch (slot.state()) {
            case ABILITY -> {
                lines.add(SpellcraftNames.colored(SpellcraftNames.abilityName(tree, slot.group()) + tierSuffix(tree, slot.group()), SpellcraftPalette.NAME_GOLD));
                StepStats.Step stats = StepStats.plain(tree, slot.group());
                if (stats != null) {
                    lines.addAll(statLines(stats, ""));
                }
                SlotCostStats.Numbers numbers = SlotCostStats.plain(tree, slot.group());
                if (numbers != null) {
                    lines.addAll(numberLines(numbers));
                }
            }
            case SPELL -> {
                lines.add(SpellcraftNames.colored(slot.spellName(), slot.implicit() ? SpellBarStrip.IMPLICIT_RING : SpellcraftPalette.ARCANE_GLOW));
                lines.add(spellTitle(tree, slot.groups()));
                if (slot.implicit()) {
                    lines.add(SpellcraftNames.colored(SpellcraftNames.tr("bar.implicit"), SpellBarStrip.IMPLICIT_RING));
                } else if (!slot.sourceName().isEmpty()) {
                    lines.add(SpellcraftNames.colored(SpellcraftNames.tr("bar.source", slot.sourceName(), slot.prio()), SpellcraftPalette.GREY));
                }
                if (slot.locked()) {
                    wrapped(lines, SpellcraftNames.tr("bar.locked", slot.lockReason()), SpellcraftPalette.BAD);
                }
                if (config != null && slot.model() != null && !slot.locked()) {
                    StepStats.SpellStats stats = StepStats.spell(config, tree, slot.model());
                    if (stats != null) {
                        lines.addAll(spellLines(tree, stats));
                    }
                    SlotCostStats.Numbers numbers = SlotCostStats.spell(config, tree, slot.model());
                    if (numbers != null) {
                        lines.addAll(numberLines(numbers));
                    }
                }
                if (slot.model() != null) {
                    for (var glyph : slot.model().glyphs()) {
                        if (!glyph.active()) {
                            lines.add(SpellcraftNames.colored(SpellcraftNames.tr("bar.inactive_glyph", glyph.type().displayName(), GlyphText.inactiveReason(config, glyph)),
                                    SpellcraftPalette.DARK));
                        }
                    }
                }
                var source = ClientSpellBar.source(slot.itemKey());
                if (source != null && source.budgetInactive() > 0) {
                    wrapped(lines, SpellcraftNames.tr("bar.over_budget", source.name(), source.budgetInactive()), SpellcraftPalette.BAD);
                }
                for (String shadowed : slot.shadowed()) {
                    wrapped(lines, SpellcraftNames.tr("bar.shadowed", shadowed), SpellcraftPalette.DARK);
                }
            }
            case HIDDEN -> {
                int by = view.hiddenBy(index);
                String name = by < 0 ? "?" : view.slot(by).state() == SpellBarView.State.SPELL ? view.slot(by).spellName()
                        : SpellcraftNames.abilityName(tree, view.slot(by).group());
                lines.add(SpellcraftNames.colored(SpellcraftNames.tr("bar.hidden", name, by + 1, SpellcraftNames.groupName(tree, slot.group())), SpellcraftPalette.BAD));
            }
            case EMPTY -> lines.add(SpellcraftNames.colored(SpellcraftNames.tr("bar.empty"), SpellcraftPalette.GREY));
        }
        if (slot.unlearned()) {
            for (String group : slot.groups()) {
                if (!SpellBarView.learned(tree, group)) {
                    lines.add(SpellcraftNames.colored(SpellcraftNames.tr("bar.unlearned", SpellcraftNames.groupName(tree, group)), SpellcraftPalette.BAD));
                }
            }
        }
        SpellCooldowns.Blocking blocking = slot.filled() ? SpellCooldowns.blocking(tree, slot.groups()) : null;
        if (blocking != null) {
            lines.add(SpellcraftNames.colored(SpellcraftNames.tr("bar.waiting", SpellcraftNames.groupName(tree, blocking.group()),
                    StatText.seconds(blocking.ticks())), SpellcraftPalette.STAT_COOLDOWN));
        }
        lines.add(SpellcraftNames.colored(keyLine(index), SpellcraftPalette.GREY));
        String hint;
        if (view.isEffective()) {
            hint = SpellcraftNames.tr("bar.hint_item");
        } else if (host.selectedGroup() != null) {
            hint = SpellcraftNames.tr("bar.hint_default", SpellcraftNames.abilityName(tree, host.selectedGroup()));
        } else {
            hint = SpellcraftNames.tr("bar.hint_choose");
        }
        lines.add(new TextComponent(hint).withStyle(ChatFormatting.DARK_GRAY));
        return lines;
    }

    private static Component spellTitle(AbilityTree tree, List<String> groups) {
        MutableComponent names = new TextComponent("");
        for (int i = 0; i < groups.size(); i++) {
            String group = groups.get(i);
            if (i > 0) {
                names.append(SpellcraftNames.colored(" + ", SpellcraftPalette.GREY));
            }
            names.append(SpellcraftNames.colored(SpellcraftNames.groupName(tree, group), SpellcraftPalette.NAME_GOLD));
            String tier = tierSuffix(tree, group);
            if (!tier.isEmpty()) {
                names.append(SpellcraftNames.colored(tier, SpellcraftPalette.STAT_YELLOW));
            }
        }
        return new TranslatableComponent("woldsvaults.spellcraft.bar.augmented", names)
                .withStyle(style -> style.withColor(TextColor.fromRgb(SpellcraftPalette.GREY & 0xFFFFFF)));
    }

    private static void wrapped(List<Component> lines, String text, int color) {
        Minecraft.getInstance().font.getSplitter().splitLines(text, WRAP, Style.EMPTY)
                .forEach(line -> lines.add(SpellcraftNames.colored(line.getString(), color)));
    }

    private static List<Component> numberLines(SlotCostStats.Numbers numbers) {
        List<Component> lines = new ArrayList<>();
        if (numbers.hasMana()) {
            String before = StatText.compact(Math.round(numbers.manaBefore()));
            String after = StatText.compact(Math.round(numbers.manaAfter()));
            String mana = before.equals(after) ? after : SpellcraftNames.tr("fmt.arrow", before, after);
            lines.add(labelled(SpellcraftNames.tr("bar.mana_label"), mana, SpellcraftPalette.STAT_MANA));
        }
        String cooldown = numbers.cooldownBefore() == numbers.cooldownAfter()
                ? SpellcraftNames.tr("fmt.seconds", StatText.seconds(numbers.cooldownAfter()))
                : SpellcraftNames.tr("fmt.arrow", SpellcraftNames.tr("fmt.seconds", StatText.seconds(numbers.cooldownBefore())),
                SpellcraftNames.tr("fmt.seconds", StatText.seconds(numbers.cooldownAfter())));
        lines.add(labelled(SpellcraftNames.tr("bar.cooldown_label"), cooldown, SpellcraftPalette.STAT_COOLDOWN));
        Integer haste = SlotCostStats.hastePercent();
        if (haste != null && haste != 0) {
            lines.add(labelled(SpellcraftNames.tr("bar.haste_label"), (haste > 0 ? "+" : "") + haste + "%", SpellcraftPalette.STAT_COOLDOWN));
        }
        return lines;
    }

    private static List<Component> spellLines(AbilityTree tree, StepStats.SpellStats stats) {
        List<Component> lines = new ArrayList<>();
        if (stats.rows().size() == 1 && stats.rows().get(0) instanceof StepStats.Step step) {
            lines.addAll(statLines(step, ""));
            return lines;
        }
        for (StepStats.Row row : stats.rows()) {
            String indent = "  ".repeat(row.depth());
            if (row instanceof StepStats.Step step) {
                lines.add(SpellcraftNames.colored(indent + SpellcraftNames.groupName(tree, step.group()), SpellcraftPalette.NAME_GOLD));
                lines.addAll(statLines(step, indent + "  "));
            } else if (row instanceof StepStats.Orb orb) {
                lines.add(SpellcraftNames.colored(indent + SpellcraftNames.tr("bar.stat.orb_step"), SpellcraftPalette.ARCANE_GLOW));
                String inner = indent + "  ";
                if (orb.copies() > 1) {
                    lines.add(labelled(inner + SpellcraftNames.tr("bar.stat.orbs"), String.valueOf(orb.copies()), SpellcraftPalette.STAT_PLAIN));
                }
                lines.add(labelled(inner + SpellcraftNames.tr("bar.stat.velocity"),
                        SpellcraftNames.tr("fmt.blocks_per_second", StatText.compact(orb.velocity())), SpellcraftPalette.STAT_YELLOW));
                lines.add(labelled(inner + SpellcraftNames.tr("bar.stat.flight"),
                        SpellcraftNames.tr("fmt.seconds", StatText.trim(orb.flightSeconds())), SpellcraftPalette.STAT_YELLOW));
                glyphLine(orb.glyphs(), orb.relevance(), inner, lines);
            }
        }
        if (stats.total() != null) {
            lines.add(labelled(SpellcraftNames.tr("bar.stat.spell_total"), StatText.hit(stats.total()), SpellcraftPalette.STAT_DAMAGE));
        }
        return lines;
    }

    private static List<Component> statLines(StepStats.Step step, String indent) {
        List<Component> lines = new ArrayList<>();
        if (step.hit() != null) {
            String hit = StatText.hit(step.hit());
            if (step.charged() != null) {
                hit = SpellcraftNames.tr("fmt.charged", hit, StatText.hit(step.charged()));
            }
            lines.add(labelled(indent + SpellcraftNames.tr("bar.stat.damage"), hit, SpellcraftPalette.STAT_DAMAGE));
        }
        if (step.total() != null) {
            String total = StatText.hit(step.total());
            int echo = Math.round(step.echoPower() * 100.0F);
            if (step.echoPower() > 0.0F) {
                total = step.casts() > 1 ? SpellcraftNames.tr("fmt.casts_echo", total, step.casts(), echo) : SpellcraftNames.tr("fmt.echo", total, echo);
            } else {
                total = SpellcraftNames.tr("fmt.casts", total, step.casts());
            }
            if (step.chargedTotal() != null) {
                total = SpellcraftNames.tr("fmt.charged", total, StatText.hit(step.chargedTotal()));
            }
            lines.add(labelled(indent + SpellcraftNames.tr("bar.stat.total"), total, SpellcraftPalette.STAT_DAMAGE));
        }
        for (StepStatRows.Value value : StepStatRows.effects(step)) {
            lines.add(labelled(indent + SpellcraftNames.tr(value.key()), value.text(), value.color()));
        }
        if (step.glyphs() != null) {
            glyphLine(step.glyphs(), step.relevance(), indent, lines);
        }
        return lines;
    }

    private static void glyphLine(GlyphStacking.Pool pool, GlyphRelevance.Filter relevance, String indent, List<Component> lines) {
        String summary = GlyphText.poolSummary(pool, relevance);
        if (!summary.isEmpty()) {
            lines.add(SpellcraftNames.colored(indent + SpellcraftNames.tr("bar.stat.glyphs"), SpellcraftPalette.GREY).copy().append(GlyphText.tagged(summary)));
        }
    }

    private static Component labelled(String label, String value, int color) {
        return new TextComponent(label).withStyle(style -> style.withColor(TextColor.fromRgb(SpellcraftPalette.GREY & 0xFFFFFF)))
                .append(new TextComponent(value).withStyle(style -> style.withColor(TextColor.fromRgb(color & 0xFFFFFF))));
    }

    private static String tierSuffix(AbilityTree tree, String group) {
        String tier = SpellcraftNames.tier(tree, group);
        return tier.isEmpty() ? "" : " " + tier;
    }
}
