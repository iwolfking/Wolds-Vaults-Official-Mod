package xyz.iwolfking.woldsvaults.mixins.cast;

import iskallia.vault.skill.talent.type.luckyhit.HealthLeechLuckyHitTalent;
import iskallia.vault.skill.talent.type.luckyhit.ManaLeechLuckyHitTalent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.cast.CastContext;
import xyz.iwolfking.woldsvaults.api.cast.CastTag;
import xyz.iwolfking.woldsvaults.events.WoldActiveFlags;

/**
 * Mana Steal and Life Steal proc at most once per fang cast. The hurt event that claimed the proc may
 * run it again, so Luckiest Hit still doubles it.
 */
@Mixin(value = {HealthLeechLuckyHitTalent.class, ManaLeechLuckyHitTalent.class}, remap = false)
public class MixinLeechLuckyHitFangCast {

    @Inject(method = "onLuckyHit", at = @At("HEAD"), cancellable = true)
    private void woldsvaults$oneStealPerFangCast(LivingHurtEvent event, CallbackInfo ci) {
        if (!WoldActiveFlags.IS_FANG_ATTACKING.isSet()) {
            return;
        }
        CastTag tag = CastContext.current();
        if (tag == null) {
            return;
        }
        boolean claimed = ((Object) this) instanceof ManaLeechLuckyHitTalent ? tag.claimManaSteal(event) : tag.claimLifeSteal(event);
        if (!claimed) {
            ci.cancel();
        }
    }
}
