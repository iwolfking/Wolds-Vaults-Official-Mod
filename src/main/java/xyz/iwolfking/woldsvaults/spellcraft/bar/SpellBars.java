package xyz.iwolfking.woldsvaults.spellcraft.bar;

import iskallia.vault.mana.ManaPlayer;
import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.ability.effect.spi.core.InstantAbility;
import iskallia.vault.skill.ability.effect.spi.core.InstantManaAbility;
import iskallia.vault.util.calc.ManaCostHelper;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.base.SpecializedSkill;
import iskallia.vault.skill.tree.AbilityTree;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.augments.FocusAugments;
import xyz.iwolfking.woldsvaults.augments.cast.AugmentCaster;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftRoller;
import xyz.iwolfking.woldsvaults.augments.spell.SpellcraftRules;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.packets.SpellBarSyncMessage;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * The spell bar service. Players cast slots: a slot key (or VH's cast key, for the selected slot) casts the slot's
 * spell; VH's quick-select keys and ability wheel select the slot holding that ability; scrolling with the cast key
 * held moves through filled slots. The tree's selected ability always follows the selected slot, so VH code that reads
 * it (Vein Miner, the HUD tray) keeps working. Whenever the effective bar changes (focus swap, an edit), every slot whose
 * spell changed starts on its full cooldown.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class SpellBars {
    private static final int REFRESH_TICKS = 4;
    private static final int RETRY_TICKS = 10;
    private static final Map<UUID, Map<Integer, Integer>> HELD = new HashMap<>();
    private static final int ROLL_SCAN_TICKS = 40;
    private static final int STAT_SYNC_TICKS = 20;
    private static final Map<UUID, State> STATES = new HashMap<>();
    private static boolean dispatching;

    private SpellBars() {
    }

    private static final class State {
        EffectiveBar bar;
        String[] keys;
        String signature;
    }

    /** True while the bar is driving VH's tree itself, so the tree mixins let the call through. */
    public static boolean isDispatching() {
        return dispatching;
    }

    @Nullable
    public static EffectiveBar current(ServerPlayer player) {
        State state = STATES.get(player.getUUID());
        return state == null ? refresh(player) : state.bar;
    }

    public static SpellBarData.PlayerBar saved(ServerPlayer player) {
        return SpellBarData.get(player.getServer()).bar(player.getUUID());
    }

    /**
     * Resolves the player's effective bar, applies the swap rule to slots whose spell changed since the last
     * resolution, keeps the selected slot and the tree's selection in step, and syncs the client when anything shown
     * changed.
     */
    public static EffectiveBar refresh(ServerPlayer player) {
        SpellBarData.PlayerBar saved = saved(player);
        AbilityTree tree = SpellCooldowns.tree(player);
        if (!saved.migrated) {
            migrate(player, saved, tree);
        }
        ItemStack source = FocusAugments.sourceItem(player);
        EffectiveBar bar = EffectiveBar.resolve(FocusAugments.config(), saved.defaults, source, SpellcraftRules.specOf(tree));
        State state = STATES.computeIfAbsent(player.getUUID(), id -> new State());
        String[] keys = new String[SpellcraftItemData.SLOTS];
        boolean swapped = false;
        for (int i = 0; i < keys.length; i++) {
            keys[i] = bar.slot(i).key();
            if (state.keys != null && !keys[i].isEmpty() && !keys[i].equals(state.keys[i])) {
                SpellCooldowns.startFull(player, bar.slot(i));
                swapped = true;
            }
        }
        if (swapped) {
            tree.sync(SkillContext.of(player));
        }
        state.keys = keys;
        state.bar = bar;
        if (bar.slot(saved.selected).kind() == EffectiveBar.Kind.EMPTY) {
            int first = firstFilled(bar, saved.selected, 1);
            if (first >= 0) {
                saved.selected = first;
                SpellBarData.get(player.getServer()).setDirty();
            }
        }
        followSelection(player, tree, bar.slot(saved.selected));
        String signature = signature(saved, bar);
        if (!Objects.equals(signature, state.signature)) {
            state.signature = signature;
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), SpellBarSyncMessage.of(saved, bar));
        }
        return bar;
    }

    private static void migrate(ServerPlayer player, SpellBarData.PlayerBar saved, AbilityTree tree) {
        int slot = 0;
        for (Skill skill : tree.skills) {
            if (slot >= SpellcraftItemData.SLOTS) {
                break;
            }
            if (skill instanceof SpecializedSkill specialized && specialized.isUnlocked()) {
                saved.defaults[slot] = specialized.getId();
                if (tree.getSelected() != null && tree.getSelected().getId().equals(specialized.getId())) {
                    saved.selected = slot;
                }
                slot++;
            }
        }
        saved.migrated = true;
        SpellBarData.get(player.getServer()).setDirty();
    }

    private static String signature(SpellBarData.PlayerBar saved, EffectiveBar bar) {
        StringBuilder builder = new StringBuilder();
        builder.append(saved.selected).append('|').append(String.join(",", saved.defaults)).append('|');
        builder.append(bar.hasItemBar() ? bar.source().getHoverName().getString() : "").append('|');
        for (EffectiveBar.Slot slot : bar.slots()) {
            builder.append(slot.kind().ordinal()).append(slot.group()).append(slot.key()).append(slot.hidden()).append(slot.inherited())
                    .append(slot.spell() == null ? "" : slot.spell().name()).append(slot.model() != null && !slot.model().isValid()).append(';');
        }
        builder.append(String.join(";", bar.itemErrors()));
        return builder.toString();
    }

    private static int firstFilled(EffectiveBar bar, int from, int direction) {
        for (int step = 1; step <= SpellcraftItemData.SLOTS; step++) {
            int index = Math.floorMod(from + step * direction, SpellcraftItemData.SLOTS);
            if (bar.slot(index).kind() != EffectiveBar.Kind.EMPTY) {
                return index;
            }
        }
        return -1;
    }

    private static void followSelection(ServerPlayer player, AbilityTree tree, EffectiveBar.Slot slot) {
        if (slot.kind() == EffectiveBar.Kind.EMPTY || slot.group().isEmpty()) {
            return;
        }
        SpecializedSkill selected = tree.getSelected();
        if (selected == null || !selected.getId().equals(slot.group())) {
            Skill skill = tree.getForId(slot.group()).orElse(null);
            if (skill instanceof SpecializedSkill specialized && specialized.isUnlocked()) {
                dispatch(() -> tree.onQuickSelect(slot.group(), SkillContext.of(player)));
            }
        }
    }

    private static void dispatch(Runnable action) {
        boolean outer = dispatching;
        dispatching = true;
        try {
            action.run();
        } finally {
            dispatching = outer;
        }
    }

    /** A spell slot key was pressed or released: select that slot, then cast it. */
    public static void onSlotKey(ServerPlayer player, int index, boolean down) {
        if (index < 0 || index >= SpellcraftItemData.SLOTS || player.isSpectator()) {
            return;
        }
        EffectiveBar bar = refresh(player);
        SpellBarData.PlayerBar saved = saved(player);
        if (bar.slot(index).kind() != EffectiveBar.Kind.EMPTY && saved.selected != index) {
            saved.selected = index;
            SpellBarData.get(player.getServer()).setDirty();
            bar = refresh(player);
        }
        press(player, bar, index, down);
    }

    /** VH's cast key: casts the selected slot. Returns true when handled (VH's own handling is skipped). */
    public static boolean onTreeKey(ServerPlayer player, boolean down) {
        if (dispatching) {
            return false;
        }
        EffectiveBar bar = refresh(player);
        press(player, bar, saved(player).selected, down);
        return true;
    }

    /**
     * VH's quick-select or wheel picked an ability. Selects the slot that casts it and returns false so VH completes
     * the selection; returns true (cancel) when the ability isn't on the bar.
     */
    public static boolean onQuickSelect(ServerPlayer player, String group) {
        if (dispatching) {
            return false;
        }
        EffectiveBar bar = refresh(player);
        int index = bar.slotOf(group);
        if (index < 0) {
            AugmentCaster.showMessage(player, group.replace('_', ' ') + " isn't on your spell bar");
            return true;
        }
        SpellBarData.PlayerBar saved = saved(player);
        if (saved.selected != index) {
            saved.selected = index;
            SpellBarData.get(player.getServer()).setDirty();
        }
        return false;
    }

    /** Scrolling with the cast key held moves the selected slot through filled slots. */
    public static void onScroll(ServerPlayer player, int delta) {
        EffectiveBar bar = refresh(player);
        SpellBarData.PlayerBar saved = saved(player);
        int index = saved.selected;
        int direction = delta > 0 ? 1 : -1;
        for (int i = 0; i < Math.abs(delta); i++) {
            int next = firstFilled(bar, index, direction);
            if (next < 0) {
                return;
            }
            index = next;
        }
        saved.selected = index;
        SpellBarData.get(player.getServer()).setDirty();
        refresh(player);
    }

    private static void press(ServerPlayer player, EffectiveBar bar, int index, boolean down) {
        EffectiveBar.Slot slot = bar.slot(index);
        if (slot.kind() != EffectiveBar.Kind.EMPTY && repeats(player, slot)) {
            Map<Integer, Integer> held = HELD.computeIfAbsent(player.getUUID(), id -> new HashMap<>());
            if (down) {
                held.put(index, player.tickCount + RETRY_TICKS);
                castOnce(player, bar, slot);
            } else {
                held.remove(index);
            }
            return;
        }
        switch (slot.kind()) {
            case EMPTY -> {
                if (!down) {
                    AugmentCaster.showMessage(player, slot.hidden()
                            ? "Slot " + (index + 1) + " is hidden: another slot casts " + slot.group().replace('_', ' ')
                            : "Slot " + (index + 1) + " is empty");
                }
            }
            case ABILITY -> {
                AbilityTree tree = SpellCooldowns.tree(player);
                followSelection(player, tree, slot);
                SpecializedSkill selected = tree.getSelected();
                if (selected == null || !selected.getId().equals(slot.group())) {
                    if (!down) {
                        AugmentCaster.showMessage(player, slot.group().replace('_', ' ') + " is not learned");
                    }
                    return;
                }
                SkillContext context = SkillContext.of(player);
                dispatch(() -> {
                    if (down) {
                        tree.onKeyDown(context);
                    } else {
                        tree.onKeyUp(context);
                    }
                });
            }
            case SPELL -> FocusAugments.onSpellKey(player, bar, slot, down);
        }
    }

    /**
     * Whether a held key keeps recasting this slot: instant abilities and instant spells without Charge. Toggles,
     * holds and Charge spells keep their own press and release handling.
     */
    private static boolean repeats(ServerPlayer player, EffectiveBar.Slot slot) {
        Ability trigger = SpellCooldowns.ability(SpellCooldowns.tree(player), slot.group());
        if (!(trigger instanceof InstantAbility)) {
            return false;
        }
        return slot.kind() == EffectiveBar.Kind.ABILITY || (slot.model() != null && !slot.model().hasCharge());
    }

    private static void castOnce(ServerPlayer player, EffectiveBar bar, EffectiveBar.Slot slot) {
        if (slot.kind() == EffectiveBar.Kind.SPELL) {
            FocusAugments.onSpellKey(player, bar, slot, false);
            return;
        }
        AbilityTree tree = SpellCooldowns.tree(player);
        followSelection(player, tree, slot);
        SpecializedSkill selected = tree.getSelected();
        if (selected == null || !selected.getId().equals(slot.group())) {
            return;
        }
        SkillContext context = SkillContext.of(player);
        dispatch(() -> {
            tree.onKeyDown(context);
            tree.onKeyUp(context);
        });
    }

    /** Recasts held slots as soon as they are ready: off cooldown and affordable. Failed attempts wait a moment. */
    private static void recastHeld(ServerPlayer player) {
        Map<Integer, Integer> held = HELD.get(player.getUUID());
        if (held == null || held.isEmpty()) {
            return;
        }
        EffectiveBar bar = current(player);
        if (bar == null) {
            return;
        }
        AbilityTree tree = SpellCooldowns.tree(player);
        for (Map.Entry<Integer, Integer> entry : new java.util.ArrayList<>(held.entrySet())) {
            EffectiveBar.Slot slot = bar.slot(entry.getKey());
            if (slot.kind() == EffectiveBar.Kind.EMPTY || !repeats(player, slot)) {
                held.remove(entry.getKey());
                continue;
            }
            Ability trigger = SpellCooldowns.ability(tree, slot.group());
            if (player.tickCount < entry.getValue() || trigger == null || trigger.isTreeOnCooldown() || !canAfford(player, tree, slot)) {
                continue;
            }
            held.put(entry.getKey(), player.tickCount + RETRY_TICKS);
            castOnce(player, bar, slot);
        }
    }

    private static boolean canAfford(ServerPlayer player, AbilityTree tree, EffectiveBar.Slot slot) {
        if (player.isCreative()) {
            return true;
        }
        float cost = 0.0F;
        for (String group : slot.groups()) {
            if (SpellCooldowns.ability(tree, group) instanceof InstantManaAbility mana) {
                cost += ManaCostHelper.adjustManaCost(player, mana, mana.getManaCost());
            }
        }
        if (slot.model() != null) {
            cost *= slot.model().manaFactor();
        }
        return ((ManaPlayer) player).getMana() >= cost;
    }

    /** Sets a default-bar slot (empty string clears it); the ability moves if it was on another default slot. */
    @Nullable
    public static String editDefault(ServerPlayer player, int index, String group) {
        if (index < 0 || index >= SpellcraftItemData.SLOTS) {
            return "no such slot";
        }
        if (!group.isEmpty() && SpellcraftRules.specOf(SpellCooldowns.tree(player)).apply(group) == null) {
            return group + " is not an ability";
        }
        SpellBarData.PlayerBar saved = saved(player);
        if (!group.isEmpty()) {
            for (int i = 0; i < saved.defaults.length; i++) {
                if (group.equals(saved.defaults[i])) {
                    saved.defaults[i] = "";
                }
            }
        }
        saved.defaults[index] = group;
        SpellBarData.get(player.getServer()).setDirty();
        refresh(player);
        return null;
    }

    /**
     * Sets a slot of the bar on the focus or wand in the given inventory slot: inherit, blank, an ability or one of the
     * item's spells. Another explicit slot using any of the same abilities goes back to inherit.
     */
    @Nullable
    public static String editItem(ServerPlayer player, int inventorySlot, int index, String value) {
        if (index < 0 || index >= SpellcraftItemData.SLOTS || inventorySlot < 0 || inventorySlot >= player.getInventory().getContainerSize()) {
            return "no such slot";
        }
        ItemStack stack = player.getInventory().getItem(inventorySlot);
        if (!SpellcraftRoller.canCarry(stack) || !SpellcraftItemData.has(stack)) {
            return "that item has no spellcraft";
        }
        AbilityTree tree = SpellCooldowns.tree(player);
        java.util.Set<String> groups;
        if (value.startsWith(SpellcraftItemData.ABILITY_PREFIX)) {
            String group = value.substring(SpellcraftItemData.ABILITY_PREFIX.length());
            if (SpellcraftRules.specOf(tree).apply(group) == null) {
                return group + " is not an ability";
            }
            groups = java.util.Set.of(group);
        } else if (value.startsWith(SpellcraftItemData.SPELL_PREFIX)) {
            int id;
            try {
                id = Integer.parseInt(value.substring(SpellcraftItemData.SPELL_PREFIX.length()));
            } catch (NumberFormatException e) {
                return "that is not a spell";
            }
            SpellcraftItemData.Spell spell = SpellcraftItemData.spell(stack, id).orElse(null);
            if (spell == null) {
                return "that spell is not on the item";
            }
            groups = new java.util.HashSet<>();
            for (String token : spell.tokens()) {
                if (xyz.iwolfking.woldsvaults.augments.AugmentType.byId(token) == null) {
                    groups.add(token);
                }
            }
        } else if (value.equals(SpellcraftItemData.INHERIT) || value.equals(SpellcraftItemData.BLANK)) {
            groups = java.util.Set.of();
        } else {
            return "unknown bar entry";
        }
        String[] bar = SpellcraftItemData.bar(stack);
        for (int i = 0; i < bar.length; i++) {
            if (i != index && overlaps(stack, bar[i], groups)) {
                SpellcraftItemData.setBarEntry(stack, i, SpellcraftItemData.INHERIT);
            }
        }
        SpellcraftItemData.setBarEntry(stack, index, value);
        refresh(player);
        return null;
    }

    private static boolean overlaps(ItemStack stack, String entry, java.util.Set<String> groups) {
        if (groups.isEmpty()) {
            return false;
        }
        if (entry.startsWith(SpellcraftItemData.ABILITY_PREFIX)) {
            return groups.contains(entry.substring(SpellcraftItemData.ABILITY_PREFIX.length()));
        }
        if (entry.startsWith(SpellcraftItemData.SPELL_PREFIX)) {
            try {
                int id = Integer.parseInt(entry.substring(SpellcraftItemData.SPELL_PREFIX.length()));
                return SpellcraftItemData.spell(stack, id).map(spell -> spell.tokens().stream().anyMatch(groups::contains)).orElse(false);
            } catch (NumberFormatException e) {
                WoldsVaults.LOGGER.error("Spell bar: item bar entry {} is not a spell id", entry);
                return false;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        recastHeld(player);
        if (player.tickCount % STAT_SYNC_TICKS == 0) {
            xyz.iwolfking.woldsvaults.spellcraft.haste.StatMirror.sync(player);
        }
        if (player.tickCount % REFRESH_TICKS != 0) {
            return;
        }
        if (player.tickCount % ROLL_SCAN_TICKS == 0) {
            rollMissingSpellcraft(player);
        }
        refresh(player);
    }

    /**
     * Gives spellcraft implicits to any identified focus or wand in the inventory that has none: ones identified
     * before spellcraft existed, uniques (identified through a path that skips the roll hook) and any other route
     * that bypasses it.
     */
    private static void rollMissingSpellcraft(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (SpellcraftRoller.canCarry(stack) && !SpellcraftItemData.has(stack)
                    && iskallia.vault.gear.data.VaultGearData.read(stack).getState() == iskallia.vault.gear.VaultGearState.IDENTIFIED) {
                SpellcraftRoller.roll(stack, player.getRandom());
            }
        }
    }

    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        HELD.remove(event.getPlayer().getUUID());
        xyz.iwolfking.woldsvaults.spellcraft.haste.StatMirror.forget(event.getPlayer().getUUID());
        STATES.remove(event.getPlayer().getUUID());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        HELD.clear();
        STATES.clear();
    }
}
