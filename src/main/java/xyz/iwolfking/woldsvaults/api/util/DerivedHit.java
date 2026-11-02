package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.entity.boss.ArtifactBossEntity;
import iskallia.vault.entity.boss.TheVesselEntity;
import iskallia.vault.entity.VaultBoss;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import xyz.iwolfking.woldsvaults.mixins.LivingEntityAccessor;

public final class DerivedHit {
    private DerivedHit() {
    }

    /**
     * Hurts {@code target} with a hit that rides on another hit (a proc, a combo burst, a bonus strike) without being
     * eaten by the invulnerability frames that hit left behind. The target's invulnerability time, hurt time and last
     * hurt amount are cleared for this one hit and restored afterwards, so the derived hit lands in full through the
     * normal damage pipeline and the next real hit is judged against the original state.
     */
    public static boolean deal(LivingEntity target, DamageSource source, float amount) {
        if (target == null || amount <= 0.0F || target.level.isClientSide() || !target.isAlive()) {
            return false;
        }
        int savedInvulnerable = target.invulnerableTime;
        int savedHurtTime = target.hurtTime;
        float savedLastHurt = ((LivingEntityAccessor) target).getLastHurt();
        try {
            target.invulnerableTime = 0;
            return target.hurt(source, amount);
        } finally {
            target.invulnerableTime = savedInvulnerable;
            target.hurtTime = savedHurtTime;
            ((LivingEntityAccessor) target).setLastHurt(savedLastHurt);
        }
    }

    /**
     * Whether {@code entity} is a boss for on-hit modifiers that must not touch bosses: vault and hyper bosses, the
     * greed trial's Vessel and artifact bosses.
     */
    public static boolean isBoss(Entity entity) {
        return entity instanceof VaultBoss || entity instanceof TheVesselEntity || entity instanceof ArtifactBossEntity;
    }
}
