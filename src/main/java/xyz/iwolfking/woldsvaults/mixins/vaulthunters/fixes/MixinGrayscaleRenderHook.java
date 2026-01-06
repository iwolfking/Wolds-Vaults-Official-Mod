package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.client.util.GrayscaleRenderHook;
import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.event.RenderLivingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GrayscaleRenderHook.class, remap = false)
public class MixinGrayscaleRenderHook {
    @Inject(method = "onRenderLivingPre", at = @At("HEAD"), cancellable = true)
    private static void cancelRenderHook(RenderLivingEvent.Pre<? extends LivingEntity, ? extends EntityModel<?>> e, CallbackInfo ci) {
        ci.cancel();
    }
}
