package xyz.iwolfking.woldsvaults.spellcraft.play;

import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.ability.effect.spi.core.HoldAbility;
import iskallia.vault.skill.ability.effect.spi.core.InstantAbility;
import iskallia.vault.skill.ability.effect.spi.core.ToggleAbility;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.base.SpecializedSkill;
import iskallia.vault.skill.tree.AbilityTree;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.spellcraft.ability.AbilityLookup;
import xyz.iwolfking.woldsvaults.spellcraft.bar.BarMode;
import xyz.iwolfking.woldsvaults.spellcraft.bar.EffectiveBar;
import xyz.iwolfking.woldsvaults.spellcraft.bar.SpellBarData;
import xyz.iwolfking.woldsvaults.spellcraft.cast.SpellMessages;
import xyz.iwolfking.woldsvaults.spellcraft.item.SpellcraftItemData;

import javax.annotation.Nullable;
import java.util.List;

public final class SpellBarInput {
    private SpellBarInput() {
    }

    /**
     * A spell slot key was pressed or released. A press selects that slot and casts it (ignored in classic mode); a
     * release ends what that key's press started.
     */
    public static void onSlotKey(ServerPlayer player, int index, boolean down) {
        if (index < 0 || index >= SpellcraftItemData.SLOTS) {
            return;
        }
        if (!down) {
            keyUp(player, HeldSlots.Key.slot(index));
            return;
        }
        if (player.isSpectator() || SpellBars.saved(player).mode != BarMode.SLOT) {
            return;
        }
        keyDown(player, HeldSlots.Key.slot(index), selectSlot(player, index), index, true);
    }

    /**
     * A Vault Hunters quick-fire key in classic mode: casts the slot that casts the ability, the way Vault Hunters'
     * quick-fire did. Instant and toggle abilities and spells without Charge act once on the press; hold abilities and
     * Charge spells get the press and, when that key comes up, the release. Presses are ignored in slot mode. A key
     * whose ability a spell casts as a later step names that spell.
     */
    public static void onQuickfire(ServerPlayer player, String group, boolean down) {
        if (!down) {
            keyUp(player, HeldSlots.Key.quickfire(group));
            return;
        }
        if (player.isSpectator() || SpellBars.saved(player).mode != BarMode.CLASSIC) {
            return;
        }
        Skill skill = AbilityLookup.tree(player).getForId(group).orElse(null);
        if (!(skill instanceof SpecializedSkill specialized) || !specialized.isUnlocked()) {
            return;
        }
        int index = SpellBars.refresh(player).slotOf(group);
        if (index < 0) {
            SpellMessages.show(player, AbilityLookup.groupName(group) + " isn't on your spell bar");
            return;
        }
        EffectiveBar bar = selectSlot(player, index);
        EffectiveBar.Slot slot = bar.slot(index);
        nameCaster(player, slot, group);
        if (actsOnPress(player, slot)) {
            finish(player, bar, slot);
        } else {
            keyDown(player, HeldSlots.Key.quickfire(group), bar, index, false);
        }
    }

    /**
     * VH's cast key: a press casts the selected slot, a release ends the slot that key's press started. Returns true
     * when handled (VH's own handling is skipped).
     */
    public static boolean onTreeKey(ServerPlayer player, boolean down) {
        if (SpellBars.isDispatching()) {
            return false;
        }
        if (!down) {
            keyUp(player, HeldSlots.Key.CAST);
            return true;
        }
        EffectiveBar bar = SpellBars.refresh(player);
        SpellBarData.PlayerBar saved = SpellBars.saved(player);
        keyDown(player, HeldSlots.Key.CAST, bar, saved.selected, saved.mode == BarMode.SLOT);
        return true;
    }

    private static EffectiveBar selectSlot(ServerPlayer player, int index) {
        EffectiveBar bar = SpellBars.refresh(player);
        SpellBarData.PlayerBar saved = SpellBars.saved(player);
        if (bar.slot(index).kind() != EffectiveBar.Kind.EMPTY && saved.selected != index) {
            SpellBars.select(player, saved, index);
            bar = SpellBars.refresh(player);
        }
        return bar;
    }

