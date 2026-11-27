package xyz.iwolfking.woldsvaults.mixins.cast;

import iskallia.vault.util.ServerScheduler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.iwolfking.woldsvaults.api.cast.CastContext;

/** Lets a task the base scheduler runs later keep the cast that scheduled it. */
@Mixin(value = ServerScheduler.class, remap = false)
public class MixinServerSchedulerCastTag {

    @ModifyVariable(method = "schedule", at = @At("HEAD"), argsOnly = true)
    private Runnable woldsvaults$captureCast(Runnable task) {
        return CastContext.capture(task);
    }
}
