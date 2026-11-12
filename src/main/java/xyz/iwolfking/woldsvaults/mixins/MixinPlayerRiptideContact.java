package xyz.iwolfking.woldsvaults.mixins;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.modifiers.gear.riptide.RiptideDash;

@Mixin(Player.class)
public abstract class MixinPlayerRiptideContact {
    /**
     * Marks the attack a riptide spin makes on touch, so its hurt event can be told apart from other hits, and makes
     * it a full-strength swing.
     */
    @Inject(method = "doAutoAttackOnTouch", at = @At("HEAD"))
    private void beginRiptideContact(LivingEntity target, CallbackInfo ci) {
        RiptideDash.beginContact((Player) (Object) this, target);
    }

    @Inject(method = "doAutoAttackOnTouch", at = @At("RETURN"))
    private void endRiptideContact(LivingEntity target, CallbackInfo ci) {
        RiptideDash.endContact((Player) (Object) this);
    }
}
