package xyz.iwolfking.woldsvaults.client.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Vector3f;
import iskallia.vault.core.vault.modifier.VaultModifierStack;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.overlay.ModifiersRenderer;
import iskallia.vault.task.renderer.context.RendererContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import xyz.iwolfking.woldsvaults.blocks.tiles.FracturedObeliskTileEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FracturedObeliskRenderer implements BlockEntityRenderer<FracturedObeliskTileEntity> {
    private final Font font;

    public FracturedObeliskRenderer(BlockEntityRendererProvider.Context context) {
        font = context.getFont();
    }


    @Override
    public void render(FracturedObeliskTileEntity tileEntity, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlayIn) {
        RenderSystem.enableDepthTest();
        matrixStack.pushPose();
        float scale = 0.02F;
        matrixStack.translate(0.5, 3.25, 0.5);
        matrixStack.scale(scale, scale, scale);
        matrixStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
        matrixStack.translate(-65.0, -11.0, 0.0);
        RendererContext context = new RendererContext(matrixStack, partialTicks, MultiBufferSource.immediate(Tesselator.getInstance().getBuilder()), this.font);



        List<Component> lines = new ArrayList<>();
        List<Component> description = new ArrayList<>();
        List<VaultModifierStack> stack = new ArrayList<>();
//        if (tileEntity.isOverStacking() && state == MonolithBlock.State.EXTINGUISHED) {
//            lines.add((new TextComponent("Pillage for Loot")).withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
//        }


        tileEntity.getModifiers().forEach((id, count) -> {
            VaultModifierRegistry.getOpt(id).ifPresent((modifier) -> {
                String modifierName = modifier.getChatDisplayNameComponent(count).getString();
                modifierName = modifierName.replaceAll(":.*: *", "");
                Style style = Style.EMPTY.withColor(modifier.getDisplayTextColor());
                lines.add(new TextComponent(modifierName).withStyle(style));
                description.add((new TextComponent(modifier.getDisplayDescriptionFormatted(count))).withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
                stack.add(VaultModifierStack.of(modifier, count));
            });
        });
        Collections.reverse(lines);
        Collections.reverse(stack);

        for (Component line: description) {
            MutableComponent shadow = (new TextComponent("")).append(line.getString()).withStyle(Style.EMPTY.withColor(ChatFormatting.BLACK));
            context.renderText(shadow, 66.0F, 68.0F, true, true);
            context.renderText(line, 65.0F, 67.0F, true, true);
            context.translate(0.0, -11.0, 0.0);
        }


        for (Component line: lines) {
            MutableComponent shadow = (new TextComponent("")).append(line.getString()).withStyle(Style.EMPTY.withColor(ChatFormatting.BLACK));
            context.renderText(shadow, 66.0F, 66.0F, true, true);
            context.renderText(line, 65.0F, 65.0F, true, true);
            context.translate(0.0, -11.0, 0.0);
        }

        Minecraft minecraft = Minecraft.getInstance();
        double xTranslation = stack.size() > 1 ? 82.5 + (stack.size() * 5) : 82.5;
        matrixStack.translate(xTranslation, 73.0, 0.0);
        matrixStack.pushPose();
        int right = minecraft.getWindow().getGuiScaledWidth();
        int bottom = minecraft.getWindow().getGuiScaledHeight();
        matrixStack.translate(-right, -bottom, 0.0);
        ModifiersRenderer.renderVaultModifiersWithDepth(stack, matrixStack);
        matrixStack.popPose();
        matrixStack.popPose();
    }
}