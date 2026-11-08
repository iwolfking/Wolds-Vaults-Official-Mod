package xyz.iwolfking.woldsvaults.spellcraft.bar;

import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.augments.spell.SpellModel;
import xyz.iwolfking.woldsvaults.augments.spell.SpellcraftRules;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * The bar a player actually casts: the default bar, overlaid by the off-hand focus's or wand's bar. An item slot that
 * isn't set inherits the default slot; explicit item slots win, and an inherited slot whose ability an explicit slot
 * already uses is hidden, so an ability is never on the bar twice. Each slot carries a key that changes whenever the
 * spell in it changes; the swap rule compares keys.
 */
public final class EffectiveBar {
    private final Slot[] slots;
    private final ItemStack source;
    @Nullable
    private final SpellcraftRules.ItemCheck itemCheck;

    public enum Kind {
        EMPTY,
        ABILITY,
        SPELL
    }

    /** One resolved slot. {@code group} is the ability (the trigger for a spell); {@code model} is null unless a spell. */
    public record Slot(int index, Kind kind, String group, @Nullable SpellcraftItemData.Spell spell, @Nullable SpellModel model,
                       boolean inherited, boolean hidden, String key) {
        public Set<String> groups() {
            return switch (this.kind) {
                case ABILITY -> Set.of(this.group);
                case SPELL -> this.model == null ? Set.of(this.group) : this.model.groups();
                case EMPTY -> Set.of();
            };
        }
    }

    private EffectiveBar(Slot[] slots, ItemStack source, @Nullable SpellcraftRules.ItemCheck itemCheck) {
        this.slots = slots;
        this.source = source;
        this.itemCheck = itemCheck;
    }

    public static EffectiveBar resolve(@Nullable FocusAugmentsConfig config, String[] defaults, ItemStack source,
                                       Function<String, String> specOf) {
        Slot[] slots = new Slot[SpellcraftItemData.SLOTS];
        boolean hasItem = !source.isEmpty() && SpellcraftItemData.has(source);
        String[] itemBar = hasItem ? SpellcraftItemData.bar(source) : null;
        SpellcraftRules.ItemCheck check = hasItem && config != null ? SpellcraftRules.check(config, source, specOf, false) : null;
        long itemId = hasItem ? SpellcraftItemData.id(source) : 0L;
        Set<String> claimed = new HashSet<>();
        for (int i = 0; i < slots.length; i++) {
            String entry = itemBar == null ? SpellcraftItemData.INHERIT : itemBar[i];
            if (entry.equals(SpellcraftItemData.INHERIT)) {
                continue;
            }
            Slot slot = explicit(i, entry, source, itemId, check);
            if (slot.groups().stream().anyMatch(claimed::contains)) {
                slot = new Slot(i, Kind.EMPTY, slot.group(), null, null, false, true, "");
            } else {
                claimed.addAll(slot.groups());
            }
            slots[i] = slot;
        }
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] != null) {
                continue;
            }
            String group = defaults[i] == null ? "" : defaults[i];
            if (group.isEmpty()) {
                slots[i] = new Slot(i, Kind.EMPTY, "", null, null, hasItem, false, "");
            } else if (claimed.contains(group)) {
                slots[i] = new Slot(i, Kind.EMPTY, group, null, null, hasItem, true, "");
            } else {
                claimed.add(group);
                slots[i] = new Slot(i, Kind.ABILITY, group, null, null, hasItem, false, "a:" + group);
            }
        }
        return new EffectiveBar(slots, hasItem ? source : ItemStack.EMPTY, check);
    }

    private static Slot explicit(int index, String entry, ItemStack source, long itemId, @Nullable SpellcraftRules.ItemCheck check) {
        if (entry.startsWith(SpellcraftItemData.ABILITY_PREFIX)) {
            String group = entry.substring(SpellcraftItemData.ABILITY_PREFIX.length());
            return new Slot(index, Kind.ABILITY, group, null, null, false, false, "a:" + group);
        }
        if (entry.startsWith(SpellcraftItemData.SPELL_PREFIX) && check != null) {
            try {
                int id = Integer.parseInt(entry.substring(SpellcraftItemData.SPELL_PREFIX.length()));
                SpellcraftItemData.Spell spell = SpellcraftItemData.spell(source, id).orElse(null);
                SpellModel model = check.models().get(id);
                if (spell != null && model != null) {
                    String key = "s:" + itemId + ":" + id + ":" + spell.tokens().hashCode();
                    return new Slot(index, Kind.SPELL, spell.trigger(), spell, model, false, false, key);
                }
            } catch (NumberFormatException e) {
                WoldsVaults.LOGGER.error("Spell bar: item bar entry {} is not a spell id; slot {} left empty", entry, index + 1);
            }
        }
        return new Slot(index, Kind.EMPTY, "", null, null, false, false, "");
    }

    public Slot slot(int index) {
        return this.slots[index];
    }

    public List<Slot> slots() {
        return List.of(this.slots);
    }

    /** The slot casting this ability group, or -1. */
    public int slotOf(String group) {
        for (Slot slot : this.slots) {
            if (slot.kind() != Kind.EMPTY && slot.groups().contains(group)) {
                return slot.index();
            }
        }
        return -1;
    }

    public ItemStack source() {
        return this.source;
    }

    public boolean hasItemBar() {
        return !this.source.isEmpty();
    }

    /** Problems with the source item as a whole (budget, glyph uses); its spells don't cast while there are any. */
    public List<String> itemErrors() {
        return this.itemCheck == null ? List.of() : this.itemCheck.errors();
    }
}
