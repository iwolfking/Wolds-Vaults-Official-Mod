package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.task.ChaosTask;
import iskallia.vault.task.TaskContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChaosTask.class, remap = false)
public class MixinChaosTask {
    @Inject(method = "onTick", at = @At(value = "HEAD"), cancellable = true)
    private void dontTickWhenPaused(TaskContext context, CallbackInfo ci) {
        if(context != null && context.getVault() != null && context.getVault().get(Vault.CLOCK).has(TickClock.PAUSED)) {
            ci.cancel();
        }
    }
}
