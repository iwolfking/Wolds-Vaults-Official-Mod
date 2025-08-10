package xyz.iwolfking.woldsvaults.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.client.ClientPrestigePowersData;
import iskallia.vault.core.vault.ClientVaults;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.prestige.TreasureHunterPrestigePower;
import iskallia.vault.world.data.PlayerPrestigePowersData;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.init.ModEffects;
import xyz.iwolfking.woldsvaults.prestige.ReachPrestigePower;

import javax.annotation.Nullable;


@Mixin(value = LivingEntity.class, priority = 900)
abstract class MixinLivingEntity extends Entity {

    private MixinLivingEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Shadow @Nullable public abstract MobEffectInstance getEffect(MobEffect pEffect);
    @Shadow public abstract boolean shouldDiscardFriction();


    @Inject(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/world/effect/MobEffect;)Z", ordinal = 2), cancellable = true)
    private void doLevitates(Vec3 pTravelVector, CallbackInfo ci, @Local(ordinal = 1) float f3, @Local(ordinal = 1) Vec3 vec35) {

        int levels = (this.hasEffect(ModEffects.LEVITATEII)
                    ? this.getEffect(ModEffects.LEVITATEII).getAmplifier()+1
                    : 0)
                   + (this.hasEffect(MobEffects.LEVITATION)
                    ? this.getEffect(MobEffects.LEVITATION).getAmplifier()+1
                    : 0);

        if(levels != 0) {
            double dY = vec35.y + (0.05 * (double)levels - vec35.y) * 0.2;

            if (this.shouldDiscardFriction()) {
                this.setDeltaMovement(vec35.x, dY, vec35.z);
            } else {
                this.setDeltaMovement(vec35.x * (double)f3, dY * (double)0.98F, vec35.z * (double)f3);
            }

            ci.cancel();
        }
    }

    @Shadow public abstract boolean hasEffect(MobEffect pEffect);
    @Shadow public abstract AttributeMap getAttributes();

    // Prestige
    @Inject(method = "getAttributeValue", at = @At("HEAD"), cancellable = true)
    public void getAttributeValue(Attribute attribute, CallbackInfoReturnable<Double> cir) {
        if (attribute == ForgeMod.REACH_DISTANCE.get()) {
            LivingEntity entity = ((LivingEntity)(Object)this);
            if (entity instanceof Player player) {
                if (this.level.isClientSide && ClientVaults.getActive().isPresent()) {
                    double increase = ClientPrestigePowersData.getTree().getAll(ReachPrestigePower.class, Skill::isUnlocked).stream().mapToDouble(ReachPrestigePower::getReachIncrease).sum();
                    cir.setReturnValue(Math.min(this.getAttributes().getValue(attribute), 7.0F + increase));
                } else if (ServerVaults.get(this.level).isPresent()) {
                    double increase =  PlayerPrestigePowersData.get((ServerLevel) this.level).getPowers(player).getAll(ReachPrestigePower.class, Skill::isUnlocked).stream().mapToDouble(ReachPrestigePower::getReachIncrease).sum();
                    cir.setReturnValue(Math.min(this.getAttributes().getValue(attribute), 7.0F + increase));
                }
            }
        }
    }

}
