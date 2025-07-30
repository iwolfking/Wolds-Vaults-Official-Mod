package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.event.ActiveFlags;
import iskallia.vault.item.VaultXPFoodItem;
import iskallia.vault.world.data.PlayerVaultStatsData;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = VaultXPFoodItem.Flat.class, remap = false)
public class MixinVaultXPFoodItem {
    @Inject(method = "grantExp", at = @At(value = "INVOKE", target = "Liskallia/vault/event/ActiveFlags;runIfNotSet(Ljava/lang/Runnable;)V"), slice = @Slice(from = @At(value = "INVOKE", target = "Liskallia/vault/util/MathUtilities;getRandomInt(II)I"), to = @At(value = "INVOKE", target = "Liskallia/vault/event/ActiveFlags;runIfNotSet(Ljava/lang/Runnable;)V")), cancellable = true)
    private void replaceRunIfNotSet(ServerPlayer sPlayer, int count, CallbackInfo ci, @Local PlayerVaultStatsData data, @Local(ordinal = 1) int exp) {
        ActiveFlags.IS_BURGER_XP.push();
        data.addVaultExp(sPlayer, exp);
        ActiveFlags.IS_BURGER_XP.pop();
        ci.cancel();
    }
}
