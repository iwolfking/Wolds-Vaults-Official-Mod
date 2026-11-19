package xyz.iwolfking.woldsvaults.spellcraft.play;

import iskallia.vault.mana.ManaPlayer;
import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.ability.effect.spi.core.InstantAbility;
import iskallia.vault.skill.ability.effect.spi.core.InstantManaAbility;
import iskallia.vault.skill.tree.AbilityTree;
import iskallia.vault.util.calc.ManaCostHelper;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.spellcraft.ability.AbilityLookup;
import xyz.iwolfking.woldsvaults.spellcraft.bar.EffectiveBar;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The keys a player holds down and the slot each one pressed, plus the held slots that recast while their key stays
 * down. A key's release always ends the slot its own press started, whatever is selected by then.
 */
final class HeldSlots {
    private static final int RETRY_TICKS = 10;
    private static final Map<UUID, Map<Integer, Integer>> HELD = new HashMap<>();
    private static final Map<UUID, Map<Key, Press>> PRESSES = new HashMap<>();

    private HeldSlots() {
    }

    /** Where a key press comes from. */
    enum Source {
        CAST,
        SLOT,
        QUICKFIRE
    }

    /** One physical key: Vault Hunters' cast key, a spell slot key by index, or a quick-fire key by ability group. */
    record Key(Source source, String id) {
        static final Key CAST = new Key(Source.CAST, "");

        static Key slot(int index) {
            return new Key(Source.SLOT, String.valueOf(index));
        }

        static Key quickfire(String group) {
            return new Key(Source.QUICKFIRE, group);
        }
    }

    /**
     * What a key's press started: the slot, its occupant and trigger group at the press, and whether the press made it
     * recast while held.
     */
    record Press(int index, String occupant, String group, boolean repeating) {
    }

    /** Records {@code press} for {@code key} and returns the press it replaces, or null. */
    @Nullable
    static Press press(ServerPlayer player, Key key, Press press) {
        return PRESSES.computeIfAbsent(player.getUUID(), id -> new HashMap<>()).put(key, press);
    }

    /** Removes and returns the press {@code key} started, or null when it has none. */
    @Nullable
    static Press release(ServerPlayer player, Key key) {
        Map<Key, Press> presses = PRESSES.get(player.getUUID());
        return presses == null ? null : presses.remove(key);
    }

    /** Starts recasting a held slot. */
    static void hold(ServerPlayer player, int index, EffectiveBar.Slot slot) {
        HELD.computeIfAbsent(player.getUUID(), id -> new HashMap<>()).put(index, nextAttempt(player, slot));
    }

    /** Stops recasting {@code index} unless another held key still repeats it. */
    static void unhold(ServerPlayer player, int index) {
        Map<Key, Press> presses = PRESSES.get(player.getUUID());
        if (presses != null) {
            for (Press other : presses.values()) {
                if (other.repeating() && other.index() == index) {
                    return;
                }
            }
        }
        Map<Integer, Integer> held = HELD.get(player.getUUID());
        if (held != null) {
            held.remove(index);
        }
    }

    /** Forgets every press and held slot of the player and returns the presses that were still open. */
    static List<Press> forget(UUID player) {
        HELD.remove(player);
        Map<Key, Press> presses = PRESSES.remove(player);
        return presses == null ? List.of() : new ArrayList<>(presses.values());
    }

    static void cancelChanged(ServerPlayer player, @Nullable String[] previous, String[] keys) {
        Map<Integer, Integer> held = HELD.get(player.getUUID());
        for (int i = 0; i < keys.length; i++) {
            if (previous != null && held != null && !keys[i].equals(previous[i])) {
                held.remove(i);
            }
        }
    }

    /**
     * Whether a held key keeps recasting this slot: instant abilities and instant spells without Charge. Toggles, holds
     * and Charge spells keep their own press and release handling.
     */
    static boolean repeats(ServerPlayer player, EffectiveBar.Slot slot) {
        Ability trigger = AbilityLookup.ability(AbilityLookup.tree(player), slot.group());
        if (!(trigger instanceof InstantAbility) || slot.locked()) {
            return false;
        }
        return slot.kind() == EffectiveBar.Kind.ABILITY || (slot.model() != null && !slot.model().hasCharge());
    }

    /**
     * Recasts held slots on the tick they are ready: the tick the last of their groups' cooldowns ends (VH ticks
     * cooldowns at the start of the player tick, this runs at its end) when the slot is affordable. An attempt that
     * does not start the cooldown (a curse, a suppressor, a failed cast) waits {@link #RETRY_TICKS} before the next
     * one.
     */
    static void recast(ServerPlayer player) {
        Map<Integer, Integer> held = HELD.get(player.getUUID());
        if (held == null || held.isEmpty()) {
            return;
        }
        EffectiveBar bar = SpellBars.current(player);
        if (bar == null) {
            return;
        }
        AbilityTree tree = AbilityLookup.tree(player);
        for (Map.Entry<Integer, Integer> entry : new ArrayList<>(held.entrySet())) {
            EffectiveBar.Slot slot = bar.slot(entry.getKey());
            if (slot.kind() == EffectiveBar.Kind.EMPTY || !repeats(player, slot)) {
                held.remove(entry.getKey());
                continue;
            }
            Ability trigger = AbilityLookup.ability(tree, slot.group());
            if (player.tickCount < entry.getValue() || trigger == null || SpellCooldowns.blocking(tree, slot.groups()) != null
                    || !canAfford(player, tree, slot)) {
                continue;
            }
            SpellBarInput.castOnce(player, bar, slot);
            held.put(entry.getKey(), nextAttempt(player, slot));
        }
    }

    private static int nextAttempt(ServerPlayer player, EffectiveBar.Slot slot) {
        return SpellCooldowns.blocking(AbilityLookup.tree(player), slot.groups()) != null ? 0 : player.tickCount + RETRY_TICKS;
    }

    private static boolean canAfford(ServerPlayer player, AbilityTree tree, EffectiveBar.Slot slot) {
        if (player.isCreative()) {
            return true;
        }
        float cost = 0.0F;
        for (String group : slot.model() != null ? slot.model().castGroups() : slot.groups()) {
            if (AbilityLookup.ability(tree, group) instanceof InstantManaAbility mana) {
                cost += ManaCostHelper.adjustManaCost(player, mana, mana.getManaCost());
            }
        }
        if (slot.model() != null) {
            cost *= slot.model().manaFactor();
        }
        return ((ManaPlayer) player).getMana() >= cost;
    }

    static void clear() {
        HELD.clear();
        PRESSES.clear();
    }
}
