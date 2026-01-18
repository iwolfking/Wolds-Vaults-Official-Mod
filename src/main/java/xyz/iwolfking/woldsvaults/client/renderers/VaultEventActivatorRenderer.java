package xyz.iwolfking.woldsvaults.client.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import iskallia.vault.client.gui.helper.LightmapHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import xyz.iwolfking.woldsvaults.api.core.vault_events.VaultEvent;
import xyz.iwolfking.woldsvaults.blocks.tiles.VaultEventActivatorTileEntity;

import java.util.ArrayList;
import java.util.List;

public class VaultEventActivatorRenderer implements BlockEntityRenderer<VaultEventActivatorTileEntity> {

    private final Font font;
    private static final float TEXT_SCALE = 0.02f;
    private static final float LINE_HEIGHT = 11f;

    public VaultEventActivatorRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.getFont();
    }

    @Override
    public void render(VaultEventActivatorTileEntity tile, float partialTicks,
                       PoseStack matrixStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {

        VaultEvent event = tile.getEvent();
        if (event == null) return;

        List<Component> lines = new ArrayList<>();
        if (tile.isOnCooldown()) {
            lines.add(formatCooldown(tile.getCooldownTicks()));
        } else {
            lines.add(event.getEventName());
            lines.addAll(splitComponent(event.getEventDescriptor(), 180));
        }

        float totalHeight = lines.size() * LINE_HEIGHT;

        Minecraft mc = Minecraft.getInstance();

        matrixStack.pushPose();

        double floatOffset =
                Math.sin((tile.getLevel().getGameTime() + partialTicks) / 10.0) * 0.05;

        double baseHeight = 1.8D;
        double textWorldOffset = (totalHeight * TEXT_SCALE) / 2.0;

        matrixStack.translate(
                0.5D,
                baseHeight + floatOffset + textWorldOffset,
                0.5D
        );

        matrixStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
        matrixStack.scale(TEXT_SCALE, TEXT_SCALE, TEXT_SCALE);

        RenderSystem.disableDepthTest();

        float y = 0;
        for (Component line : lines) {
            renderTextWithShadow(matrixStack, line, y, buffer);
            y += LINE_HEIGHT;
        }

        RenderSystem.enableDepthTest();
        matrixStack.popPose();
    }


    private void renderTextWithShadow(PoseStack poseStack, Component line, float y, MultiBufferSource buffer) {
        font.drawInBatch(
                new TextComponent("").append(line.getString())
                        .withStyle(Style.EMPTY.withColor(net.minecraft.ChatFormatting.BLACK)),
                -font.width(line) / 2f + 1f,
                y + 1f,
                0x000000,
                false,
                poseStack.last().pose(),
                buffer,
                false,
                0,
                LightmapHelper.getPackedFullbrightCoords()
        );

        font.drawInBatch(
                line,
                -font.width(line) / 2f,
                y,
                0xFFFFFF,
                false,
                poseStack.last().pose(),
                buffer,
                false,
                0,
                LightmapHelper.getPackedFullbrightCoords()
        );
    }

    private Component formatCooldown(int ticks) {
        int seconds = Mth.ceil(ticks / 20f);
        return new TextComponent(String.format("Cooldown: %d:%02d", seconds / 60, seconds % 60))
                .withStyle(Style.EMPTY.withColor(net.minecraft.ChatFormatting.RED));
    }

    private List<Component> splitComponent(Component component, int maxWidth) {
        Font font = Minecraft.getInstance().font;
        List<Component> lines = new ArrayList<>();

        String text = component.getString();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String potential = currentLine.isEmpty() ? word : currentLine + " " + word;
            if (font.width(potential) > maxWidth) {
                lines.add(new TextComponent(currentLine.toString()));
                currentLine = new StringBuilder(word);
            } else {
                if (!currentLine.isEmpty()) currentLine.append(" ");
                currentLine.append(word);
            }
        }
        if (!currentLine.isEmpty()) {
            lines.add(new TextComponent(currentLine.toString()));
        }
        return lines;
    }
}
