package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.chain;

import iskallia.vault.gear.trinket.effects.DiceTrinket;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.spellcraft.scope.ChainScope;

@Mixin(value = DiceTrinket.class, remap = false)
public abstract class MixinDiceTrinketChainScope {
    /**
     * A Chaining glyph link is pure damage: no on-hit proc and no attacker-side increase on its already final amount.
     */
    @Inject(method = "onLivingHurt", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$chainScopeOnLivingHurt(LivingHurtEvent event, CallbackInfo ci) {
        if (ChainScope.active()) {
            ci.cancel();
        }
    }
}
