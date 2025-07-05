package xyz.iwolfking.woldsvaults.client.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import iskallia.vault.block.model.PylonCrystalModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import xyz.iwolfking.woldsvaults.blocks.tiles.BrewingAltarTileEntity;
import xyz.iwolfking.woldsvaults.init.ModItems;

import java.util.List;
import java.util.function.IntFunction;



public class BrewingAltarRenderer implements BlockEntityRenderer<BrewingAltarTileEntity> {
    private static final Minecraft mc = Minecraft.getInstance();

    public BrewingAltarRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BrewingAltarTileEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        NonNullList<ItemStack> items = pBlockEntity.getAllIngredients();


        renderText(pBlockEntity, pPoseStack, pBufferSource);


        renderFloatingItems(pPoseStack, pBufferSource, pPackedLight, pPackedOverlay, items.size(), items::get,
                i -> ((float) i / items.size()) * 2 * Math.PI + (Math.PI * (System.currentTimeMillis() / 3000D) % (2 * Math.PI)));
    }

    private static void renderFloatingItems(PoseStack poseStack, MultiBufferSource buffer, int pPackedLight, int pPackedOverlay, int count, IntFunction<ItemStack> getItem, IntFunction<Double> getAngle) {
        poseStack.pushPose();
        poseStack.translate(0.5, 1.2, 0.5);
        poseStack.scale(1.2F, 1.2F, 1.2F);

        for (int i = 0; i < count; i++) {
            ItemStack item = getItem.apply(i);
            if (item.isEmpty()) continue;

            poseStack.pushPose();
            double angle = getAngle.apply(i);
            double y = Math.sin(angle) * 1.2;
            double x = Math.cos(angle) * 1.2;
            poseStack.translate(x, 0.2 + Math.sin(angle * 2) * 0.1, y);
            poseStack.mulPose(Vector3f.YN.rotation((float) (angle + Math.PI / 2f)));
            BakedModel ibakedmodel = mc.getItemRenderer().getModel(item, null, null, 0);
            mc.getItemRenderer().render(item, ItemTransforms.TransformType.GROUND, true, poseStack, buffer, 0xFFFFFF, pPackedOverlay, ibakedmodel);
            poseStack.popPose();

//            if (i != 0) continue;
//            ItemStack item = getItem.apply(i);
//            if (item.isEmpty()) continue;
//            poseStack.pushPose();
//            double angle = 0.70;
//            double z = Math.sin(angle);
//            double x = Math.cos(angle);
//            double hoverY = Math.sin((System.currentTimeMillis() / 200D) + i) * 0.1;
//            poseStack.translate(x, 0.2 + hoverY, z);
//            poseStack.mulPose(Vector3f.YN.rotation((float) (angle + Math.PI / 2f)));
//            BakedModel ibakedmodel = mc.getItemRenderer().getModel(item, null, null, 0);
//            mc.getItemRenderer().render(item, ItemTransforms.TransformType.GROUND, true, poseStack, buffer, 0xFFFFFF, pPackedOverlay, ibakedmodel);
//            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void renderText(BrewingAltarTileEntity tileEntity, PoseStack poseStack, MultiBufferSource bufferSource) {
        List<MutableComponent> lines = tileEntity.getLines();
        int length = lines.size();
        Minecraft minecraft = Minecraft.getInstance();
        Font fontRenderer = minecraft.font;

        for(int i = length - 1; i >= 0; --i) {
            MutableComponent text = lines.get(i);
            if (text != null) {
                float scale = 0.02F;
                int color = -1;
                int opacity = 1711276032;
                int lightLevel = 1;
                poseStack.pushPose();
                Matrix4f matrix4f = poseStack.last().pose();
                float offset = (float)(-fontRenderer.width(text) / 2);
                poseStack.translate(0.5F, 1.7F + 0.25F * (float)(length - i), 0.5F);
                poseStack.scale(scale, scale, scale);
                poseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
                fontRenderer.drawInBatch(text, offset, 0.0F, color, false, matrix4f, bufferSource, true, opacity, lightLevel);
                fontRenderer.drawInBatch(text, offset, 0.0F, -1, false, matrix4f, bufferSource, false, 0, lightLevel);
                poseStack.popPose();
            }
        }
    }



}
