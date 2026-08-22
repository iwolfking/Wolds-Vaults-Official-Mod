package xyz.iwolfking.woldsvaults.client.champion;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.VaultMod;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.GreedChampionConfig;
import xyz.iwolfking.woldsvaults.medallions.champion.VaultChampion;

/**
 * Draws the Vault Champion's health bar using the greed trial's own artwork.
 *
 * <p>The Champion is the trial's Vessel, so it gets the trial's bar: the same
 * {@code the_vault:textures/gui/greed/hud.png} sheet, the same frame and fill geometry, the same
 * centred "dealt / pool" readout and the same red damage-ramp multiplier off to the side. A vanilla
 * boss bar would have read as a wither.
 *
 * <p>It is drawn from a Forge overlay rather than an objective's own render hook, because a Champion
 * turns up in an ordinary vault where the objective is something else entirely and the trial's HUD
 * path is never reached.</p>
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ChampionHudOverlay {
    private static final ResourceLocation HUD = VaultMod.id("textures/gui/greed/hud.png");
    private static final float SCIENTIFIC_THRESHOLD = 1.0E10F;

    private ChampionHudOverlay() {
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL || !ClientChampionHud.isActive()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui || minecraft.player == null) {
            return;
        }
        GreedChampionConfig.Hud hud = VaultChampion.config().getHud();
        PoseStack poseStack = event.getMatrixStack();
        float pool = ClientChampionHud.getPool();
        float dealt = Math.min(ClientChampionHud.getDealt(), pool);
        float progress = pool <= 0.0F ? 0.0F : Math.min(dealt / pool, 1.0F);

        poseStack.pushPose();
        poseStack.translate(event.getWindow().getGuiScaledWidth() / 2.0F + hud.offsetX, hud.offsetY, 0.0D);
        drawFrame(poseStack, progress);
        drawReadout(minecraft.font, poseStack, pool - dealt, pool);
        drawMultiplier(minecraft.font, poseStack);
        poseStack.popPose();
    }

    private static void drawFrame(PoseStack poseStack, float progress) {
        poseStack.pushPose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        int previousTexture = RenderSystem.getShaderTexture(0);
        RenderSystem.setShaderTexture(0, HUD);
        poseStack.translate(-80.0D, 0.0D, 0.0D);
        GuiComponent.blit(poseStack, 0, 0, 0.0F, 0.0F, 200, 26, 200, 50);
        GuiComponent.blit(poseStack, 0, 8, 0.0F, 28.0F, 15 + (int) (130.0F * progress), 10, 200, 50);
        RenderSystem.setShaderTexture(0, previousTexture);
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    /**
     * The remaining pool over its total, in the same scientific-notation cutover the greed trial's own
     * readout uses. A Legend pool with vault modifiers on it runs to ten digits and would otherwise
     * overrun the bar.
     */
    private static void drawReadout(Font font, PoseStack poseStack, float remaining, float pool) {
        String format = remaining >= SCIENTIFIC_THRESHOLD || pool >= SCIENTIFIC_THRESHOLD
                ? "%.3e / %.3e" : "%.0f / %.0f";
        MutableComponent text = new TextComponent(String.format(format, Math.max(0.0F, remaining), pool))
                .withStyle(ChatFormatting.WHITE);
        poseStack.pushPose();
        float scale = 0.85F;
        poseStack.scale(scale, scale, scale);
        float x = -(font.width(text) / 2.0F);
        float y = 13.5F / scale - 9.0F / 2.0F;
        font.drawShadow(poseStack, text, x, y, 0xFFFFFF);
        poseStack.popPose();
    }

    private static void drawMultiplier(Font font, PoseStack poseStack) {
        MutableComponent text = new TextComponent(String.format("x%.2f", ClientChampionHud.getDamageMultiplier()))
                .withStyle(ChatFormatting.RED);
        poseStack.pushPose();
        float scale = 0.75F;
        poseStack.scale(scale, scale, scale);
        font.drawShadow(poseStack, text, 86.0F / scale, 9.0F / scale, 0xFF5555);
        poseStack.popPose();
    }
}
