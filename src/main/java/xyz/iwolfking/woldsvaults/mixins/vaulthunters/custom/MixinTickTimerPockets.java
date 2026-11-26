package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.vault.time.TickTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.loadingzone.PocketTerritory;

@Mixin(value = TickTimer.class, remap = false)
public abstract class MixinTickTimerPockets {
    /** Keeps the low-time warning quiet while the local player is in a pocket, where the vault clock stands still. */
    @Inject(method = "tickClient", at = @At("HEAD"), cancellable = true)
    private void woldsvaults$quietInPockets(CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (PocketTerritory.inPocket(player)) {
            ci.cancel();
        }
    }
}
