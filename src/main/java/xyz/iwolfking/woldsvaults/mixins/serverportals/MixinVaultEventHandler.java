package xyz.iwolfking.woldsvaults.mixins.serverportals;

import com.dog.serverportals.handlers.VaultEventHandler;
import com.dog.serverportals.network.NetworkHandler;
import iskallia.vault.core.vault.VaultUtils;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = VaultEventHandler.class,remap = false)
public class MixinVaultEventHandler {
    @Redirect(method = "onServerTick", at = @At(value = "INVOKE", target = "Lcom/dog/serverportals/network/NetworkHandler;sendVaultTime(Lnet/minecraft/server/level/ServerPlayer;)V"))
    private void dontSendTimePacket(ServerPlayer player) {

    }
}
