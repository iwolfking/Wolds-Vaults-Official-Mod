package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.base.SkillContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.spellcraft.bar.SpellCooldowns;

@Mixin(value = Ability.class, remap = false)
public abstract class MixinAbilitySpellCooldown {

    /**
     * Lets the spell bar start the cooldown instead: inside a slot cast the trigger starts the whole slot's cooldown and
     * other steps start none, and an ability in an augmented slot gets that spell's cooldown when VH starts it later.
     */
    @Inject(method = "putOnCooldown(ILiskallia/vault/skill/base/SkillContext;)V", at = @At("HEAD"), cancellable = true)
    private void woldsvaults$slotCooldown(int cooldownDelayTicks, SkillContext context, CallbackInfo ci) {
        if (SpellCooldowns.onPutOnCooldown((Ability) (Object) this, cooldownDelayTicks, context)) {
            ci.cancel();
        }
    }
}
