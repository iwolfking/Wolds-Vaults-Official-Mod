package xyz.iwolfking.woldsvaults.augments;

import iskallia.vault.init.ModSounds;
import iskallia.vault.skill.tree.AbilityTree;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.augments.cast.AugmentCaster;
import xyz.iwolfking.woldsvaults.augments.cast.SpellCharge;
import xyz.iwolfking.woldsvaults.augments.effect.AugmentEvents;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftRoller;
import xyz.iwolfking.woldsvaults.augments.spell.SpellModel;
import xyz.iwolfking.woldsvaults.augments.spell.SpellcraftRules;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.spellcraft.bar.EffectiveBar;
import xyz.iwolfking.woldsvaults.spellcraft.bar.SpellBars;
import xyz.iwolfking.woldsvaults.spellcraft.bar.SpellCooldowns;
import xyz.iwolfking.woldsvaults.spellcraft.gear.GearGlyphs;
import xyz.iwolfking.woldsvaults.spellcraft.pool.GlyphSources;
import xyz.iwolfking.woldsvaults.spellcraft.pool.GlyphView;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Spellcraft: focuses and wands roll arcana, glyph lines and depth; their owner builds augmented spells from the glyphs
 * the item's view offers and gives each spell a bar slot and a priority. While the item is equipped its spells overlay
 * those slots of the default bar. This class holds the entry points the bar and the focus screen call.
 */
public final class FocusAugments {
    private FocusAugments() {
    }

    /** Registers the VH event listeners; called once from common setup. */
    public static void init() {
        AbilityCastTracker.register();
        AugmentEvents.register();
        GearGlyphs.init();
    }

    @Nullable
    public static FocusAugmentsConfig config() {
        return ModConfigs.FOCUS_AUGMENTS;
    }

    public static boolean worksHere(FocusAugmentsConfig config, ServerPlayer player) {
        return config.worksOutsideVaults || player.getLevel().dimension().location().getNamespace().equals("the_vault");
    }

    /**
     * A key on a slot holding an augmented or implicit spell. Spells with Charge wind up while the key is held and cast on
     * release; others cast on release. A LOCKED slot, a spell with a structural error, or a spell any of whose ability
     * groups is still cooling down refuses with the reason. Inactive glyphs never refuse: they are skipped.
     */
    public static void onSpellKey(ServerPlayer player, EffectiveBar bar, EffectiveBar.Slot slot, boolean down) {
        FocusAugmentsConfig config = config();
        SpellModel model = slot.model();
        if (config == null || model == null) {
            return;
        }
        if (!worksHere(config, player)) {
            if (!down) {
                AugmentCaster.showMessage(player, "Spells don't work here");
            }
            return;
        }
        String name = slot.spell() == null ? slot.group().replace('_', ' ') : slot.spell().name();
        if (slot.locked()) {
            if (!down) {
                AugmentCaster.showMessage(player, "Slot " + (slot.index() + 1) + " is locked: " + slot.lockReason());
            }
            return;
        }
        if (!model.isValid()) {
            if (!down) {
                AugmentCaster.showMessage(player, "Can't cast " + name + ": " + model.errors().get(0));
            }
            return;
        }
        boolean charges = model.hasCharge() && !slot.implicit() && slot.hand() != null;
        if (!charges) {
            if (!down && ready(player, slot)) {
                AugmentCaster.castSlot(player, slot, -1.0F);
            }
            return;
        }
        if (down) {
            if (ready(player, slot)) {
                SpellCharge.start(player, slot.index(), slot.key(), slot.hand(), Math.round(config.tuning.chargeSeconds * 20.0F));
            }
        } else {
            float share = SpellCharge.release(player, slot.index(), slot.key());
            if (share >= 0.0F && ready(player, slot)) {
                AugmentCaster.castSlot(player, slot, share);
            }
        }
    }

    /**
     * The all-groups gate: every ability group the slot holds must be learned and off cooldown. Otherwise plays VH's
     * cooldown sound and names the slowest group when it isn't the trigger.
     */
    private static boolean ready(ServerPlayer player, EffectiveBar.Slot slot) {
        AbilityTree tree = SpellCooldowns.tree(player);
        for (String group : slot.groups()) {
            if (SpellCooldowns.ability(tree, group) == null) {
                AugmentCaster.showMessage(player, group.replace('_', ' ') + " is not learned");
                return false;
            }
        }
        SpellCooldowns.Blocking blocking = SpellCooldowns.blocking(tree, slot.groups());
        if (blocking == null) {
            return true;
        }
        player.level.playSound(null, player.blockPosition(), ModSounds.ABILITY_ON_COOLDOWN, SoundSource.PLAYERS, 1.0F, 1.0F);
        if (!blocking.group().equals(slot.group())) {
            AugmentCaster.showMessage(player, blocking.group().replace('_', ' ') + ": " + Math.max(1, Math.round(blocking.ticks() / 20.0F)) + " s");
        }
        return false;
    }

