package xyz.iwolfking.woldsvaults.mixins.vaultmapper;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.nodiumhosting.vaultmapper.map.VaultMap;
import com.nodiumhosting.vaultmapper.map.VaultMapOverlayRenderer;
import iskallia.vault.core.vault.ClientVaults;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

@Mixin(value = VaultMapOverlayRenderer.class, remap = false)
public abstract class MixinVaultMapOverlayRenderer {
    @Shadow
    private static void renderPlayerArrow(BufferBuilder bufferBuilder, VaultMap.MapPlayer data) {
    }

    @Shadow
    private static void renderPlayerName(PoseStack posestack, String name, VaultMap.MapPlayer data) {
    }

    @Inject(method = "eventHandler", at = @At("HEAD"), cancellable = true)
    private static void cancelVaultMapperRender(RenderGameOverlayEvent.Post event, CallbackInfo ci) {
        if(WoldsVaultsConfig.CLIENT.showVanillaVaultMap.get()) {
            ci.cancel();
        }
    }

    @Redirect( method = "eventHandler", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/ConcurrentHashMap;forEach(Ljava/util/function/BiConsumer;)V", ordinal = 0))
    private static void cancelOtherPlayerArrowRenderInPvP(ConcurrentHashMap<String, VaultMap.MapPlayer> instance, BiConsumer<? super String, ? super VaultMap.MapPlayer> it, @Local BufferBuilder bufferBuilder) {
        Vault vault = ClientVaults.getActive().orElse(null);
        if(vault == null) {
            return;
        }

        if(VaultUtils.isPvPVault(vault)) {
            return;
        }


        instance.forEach((name, data) -> renderPlayerArrow(bufferBuilder, data));
    }

    @Redirect( method = "eventHandler", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/ConcurrentHashMap;forEach(Ljava/util/function/BiConsumer;)V", ordinal = 1))
    private static void cancelOtherPlayerNameRenderInPvP(ConcurrentHashMap<String, VaultMap.MapPlayer> instance, BiConsumer<? super String, ? super VaultMap.MapPlayer> it, @Local(argsOnly = true) RenderGameOverlayEvent.Post event) {
        Vault vault = ClientVaults.getActive().orElse(null);
        if(vault == null) {
            return;
        }

        if(VaultUtils.isPvPVault(vault)) {
            return;
        }


        instance.forEach((name, data) -> renderPlayerName(event.getMatrixStack(), name, data));
    }
}
