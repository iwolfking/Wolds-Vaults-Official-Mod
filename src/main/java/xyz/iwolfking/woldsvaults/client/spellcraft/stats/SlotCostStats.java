package xyz.iwolfking.woldsvaults.client.spellcraft.stats;

import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.ability.effect.spi.core.InstantManaAbility;
import iskallia.vault.skill.tree.AbilityTree;
import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import iskallia.vault.util.calc.CooldownHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods.idona.ManaCostHelperAccessor;
import xyz.iwolfking.woldsvaults.spellcraft.ability.AbilityLookup;
import xyz.iwolfking.woldsvaults.spellcraft.haste.SpellHaste;
import xyz.iwolfking.woldsvaults.spellcraft.play.SpellCooldowns;
import xyz.iwolfking.woldsvaults.spellcraft.spell.SpellModel;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Mana and cooldown numbers for the client screens, computed from the client's ability tree and attribute snapshot.
 * Mana is Vault Hunters' cost after the per-ability mana attributes. A spell's cooldown repeats the server's
 * chain from {@code SpellCooldowns.spellCooldownTicks} and shares its {@code glyphedTicks} step.
 */
public final class SlotCostStats {
    private SlotCostStats() {
    }

    /** Mana is negative when no step pays mana up front. */
    public record Numbers(float manaBefore, float manaAfter, int cooldownBefore, int cooldownAfter) {
        public boolean hasMana() {
            return this.manaBefore >= 0.0F;
        }
    }

    @Nullable
    private static LocalPlayer player() {
        return Minecraft.getInstance().player;
    }

    /** An instant ability's mana cost after the mana attributes, or -1 when it is not an instant mana ability. */
    private static float mana(Ability ability) {
        LocalPlayer player = player();
        if (player == null || !(ability instanceof InstantManaAbility instant)) {
            return -1.0F;
        }
        AttributeSnapshot snapshot = AttributeSnapshotHelper.getInstance().getSnapshot(player);
        return ability.up((skill, cost) -> ManaCostHelperAccessor.woldsvaults$adjustManaCost(snapshot, skill.getId(), cost),
                instant.getManaCost());
    }

    private static int cooldown(Ability ability) {
        LocalPlayer player = player();
        return player == null ? ability.getCooldownTicks() : CooldownHelper.adjustCooldown(player, ability, ability.getCooldownTicks());
    }

    /** A plain ability's mana and cooldown before and after the player's adjustments, or null without the ability. */
    @Nullable
    public static Numbers plain(AbilityTree tree, String group) {
        Ability ability = AbilityLookup.ability(tree, group);
        if (ability == null) {
            return null;
        }
        float mana = mana(ability);
        return new Numbers(mana, mana, ability.getCooldownTicks(), cooldown(ability));
    }

    /** A spell's summed mana before and after its mana factor, and its cooldown without and with glyphs. */
    @Nullable
    public static Numbers spell(SpellcraftConfig config, AbilityTree tree, SpellModel model) {
        LocalPlayer player = player();
        if (player == null || model.groups().isEmpty()) {
            return null;
        }
        float mana = -1.0F;
        for (String group : model.castGroups()) {
            Ability ability = AbilityLookup.ability(tree, group);
            float cost = ability == null ? -1.0F : mana(ability);
            if (cost >= 0.0F) {
                mana = Math.max(0.0F, mana) + cost;
            }
        }
        int plain = chain(config, tree, model.castGroups());
        int glyphed = spellCooldown(config, tree, model.castGroups(),model.cooldownBase(), model.glyphHaste());
        return new Numbers(mana, mana < 0.0F ? mana : mana * model.manaFactor(), plain, glyphed);
    }

    private static int chain(SpellcraftConfig config, AbilityTree tree, Collection<String> groups) {
        List<Integer> adjusted = new ArrayList<>();
        for (String group : groups) {
            Ability ability = AbilityLookup.ability(tree, group);
            if (ability != null) {
                adjusted.add(cooldown(ability));
            }
        }
        if (adjusted.isEmpty()) {
            return 0;
        }
        adjusted.sort(Comparator.reverseOrder());
        double chain = adjusted.get(0);
        for (int i = 1; i < adjusted.size(); i++) {
            chain += config.rules.chainCooldownShare * adjusted.get(i);
        }
        return (int) Math.round(chain);
    }

    private static int spellCooldown(SpellcraftConfig config, AbilityTree tree, Collection<String> groups, float cooldownBase,
                                     float glyphHaste) {
        LocalPlayer player = player();
        double chain = chain(config, tree, groups);
        if (chain <= 0 || player == null) {
            return 0;
        }
        Float plain = ModConfigs.SPELL_HASTE == null ? null : SpellHaste.factor(player, 1.0F);
        Float spell = ModConfigs.SPELL_HASTE == null ? null : SpellHaste.spellFactor(player, glyphHaste, config.rules.glyphHasteHeadroom);
        return SpellCooldowns.glyphedTicks(chain, cooldownBase, plain, spell, glyphHaste);
    }

    /** The player's haste as a percentage (+100 for a factor of 2), or null when the haste table isn't loaded. */
    @Nullable
    public static Integer hastePercent() {
        LocalPlayer player = player();
        if (player == null || ModConfigs.SPELL_HASTE == null) {
            return null;
        }
        Float factor = SpellHaste.factor(player, 1.0F);
        return factor == null ? null : Math.round((factor - 1.0F) * 100.0F);
    }
}
