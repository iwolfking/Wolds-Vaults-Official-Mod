package xyz.iwolfking.woldsvaults.gods.ultimates;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.server.level.ServerPlayer;

/**
 * Wendarr's ultimate, implemented in {@link BulletTimeState}; its duration is not run through
 * {@code EffectDurationHelper}.
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

    public UltimateLevels.BulletTime getDisplayValues() {
        return UltimateLevels.bulletTime(UltimateLevels.displayLevel());
    }

    public float getTimerStretch() {
        return this.getDisplayValues().timerStretch();
    }

    public float getDodgeChance() {
        return this.getDisplayValues().dodgeChance();
    }

    public int getDurationTicks() {
        return this.getDisplayValues().durationTicks();
    }

    public float getAttackSpeed() {
        return this.getDisplayValues().attackSpeed();
    }

    public float getMovementSpeed() {
        return this.getDisplayValues().movementSpeed();
    }
}
