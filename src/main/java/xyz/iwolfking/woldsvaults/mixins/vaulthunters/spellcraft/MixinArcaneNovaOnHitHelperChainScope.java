package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft;

import iskallia.vault.util.ArcaneNovaOnHitHelper;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.spellcraft.chain.SpellChainDamage;

@Mixin(value = ArcaneNovaOnHitHelper.class, remap = false)
public abstract class MixinArcaneNovaOnHitHelperChainScope {
    /** A Chaining glyph link is pure damage: no on-hit proc and no attacker-side increase on its already final amount. */
    @Inject(method = "onPlayerHitsMob", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$chainScopeOnPlayerHitsMob(LivingHurtEvent event, CallbackInfo ci) {
        if (SpellChainDamage.active()) {
            ci.cancel();
        }
    }
}
