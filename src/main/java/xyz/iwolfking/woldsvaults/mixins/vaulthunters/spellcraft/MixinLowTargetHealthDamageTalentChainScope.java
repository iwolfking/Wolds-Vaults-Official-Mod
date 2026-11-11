package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft;

import iskallia.vault.skill.talent.type.LowTargetHealthDamageTalent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.spellcraft.chain.SpellChainDamage;

@Mixin(value = LowTargetHealthDamageTalent.class, remap = false)
public abstract class MixinLowTargetHealthDamageTalentChainScope {
    /** A Chaining glyph link is pure damage: no on-hit proc and no attacker-side increase on its already final amount. */
    @Inject(method = "lambda$static$0", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$chainScopeLambdastatic0(LivingDamageEvent event, CallbackInfo ci) {
        if (SpellChainDamage.active()) {
            ci.cancel();
        }
    }
}
