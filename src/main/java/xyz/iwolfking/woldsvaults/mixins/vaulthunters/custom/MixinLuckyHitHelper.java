package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.util.calc.LuckyHitHelper;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.api.util.LuckHelper;

@Mixin(value = LuckyHitHelper.class, remap = false)
public class MixinLuckyHitHelper {
    @Inject(method = "getLuckyHitChanceUnlimited", at = @At("TAIL"), cancellable = true)
    private static void luckAffectsChance(LivingEntity entity, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(LuckHelper.getLuckAffectedChance(cir.getReturnValue(), entity));
    }
}
