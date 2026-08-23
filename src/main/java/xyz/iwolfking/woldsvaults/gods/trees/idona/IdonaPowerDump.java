package xyz.iwolfking.woldsvaults.gods.trees.idona;

import iskallia.vault.mana.Mana;
import iskallia.vault.skill.ability.effect.spi.core.IPerSecondManaAbility;
import iskallia.vault.skill.ability.effect.spi.core.InstantManaAbility;
import iskallia.vault.skill.base.Skill;
import net.minecraft.server.level.ServerPlayer;

/**
 * Power Dump. Every instant ability is charged the player's whole current mana pool -  a partial
 * bar spends whatever is there -  and the mana spent beyond the ability's own cost is converted
 * into ability damage. Hold and toggle abilities are untouched on both halves: they never pay
 * extra, and while one of them is actively draining mana the surplus boost is suppressed, so
 * channelled damage can never ride a surplus banked by an earlier instant cast.
 *
 * <p>There is no event for mana cost -  {@code ABILITY_CAST} carries no cost field and fires after
 * the spend -  so the cost override is a mixin on {@code ManaCostHelper.adjustManaCost}, and the
 * surplus is carried in per-player state. The surplus stays alive for a bounded window
 * ({@code surplus_ttl_ticks}) because instant abilities like Fireball and totems read
 * their ability power at impact or pulse time, not at cast time.
 *
 * <p>The cost override runs on the affordability check as well as on payment, so it only stages
 * the surplus; {@link #commit} banks it from the payment adjuster once mana has actually been
 * spent, and drops it when the cast turned out to be free (Ethereal), so a refused or free cast
 * never hands out a damage boost it did not pay for.
 */
public final class IdonaPowerDump {
    private IdonaPowerDump() {
    }

    /**
     * Returns the mana an ability should actually cost. Only instant abilities are affected;
     * per-second payers instead refresh the suppression window that holds the damage boost off
     * while they are running.
     */
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

    /**
     * Banks the staged surplus once the payment adjuster has settled what an instant cast really
     * costs. A zero result means the cast was free, so the stage is discarded instead.
     */
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
