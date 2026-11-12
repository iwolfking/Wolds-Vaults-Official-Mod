package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.util.calc.AreaOfEffectHelper;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.spellcraft.haste.StatMirror;

@Mixin(value = AreaOfEffectHelper.class, remap = false)
public abstract class MixinStatMirrorArea {

    /** On the client, the local player's value comes from the server's mirror, so server-only bonuses show. */
    @Inject(method = "getAreaOfEffectUnlimited", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$mirrored(Player player, CallbackInfoReturnable<Float> cir) {
        Float value = StatMirror.client(player, StatMirror.AREA);
        if (value != null) {
            cir.setReturnValue(value);
        }
    }
}
