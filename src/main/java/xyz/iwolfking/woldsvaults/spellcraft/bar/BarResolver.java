package xyz.iwolfking.woldsvaults.spellcraft.bar;

import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.spellcraft.ability.AbilityLookup;
import xyz.iwolfking.woldsvaults.spellcraft.bar.EffectiveBar.ArcanaItem;
import xyz.iwolfking.woldsvaults.spellcraft.bar.EffectiveBar.Kind;
import xyz.iwolfking.woldsvaults.spellcraft.bar.EffectiveBar.Slot;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphType;
import xyz.iwolfking.woldsvaults.spellcraft.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.spellcraft.item.StoredSpell;
import xyz.iwolfking.woldsvaults.spellcraft.source.GlyphView;
import xyz.iwolfking.woldsvaults.spellcraft.source.GlyphViewLine;
import xyz.iwolfking.woldsvaults.spellcraft.spell.PlacedGlyph;
import xyz.iwolfking.woldsvaults.spellcraft.spell.ReadMode;
import xyz.iwolfking.woldsvaults.spellcraft.spell.SpellModel;
import xyz.iwolfking.woldsvaults.spellcraft.spell.SpellcraftRules;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

final class BarResolver {
    private static final Set<String> DROPPED = ConcurrentHashMap.newKeySet();
    @Nullable
    private final SpellcraftConfig config;
    private final String[] defaults;
    private final List<ArcanaItem> items;
    private final GlyphView auto;
    private final Function<String, String> specOf;
    private final Slot[] slots = new Slot[SpellcraftItemData.SLOTS];
    private final Map<Integer, List<Overlay>> bySlot = new TreeMap<>();
    private final Map<Overlay, String> locked = new LinkedHashMap<>();
    private final Map<Integer, Overlay> winners = new TreeMap<>();
    private final Map<Integer, List<String>> shadowed = new HashMap<>();
    private final Map<String, Integer> claimedBy = new HashMap<>();

    private record Overlay(ArcanaItem item, StoredSpell spell, SpellModel model, int prio, int order) {
        String label() {
            return this.spell.name() + " (" + this.item.name() + ")";
        }
    }

    private BarResolver(@Nullable SpellcraftConfig config, String[] defaults, List<ArcanaItem> items, GlyphView auto,
                        Function<String, String> specOf) {
        this.config = config;
        this.defaults = defaults;
        this.items = items;
        this.auto = auto;
        this.specOf = specOf;
    }

    static Slot[] resolve(@Nullable SpellcraftConfig config, String[] defaults, List<ArcanaItem> items, GlyphView auto,
                          Function<String, String> specOf) {
        BarResolver resolver = new BarResolver(config, defaults, items, auto, specOf);
        resolver.collectOverlays();
        resolver.pickSlotWinners();
        resolver.claimGroups();
        resolver.placeWinners();
        resolver.fillBaseSlots();
        return resolver.slots;
    }

    private void collectOverlays() {
        int order = 0;
        for (ArcanaItem item : this.items) {
            for (StoredSpell spell : item.spells()) {
                SpellModel model = item.check().models().get(spell.id());
                if (this.config == null || !spell.assigned() || model == null) {
                    continue;
                }
                if (!model.isValid()) {
                    if (DROPPED.add(item.key() + ":" + spell.id() + ":" + model.errors().get(0))) {
                        WoldLogger.warn("Spellcraft: spell '{}' on {} is assigned to slot {} but can't be cast, so it is off the bar: {}",
                                spell.name(), item.name(), spell.slot() + 1, model.errors().get(0));
                    }
                    continue;
                }
                this.bySlot.computeIfAbsent(spell.slot(), slot -> new ArrayList<>())
                        .add(new Overlay(item, spell, model, SpellcraftRules.priority(this.config, spell), order++));
            }
        }
    }

    private void pickSlotWinners() {
        for (Map.Entry<Integer, List<Overlay>> entry : this.bySlot.entrySet()) {
            List<Overlay> overlays = new ArrayList<>(entry.getValue());
            overlays.sort(Comparator.comparingInt((Overlay overlay) -> -overlay.prio()).thenComparingInt(Overlay::order));
            int top = overlays.get(0).prio();
            List<Overlay> tied = overlays.stream().filter(overlay -> overlay.prio() == top).toList();
            List<String> lower = new ArrayList<>();
            overlays.stream().filter(overlay -> overlay.prio() != top)
                    .forEach(overlay -> lower.add(overlay.label() + ", priority " + overlay.prio()));
            this.shadowed.put(entry.getKey(), lower);
            this.winners.put(entry.getKey(), tied.get(0));
            if (tied.size() > 1) {
                List<String> names = new ArrayList<>();
                tied.forEach(overlay -> names.add(overlay.label()));
                this.locked.put(tied.get(0), String.join(" and ", names) + (names.size() == 2 ? " both" : " all") + " want slot " + (entry.getKey() + 1) + " at priority " + top);
            }
        }
    }

