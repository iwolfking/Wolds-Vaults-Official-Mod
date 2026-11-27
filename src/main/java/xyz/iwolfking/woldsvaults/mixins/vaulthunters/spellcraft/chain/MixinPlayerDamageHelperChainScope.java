package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.chain;

import iskallia.vault.util.damage.PlayerDamageHelper;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.spellcraft.scope.ChainScope;

@Mixin(value = PlayerDamageHelper.class, remap = false)
public abstract class MixinPlayerDamageHelperChainScope {
    /**
     * A Chaining glyph link is pure damage: no on-hit proc and no attacker-side increase on its already final amount.
     */
    @Inject(method = "onPlayerDamage", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$chainScopeOnPlayerDamage(LivingHurtEvent event, CallbackInfo ci) {
        if (ChainScope.active()) {
            ci.cancel();
        }
    }

    @Inject(method = "regenMana", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$chainScopeRegenMana(LivingHurtEvent event, CallbackInfo ci) {
        if (ChainScope.active()) {
            ci.cancel();
        }
    }

    @Inject(method = "applyVulnerableFromTotem", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$chainScopeApplyVulnerableFromTotem(LivingHurtEvent event, CallbackInfo ci) {
        if (ChainScope.active()) {
            ci.cancel();
        }
    }
}
