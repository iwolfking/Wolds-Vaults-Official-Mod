package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.skill.talent.type.onhit.DamageOnHitTalent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.events.WoldActiveFlags;

@Mixin(value = DamageOnHitTalent.class, remap = false)
public class MixinDamageOnHitTalent {
    /**
     * Keeps proc fangs from re-applying Damage On Hit, which the triggering hit already paid for.
     */
    @Inject(method = "onAttack", at = @At("HEAD"), cancellable = true)
    private static void woldsVaults$skipProcFangs(LivingHurtEvent event, CallbackInfo ci) {
        if (WoldActiveFlags.IS_PROC_FANG_ATTACKING.isSet()) {
            ci.cancel();
        }
    }
}
