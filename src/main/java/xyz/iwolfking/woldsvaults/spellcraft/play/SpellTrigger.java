package xyz.iwolfking.woldsvaults.spellcraft.play;

import iskallia.vault.init.ModSounds;
import iskallia.vault.skill.tree.AbilityTree;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.spellcraft.ability.AbilityLookup;
import xyz.iwolfking.woldsvaults.spellcraft.bar.EffectiveBar;
import xyz.iwolfking.woldsvaults.spellcraft.cast.SpellCaster;
import xyz.iwolfking.woldsvaults.spellcraft.cast.SpellMessages;
import xyz.iwolfking.woldsvaults.spellcraft.spell.SpellModel;

final class SpellTrigger {
    private SpellTrigger() {
    }

    private static boolean worksHere(SpellcraftConfig config, ServerPlayer player) {
        return config.worksOutsideVaults || player.getLevel().dimension().location().getNamespace().equals("the_vault");
    }

    /**
     * A key on a slot holding a spell or an implicit spell: spells with Charge wind up while the key is held, and
     * every spell casts on release. A LOCKED slot, a spell with a structural error or a spell with an ability group
     * still cooling down refuses with the reason, while inactive glyphs are skipped.
     */
    static void onSpellKey(ServerPlayer player, EffectiveBar bar, EffectiveBar.Slot slot, boolean down) {
        SpellcraftConfig config = ModConfigs.SPELLCRAFT;
        SpellModel model = slot.model();
        if (config == null || model == null) {
            return;
        }
        if (!worksHere(config, player)) {
            if (!down) {
                SpellMessages.show(player, "Spells don't work here");
            }
            return;
        }
        String name = slot.spell() == null ? AbilityLookup.groupName(slot.group()) : slot.spell().name();
        if (slot.locked()) {
            if (!down) {
                SpellMessages.show(player, "Slot " + (slot.index() + 1) + " is locked: " + slot.lockReason());
            }
            return;
        }
        if (!model.isValid()) {
            if (!down) {
                SpellMessages.show(player, "Can't cast " + name + ": " + model.errors().get(0));
            }
            return;
        }
        if (!charges(slot)) {
            if (!down && ready(player, slot)) {
                SpellCaster.castSlot(player, slot, -1.0F);
            }
            return;
        }
        if (down) {
            if (ready(player, slot)) {
                SpellCharge.start(player, slot.index(), slot.occupantKey(), slot.hand(), Math.round(config.tuning.chargeSeconds * 20.0F));
            }
        } else {
            float share = SpellCharge.release(player, slot.index(), slot.occupantKey());
            if (share >= 0.0F && ready(player, slot)) {
                SpellCaster.castSlot(player, slot, share);
            }
        }
    }

    /** Whether the slot's spell winds up while its key is held: a Charge spell from an item in hand. */
    static boolean charges(EffectiveBar.Slot slot) {
        return slot.model() != null && slot.model().hasCharge() && !slot.implicit() && slot.hand() != null;
    }

    private static boolean ready(ServerPlayer player, EffectiveBar.Slot slot) {
        AbilityTree tree = AbilityLookup.tree(player);
        for (String group : slot.groups()) {
            if (AbilityLookup.ability(tree, group) == null) {
                SpellMessages.show(player, AbilityLookup.groupName(group) + " is not learned");
                return false;
            }
        }
        SpellCooldowns.Blocking blocking = SpellCooldowns.blocking(tree, slot.groups());
        if (blocking == null) {
            return true;
        }
        player.level.playSound(null, player.blockPosition(), ModSounds.ABILITY_ON_COOLDOWN, SoundSource.PLAYERS, 1.0F, 1.0F);
        if (!blocking.group().equals(slot.group())) {
            SpellMessages.show(player, AbilityLookup.groupName(blocking.group()) + ": " + Math.max(1, Math.round(blocking.ticks() / 20.0F)) + " s");
        }
        return false;
    }
}
