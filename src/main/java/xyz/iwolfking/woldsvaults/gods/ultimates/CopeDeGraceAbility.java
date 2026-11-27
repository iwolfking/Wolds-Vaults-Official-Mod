package xyz.iwolfking.woldsvaults.gods.ultimates;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.server.level.ServerPlayer;

/**
 * Idona's ultimate: a damage and ability-power infusion with per-hit compounding, cashed out as a forward dash.
 * Implemented in {@link CopeDeGraceState}.
 */
public class CopeDeGraceAbility extends GodUltimateAbility {
    public CopeDeGraceAbility(int unlockLevel, int learnPointCost, int regretPointCost, int cooldownTicks,
                              float manaCost) {
        super(unlockLevel, learnPointCost, regretPointCost, cooldownTicks, manaCost);
    }

    public CopeDeGraceAbility() {
    }

    @Override
    public VaultGod getGod() {
        return VaultGod.IDONA;
    }

    @Override
    protected ActionResult doUltimate(ServerPlayer player, int level) {
        CopeDeGraceState.begin(player, UltimateLevels.cope(level));
        return ActionResult.successCooldownImmediate();
    }

    public UltimateLevels.Cope getDisplayValues() {
        return UltimateLevels.cope(UltimateLevels.displayLevel());
    }

    public float getAttackDamageMultiplier() {
        return this.getDisplayValues().attackDamageMultiplier();
    }

    public float getAbilityPowerMultiplier() {
        return this.getDisplayValues().abilityPowerMultiplier();
    }

    public float getAdditionalResistance() {
        return this.getDisplayValues().resistance();
    }

    public float getDamageCompounding() {
        return this.getDisplayValues().compoundingPerHit();
    }

    public int getDurationTicks() {
        return this.getDisplayValues().durationTicks();
    }
}
