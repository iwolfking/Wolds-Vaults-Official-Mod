package xyz.iwolfking.woldsvaults.gods.ultimates;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.server.level.ServerPlayer;

/**
 * Wendarr's ultimate. Stretches the vault clock and grants dodge, attack speed and movement speed
 * for the duration. All of the behaviour lives in {@link BulletTimeState}; this is the castable
 * shell.
 *
 * <p>Note the duration deliberately does not go through {@code EffectDurationHelper}, unlike every
 * other timed ability in the addon: ability-boosting stats must not reach a god ultimate.
 */
public class BulletTimeAbility extends GodUltimateAbility {
    public BulletTimeAbility(int unlockLevel, int learnPointCost, int regretPointCost, int cooldownTicks,
                             float manaCost) {
        super(unlockLevel, learnPointCost, regretPointCost, cooldownTicks, manaCost);
    }

    public BulletTimeAbility() {
    }

    @Override
    public VaultGod getGod() {
        return VaultGod.WENDARR;
    }

    @Override
    protected ActionResult doUltimate(ServerPlayer player, int level) {
        BulletTimeState.begin(player, UltimateLevels.bulletTime(level));
        return ActionResult.successCooldownImmediate();
    }
}
