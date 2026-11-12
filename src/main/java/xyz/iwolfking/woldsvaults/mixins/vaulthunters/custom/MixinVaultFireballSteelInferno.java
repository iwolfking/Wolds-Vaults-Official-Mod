package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.entity.entity.VaultFireball;
import iskallia.vault.util.damage.DamageUtil;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.modifiers.gear.unique.SteelInferno;

/**
 * Steel Inferno and fireball i-frames. Every player fireball explosion runs inside a {@link SteelInferno}
 * scope; with the roll its radius shrinks, targets are filtered by distance to their hitbox and damage gets
 * the centre bonus. Every non-meteor explosion hit ignores i-frames.
 */
@Mixin(value = VaultFireball.class, remap = false)
public abstract class MixinVaultFireballSteelInferno {

    @WrapMethod(method = "explode")
    private void woldsvaults$steelInfernoScope(Vec3 pos, Operation<Void> original) {
        SteelInferno.Explosion previous = SteelInferno.current();
        SteelInferno.set(SteelInferno.forFireball((VaultFireball) (Object) this, pos));
        try {
            original.call(pos);
        } finally {
            SteelInferno.set(previous);
        }
    }

    @WrapOperation(method = "explode", at = @At(value = "INVOKE", target = "Liskallia/vault/util/calc/AreaOfEffectHelper;adjustAreaOfEffectKey(Lnet/minecraft/world/entity/LivingEntity;Ljava/lang/String;F)F"))
    private float woldsvaults$steelInfernoRadius(LivingEntity entity, String abilityKey, float radius, Operation<Float> original) {
        float adjusted = original.call(entity, abilityKey, radius);
        SteelInferno.Explosion explosion = SteelInferno.current();
        return explosion == null ? adjusted : explosion.applyRadius(adjusted);
    }

    @WrapOperation(method = "lambda$explode$11", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;distanceToSqr(Lnet/minecraft/world/phys/Vec3;)D"), remap = true)
    private static double woldsvaults$steelInfernoHitboxFilter(LivingEntity entity, Vec3 pos, Operation<Double> original) {
        SteelInferno.Explosion explosion = SteelInferno.current();
        if (explosion != null && explosion.attribute() != null) {
            return SteelInferno.hitboxDistanceSqr(entity, pos);
        }
        return original.call(entity, pos);
    }

    @WrapOperation(method = "lambda$explode$13", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", ordinal = 0), remap = true)
    private static boolean woldsvaults$steelInfernoHurt(LivingEntity entity, DamageSource source, float damage, Operation<Boolean> original) {
        float scaled = SteelInferno.scaleDamage(entity, damage);
        if (!SteelInferno.ignoresIFrames()) {
            return original.call(entity, source, scaled);
        }
        Boolean hurt = DamageUtil.shotgunAttackApply(entity, target -> original.call(target, source, scaled));
        return hurt != null && hurt;
    }
}
