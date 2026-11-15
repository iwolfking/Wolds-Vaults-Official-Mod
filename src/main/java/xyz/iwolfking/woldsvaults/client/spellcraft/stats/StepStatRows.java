package xyz.iwolfking.woldsvaults.client.spellcraft.stats;

import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftNames;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftPalette;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.StatText;

import java.util.ArrayList;
import java.util.List;

/** The effect lines of one ability step that the slot tooltip and the spell editor both list, in the same order. */
public final class StepStatRows {
    private StepStatRows() {
    }

    /** One stat line, where {@code key} is a lang key under {@code woldsvaults.spellcraft.}. */
    public record Value(String key, String text, int color) {
    }

    /**
     * Everything a step does besides its main hit: splinter and splash hits, main effects, radius, splash radius,
     * velocity, duration and the ability's other numbers.
     */
    public static List<Value> effects(StepStats.Step step) {
        List<Value> rows = new ArrayList<>();
        if (step.splinter() != null) {
            rows.add(new Value("bar.stat.splinter", StatText.hit(step.splinter()), SpellcraftPalette.STAT_DAMAGE));
        }
        if (step.splash() != null) {
            rows.add(new Value("bar.stat.splash", StatText.hit(step.splash()), SpellcraftPalette.STAT_DAMAGE));
        }
        for (StepStats.Extra main : step.mains()) {
            rows.add(new Value("bar.stat." + main.key(), main.value(), main.color()));
        }
        if (step.radius() >= 0.0F) {
            rows.add(new Value("bar.stat.radius", SpellcraftNames.tr("fmt.blocks", StatText.compact(step.radius())), SpellcraftPalette.STAT_AREA));
        }
        if (step.splashRadius() >= 0.0F) {
            rows.add(new Value("bar.stat.splash_radius", SpellcraftNames.tr("fmt.blocks", StatText.compact(step.splashRadius())), SpellcraftPalette.STAT_AREA));
        }
        if (step.velocity() >= 0.0F) {
            rows.add(new Value("bar.stat.velocity", SpellcraftNames.tr("fmt.blocks_per_second", StatText.compact(step.velocity())),
                    SpellcraftPalette.STAT_YELLOW));
        }
        if (step.durationTicks() >= 0) {
            rows.add(new Value("bar.stat.duration", SpellcraftNames.tr("fmt.seconds", StatText.seconds(step.durationTicks())), SpellcraftPalette.STAT_YELLOW));
        }
        for (StepStats.Extra extra : step.extras()) {
            rows.add(new Value("bar.stat." + extra.key(), extra.value(), extra.color()));
        }
        return rows;
    }
}
