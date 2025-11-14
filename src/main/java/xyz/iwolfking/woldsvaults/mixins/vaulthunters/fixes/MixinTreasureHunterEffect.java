package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.skill.prestige.TreasureHunterPrestigePower;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TreasureHunterPrestigePower.class, remap = false)
public abstract class MixinTreasureHunterEffect {
    @Shadow
    private static void resetStacks(ServerPlayer player) {
    }

    @ModifyConstant(method = "updateIndicatorEffect", constant = @Constant(intValue = 40))
    private static int updateEffectEarlier(int constant) {
        return 180;
    }

    @Inject(method = "lambda$static$3", at = @At("HEAD"), cancellable = true)
    private static void resetOnDamageTakenOnly(LivingDamageEvent event, CallbackInfo ci) {
        if(event.getEntity() instanceof ServerPlayer damaged) {
            resetStacks(damaged);
        }
        ci.cancel();
    }
}
