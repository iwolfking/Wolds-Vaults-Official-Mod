package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.core.vault.objective.DeathObjective;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.stat.StatsCollector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DeathObjective.class, remap = false)
public class MixinDeathObjective {
    @Inject(method = "lambda$tickListener$1", at = @At("HEAD"), cancellable = true)
    private static void test(Listener listener, StatsCollector collector, CallbackInfo ci) {
        if(collector == null) {
            ci.cancel();
        }
    }
}
