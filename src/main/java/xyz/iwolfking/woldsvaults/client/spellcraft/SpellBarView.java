package xyz.iwolfking.woldsvaults.client.spellcraft;

import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SpecializedSkill;
import iskallia.vault.skill.tree.AbilityTree;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.augments.FocusAugments;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.augments.spell.SpellModel;
import xyz.iwolfking.woldsvaults.augments.spell.SpellcraftRules;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.packets.SpellBarEditMessage;
import xyz.iwolfking.woldsvaults.network.packets.SpellBarSyncMessage;
import xyz.iwolfking.woldsvaults.spellcraft.bar.EffectiveBar;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * One bar as the spell bar strip draws it: the default bar, the active focus bar as last synced (skill menu), or the
 * bar stored on one focus or wand (focus screen, resolved locally from its NBT). Each slot knows what it shows
 * (ability, augmented spell, blank, hidden, empty), whether it is inherited, unlearned or broken, and which ability
 * groups it casts. Edits go to the server; the view is rebuilt from the next sync.
 */
public final class SpellBarView {
    public enum State {
        EMPTY,
        BLANK,
        ABILITY,
        SPELL,
        HIDDEN
    }

    /** One displayed slot; {@code spell} and {@code model} are set for augmented spells found on the item. */
    public record Slot(int index, State state, String group, String spellName, boolean inherited, boolean unlearned, boolean broken,
                       @Nullable SpellcraftItemData.Spell spell, @Nullable SpellModel model, List<String> groups) {
        public boolean filled() {
            return this.state == State.ABILITY || this.state == State.SPELL;
        }
    }

    private final Slot[] slots;
    private final boolean item;
    private final ItemStack stack;
    private final int inventorySlot;
    private final List<String> itemErrors;

    private SpellBarView(Slot[] slots, boolean item, ItemStack stack, int inventorySlot, List<String> itemErrors) {
        this.slots = slots;
        this.item = item;
        this.stack = stack;
        this.inventorySlot = inventorySlot;
        this.itemErrors = itemErrors;
    }

    /** The default bar as last synced. */
    public static SpellBarView ofDefaults(AbilityTree tree) {
        Slot[] slots = new Slot[SpellcraftItemData.SLOTS];
        for (int i = 0; i < slots.length; i++) {
            String group = ClientSpellBar.defaultSlot(i);
            slots[i] = group.isEmpty()
                    ? new Slot(i, State.EMPTY, "", "", false, false, false, null, null, List.of())
                    : new Slot(i, State.ABILITY, group, "", false, !learned(tree, group), false, null, null, List.of(group));
        }
        return new SpellBarView(slots, false, ItemStack.EMPTY, -1, List.of());
    }

    /**
     * The active focus bar as the server last synced it ({@link ClientSpellBar}), with spell details looked up on the
     * off-hand item; the off-hand NBT is not synced while the skill menu is open, the bar sync is.
     */
    public static SpellBarView ofSynced(AbilityTree tree, ItemStack offhand, int inventorySlot) {
        Slot[] slots = new Slot[SpellcraftItemData.SLOTS];
        FocusAugmentsConfig config = FocusAugments.config();
        Function<String, String> specOf = SpellcraftRules.specOf(tree);
        List<SpellcraftItemData.Spell> spells = SpellcraftItemData.spells(offhand);
        for (int i = 0; i < slots.length; i++) {
            SpellBarSyncMessage.Entry entry = ClientSpellBar.entry(i);
            String group = entry.group();
            if (entry.kind() == ClientSpellBar.ABILITY) {
                slots[i] = new Slot(i, State.ABILITY, group, "", entry.inherited(), !learned(tree, group), false, null, null, List.of(group));
            } else if (entry.kind() == ClientSpellBar.SPELL) {
                SpellcraftItemData.Spell spell = find(spells, entry.spellName(), group);
                SpellModel model = spell == null || config == null ? null
                        : SpellModel.build(config, SpellcraftItemData.lines(offhand), SpellcraftItemData.depth(offhand), spell.tokens(), specOf, false);
                List<String> groups = spell == null ? List.of(group) : SpellcraftGui.groups(spell);
                slots[i] = new Slot(i, State.SPELL, group, entry.spellName(), false, groups.stream().anyMatch(g -> !learned(tree, g)),
                        entry.broken(), spell, model, groups);
            } else if (entry.hidden()) {
                slots[i] = new Slot(i, State.HIDDEN, group, "", entry.inherited(), false, false, null, null, List.of());
            } else {
                slots[i] = new Slot(i, entry.inherited() ? State.EMPTY : State.BLANK, "", "", entry.inherited(), false, false, null, null, List.of());
            }
        }
        return new SpellBarView(slots, true, offhand, inventorySlot, List.copyOf(ClientSpellBar.itemErrors()));
    }

