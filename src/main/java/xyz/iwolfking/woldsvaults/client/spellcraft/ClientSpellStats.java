package xyz.iwolfking.woldsvaults.client.spellcraft;

import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.ability.effect.spi.core.InstantManaAbility;
import iskallia.vault.skill.tree.AbilityTree;
import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import iskallia.vault.util.calc.CooldownHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import xyz.iwolfking.woldsvaults.augments.spell.SpellModel;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods.idona.ManaCostHelperAccessor;
import xyz.iwolfking.woldsvaults.spellcraft.bar.SpellCooldowns;
import xyz.iwolfking.woldsvaults.spellcraft.haste.SpellHaste;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Mana and cooldown numbers for the client screens, from the client's ability tree and attribute snapshot. Mana is VH's
 * cost after the per-ability mana attributes (Idona's Power Dump is server-only and not shown). An augmented spell's
 * cooldown follows {@code SpellCooldowns.spellCooldownTicks}: the longest ability cooldown plus the chain share of the
 * others, times the glyph cooldown factor and the Quicken/Discount haste ratio, floored at the table minimum.
 */
public final class ClientSpellStats {
    private ClientSpellStats() {
    }

    /** Mana and cooldown before glyphs and after; mana is negative when no step pays mana up front. */
    public record Numbers(float manaBefore, float manaAfter, int cooldownBefore, int cooldownAfter) {
        public boolean hasMana() {
            return this.manaBefore >= 0.0F;
        }
    }

    @Nullable
    private static LocalPlayer player() {
        return Minecraft.getInstance().player;
    }

    /** An instant ability's mana cost after VH's mana attributes, or -1 for toggles and holds. */
    public static float mana(Ability ability) {
        LocalPlayer player = player();
        if (player == null || !(ability instanceof InstantManaAbility instant)) {
            return -1.0F;
        }
        AttributeSnapshot snapshot = AttributeSnapshotHelper.getInstance().getSnapshot(player);
        return ability.up((skill, cost) -> ManaCostHelperAccessor.woldsvaults$adjustManaCost(snapshot, skill.getId(), cost),
                instant.getManaCost());
    }

    /** An ability's cooldown in ticks after the player's haste and per-ability cooldown attributes. */
    public static int cooldown(Ability ability) {
        LocalPlayer player = player();
        return player == null ? ability.getCooldownTicks() : CooldownHelper.adjustCooldown(player, ability, ability.getCooldownTicks());
    }

    /** A plain spell: VH's mana, and the cooldown before and after haste. Null when the ability isn't learned. */
    @Nullable
    public static Numbers plain(AbilityTree tree, String group) {
        Ability ability = SpellCooldowns.ability(tree, group);
        if (ability == null) {
            return null;
        }
        float mana = mana(ability);
        return new Numbers(mana, mana, ability.getCooldownTicks(), cooldown(ability));
    }

    /** An augmented spell: summed mana before and after the spell's mana factor, cooldown without and with glyphs. */
    @Nullable
    public static Numbers spell(FocusAugmentsConfig config, AbilityTree tree, SpellModel model) {
        LocalPlayer player = player();
        if (player == null || model.groups().isEmpty()) {
            return null;
        }
        float mana = -1.0F;
        for (String group : model.groups()) {
            Ability ability = SpellCooldowns.ability(tree, group);
            float cost = ability == null ? -1.0F : mana(ability);
            if (cost >= 0.0F) {
                mana = Math.max(0.0F, mana) + cost;
            }
        }
        int plain = chain(config, tree, model.groups());
        int augmented = spellCooldown(config, tree, model.groups(), model.cooldownFactor(), model.hasteMultiplier());
        return new Numbers(mana, mana < 0.0F ? mana : mana * model.manaFactor(), plain, augmented);
    }

    private static int chain(FocusAugmentsConfig config, AbilityTree tree, Collection<String> groups) {
        List<Integer> adjusted = new ArrayList<>();
        for (String group : groups) {
            Ability ability = SpellCooldowns.ability(tree, group);
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

    private static int spellCooldown(FocusAugmentsConfig config, AbilityTree tree, Collection<String> groups, float cooldownFactor,
                                     float hasteMultiplier) {
        LocalPlayer player = player();
        double chain = chain(config, tree, groups);
        if (chain <= 0 || player == null) {
            return 0;
        }
        Float plain = ModConfigs.SPELL_HASTE == null ? null : SpellHaste.factor(player, 1.0F);
        Float spell = ModConfigs.SPELL_HASTE == null ? null : SpellHaste.factor(player, hasteMultiplier);
        double ticks = chain * cooldownFactor * (plain != null && spell != null ? plain / spell : 1.0);
        return Math.max(Math.round(config.rules.minCooldownSeconds * 20.0F), (int) Math.round(ticks));
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