    /**
     * Creates (id 0), changes or deletes a spell on the focus or wand in the player's inventory slot. The whole item is
     * checked against its glyph view first; a change that breaks any rule is refused with the reason. A changed spell
     * keeps its slot and priority.
     */
    public static void editSpell(ServerPlayer player, int inventorySlot, int spellId, String name, List<String> tokens, boolean delete) {
        FocusAugmentsConfig config = config();
        ItemStack stack = carrier(player, inventorySlot);
        if (config == null || stack == null) {
            return;
        }
        if (delete) {
            SpellcraftItemData.removeSpell(stack, spellId);
            SpellBars.refresh(player);
            return;
        }
        String cleanName = name.strip();
        if (cleanName.isEmpty()) {
            cleanName = "Spell";
        }
        if (cleanName.length() > config.rules.maxNameLength) {
            cleanName = cleanName.substring(0, config.rules.maxNameLength);
        }
        SpellcraftItemData.Spell stored = spellId > 0 ? SpellcraftItemData.spell(stack, spellId).orElse(null) : null;
        int slot = stored == null ? SpellcraftItemData.UNASSIGNED : stored.slot();
        int prio = stored == null ? 0 : stored.prio();
        int checkId = spellId > 0 ? spellId : Integer.MAX_VALUE;
        SpellcraftItemData.Spell edited = new SpellcraftItemData.Spell(checkId, cleanName, List.copyOf(tokens), slot, prio);
        if (!store(player, config, stack, SpellcraftRules.withEdit(stack, checkId, edited), "Spell not saved: ")) {
            return;
        }
        SpellcraftItemData.putSpell(stack, new SpellcraftItemData.Spell(spellId > 0 ? spellId : 0, cleanName, List.copyOf(tokens), slot, prio));
        SpellBars.refresh(player);
    }

    /**
     * Moves a spell of the item in the player's inventory slot to a bar slot ({@link SpellcraftItemData#UNASSIGNED} takes
     * it off the bar) and sets its priority (0 keeps it). Refused when another spell of the item already uses the slot.
     */
    public static void placeSpell(ServerPlayer player, int inventorySlot, int spellId, int barSlot, int prio) {
        FocusAugmentsConfig config = config();
        ItemStack stack = carrier(player, inventorySlot);
        if (config == null || stack == null) {
            return;
        }
        SpellcraftItemData.Spell stored = SpellcraftItemData.spell(stack, spellId).orElse(null);
        if (stored == null) {
            AugmentCaster.showMessage(player, "That spell is not on the item");
            return;
        }
        if (barSlot < SpellcraftItemData.UNASSIGNED || barSlot >= SpellcraftItemData.SLOTS || prio < 0 || prio > config.rules.priorities) {
            AugmentCaster.showMessage(player, "Spell not moved: slot or priority out of range");
            return;
        }
        SpellcraftItemData.Spell moved = stored.withSlot(barSlot).withPrio(prio > 0 ? prio : stored.prio());
        if (!store(player, config, stack, SpellcraftRules.withEdit(stack, spellId, moved), "Spell not moved: ")) {
            return;
        }
        SpellcraftItemData.putSpell(stack, moved);
        SpellBars.refresh(player);
    }

    private static boolean store(ServerPlayer player, FocusAugmentsConfig config, ItemStack stack, List<SpellcraftItemData.Spell> spells,
                                 String refusal) {
        GlyphView view = GlyphSources.get(player).view(config, stack);
        SpellcraftRules.ItemCheck check = SpellcraftRules.checkEdit(config, stack, view, spells, SpellcraftRules.specOf(SpellCooldowns.tree(player)));
        if (!check.isValid()) {
            AugmentCaster.showMessage(player, refusal + check.errors().get(0));
            return false;
        }
        return true;
    }

    @Nullable
    private static ItemStack carrier(ServerPlayer player, int inventorySlot) {
        if (inventorySlot < 0 || inventorySlot >= player.getInventory().getContainerSize()) {
            return null;
        }
        ItemStack stack = player.getInventory().getItem(inventorySlot);
        if (!SpellcraftRoller.canCarry(stack) || !SpellcraftItemData.has(stack)) {
            AugmentCaster.showMessage(player, "That item has no spellcraft");
            return null;
        }
        return stack;
    }
}
