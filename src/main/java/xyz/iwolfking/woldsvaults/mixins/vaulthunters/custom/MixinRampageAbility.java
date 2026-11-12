package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.skill.ability.effect.RampageAbility;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.events.PostMultiplierFlatAdds;

@Mixin(value = RampageAbility.class, remap = false)
public class MixinRampageAbility {

    /** Lands thorns and ability power scaling before Rampage's LOW-priority multiplier, so Rampage scales them. */
    @Inject(method = "onDirectMeleeDamage", at = @At("HEAD"))
    private static void woldsvaults$applyFlatAddsFirst(LivingHurtEvent event, CallbackInfo ci) {
        PostMultiplierFlatAdds.apply(event);
    }
}
