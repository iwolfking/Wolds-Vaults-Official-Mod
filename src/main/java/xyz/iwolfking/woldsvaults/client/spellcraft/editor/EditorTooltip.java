package xyz.iwolfking.woldsvaults.client.spellcraft.editor;

import net.minecraft.network.chat.Component;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.GlyphText;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftNames;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftPalette;
import xyz.iwolfking.woldsvaults.client.spellcraft.stats.GlyphRelevance;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphType;
import xyz.iwolfking.woldsvaults.spellcraft.spell.SpellcraftRules;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

final class EditorTooltip {
    private final PouchPanel pouch;
    private final SpellTabs tabs;
    private final WeavePanel weave;
    private final StatsPanel stats;
    private final TitleBar title;

    EditorTooltip(PouchPanel pouch, SpellTabs tabs, WeavePanel weave, StatsPanel stats, TitleBar title) {
        this.pouch = pouch;
        this.tabs = tabs;
        this.weave = weave;
        this.stats = stats;
        this.title = title;
    }

    List<Component> hoverTooltip(SpellcraftConfig config, @Nullable SpellDrafts.Draft draft, @Nullable SpellcraftRules.ItemCheck check, int mouseX,
                                 int mouseY) {
        List<Component> lines = this.pouch.tooltipAt(config, mouseX, mouseY);
        if (lines != null) {
            return lines;
        }
        lines = this.tabs.tooltipAt(mouseX, mouseY);
        if (lines != null) {
            return lines;
        }
        lines = this.weave.tooltipAt(config, draft, check, mouseX, mouseY);
        if (lines != null) {
            return lines;
        }
        lines = this.title.tooltipAt(check, mouseX, mouseY);
        if (lines != null) {
            return lines;
        }
        lines = this.stats.tooltipAt(draft, check, mouseX, mouseY);
        return lines == null ? List.of() : lines;
    }

    static void glyphLines(SpellcraftConfig config, GlyphType type, float potency, float quality, GlyphRelevance.Filter relevance,
                           List<Component> lines) {
        SpellcraftConfig.GlyphDefinition glyph = config.glyph(type);
        lines.add(SpellcraftNames.colored(type.displayName(), SpellcraftPalette.categoryColor(config, type)));
        lines.add(GlyphText.describe(config, type, potency, quality));
        String side = GlyphText.sideEffects(config, type, potency, relevance);
        if (!side.isEmpty()) {
            lines.add(GlyphText.tagged(side));
        }
        List<String> fits = GlyphText.fits(glyph);
        if (!fits.isEmpty()) {
            lines.add(SpellcraftNames.colored(SpellcraftNames.tr("screen.fits", String.join(", ", fits)), SpellcraftPalette.GREY));
        }
        if (!glyph.forbids.isEmpty()) {
            List<String> words = new ArrayList<>();
            glyph.forbids.forEach(trait -> words.add(SpellcraftNames.capitalize(trait)));
            lines.add(SpellcraftNames.colored(SpellcraftNames.tr("screen.forbids", String.join(", ", words)), SpellcraftPalette.GREY));
        }
    }
}
