package xyz.iwolfking.woldsvaults.client.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import iskallia.vault.block.model.PylonCrystalModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import xyz.iwolfking.woldsvaults.blocks.tiles.BrewingAltarTileEntity;
import xyz.iwolfking.woldsvaults.init.ModItems;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;



public class BrewingAltarRenderer implements BlockEntityRenderer<BrewingAltarTileEntity> {
    private static final Minecraft mc = Minecraft.getInstance();

    public BrewingAltarRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BrewingAltarTileEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        NonNullList<ItemStack> items = pBlockEntity.getAllIngredients();





        renderFloatingItems(pPoseStack, pBufferSource, pPackedLight, pPackedOverlay, items.size(), items::get,
                i -> ((float) i / items.size()) * 2 * Math.PI + (Math.PI * (System.currentTimeMillis() / 3000D) % (2 * Math.PI)));
    }

    private static void renderFloatingItems(PoseStack poseStack, MultiBufferSource buffer, int pPackedLight, int pPackedOverlay, int count, IntFunction<ItemStack> getItem, IntFunction<Double> getAngle) {
        poseStack.pushPose();
        poseStack.translate(0.5, 1, 0.45);
        poseStack.scale(0.6F, 0.6F, 0.6F);

        for (int i = 0; i < count; i++) {
            if (i != 0) continue;
            ItemStack item = getItem.apply(i);
            if (item.isEmpty()) continue;
            poseStack.pushPose();
            double angle = 0.70;
            double z = Math.sin(angle);
            double x = Math.cos(angle);
            double hoverY = Math.sin((System.currentTimeMillis() / 200D) + i) * 0.1;
            poseStack.translate(x, 0.2 + hoverY, z);
            poseStack.mulPose(Vector3f.YN.rotation((float) (angle + Math.PI / 2f)));
            BakedModel ibakedmodel = mc.getItemRenderer().getModel(item, null, null, 0);
            mc.getItemRenderer().render(item, ItemTransforms.TransformType.GROUND, true, poseStack, buffer, 0xFFFFFF, pPackedOverlay, ibakedmodel);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    //            ItemStack item = getItem.apply(i);
//            if (item.isEmpty()) continue;
//
//            poseStack.pushPose();
//            double radius = .7; // how far from center the items should be
//            //double angle = getAngle.apply(i);
//            double angle = i == 0 ? 0.8 : i * 2.3;
//            double z = Math.sin(angle) * radius;
//            double x = Math.cos(angle) * radius;
//            double hoverY = Math.sin((System.currentTimeMillis() / 200D) + i) * 0.1;
//            //poseStack.translate(x, 0.2 + Math.sin(angle * 2) * 0.1, y);
//            poseStack.translate(x, 0.2 + hoverY, z);
//            poseStack.mulPose(Vector3f.YN.rotation((float) (angle + Math.PI / 2f)));
//            BakedModel ibakedmodel = mc.getItemRenderer().getModel(item, null, null, 0);
//            mc.getItemRenderer().render(item, ItemTransforms.TransformType.GROUND, true, poseStack, buffer, 0xFFFFFF, pPackedOverlay, ibakedmodel);
//            poseStack.popPose();

}
