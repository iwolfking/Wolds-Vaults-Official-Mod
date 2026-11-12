package xyz.iwolfking.woldsvaults.spellcraft.pool;

import xyz.iwolfking.woldsvaults.augments.AugmentType;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One arcana item's glyph view: for each glyph type, the line its spells use. The candidates for a type are the item's
 * own line plus every transferable line of that type on other equipped sources. Potency, quality and cost come from
 * the candidate with the highest potency (ties: higher quality, then lower cost, then source order, own line first);
 * the per-spell limit is the largest {@code min(cap, uses)} over the candidates, where the own line may carry its own
 * cap and a borrowed line always uses the type's cap. The same code builds views on the server and the client.
 */
public final class GlyphView {
    public static final GlyphView EMPTY = new GlyphView(Map.of());

    private final Map<AugmentType, ViewLine> lines;

    private GlyphView(Map<AugmentType, ViewLine> lines) {
        this.lines = lines;
    }

    /** The per-spell limit of an item's own line: its cap override when it has one, else the type's cap, within its uses. */
    public static int ownLimit(FocusAugmentsConfig config, SpellcraftItemData.Line line) {
        int cap = line.cap() != null ? line.cap() : config.glyph(line.type()).capPerSpell;
        return Math.max(0, Math.min(cap, line.uses()));
    }

    /** The per-spell limit a borrowed line lends: never its own cap override, only the type's cap. */
    public static int borrowedLimit(FocusAugmentsConfig config, SpellcraftItemData.Line line) {
        return Math.max(0, Math.min(config.glyph(line.type()).capPerSpell, line.uses()));
    }

    /**
     * Builds the view of the item {@code ownKey} from its own lines and the shared (transferable) candidates of every
     * equipped source; shared candidates from the item itself are skipped.
     */
    public static GlyphView of(FocusAugmentsConfig config, String ownKey, String ownName, List<SpellcraftItemData.Line> own,
                               List<GlyphCandidate> shared) {
        Map<AugmentType, List<Entry>> byType = new LinkedHashMap<>();
        for (SpellcraftItemData.Line line : own) {
            byType.computeIfAbsent(line.type(), type -> new ArrayList<>()).add(new Entry(line, ownName, true, ownLimit(config, line)));
        }
        for (GlyphCandidate candidate : shared) {
            if (candidate.sourceKey().equals(ownKey)) {
                continue;
            }
            byType.computeIfAbsent(candidate.line().type(), type -> new ArrayList<>())
                    .add(new Entry(candidate.line(), candidate.sourceName(), false, borrowedLimit(config, candidate.line())));
        }
        Map<AugmentType, ViewLine> lines = new LinkedHashMap<>();
        byType.forEach((type, entries) -> lines.put(type, combine(type, entries)));
        return new GlyphView(Collections.unmodifiableMap(lines));
    }

    /** The best line of each type among candidates that have no owning item (auto lines). */
    public static GlyphView best(FocusAugmentsConfig config, List<GlyphCandidate> candidates) {
        return of(config, "", "", List.of(), candidates);
    }

    private record Entry(SpellcraftItemData.Line line, String source, boolean own, int limit) {
    }

    private static ViewLine combine(AugmentType type, List<Entry> entries) {
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
        return new ViewLine(type, potency.line().potency(), potency.line().quality(), potency.line().cost(), uses.limit(), potency.source(),
                uses.source(), potency.own(), uses.own(), transferable);
    }

    private static boolean stronger(SpellcraftItemData.Line a, SpellcraftItemData.Line b) {
        if (a.potency() != b.potency()) {
            return a.potency() > b.potency();
        }
        if (a.quality() != b.quality()) {
            return a.quality() > b.quality();
        }
        return a.cost() < b.cost();
    }

    @Nullable
    public ViewLine get(AugmentType type) {
        return this.lines.get(type);
    }

    /** The view's lines: the item's own types in line order, then borrowed types in source order. */
    public Collection<ViewLine> lines() {
        return this.lines.values();
    }

    public boolean isEmpty() {
        return this.lines.isEmpty();
    }
}
