package xyz.iwolfking.woldsvaults.spellcraft.play;

import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.ability.effect.spi.core.InstantAbility;
import iskallia.vault.skill.ability.effect.spi.core.ToggleAbility;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.base.SpecializedSkill;
import iskallia.vault.skill.tree.AbilityTree;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.spellcraft.ability.AbilityLookup;
import xyz.iwolfking.woldsvaults.spellcraft.bar.BarMode;
import xyz.iwolfking.woldsvaults.spellcraft.bar.EffectiveBar;
import xyz.iwolfking.woldsvaults.spellcraft.bar.SpellBarData;
import xyz.iwolfking.woldsvaults.spellcraft.cast.SpellMessages;
import xyz.iwolfking.woldsvaults.spellcraft.item.SpellcraftItemData;

import javax.annotation.Nullable;
import java.util.Map;

public final class SpellBarInput {
    private SpellBarInput() {
    }

    /** A spell slot key was pressed or released: select that slot, then cast it. Ignored in classic mode. */
    public static void onSlotKey(ServerPlayer player, int index, boolean down) {
        if (index < 0 || index >= SpellcraftItemData.SLOTS || player.isSpectator() || SpellBars.saved(player).mode != BarMode.SLOT) {
            return;
        }
        press(player, selectSlot(player, index), index, down, true);
    }

    /**
     * A Vault Hunters quick-fire key in classic mode: casts the slot that casts the ability, the way Vault Hunters'
     * quick-fire did. Instant and toggle abilities and spells without Charge act once on the press; hold abilities and
     * Charge spells get the press and the release. Ignored in slot mode.
     */
    public static void onQuickfire(ServerPlayer player, String group, boolean down) {
        if (player.isSpectator() || SpellBars.saved(player).mode != BarMode.CLASSIC) {
            return;
        }
        Skill skill = AbilityLookup.tree(player).getForId(group).orElse(null);
        if (!(skill instanceof SpecializedSkill specialized) || !specialized.isUnlocked()) {
            return;
        }
        int index = SpellBars.refresh(player).slotOf(group);
        if (index < 0) {
            if (down) {
                SpellMessages.show(player, AbilityLookup.groupName(group) + " isn't on your spell bar");
            }
            return;
        }
        EffectiveBar bar = selectSlot(player, index);
        if (!actsOnPress(player, bar.slot(index))) {
            press(player, bar, index, down, false);
        } else if (down) {
            press(player, bar, index, false, false);
        }
    }

    /** VH's cast key: casts the selected slot. Returns true when handled (VH's own handling is skipped). */
    public static boolean onTreeKey(ServerPlayer player, boolean down) {
        if (SpellBars.isDispatching()) {
            return false;
        }
        EffectiveBar bar = SpellBars.refresh(player);
        SpellBarData.PlayerBar saved = SpellBars.saved(player);
        press(player, bar, saved.selected, down, saved.mode == BarMode.SLOT);
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
     * VH's quick-select or wheel picked an ability. Selects the slot that casts it and returns false so VH completes
     * the selection; returns true (cancel) when the ability isn't on the bar.
     */
    public static boolean onQuickSelect(ServerPlayer player, String group) {
        if (SpellBars.isDispatching()) {
            return false;
        }
        EffectiveBar bar = SpellBars.refresh(player);
        int index = bar.slotOf(group);
        if (index < 0) {
            SpellMessages.show(player, AbilityLookup.groupName(group) + " isn't on your spell bar");
            return true;
        }
        SpellBarData.PlayerBar saved = SpellBars.saved(player);
        if (saved.selected != index) {
            SpellBars.select(player, saved, index);
        }
        return false;
    }

    /** Scrolling with the cast key held moves the selected slot through filled slots. */
    public static void onScroll(ServerPlayer player, int delta) {
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

    private static void press(ServerPlayer player, EffectiveBar bar, int index, boolean down, boolean repeat) {
        EffectiveBar.Slot slot = bar.slot(index);
        if (repeat && slot.kind() != EffectiveBar.Kind.EMPTY && HeldSlots.repeats(player, slot)) {
            Map<Integer, Integer> held = HeldSlots.held(player);
            if (down) {
                castOnce(player, bar, slot);
                held.put(index, HeldSlots.nextAttempt(player, slot));
            } else {
                held.remove(index);
            }
            return;
        }
        switch (slot.kind()) {
            case EMPTY -> {
                if (!down) {
                    SpellMessages.show(player, slot.hidden()
                            ? "Slot " + (index + 1) + " is hidden: another slot casts " + AbilityLookup.groupName(slot.group())
                            : "Slot " + (index + 1) + " is empty");
                }
            }
            case ABILITY -> {
                AbilityTree tree = selectedTree(player, slot);
                if (tree == null) {
                    if (!down) {
                        SpellMessages.show(player, AbilityLookup.groupName(slot.group()) + " is not learned");
                    }
                    return;
                }
                SkillContext context = SkillContext.of(player);
                SpellBars.dispatch(() -> {
                    if (down) {
                        tree.onKeyDown(context);
                    } else {
                        tree.onKeyUp(context);
                    }
                });
            }
            case SPELL -> SpellTrigger.onSpellKey(player, bar, slot, down);
        }
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
