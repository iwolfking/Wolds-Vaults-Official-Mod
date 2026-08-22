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

/**
 * Hangs the Vault Champion's health bar underneath whatever the vault's objective is drawing.
 *
 * <p>The objective HUD is a repositionable module - the player can drag it anywhere and rescale it -
 * so a screen-space overlay could never reliably sit "just below the objective bar". Drawing from the
 * tail of the objective dispatch instead means the Champion bar inherits the module's position, its
 * scale and its tab-list nudge, and the offset only has to describe the gap between the two.
 *
 * <p>{@code renderObjectives} returns as soon as one objective has drawn, so injecting at every return
 * fires exactly once per frame whether or not the vault had an objective to show.</p>
 */
@Mixin(value = Listener.class, remap = false)
public abstract class MixinListenerChampionHud {

    @Inject(method = "renderObjectives", at = @At("RETURN"))
    private void woldsvaults$drawChampionBar(Vault vault, PoseStack matrixStack, Window window,
                                             float partialTicks, Player player, CallbackInfo ci) {
        ChampionHudRenderer.render(matrixStack);
    }
}
