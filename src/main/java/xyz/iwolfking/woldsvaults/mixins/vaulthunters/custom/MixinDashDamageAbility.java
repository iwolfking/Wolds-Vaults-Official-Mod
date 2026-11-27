package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.skill.ability.effect.DashDamageAbility;
import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SkillContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.modifiers.gear.bullet.BulletBill;

/**
 * Bullet Dash as a Bullet Bill ({@link BulletBill}): casting it spins the Bill, makes it pulse red and fly faster for a
 * moment, and the dash's hits for the second after deal the set's extra damage. A giant Bill's dash also reaches further.
 */
@Mixin(value = DashDamageAbility.class, remap = false)
public abstract class MixinDashDamageAbility {
    @Inject(method = "doAction", at = @At("HEAD"))
    private void woldsvaults$bulletBillBoost(SkillContext context, CallbackInfoReturnable<Ability.ActionResult> cir) {
        context.getSource().as(ServerPlayer.class).ifPresent(BulletBill::bulletDash);
    }

    @WrapOperation(method = "lambda$onTick$5", at = @At(value = "INVOKE", target = "Liskallia/vault/util/calc/AreaOfEffectHelper;adjustAreaOfEffect(Lnet/minecraft/world/entity/LivingEntity;Liskallia/vault/skill/base/Skill;F)F"))
    private float woldsvaults$giantBillReach(LivingEntity entity, Skill skill, float range, Operation<Float> original) {
        float reach = original.call(entity, skill, range);
        return entity instanceof ServerPlayer player ? reach * BulletBill.reachMultiplier(player) : reach;
    }

    @ModifyArg(method = "lambda$onTick$4", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", remap = true), index = 1)
    private static float woldsvaults$bulletBillDamage(DamageSource source, float amount) {
        return source.getEntity() instanceof ServerPlayer player ? amount * BulletBill.dashDamageMultiplier(player) : amount;
    }
}
