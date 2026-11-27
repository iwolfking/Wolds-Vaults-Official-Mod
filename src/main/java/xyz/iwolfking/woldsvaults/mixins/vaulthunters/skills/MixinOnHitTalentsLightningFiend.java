package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills;

import iskallia.vault.skill.talent.type.onhit.CastOnHitTalent;
import iskallia.vault.skill.talent.type.onhit.EffectOnHitTalent;
import iskallia.vault.skill.talent.type.onhit.SweepingOnHitTalent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.events.WoldActiveFlags;

@Mixin(value = {CastOnHitTalent.class, SweepingOnHitTalent.class, EffectOnHitTalent.class}, remap = false)
public class MixinOnHitTalentsLightningFiend {
    /** Lightning Fiend set hits carry no on-hit procs, so cast, sweeping and effect on-hit talents skip them. */
    @Inject(method = "onAttack", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$skipLightningFiendHits(LivingHurtEvent event, CallbackInfo ci) {
        if (WoldActiveFlags.IS_LIGHTNING_FIEND_ATTACKING.isSet()) {
            ci.cancel();
        }
    }
}
