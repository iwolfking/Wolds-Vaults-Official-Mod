package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Vector3f;
import iskallia.vault.block.MonolithBlock;
import iskallia.vault.block.entity.MonolithTileEntity;
import iskallia.vault.block.render.MonolithRenderer;
import iskallia.vault.core.vault.modifier.VaultModifierStack;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.overlay.ModifiersRenderer;
import iskallia.vault.task.renderer.context.RendererContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mixin(value = MonolithRenderer.class, remap = false)
public class MixinMonolithRenderer {
    @Shadow @Final private Font font;

    /**
     * @author iwolfking
     * @reason Yes this is terrible, no I don't care, hides emojis from monolith text rendering
     */
    @Overwrite
    public void render(MonolithTileEntity tileEntity, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlayIn) {
        RenderSystem.enableDepthTest();
        matrixStack.pushPose();
        float scale = 0.02F;
        matrixStack.translate(0.5, 2.5, 0.5);
        matrixStack.scale(scale, scale, scale);
        matrixStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
        matrixStack.translate(-65.0, -11.0, 0.0);
        RendererContext context = new RendererContext(matrixStack, partialTicks, MultiBufferSource.immediate(Tesselator.getInstance().getBuilder()), this.font);
        MonolithBlock.State state =  tileEntity.getBlockState().getValue(MonolithBlock.STATE);
        List<Component> lines = new ArrayList<>();
        List<Component> description = new ArrayList<>();
        List<VaultModifierStack> stack = new ArrayList<>();
        if (tileEntity.isOverStacking() && state == MonolithBlock.State.EXTINGUISHED) {
            lines.add((new TextComponent("Pillage for Loot")).withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
        }

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
        ModifiersRenderer.renderVaultModifiersWithDepth(stack, matrixStack, false);
        matrixStack.popPose();
        matrixStack.popPose();
    }
}
