package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills;

import iskallia.vault.skill.talent.type.luckyhit.LuckyHitTalent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.abilities.block.LifeBlockState;
import xyz.iwolfking.woldsvaults.modifiers.gear.riptide.RiptideDerivedHits;

@Mixin(value = LuckyHitTalent.class, remap = false)
public class MixinLuckyHitTalentDerivedHits {
    /**
     * Records the amount an authored derived hit enters the lucky hit roll with, so the roll's own multiplier can be
     * kept on top of the authored amount whichever order the two lowest-priority hurt listeners run in. Life Bond's
     * reflected hit is not an attack and never rolls.
     */
    @Inject(method = "doLuckyHit", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$beginDerivedLuckyHit(LivingHurtEvent event, CallbackInfo ci) {
        if (LifeBlockState.isLifeBond(event.getSource())) {
            ci.cancel();
            return;
        }
        if (event.getSource() instanceof RiptideDerivedHits.Source source) {
            source.beginLuckyHit(event.getAmount());
        }
    }

    /** Folds the lucky hit roll's multiplier into the authored derived hit's landed amount. */
    @Inject(method = "doLuckyHit", at = @At("RETURN"))
    private static void woldsvaults$endDerivedLuckyHit(LivingHurtEvent event, CallbackInfo ci) {
        if (event.getSource() instanceof RiptideDerivedHits.Source source) {
            source.endLuckyHit(event.getAmount());
        }
    }
}
