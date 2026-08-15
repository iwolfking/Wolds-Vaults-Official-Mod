package xyz.iwolfking.woldsvaults.gods.ultimates;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.server.level.ServerPlayer;

/**
 * Idona's ultimate. Infuses the caster for fifteen seconds with an attack-damage and ability-power
 * multiplier, its own damage reduction and a per-hit compounding stack, then spends everything the
 * infusion killed with on a forward dash. All of the behaviour lives in {@link CopeDeGraceState};
 * this is the castable shell.
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
}
