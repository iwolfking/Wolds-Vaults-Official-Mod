package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.event.common.ListenerLeaveEvent;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.RoyaleObjective;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.helper.TrinketHelper;

@Mixin(value = RoyaleObjective.class, remap = false)
public class MixinRoyaleObjective {
    @Inject(method = "lambda$initServer$7", at = @At(value = "INVOKE", target = "Liskallia/vault/util/MiscUtils;clearPlayerInventory(Lnet/minecraft/world/entity/player/Player;)V", shift = At.Shift.BEFORE))
    private void clearTrinketSlotsOnCompletion(Vault vault, ListenerLeaveEvent.Data data, CallbackInfo ci, @Local ServerPlayer player) {
            TrinketHelper.clearCurios(player);
    }
}
