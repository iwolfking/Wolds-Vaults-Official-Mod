package xyz.iwolfking.woldsvaults.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.NotNull;
import xyz.iwolfking.woldsvaults.blocks.tiles.ConfigurableFloatingTextTileEntity;

import java.util.List;

public class ConfigurableFloatingTextRenderer implements BlockEntityRenderer<ConfigurableFloatingTextTileEntity> {


    public ConfigurableFloatingTextRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(
            ConfigurableFloatingTextTileEntity tile,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        List<ConfigurableFloatingTextTileEntity.TextLine> lines = tile.getRenderLines();
        if (lines.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        poseStack.pushPose();
        poseStack.translate(0.5, 1.7, 0.5);
        poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(180));

        float baseScale = tile.getScale();
        poseStack.scale(baseScale, baseScale, baseScale);

        for (int i = lines.size() - 1; i >= 0; i--) {
            ConfigurableFloatingTextTileEntity.TextLine line = lines.get(i);

            MutableComponent text = new TextComponent(line.text)
                    .withStyle(s -> s
                            .withBold(line.bold)
                            .withItalic(line.italic)
                            .withUnderlined(line.underlined)
                            .withColor(line.color)
                    );

            float y = (lines.size() - i - 1) * 10;
            float x = -font.width(text) / 2f;

            font.drawInBatch(text, x, y, line.color, false,
                    poseStack.last().pose(), buffer, false, 0, light);
        }

        poseStack.popPose();
    }
}