    private void claimGroups() {
        List<Integer> claimOrder = new ArrayList<>(this.winners.keySet());
        claimOrder.sort(Comparator.comparingInt((Integer slot) -> -this.winners.get(slot).prio()).thenComparingInt(slot -> slot));
        int i = 0;
        while (i < claimOrder.size()) {
            int prio = this.winners.get(claimOrder.get(i)).prio();
            List<Integer> level = new ArrayList<>();
            while (i < claimOrder.size() && this.winners.get(claimOrder.get(i)).prio() == prio) {
                level.add(claimOrder.get(i++));
            }
            List<Integer> open = new ArrayList<>();
            for (int slot : level) {
                Overlay overlay = this.winners.get(slot);
                if (this.locked.containsKey(overlay)) {
                    continue;
                }
                String held = null;
                for (String group : overlay.model().groups()) {
                    if (this.claimedBy.containsKey(group)) {
                        held = AbilityLookup.groupName(group) + " is on slot " + (this.claimedBy.get(group) + 1);
                        break;
                    }
                }
                if (held != null) {
                    this.locked.put(overlay, held);
                } else {
                    open.add(slot);
                }
            }
            for (int a : open) {
                for (int b : open) {
                    if (a == b) {
                        continue;
                    }
                    for (String group : this.winners.get(a).model().groups()) {
                        if (this.winners.get(b).model().groups().contains(group)) {
                            this.locked.putIfAbsent(this.winners.get(a), this.winners.get(a).label() + " and " + this.winners.get(b).label()
                                    + " both cast " + AbilityLookup.groupName(group) + " at priority " + prio);
                        }
                    }
                }
            }
            for (int slot : open) {
                Overlay overlay = this.winners.get(slot);
                if (!this.locked.containsKey(overlay)) {
                    overlay.model().groups().forEach(group -> this.claimedBy.put(group, slot));
                }
            }
        }
    }

    private void placeWinners() {
        for (Map.Entry<Integer, Overlay> entry : this.winners.entrySet()) {
            Overlay overlay = entry.getValue();
            String reason = this.locked.get(overlay);
            int index = entry.getKey();
            this.slots[index] = new Slot(index, Kind.SPELL, overlay.spell().trigger(), overlay.spell(), overlay.model(), false,
                    reason == null ? overlay.item().key() + ":" + overlay.spell().id() : "", overlay.item().key(), overlay.item().name(),
                    overlay.item().hand(), overlay.prio(), reason != null, reason == null ? "" : reason, false,
                    List.copyOf(this.shadowed.getOrDefault(index, List.of())));
        }
    }

    private void fillBaseSlots() {
        Set<String> claimed = new HashSet<>(this.claimedBy.keySet());
        for (int index = 0; index < this.slots.length; index++) {
            if (this.slots[index] != null) {
                continue;
            }
            String group = this.defaults[index] == null ? "" : this.defaults[index];
            if (group.isEmpty()) {
                this.slots[index] = Slot.empty(index, "", false);
            } else if (claimed.contains(group)) {
                this.slots[index] = Slot.empty(index, group, true);
            } else {
                claimed.add(group);
                this.slots[index] = base(this.config, index, group, this.auto, this.specOf);
            }
        }
    }

    private static Slot base(@Nullable SpellcraftConfig config, int index, String group, GlyphView auto, Function<String, String> specOf) {
        String key = "base:" + group;
        String spec = specOf.apply(group);
        if (config != null && spec != null && !auto.isEmpty()) {
            Set<String> traits = SpellModel.traits(config, spec);
            List<String> tokens = new ArrayList<>();
            tokens.add(group);
            for (GlyphType type : GlyphType.values()) {
                GlyphViewLine line = auto.get(type);
                if (line != null && type.isModifier(config) && type.fits(config, traits, spec)) {
                    tokens.add(type.id());
                }
            }
            if (tokens.size() > 1) {
                SpellModel model = SpellModel.build(config, auto, config.rules.maxGlyphsPerSpell, tokens, specOf, ReadMode.LENIENT);
                if (model.isValid() && model.glyphs().stream().anyMatch(PlacedGlyph::active)) {
                    StoredSpell spell = new StoredSpell(0, AbilityLookup.groupName(group), List.copyOf(tokens));
                    return new Slot(index, Kind.SPELL, group, spell, model, false, key, null, "", null, 0, false, "", true, List.of());
                }
            }
        }
        return new Slot(index, Kind.ABILITY, group, null, null, false, key, null, "", null, 0, false, "", false, List.of());
    }
}
