package xyz.iwolfking.woldsvaults.client.lightning;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.Random;

/**
 * Speed lines over the dashing player's own view (Lightning Fiend 6-piece): thin streaks rushing out to the screen's
 * edges from the point they're dashing towards, jumping to new spots each tick. A dash is over in a moment, so the
 * lines hold through it and then fade out over {@link #FADE} ticks. Also the white-blue flash over the whole screen
 * of a viewer standing inside a Thunderstrike's bolt, fading over {@link #FLASH} ticks. Drawn under the HUD.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class LightningSpeedLines {
    private static final int FADE = 8;
    private static final int LINES = 48;
    private static final int FLASH = 10;

    private static int holdTicks;
    private static int age = -1;
    private static Vec3 heading = Vec3.ZERO;
    private static int flashAge = -1;

    private LightningSpeedLines() {
    }

    /** Starts the lines for a dash of the given length along the given way (any length; only its direction counts). */
    public static void start(int dashTicks, Vec3 travel) {
        holdTicks = dashTicks;
        heading = travel;
        age = 0;
    }

    public static void flash() {
        flashAge = 0;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (age >= 0 && ++age > holdTicks + FADE) {
            age = -1;
        }
        if (flashAge >= 0 && ++flashAge > FLASH) {
            flashAge = -1;
        }
    }

    @SubscribeEvent
    public static void onOverlay(RenderGameOverlayEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL || mc.player == null) {
            return;
        }
        if (flashAge >= 0) {
            float flash = 1.0F - (flashAge + event.getPartialTicks()) / FLASH;
            if (flash > 0.0F) {
                int alpha = (int) (flash * flash * 220.0F);
                GuiComponent.fill(event.getMatrixStack(), 0, 0, event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight(), alpha << 24 | 0xDDF4FF);
            }
        }
        if (age < 0) {
            return;
        }
        float time = age + event.getPartialTicks();
        float strength = time <= holdTicks ? 1.0F : 1.0F - (time - holdTicks) / FADE;
        if (strength <= 0.0F) {
            return;
        }
        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();
        double yaw = Math.toRadians(mc.gameRenderer.getMainCamera().getYRot());
        Vec3 right = new Vec3(-Math.cos(yaw), 0.0, -Math.sin(yaw));
        Vec3 way = heading.lengthSqr() < 1.0E-6 ? Vec3.ZERO : heading.normalize();
        float cx = width * 0.5F + (float) way.dot(right) * width * 0.3F;
        float cy = height * 0.5F - (float) way.y * height * 0.3F;
        float reach = (float) Math.hypot(width, height);
        float near = Math.min(width, height);

        Random random = new Random(age * 7919L + 13L);
        Matrix4f matrix = event.getMatrixStack().last().pose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i < LINES; i++) {
            double angle = (i + random.nextDouble()) * Math.PI * 2.0 / LINES;
            float dx = (float) Math.cos(angle), dy = (float) Math.sin(angle);
            float tx = dx > 1.0E-4F ? (width - cx) / dx : dx < -1.0E-4F ? -cx / dx : reach;
            float ty = dy > 1.0E-4F ? (height - cy) / dy : dy < -1.0E-4F ? -cy / dy : reach;
            float edge = Math.min(tx, ty) + 4.0F;
            float inner = Math.min(near * (0.28F + random.nextFloat() * 0.18F), edge * 0.7F);
            float halfWidth = 0.6F + random.nextFloat() * 1.4F;
            float alpha = strength * (0.35F + random.nextFloat() * 0.45F);
            buffer.vertex(matrix, cx + dx * inner, cy + dy * inner, 0.0F).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
            buffer.vertex(matrix, cx + dx * edge - dy * halfWidth, cy + dy * edge + dx * halfWidth, 0.0F).color(0.85F, 0.93F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, cx + dx * edge + dy * halfWidth, cy + dy * edge - dx * halfWidth, 0.0F).color(0.85F, 0.93F, 1.0F, alpha).endVertex();
        }
        Tesselator.getInstance().end();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
}
