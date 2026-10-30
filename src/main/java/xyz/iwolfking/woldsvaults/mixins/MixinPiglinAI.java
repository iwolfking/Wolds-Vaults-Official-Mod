package xyz.iwolfking.woldsvaults.mixins;

import iskallia.vault.entity.Targeting;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(value = PiglinAi.class)
public class MixinPiglinAI {

    @Unique
    private static final int TARGET_DELAY_TICKS = 20;

    @Inject(method = "findNearestValidAttackTarget", at = @At("HEAD"), cancellable = true)
    private static void findNearestValidAttackTarget(Piglin piglin, CallbackInfoReturnable<Optional<? extends LivingEntity>> cir) {
        if (ServerVaults.get(piglin.level).isPresent()) {
            if (piglin.tickCount < TARGET_DELAY_TICKS) {
                cir.setReturnValue(Optional.empty());
                return;
            }

            cir.setReturnValue(Optional.ofNullable(piglin.getTarget()));
        }
    }

    @Inject(method = "setAngerTarget", at = @At("HEAD"), cancellable = true)
    private static void checkAttackable(AbstractPiglin piglin, LivingEntity target, CallbackInfo ci) {
        if (piglin.tickCount < TARGET_DELAY_TICKS) {
            ci.cancel();
            return;
        }

        if (Targeting.getTargetingResult(piglin, target) == Targeting.TargetingResult.IGNORE) {
            ci.cancel();
        }
    }

    @Inject(method = "isNearestValidAttackTarget", at = @At("HEAD"), cancellable = true)
    private static void checkTargetOverrides(Piglin piglin, LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        Targeting.TargetingResult targetingResult = Targeting.getTargetingResult(piglin, entity);
        if (targetingResult != Targeting.TargetingResult.DEFAULT) {
            if (piglin.tickCount < TARGET_DELAY_TICKS) {
                cir.setReturnValue(false);
                return;
            }
            cir.setReturnValue(targetingResult.getShouldTarget());
            cir.cancel();
        }
    }
}
