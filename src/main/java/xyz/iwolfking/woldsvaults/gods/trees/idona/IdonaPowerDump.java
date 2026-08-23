package xyz.iwolfking.woldsvaults.gods.trees.idona;

import iskallia.vault.mana.Mana;
import iskallia.vault.skill.ability.effect.spi.core.IPerSecondManaAbility;
import iskallia.vault.skill.ability.effect.spi.core.InstantManaAbility;
import iskallia.vault.skill.base.Skill;
import net.minecraft.server.level.ServerPlayer;

/**
 * Power Dump: instant abilities are charged the player's whole mana pool, and the mana spent beyond
 * their own cost becomes ability damage for {@code surplus_ttl_ticks}. Hold and toggle abilities
 * never pay extra, and suppress the boost while draining.
 */
public final class IdonaPowerDump {
    private IdonaPowerDump() {
    }

    /** The mana an instant ability should cost, staging any surplus. Others are unaffected. */
    public static float adjustCost(ServerPlayer player, Skill skill, float cost) {
        int points = IdonaNodes.points(player, IdonaNodes.POWER_DUMP);
        if (points <= 0) {
            return cost;
        }
        if (skill instanceof IPerSecondManaAbility) {
            IdonaState.markContinuousManaPayment(player);
            return cost;
        }
        if (!(skill instanceof InstantManaAbility)) {
            return cost;
        }
        float available = Mana.get(player);
        if (available <= cost) {
            IdonaState.stagePowerDumpExtra(player, 0.0F);
            return cost;
        }
        IdonaState.stagePowerDumpExtra(player, available - cost);
        return available;
    }

    /** Banks the staged surplus once a cast has paid. A {@code paid} of zero discards it. */
    public static float commit(ServerPlayer player, Skill skill, float paid) {
        if (!(skill instanceof InstantManaAbility) || IdonaNodes.points(player, IdonaNodes.POWER_DUMP) <= 0) {
            return paid;
        }
        if (paid <= 0.0F) {
            IdonaState.discardPowerDumpStage(player);
            return paid;
        }
        IdonaState.commitPowerDumpExtra(player);
        return paid;
    }
}
