package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft;

import iskallia.vault.skill.talent.type.ConditionalDamageTalent;
import iskallia.vault.skill.talent.type.onhit.CastOnHitTalent;
import iskallia.vault.skill.talent.type.onhit.DamageOnHitTalent;
import iskallia.vault.skill.talent.type.onhit.EffectOnHitTalent;
import iskallia.vault.skill.talent.type.onhit.SweepingOnHitTalent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.spellcraft.chain.SpellChainDamage;

@Mixin(value = {EffectOnHitTalent.class, CastOnHitTalent.class, SweepingOnHitTalent.class, DamageOnHitTalent.class, ConditionalDamageTalent.class}, remap = false)
public abstract class MixinOnHitTalentsChainScope {
    /** A Chaining glyph link is pure damage: no on-hit proc and no attacker-side increase on its already final amount. */
    @Inject(method = "onAttack", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$chainScopeOnAttack(LivingHurtEvent event, CallbackInfo ci) {
        if (SpellChainDamage.active()) {
            ci.cancel();
        }
    }
}