    /** The bar stored on a focus or wand, resolved over the default bar as the server would. */
    public static SpellBarView ofItem(AbilityTree tree, ItemStack stack, int inventorySlot) {
        FocusAugmentsConfig config = FocusAugments.config();
        String[] defaults = new String[SpellcraftItemData.SLOTS];
        Arrays.setAll(defaults, ClientSpellBar::defaultSlot);
        EffectiveBar bar = EffectiveBar.resolve(config, defaults, stack, SpellcraftRules.specOf(tree));
        String[] raw = SpellcraftItemData.bar(stack);
        Slot[] slots = new Slot[SpellcraftItemData.SLOTS];
        for (int i = 0; i < slots.length; i++) {
            EffectiveBar.Slot slot = bar.slot(i);
            String group = slot.group();
            switch (slot.kind()) {
                case ABILITY -> slots[i] = new Slot(i, State.ABILITY, group, "", slot.inherited(), !learned(tree, group), false, null, null,
                        List.of(group));
                case SPELL -> {
                    List<String> groups = new ArrayList<>(slot.groups());
                    slots[i] = new Slot(i, State.SPELL, group, slot.spell() == null ? "" : slot.spell().name(), false,
                            groups.stream().anyMatch(g -> !learned(tree, g)), slot.model() != null && !slot.model().isValid(),
                            slot.spell(), slot.model(), groups);
                }
                case EMPTY -> {
                    if (slot.hidden()) {
                        slots[i] = new Slot(i, State.HIDDEN, group, "", slot.inherited(), false, false, null, null, List.of());
                    } else {
                        boolean blank = SpellcraftItemData.BLANK.equals(raw[i]);
                        slots[i] = new Slot(i, blank ? State.BLANK : State.EMPTY, "", "", !blank, false, false, null, null, List.of());
                    }
                }
            }
        }
        return new SpellBarView(slots, true, stack, inventorySlot, bar.itemErrors());
    }

    @Nullable
    private static SpellcraftItemData.Spell find(List<SpellcraftItemData.Spell> spells, String name, String trigger) {
        SpellcraftItemData.Spell byName = null;
        for (SpellcraftItemData.Spell spell : spells) {
            if (spell.name().equals(name)) {
                if (spell.trigger().equals(trigger)) {
                    return spell;
                }
                byName = byName == null ? spell : byName;
            }
        }
        return byName;
    }

    public static boolean learned(AbilityTree tree, String group) {
        Skill skill = tree.getForId(group).orElse(null);
        return skill instanceof SpecializedSkill specialized && specialized.isUnlocked();
    }

    public Slot slot(int index) {
        return this.slots[index];
    }

    public boolean isItem() {
        return this.item;
    }

    public ItemStack stack() {
        return this.stack;
    }

    public List<String> itemErrors() {
        return this.itemErrors;
    }

    /** The slot that casts this ability group, or -1. */
    public int slotOf(String group) {
        for (Slot slot : this.slots) {
            if (slot.filled() && slot.groups().contains(group)) {
                return slot.index();
            }
        }
        return -1;
    }

    /** The filled slot that hides a hidden slot (it casts the same ability), or -1. */
    public int hiddenBy(int index) {
        Slot hidden = this.slots[index];
        if (hidden.state() != State.HIDDEN) {
            return -1;
        }
        for (Slot slot : this.slots) {
            if (slot.index() != index && slot.filled() && slot.groups().contains(hidden.group())) {
                return slot.index();
            }
        }
        return -1;
    }

    /** Sets a slot: an ability group (or empty) on the default bar, a bar entry on an item bar. */
    public void send(int index, String value) {
        ModNetwork.sendToServer(new SpellBarEditMessage(this.item, Math.max(0, this.inventorySlot), index, value));
    }
}
