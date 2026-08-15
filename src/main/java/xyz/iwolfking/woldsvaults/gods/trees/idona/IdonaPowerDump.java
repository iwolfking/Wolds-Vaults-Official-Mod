package xyz.iwolfking.woldsvaults.gods.trees.idona;

import iskallia.vault.mana.Mana;
import iskallia.vault.skill.ability.effect.spi.core.InstantManaAbility;
import iskallia.vault.skill.base.Skill;
import net.minecraft.server.level.ServerPlayer;

/**
 * Power Dump. Every instant ability is charged the player's entire mana bar, and the mana spent
 * beyond the ability's own cost is converted into ability damage.
 *
 * <p>There is no event for mana cost -  {@code ABILITY_CAST} carries no cost field and fires after
 * the spend -  so the cost override is a mixin on {@code ManaCostHelper.adjustManaCost}, and the
 * surplus is carried in per-player state until the ability-power multiplier is read later in the
 * same cast.
 */
public final class IdonaPowerDump {
    private IdonaPowerDump() {
    }

    /**
     * Returns the mana an ability should actually cost. Only instant abilities are affected:
     * charging a channelled or toggled ability the whole bar every tick would make it uncastable
     * rather than expensive.
     */
    public static float adjustCost(ServerPlayer player, Skill skill, float cost) {
        int points = IdonaNodes.minorPoints(player, IdonaNodes.POWER_DUMP);
        if (points <= 0 || !(skill instanceof InstantManaAbility)) {
            return cost;
        }
        float available = Mana.get(player);
        if (available <= cost) {
            IdonaState.setPowerDumpExtra(player, 0.0F);
            return cost;
        }
        IdonaState.setPowerDumpExtra(player, available - cost);
        return available;
    }
}
