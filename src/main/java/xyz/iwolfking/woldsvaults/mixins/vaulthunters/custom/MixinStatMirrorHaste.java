package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.util.calc.CooldownHelper;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.spellcraft.haste.StatMirror;

@Mixin(value = CooldownHelper.class, remap = false)
public abstract class MixinStatMirrorHaste {

    /** On the client, the local player's value comes from the server's mirror, so server-only bonuses show. */
    @Inject(method = "getCooldownMultiplierUnlimited", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$mirrored(LivingEntity player, CallbackInfoReturnable<Float> cir) {
        Float value = StatMirror.client(player, StatMirror.HASTE);
        if (value != null) {
            cir.setReturnValue(value);
        }
    }
}
