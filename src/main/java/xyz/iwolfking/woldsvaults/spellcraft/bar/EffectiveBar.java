package xyz.iwolfking.woldsvaults.spellcraft.bar;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.augments.AugmentType;
import xyz.iwolfking.woldsvaults.augments.FocusAugments;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.augments.spell.SpellModel;
import xyz.iwolfking.woldsvaults.augments.spell.SpellcraftRules;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;
import xyz.iwolfking.woldsvaults.spellcraft.pool.GlyphSources;
import xyz.iwolfking.woldsvaults.spellcraft.pool.GlyphView;
import xyz.iwolfking.woldsvaults.spellcraft.pool.ViewLine;

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
import java.util.function.Function;

/**
 * The bar a player actually casts. Every equipped arcana item lays its assigned spells over their slots; the lowest
 * priority number wins a slot, and a tie for the top is LOCKED. One ability group lives in only one slot: overlays
 * claim their groups in priority order, an overlay needing a group a higher priority already holds is LOCKED, and equal
 * priorities clashing on a group lock each other. Base slots claim last; a base slot whose group an overlay holds is
 * hidden. A base slot whose ability fits an auto glyph casts as an implicit spell. Each slot carries its occupant's
 * identity ({@code itemId:spellId} or {@code base:<group>}).
 */
public final class EffectiveBar {
    private final Slot[] slots;
    private final List<ArcanaItem> items;

    public enum Kind {
        EMPTY,
        ABILITY,
        SPELL
    }

    /**
     * One resolved slot. {@code group} is the ability (the trigger for a spell); {@code model} is null unless a spell.
     * {@code source*}, {@code hand} and {@code prio} describe the overlay's item; {@code shadowed} names lower-priority
     * overlays of the slot.
     */
    public record Slot(int index, Kind kind, String group, @Nullable SpellcraftItemData.Spell spell, @Nullable SpellModel model,
                       boolean hidden, String key, @Nullable String sourceKey, String sourceName, @Nullable InteractionHand hand, int prio,
                       boolean locked, String lockReason, boolean implicit, List<String> shadowed) {
        public Set<String> groups() {
            return switch (this.kind) {
                case ABILITY -> Set.of(this.group);
                case SPELL -> this.model == null ? Set.of(this.group) : this.model.groups();
                case EMPTY -> Set.of();
            };
        }

        public int spellId() {
            return this.spell == null || this.implicit ? 0 : this.spell.id();
        }

        static Slot empty(int index, String group, boolean hidden) {
            return new Slot(index, Kind.EMPTY, group, null, null, hidden, "", null, "", null, 0, false, "", false, List.of());
        }
    }

    /** One equipped arcana item: its identity, name, hand, spells and their check against its view. */
    public record ArcanaItem(String key, String name, @Nullable InteractionHand hand, ItemStack stack, List<SpellcraftItemData.Spell> spells,
                             SpellcraftRules.ItemCheck check) {
    }

    private record Overlay(ArcanaItem item, SpellcraftItemData.Spell spell, SpellModel model, int prio, int order) {
        String label() {
            return this.spell.name() + " (" + this.item.name() + ")";
        }
    }

    private EffectiveBar(Slot[] slots, List<ArcanaItem> items) {
        this.slots = slots;
        this.items = items;
    }

    /** The player's bar from their equipped sources (server). */
    public static EffectiveBar forPlayer(ServerPlayer player, String[] defaults, Function<String, String> specOf) {
        FocusAugmentsConfig config = FocusAugments.config();
        GlyphSources.Snapshot sources = GlyphSources.get(player);
        List<ArcanaItem> items = new ArrayList<>();
        if (config != null) {
            for (GlyphSources.Equipped equipped : sources.arcanaItems()) {
                GlyphView view = sources.view(config, equipped.stack());
                items.add(new ArcanaItem(equipped.key(), equipped.name(), equipped.hand(), equipped.stack(),
                        SpellcraftItemData.spells(equipped.stack()), SpellcraftRules.check(config, equipped.stack(), view, specOf)));
            }
        }
        return resolve(config, defaults, items, config == null ? GlyphView.EMPTY : sources.autoLines(config), specOf);
    }