    private static boolean actsOnPress(ServerPlayer player, EffectiveBar.Slot slot) {
        if (slot.kind() == EffectiveBar.Kind.SPELL) {
            return !SpellTrigger.charges(slot);
        }
        Ability trigger = AbilityLookup.ability(AbilityLookup.tree(player), slot.group());
        return trigger instanceof InstantAbility || trigger instanceof ToggleAbility;
    }

    /**
     * VH's quick-select or wheel picked an ability. Ends every held key, selects the slot that casts it and returns
     * false so VH completes the selection; returns true (cancel) when the ability isn't on the bar. Picking an ability
     * that a spell casts as a later step names that spell.
     */
    public static boolean onQuickSelect(ServerPlayer player, String group) {
        if (SpellBars.isDispatching()) {
            return false;
        }
        releaseAll(player);
        EffectiveBar bar = SpellBars.refresh(player);
        int index = bar.slotOf(group);
        if (index < 0) {
            SpellMessages.show(player, AbilityLookup.groupName(group) + " isn't on your spell bar");
            return true;
        }
        nameCaster(player, bar.slot(index), group);
        SpellBarData.PlayerBar saved = SpellBars.saved(player);
        if (saved.selected != index) {
            SpellBars.select(player, saved, index);
        }
        return false;
    }

    /** Names the spell when {@code group} is a later step of the spell in {@code slot} rather than its trigger. */
    private static void nameCaster(ServerPlayer player, EffectiveBar.Slot slot, String group) {
        if (!slot.group().equals(group)) {
            String name = slot.spell() == null ? AbilityLookup.groupName(slot.group()) : slot.spell().name();
            SpellMessages.show(player, AbilityLookup.groupName(group) + " is cast by " + name + " in slot " + (slot.index() + 1));
        }
    }

    /** Scrolling with the cast key held ends every held key and moves the selected slot through filled slots. */
    public static void onScroll(ServerPlayer player, int delta) {
        releaseAll(player);
        EffectiveBar bar = SpellBars.refresh(player);
        SpellBarData.PlayerBar saved = SpellBars.saved(player);
        int index = saved.selected;
        int direction = delta > 0 ? 1 : -1;
        for (int i = 0; i < Math.abs(delta); i++) {
            int next = SpellBars.firstFilled(bar, index, direction);
            if (next < 0) {
                return;
            }
            index = next;
        }
        SpellBars.select(player, saved, index);
        SpellBars.refresh(player);
    }

    /**
     * Ends every key the player holds, the way a release would for VH: repeats stop, a Charge stops without casting and
     * active hold abilities get their key-up (their end action and cooldown). Returns how many were still open.
     */
    public static int releaseAll(ServerPlayer player) {
        return endAll(player, false);
    }

    /**
     * Cancels every key the player holds: repeats stop, a Charge stops without casting and active hold abilities are
     * cancelled the way VH's cancel key does (no end action). Returns how many were still open.
     */
    public static int cancelAll(ServerPlayer player) {
        return endAll(player, true);
    }

    private static int endAll(ServerPlayer player, boolean cancel) {
        List<HeldSlots.Press> presses = HeldSlots.forget(player.getUUID());
        int open = presses.size();
        if (SpellCharge.cancel(player)) {
            open++;
        }
        AbilityTree tree = AbilityLookup.tree(player);
        for (HoldAbility hold : tree.getAll(HoldAbility.class, Ability::isActive)) {
            endHold(player, tree, hold, cancel);
            open++;
        }
        return open;
    }

    private static void keyDown(ServerPlayer player, HeldSlots.Key key, EffectiveBar bar, int index, boolean repeatable) {
        EffectiveBar.Slot slot = bar.slot(index);
        boolean repeating = repeatable && slot.kind() != EffectiveBar.Kind.EMPTY && HeldSlots.repeats(player, slot);
        HeldSlots.Press previous = HeldSlots.press(player, key, new HeldSlots.Press(index, slot.occupantKey(), slot.group(), repeating));
        if (previous != null) {
            WoldLogger.error("Spellcraft: {} pressed {} {} again without a release; ending its press on slot {} first",
                    player.getScoreboardName(), key.source(), key.id(), previous.index() + 1);
            release(player, previous);
        }
        if (repeating) {
            castOnce(player, bar, slot);
            HeldSlots.hold(player, index, slot);
            return;
        }
        switch (slot.kind()) {
            case EMPTY -> {
            }
            case ABILITY -> {
                AbilityTree tree = selectedTree(player, slot);
                if (tree != null) {
                    SkillContext context = SkillContext.of(player);
                    SpellBars.dispatch(() -> tree.onKeyDown(context));
                }
            }
            case SPELL -> SpellTrigger.onSpellKey(player, bar, slot, true);
        }
    }

