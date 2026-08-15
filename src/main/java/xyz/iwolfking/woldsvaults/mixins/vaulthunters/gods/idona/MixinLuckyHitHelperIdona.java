package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods.idona;

import iskallia.vault.util.calc.LuckyHitHelper;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.gods.trees.idona.IdonaLuckyHit;

@Mixin(value = LuckyHitHelper.class, remap = false)
public class MixinLuckyHitHelperIdona {
    /**
     * @author PoorMansPhysicist
     * @reason Luckiest Hit multiplies lucky hit chance by 0.1. Injected at the tail of the
     * unlimited getter, alongside the addon's existing luck injection, so the clamp and every
     * downstream reader (including Overcrit) see the reduced value.
     */
    @Inject(method = "getLuckyHitChanceUnlimited", at = @At("TAIL"), cancellable = true)
    private static void applyLuckiestHit(LivingEntity entity, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(IdonaLuckyHit.scaleChance(entity, cir.getReturnValue()));
    }
}
