package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.Window;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.player.Listener;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.client.champion.ChampionHudRenderer;

/** Draws the Champion's health bar from the objective HUD's tail, inheriting its position and scale. */
@Mixin(value = Listener.class, remap = false)
public abstract class MixinListenerChampionHud {

    @Inject(method = "renderObjectives", at = @At("RETURN"))
    private void woldsvaults$drawChampionBar(Vault vault, PoseStack matrixStack, Window window,
                                             float partialTicks, Player player, CallbackInfo ci) {
        ChampionHudRenderer.render(matrixStack);
    }
}
