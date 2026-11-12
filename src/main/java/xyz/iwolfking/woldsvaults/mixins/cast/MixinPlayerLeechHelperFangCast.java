package xyz.iwolfking.woldsvaults.mixins.cast;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.util.damage.AttackScaleHelper;
import iskallia.vault.util.PlayerLeechHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.cast.CastContext;
import xyz.iwolfking.woldsvaults.api.cast.CastTag;
import xyz.iwolfking.woldsvaults.events.WoldActiveFlags;

/**
 * Fang hits leech from gear at most once per cast, and are not blocked by the last swing's attack scale,
 * since a fang is not a swing.
 */
@Mixin(value = PlayerLeechHelper.class, remap = false)
public class MixinPlayerLeechHelperFangCast {

    @Inject(method = "onLivingHurt", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$oneFangLeechPerCast(LivingDamageEvent event, CallbackInfo ci) {
        if (!WoldActiveFlags.IS_FANG_ATTACKING.isSet()) {
            return;
        }
        CastTag tag = CastContext.current();
        if (tag != null && !tag.claimLeech()) {
            ci.cancel();
        }
    }

    @WrapOperation(method = "onLeech", at = @At(value = "INVOKE",
            target = "Liskallia/vault/util/damage/AttackScaleHelper;getLastAttackScale(Lnet/minecraft/world/entity/player/Player;)F"))
    private static float woldsvaults$fangsIgnoreSwingScale(Player player, Operation<Float> original) {
        if (WoldActiveFlags.IS_FANG_ATTACKING.isSet()) {
            return 1.0F;
        }
        return original.call(player);
    }
}
