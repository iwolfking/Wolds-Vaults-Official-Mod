package xyz.iwolfking.woldsvaults.client.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Vector3f;
import iskallia.vault.task.renderer.context.RendererContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.chat.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import xyz.iwolfking.woldsvaults.blocks.tiles.FracturedObeliskTileEntity;
import xyz.iwolfking.woldsvaults.util.ComponentUtils;

import java.util.ArrayList;
import java.util.List;

public class FracturedObeliskRenderer implements BlockEntityRenderer<FracturedObeliskTileEntity> {
    private final Font font;

    public FracturedObeliskRenderer(BlockEntityRendererProvider.Context context) {
        font = context.getFont();
    }

    @Override
    public void render(@NotNull FracturedObeliskTileEntity tileEntity, float partialTicks, @NotNull PoseStack matrixStack, @NotNull MultiBufferSource buffer, int combinedLight, int combinedOverlayIn) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        RenderSystem.enableDepthTest();
        matrixStack.pushPose();

        // Set up base transform
        matrixStack.translate(0.5, 3.25, 0.5);
        matrixStack.scale(0.02F, 0.02F, 0.02F);
        matrixStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
        matrixStack.translate(-65.0, -11.0, 0.0);

        RendererContext context = new RendererContext(matrixStack, partialTicks, MultiBufferSource.immediate(Tesselator.getInstance().getBuilder()), this.font);

        float alpha = getAlpha(tileEntity, player);

        // Skip rendering if alpha is too low to avoid flickering
        if (alpha < 0.1F) {
            matrixStack.popPose();
            return;
        }

        int alphaByte = (int)(alpha * 255);
        int textColor = (alphaByte << 24) | 0xFFFFFF;
        int shadowAlphaByte = (int)(alpha * 0.5F * 255); // 50% opacity
        int shadowColor = (shadowAlphaByte << 24);

        // Prepare text lines with obfuscation
        List<MutableComponent> lines = new ArrayList<>();
        float obf = Math.max(0.0F, Math.min(1.0F, tileEntity.getPercentObfuscated()));

        if (tileEntity.getInitialCompletion()) {
            lines.add(ComponentUtils.partiallyObfuscate(new TextComponent("Echoes surge from the wound, but find no resolve.").withStyle(ChatFormatting.DARK_RED), obf));
            lines.add(ComponentUtils.partiallyObfuscate(new TextComponent("This is not ascent. It is collapse.").withStyle(ChatFormatting.DARK_RED), obf));
        } else {
            lines.add(ComponentUtils.partiallyObfuscate(new TextComponent("Shattered purpose, remade in ruin;").withStyle(ChatFormatting.DARK_RED), obf));
            lines.add(ComponentUtils.partiallyObfuscate(new TextComponent("Lingering with dread, yet no way to escape").withStyle(ChatFormatting.DARK_RED), obf));
            lines.add(ComponentUtils.partiallyObfuscate(new TextComponent("Beneath, the world forgets what it buried.").withStyle(ChatFormatting.DARK_RED), obf));
        }

        // Render each line
        for (Component line : lines) {
            context.renderText(line.copy().withStyle(Style.EMPTY.withColor(ChatFormatting.BLACK)), 66.0F, 68.0F, true, true, shadowColor, false);
            context.renderText(line, 65.0F, 67.0F, true, true, textColor, false);
            context.translate(0.0, -11.0, 0.0);
        }

        matrixStack.popPose();
    }

    private static float getAlpha(@NotNull FracturedObeliskTileEntity tileEntity, Player player) {
        double dx = player.getX() - tileEntity.getBlockPos().getX();
        double dy = (player.getY() + player.getEyeHeight()) - (tileEntity.getBlockPos().getY() + 1.5);
        double dz = player.getZ() - tileEntity.getBlockPos().getZ();
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        float fadeStart = 10.0F;
        float fadeEnd = 25.0F;
        float alpha = 1.0F;

        if (distance > fadeStart) {
            alpha = 1.0F - ((float)(distance - fadeStart) / (fadeEnd - fadeStart));
        }

        alpha = Mth.clamp(alpha, 0.0F, 1.0F);
        return alpha;
    }

}