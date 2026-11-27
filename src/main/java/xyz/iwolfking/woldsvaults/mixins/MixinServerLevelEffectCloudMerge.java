package xyz.iwolfking.woldsvaults.mixins;

import iskallia.vault.entity.entity.EffectCloudEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.api.util.EffectCloudMerger;

@Mixin(ServerLevel.class)
public abstract class MixinServerLevelEffectCloudMerge {

    /**
     * Stacks a newly spawned effect cloud onto a matching live cloud instead of adding another entity.
     */
    @Inject(method = "addFreshEntity", at = @At("HEAD"), cancellable = true)
    private void woldsvaults$mergeEffectCloud(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof EffectCloudEntity cloud && EffectCloudMerger.absorbIntoExisting((ServerLevel) (Object) this, cloud)) {
            cir.setReturnValue(false);
        }
    }
}
