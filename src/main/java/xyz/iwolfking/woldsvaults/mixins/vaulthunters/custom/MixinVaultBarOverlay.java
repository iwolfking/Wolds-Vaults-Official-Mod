package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.overlay.VaultBarOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import xyz.iwolfking.woldsvaults.util.DivinityUtils;

@Mixin(value = VaultBarOverlay.class)
public class MixinVaultBarOverlay {

    @Inject(method = "renderPointText", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", ordinal = 0, shift = At.Shift.BEFORE))
    private static void renderDivinityPoints(Minecraft minecraft, LocalPlayer player, PoseStack matrixStack, int right, MultiBufferSource.BufferSource buffer, CallbackInfo ci) {
        DivinityUtils.handleOverlayTextRendering(minecraft, right, matrixStack, buffer);
    }


}