    private static void keyUp(ServerPlayer player, HeldSlots.Key key) {
        HeldSlots.Press press = HeldSlots.release(player, key);
        if (press != null) {
            release(player, press);
        }
    }

    /**
     * Ends what a press started. A repeat stops. When the slot's occupant changed since the press, only a hold ability
     * the press left running is ended; otherwise the slot gets its release.
     */
    private static void release(ServerPlayer player, HeldSlots.Press press) {
        if (press.repeating()) {
            HeldSlots.unhold(player, press.index());
            return;
        }
        EffectiveBar bar = SpellBars.refresh(player);
        EffectiveBar.Slot slot = bar.slot(press.index());
        if (!slot.occupantKey().equals(press.occupant())) {
            endHold(player, press.group());
            return;
        }
        finish(player, bar, slot);
    }

    /**
     * A slot's release: an empty slot says so, an ability gets VH's key-up while it is the tree's selection (else only
     * a hold it left running ends), a spell casts or ends its Charge.
     */
    private static void finish(ServerPlayer player, EffectiveBar bar, EffectiveBar.Slot slot) {
        switch (slot.kind()) {
            case EMPTY -> SpellMessages.show(player, slot.hidden()
                    ? "Slot " + (slot.index() + 1) + " is hidden: another slot casts " + AbilityLookup.groupName(slot.group())
                    : "Slot " + (slot.index() + 1) + " is empty");
            case ABILITY -> {
                AbilityTree tree = AbilityLookup.tree(player);
                if (AbilityLookup.ability(tree, slot.group()) == null) {
                    SpellMessages.show(player, AbilityLookup.groupName(slot.group()) + " is not learned");
                    return;
                }
                SpecializedSkill selected = tree.getSelected();
                if (selected == null || !selected.getId().equals(slot.group())) {
                    endHold(player, slot.group());
                    return;
                }
                SkillContext context = SkillContext.of(player);
                SpellBars.dispatch(() -> tree.onKeyUp(context));
            }
            case SPELL -> SpellTrigger.onSpellKey(player, bar, slot, false);
        }
    }

    private static void endHold(ServerPlayer player, String group) {
        AbilityTree tree = AbilityLookup.tree(player);
        if (AbilityLookup.ability(tree, group) instanceof HoldAbility hold && hold.isActive()) {
            endHold(player, tree, hold, false);
        }
    }

    /** Ends an active hold ability through VH's key-up, or its cancel, and switches it off if VH refused. */
    private static void endHold(ServerPlayer player, AbilityTree tree, HoldAbility hold, boolean cancel) {
        SkillContext context = SkillContext.of(player);
        SpellBars.dispatch(() -> {
            if (cancel) {
                hold.onCancelKeyDown(context);
            } else {
                hold.onKeyUp(context);
            }
        });
        if (hold.isActive()) {
            WoldLogger.error("Spellcraft: VH did not end {}'s held {}; switching it off without its end action",
                    player.getScoreboardName(), AbilityLookup.groupOf(hold));
            hold.setActive(false);
        }
        tree.sync(context);
    }

    static void castOnce(ServerPlayer player, EffectiveBar bar, EffectiveBar.Slot slot) {
        if (slot.kind() == EffectiveBar.Kind.SPELL) {
            SpellTrigger.onSpellKey(player, bar, slot, false);
            return;
        }
        AbilityTree tree = selectedTree(player, slot);
        if (tree == null) {
            return;
        }
        SkillContext context = SkillContext.of(player);
        SpellBars.dispatch(() -> {
            tree.onKeyDown(context);
            tree.onKeyUp(context);
        });
    }

    @Nullable
    private static AbilityTree selectedTree(ServerPlayer player, EffectiveBar.Slot slot) {
        AbilityTree tree = AbilityLookup.tree(player);
        SpellBars.followSelection(player, tree, slot);
        SpecializedSkill selected = tree.getSelected();
        if (selected == null || !selected.getId().equals(slot.group())) {
            return null;
        }
        return tree;
    }
}
