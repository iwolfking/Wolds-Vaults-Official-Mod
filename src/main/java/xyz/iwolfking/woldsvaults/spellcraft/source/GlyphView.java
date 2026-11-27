package xyz.iwolfking.woldsvaults.spellcraft.source;

import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphType;
import xyz.iwolfking.woldsvaults.spellcraft.item.GlyphLine;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One arcana item's glyph view: for each glyph type, the line its spells use, chosen from the item's own line and every
 * transferable line of that type on other equipped sources. Potency, quality and cost come from the candidate with the
 * highest potency (ties: higher quality, then lower cost, then source order, own line first), and the per-spell limit
 * is the largest {@code min(cap, uses)} over the candidates, where only the own line may carry its own cap. The same
 * code builds views on the server and the client.
 */
public final class GlyphView {
    public static final GlyphView EMPTY = new GlyphView(Map.of());

    private final Map<GlyphType, GlyphViewLine> lines;

    private GlyphView(Map<GlyphType, GlyphViewLine> lines) {
        this.lines = lines;
    }

    /**
     * The per-spell limit of an item's own line: its cap override when it has one, else the type's cap, within its
     * uses.
     */
    public static int ownLimit(SpellcraftConfig config, GlyphLine line) {
        int cap = line.cap() != null ? line.cap() : config.glyph(line.type()).capPerSpell;
        return Math.max(0, Math.min(cap, line.uses()));
    }

    private static int borrowedLimit(SpellcraftConfig config, GlyphLine line) {
        return Math.max(0, Math.min(config.glyph(line.type()).capPerSpell, line.uses()));
    }

    /**
     * Builds the view of the item {@code ownKey} from its own lines and the shared (transferable) candidates of every
     * equipped source; shared candidates from the item itself are skipped.
     */
    public static GlyphView of(SpellcraftConfig config, String ownKey, String ownName, List<GlyphLine> own,
                               List<GlyphCandidate> shared) {
        Map<GlyphType, List<Entry>> byType = new LinkedHashMap<>();
        for (GlyphLine line : own) {
            byType.computeIfAbsent(line.type(), type -> new ArrayList<>()).add(new Entry(line, ownName, true, ownLimit(config, line)));
        }
        for (GlyphCandidate candidate : shared) {
            if (candidate.sourceKey().equals(ownKey)) {
                continue;
            }
            byType.computeIfAbsent(candidate.line().type(), type -> new ArrayList<>())
                    .add(new Entry(candidate.line(), candidate.sourceName(), false, borrowedLimit(config, candidate.line())));
        }
        Map<GlyphType, GlyphViewLine> lines = new LinkedHashMap<>();
        byType.forEach((type, entries) -> lines.put(type, combine(type, entries)));
        return new GlyphView(Collections.unmodifiableMap(lines));
    }

    /** The best line of each type among candidates that have no owning item (auto lines). */
    public static GlyphView best(SpellcraftConfig config, List<GlyphCandidate> candidates) {
        return of(config, "", "", List.of(), candidates);
    }

    private record Entry(GlyphLine line, String source, boolean own, int limit) {
    }

    private static GlyphViewLine combine(GlyphType type, List<Entry> entries) {
        Entry potency = entries.get(0);
        Entry uses = entries.get(0);
        for (Entry entry : entries) {
            if (stronger(entry.line(), potency.line())) {
                potency = entry;
            }
            if (entry.limit() > uses.limit()) {
                uses = entry;
            }
        }
        boolean transferable = entries.stream().anyMatch(entry -> entry.own() && entry.line().transferable());
        return new GlyphViewLine(type, potency.line().potency(), potency.line().quality(), potency.line().cost(), uses.limit(), potency.source(),
                uses.source(), potency.own(), uses.own(), transferable);
    }

    private static boolean stronger(GlyphLine a, GlyphLine b) {
        if (a.potency() != b.potency()) {
            return a.potency() > b.potency();
        }
        if (a.quality() != b.quality()) {
            return a.quality() > b.quality();
        }
        return a.cost() < b.cost();
    }

    @Nullable
    public GlyphViewLine get(GlyphType type) {
        return this.lines.get(type);
    }

    /** The view's lines: the item's own types in line order, then borrowed types in source order. */
    public Collection<GlyphViewLine> lines() {
        return this.lines.values();
    }

    public boolean isEmpty() {
        return this.lines.isEmpty();
    }
}