    public static EffectiveBar resolve(@Nullable FocusAugmentsConfig config, String[] defaults, List<ArcanaItem> items, GlyphView auto,
                                       Function<String, String> specOf) {
        Slot[] slots = new Slot[SpellcraftItemData.SLOTS];
        Map<Integer, List<Overlay>> bySlot = new TreeMap<>();
        int order = 0;
        for (ArcanaItem item : items) {
            for (SpellcraftItemData.Spell spell : item.spells()) {
                SpellModel model = item.check().models().get(spell.id());
                if (config == null || !spell.assigned() || model == null || !model.isValid()) {
                    continue;
                }
                bySlot.computeIfAbsent(spell.slot(), slot -> new ArrayList<>())
                        .add(new Overlay(item, spell, model, SpellcraftRules.priority(config, spell), order++));
            }
        }
        Map<Overlay, String> locked = new LinkedHashMap<>();
        Map<Integer, Overlay> winners = new TreeMap<>();
        Map<Integer, List<String>> shadowed = new HashMap<>();
        for (Map.Entry<Integer, List<Overlay>> entry : bySlot.entrySet()) {
            List<Overlay> overlays = new ArrayList<>(entry.getValue());
            overlays.sort(Comparator.comparingInt(Overlay::prio).thenComparingInt(Overlay::order));
            int top = overlays.get(0).prio();
            List<Overlay> tied = overlays.stream().filter(overlay -> overlay.prio() == top).toList();
            List<String> lower = new ArrayList<>();
            overlays.stream().filter(overlay -> overlay.prio() != top)
                    .forEach(overlay -> lower.add(overlay.label() + ", priority " + overlay.prio()));
            shadowed.put(entry.getKey(), lower);
            winners.put(entry.getKey(), tied.get(0));
            if (tied.size() > 1) {
                List<String> names = new ArrayList<>();
                tied.forEach(overlay -> names.add(overlay.label()));
                locked.put(tied.get(0), String.join(" and ", names) + (names.size() == 2 ? " both" : " all") + " want slot " + (entry.getKey() + 1) + " at priority " + top);
            }
        }
        Map<String, Integer> claimedBy = new HashMap<>();
        List<Integer> claimOrder = new ArrayList<>(winners.keySet());
        claimOrder.sort(Comparator.comparingInt((Integer slot) -> winners.get(slot).prio()).thenComparingInt(slot -> slot));
        int i = 0;
        while (i < claimOrder.size()) {
            int prio = winners.get(claimOrder.get(i)).prio();
            List<Integer> level = new ArrayList<>();
            while (i < claimOrder.size() && winners.get(claimOrder.get(i)).prio() == prio) {
                level.add(claimOrder.get(i++));
            }
            List<Integer> open = new ArrayList<>();
            for (int slot : level) {
                Overlay overlay = winners.get(slot);
                if (locked.containsKey(overlay)) {
                    continue;
                }
                String held = null;
                for (String group : overlay.model().groups()) {
                    if (claimedBy.containsKey(group)) {
                        held = groupName(group) + " is on slot " + (claimedBy.get(group) + 1);
                        break;
                    }
                }
                if (held != null) {
                    locked.put(overlay, held);
                } else {
                    open.add(slot);
                }
            }
            for (int a : open) {
                for (int b : open) {
                    if (a == b) {
                        continue;
                    }
                    for (String group : winners.get(a).model().groups()) {
                        if (winners.get(b).model().groups().contains(group)) {
                            locked.putIfAbsent(winners.get(a), winners.get(a).label() + " and " + winners.get(b).label()
                                    + " both cast " + groupName(group) + " at priority " + prio);
                        }
                    }
                }
            }
            for (int slot : open) {
                Overlay overlay = winners.get(slot);
                if (!locked.containsKey(overlay)) {
                    overlay.model().groups().forEach(group -> claimedBy.put(group, slot));
                }
            }
        }
        for (Map.Entry<Integer, Overlay> entry : winners.entrySet()) {
            Overlay overlay = entry.getValue();
            String reason = locked.get(overlay);
            int index = entry.getKey();
            slots[index] = new Slot(index, Kind.SPELL, overlay.spell().trigger(), overlay.spell(), overlay.model(), false,
                    reason == null ? overlay.item().key() + ":" + overlay.spell().id() : "", overlay.item().key(), overlay.item().name(),
                    overlay.item().hand(), overlay.prio(), reason != null, reason == null ? "" : reason, false,
                    List.copyOf(shadowed.getOrDefault(index, List.of())));
        }
        Set<String> claimed = new HashSet<>(claimedBy.keySet());
        for (int index = 0; index < slots.length; index++) {
            if (slots[index] != null) {
                continue;
            }
            String group = defaults[index] == null ? "" : defaults[index];
            if (group.isEmpty()) {
                slots[index] = Slot.empty(index, "", false);
            } else if (claimed.contains(group)) {
                slots[index] = Slot.empty(index, group, true);
            } else {
                claimed.add(group);
                slots[index] = base(config, index, group, auto, specOf);
            }
        }
        return new EffectiveBar(slots, List.copyOf(items));
    }

    private static Slot base(@Nullable FocusAugmentsConfig config, int index, String group, GlyphView auto, Function<String, String> specOf) {
        String key = "base:" + group;
        String spec = specOf.apply(group);
        if (config != null && spec != null && !auto.isEmpty()) {
            Set<String> traits = SpellModel.traits(config, spec);
            List<String> tokens = new ArrayList<>();
            tokens.add(group);
            for (AugmentType type : AugmentType.values()) {
                ViewLine line = auto.get(type);
                if (line != null && type.isModifier(config) && type.fits(config, traits, spec)) {
                    tokens.add(type.id());
                }
            }
            if (tokens.size() > 1) {
                SpellModel model = SpellModel.build(config, auto, config.rules.maxGlyphsPerSpell, tokens, specOf, SpellModel.Mode.LENIENT);
                if (model.isValid() && model.glyphs().stream().anyMatch(SpellModel.Glyph::active)) {
                    SpellcraftItemData.Spell spell = new SpellcraftItemData.Spell(0, groupName(group), List.copyOf(tokens));
                    return new Slot(index, Kind.SPELL, group, spell, model, false, key, null, "", null, 0, false, "", true, List.of());
                }
            }
        }
        return new Slot(index, Kind.ABILITY, group, null, null, false, key, null, "", null, 0, false, "", false, List.of());
    }

    private static String groupName(String group) {
        return group.replace('_', ' ');
    }

    public Slot slot(int index) {
        return this.slots[index];
    }

    public List<Slot> slots() {
        return List.of(this.slots);
    }

    /** The equipped arcana items this bar was resolved from, with their checks. */
    public List<ArcanaItem> items() {
        return this.items;
    }

    /** The slot casting this ability group (a locked slot casts nothing), or -1. */
    public int slotOf(String group) {
        for (Slot slot : this.slots) {
            if (slot.kind() != Kind.EMPTY && !slot.locked() && slot.groups().contains(group)) {
                return slot.index();
            }
        }
        return -1;
    }

    /** Whether any slot is laid over by an item's spell. */
    public boolean hasOverlays() {
        for (Slot slot : this.slots) {
            if (slot.sourceKey() != null) {
                return true;
            }
        }
        return false;
    }
}
