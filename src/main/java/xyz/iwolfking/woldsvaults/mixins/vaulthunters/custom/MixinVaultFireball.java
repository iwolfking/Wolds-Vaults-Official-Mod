package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.entity.entity.VaultFireball;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.etching.EtchingHelper;
import iskallia.vault.skill.ability.effect.spi.AbstractFireballAbility;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.util.WoldEtchingHelper;
import xyz.iwolfking.woldsvaults.effect.mobeffects.PercentBurnEffect;
import xyz.iwolfking.woldsvaults.init.ModEtchingGearAttributes;

import java.util.Random;

@Mixin(value = VaultFireball.class, remap = false)
public abstract class MixinVaultFireball extends AbstractArrow {
    protected MixinVaultFireball(EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Shadow
    public abstract void explode(Vec3 pos);

    @Shadow
    private int bounceCount;

    @Shadow
    public abstract Player getThrower();

    @Shadow
    public abstract VaultFireball createBouncingFireball(Level level, LivingEntity thrower, int bounceCount);

    @Inject(method = "onHit", at = @At(value = "INVOKE", target = "Liskallia/vault/entity/entity/VaultFireball;createBouncingFireball(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)Liskallia/vault/entity/entity/VaultFireball;", ordinal = 0, remap = false), remap = true)
    private void fireVolleyBouncesExplode(HitResult result, CallbackInfo ci) {
        if(bounceCount <= 99) {
            VaultGearAttributeInstance<Integer> mitosisEtchingAttribute = EtchingHelper.getEtchings(this.getThrower(), ModEtchingGearAttributes.FIREBALL_VOLLEY_MITOSIS).stream().findFirst().orElse(null);
            if(mitosisEtchingAttribute != null) {
                for(int i = 0; i < mitosisEtchingAttribute.getValue(); i++) {
                    VaultFireball ball = this.createBouncingFireball(this.getThrower().level, this.getThrower(), 0);

                    Random rand = new Random();

                    float yaw = rand.nextFloat() * 360.0f - 180.0f;
                    float pitch = rand.nextFloat() * 180.0f - 90.0f;

                    float fYaw = -yaw * ((float)Math.PI / 180F);
                    float fPitch = -pitch * ((float)Math.PI / 180F);

                    Vec3 dir = new Vec3(
                            Mth.sin(fYaw) * Mth.cos(fPitch),
                            Mth.sin(fPitch),
                            Mth.cos(fYaw) * Mth.cos(fPitch)
                    ).normalize();

                    double speed = 0.5;

                    ball.setPos(result.getLocation());
                    ball.setDeltaMovement(dir.scale(speed));

                    ball.setYRot(yaw);
                    ball.setXRot(pitch);
                    ball.yRotO = yaw;
                    ball.xRotO = pitch;

                    this.getThrower().level.addFreshEntity(ball);
                }
            }
        }
        explode(result.getLocation());
    }

    @WrapOperation(method = "getRadius", at = @At(value = "INVOKE", target = "Liskallia/vault/skill/ability/effect/spi/AbstractFireballAbility;getRadius()F"))
    private float getGreedBallRadius(AbstractFireballAbility instance, Operation<Float> original, @Local(name = "player") ServerPlayer player) {
        if(WoldEtchingHelper.hasEtching(player, ModEtchingGearAttributes.FIREBALL_GREEDBALL)) {
            return 3.0F;
        }

        return original.call(instance);
    }

    @Inject(method = "lambda$onHit$6", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"), remap = true)
    private static void applyBurnEffectToFireshot(LivingEntity living, DamageSource damageSource, float attackDamage, CallbackInfo ci) {
        if(damageSource.getEntity() instanceof LivingEntity attacker) {
            PercentBurnEffect.applyPercentBurn(living, attacker,220, attackDamage);
        }
    }
}
