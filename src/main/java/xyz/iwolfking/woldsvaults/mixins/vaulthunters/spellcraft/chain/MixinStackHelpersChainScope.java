package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.chain;

import iskallia.vault.util.PlayerRageHelper;
import iskallia.vault.util.PlayerRelentlessStrikeHelper;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.spellcraft.scope.ChainScope;

@Mixin(value = {PlayerRageHelper.class, PlayerRelentlessStrikeHelper.class}, remap = false)
public abstract class MixinStackHelpersChainScope {
    /**
     * A Chaining glyph link is pure damage: no on-hit proc and no attacker-side increase on its already final amount.
     */
    @Inject(method = "on(Lnet/minecraftforge/event/entity/living/LivingHurtEvent;)V", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$chainScopeOn(LivingHurtEvent event, CallbackInfo ci) {
        if (ChainScope.active()) {
            ci.cancel();
        }
    }
}
