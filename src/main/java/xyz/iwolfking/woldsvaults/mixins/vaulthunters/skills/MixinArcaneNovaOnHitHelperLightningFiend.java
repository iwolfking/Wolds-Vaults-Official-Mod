package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills;

import iskallia.vault.util.ArcaneNovaOnHitHelper;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.events.WoldActiveFlags;

@Mixin(value = ArcaneNovaOnHitHelper.class, remap = false)
public class MixinArcaneNovaOnHitHelperLightningFiend {
    /** Lightning Fiend set hits carry no on-hit procs, so they neither count towards nor cast an arcane nova. */
    @Inject(method = "onPlayerHitsMob", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$skipLightningFiendHits(LivingHurtEvent event, CallbackInfo ci) {
        if (WoldActiveFlags.IS_LIGHTNING_FIEND_ATTACKING.isSet()) {
            ci.cancel();
        }
    }
}
