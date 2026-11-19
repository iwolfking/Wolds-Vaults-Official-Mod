package xyz.iwolfking.woldsvaults.spellcraft.bar;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.spellcraft.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.spellcraft.item.StoredSpell;
import xyz.iwolfking.woldsvaults.spellcraft.source.EquippedSources;
import xyz.iwolfking.woldsvaults.spellcraft.source.GlyphView;
import xyz.iwolfking.woldsvaults.spellcraft.spell.SpellModel;
import xyz.iwolfking.woldsvaults.spellcraft.spell.SpellcraftRules;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * The bar a player actually casts. Its base slots are the saved default bar in slot mode, and one slot per learned
 * ability in classic mode. Every equipped arcana item lays its assigned spells over their slots (in classic mode, the
 * slot of the spell's trigger ability): the highest
 * priority number wins a slot and a tie for the top is LOCKED. One ability group lives in only one slot, so overlays
 * claim their groups in priority order, an overlay needing a group a higher priority already holds is LOCKED, equal
 * priorities clashing on a group lock each other, and base slots claim last and are hidden when an overlay holds their
 * group. A base slot whose ability fits an auto glyph casts as an implicit spell.
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
     * {@code item*}, {@code hand} and {@code prio} describe the overlay's item; {@code shadowed} names lower-priority
     * overlays of the slot.
     */
    public record Slot(int index, Kind kind, String group, @Nullable StoredSpell spell, @Nullable SpellModel model,
                       boolean hidden, String occupantKey, @Nullable String itemKey, String itemName, @Nullable InteractionHand hand, int prio,
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

    public record ArcanaItem(String key, String name, @Nullable InteractionHand hand, ItemStack stack, List<StoredSpell> spells,
                             SpellcraftRules.ItemCheck check) {
    }

    private EffectiveBar(Slot[] slots, List<ArcanaItem> items) {
        this.slots = slots;
        this.items = items;
    }

    public static EffectiveBar forPlayer(ServerPlayer player, String[] base, BarMode mode, Function<String, String> specOf) {
        SpellcraftConfig config = ModConfigs.SPELLCRAFT;
        EquippedSources.Snapshot sources = EquippedSources.get(player);
        List<ArcanaItem> items = new ArrayList<>();
        if (config != null) {
            for (EquippedSources.Equipped equipped : sources.arcanaItems()) {
                GlyphView view = sources.view(config, equipped.stack());
                items.add(new ArcanaItem(equipped.key(), equipped.name(), equipped.hand(), equipped.stack(),
                        SpellcraftItemData.spells(equipped.stack()), SpellcraftRules.check(config, equipped.stack(), view, specOf)));
            }
        }
        return resolve(config, base, mode, items, config == null ? GlyphView.EMPTY : sources.autoLines(config), specOf);
    }

    public static EffectiveBar resolve(@Nullable SpellcraftConfig config, String[] base, BarMode mode, List<ArcanaItem> items, GlyphView auto,
                                       Function<String, String> specOf) {
        return new EffectiveBar(BarResolver.resolve(config, base, mode, items, auto, specOf), List.copyOf(items));
    }

    public Slot slot(int index) {
        return this.slots[index];
    }

    public List<Slot> slots() {
        return List.of(this.slots);
    }

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

    public boolean hasOverlays() {
        for (Slot slot : this.slots) {
            if (slot.itemKey() != null) {
                return true;
            }
        }
        return false;
    }
}
