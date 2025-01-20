package xyz.iwolfking.woldsvaults.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import xyz.iwolfking.woldsvaults.blocks.MonolithControllerBlock;
import xyz.iwolfking.woldsvaults.blocks.models.MonolithControllerModel;
import xyz.iwolfking.woldsvaults.blocks.tiles.MonolithControllerTileEntity;

public class MonolithControllerRenderer implements BlockEntityRenderer<MonolithControllerTileEntity> {
    private final MonolithControllerModel controllerModel;

    public MonolithControllerRenderer(BlockEntityRendererProvider.Context context) {
        this.controllerModel = new MonolithControllerModel(context.bakeLayer(MonolithControllerModel.MODEL_LOCATION));
    }

    @Override
    public void render(MonolithControllerTileEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        BlockState blockState = pBlockEntity.getBlockState();
        Direction facing = blockState.getValue(MonolithControllerBlock.FACING);
        boolean filled = blockState.getValue(MonolithControllerBlock.FILLED);
        Material material = filled ? MonolithControllerModel.FILLED_MATERIAL : MonolithControllerModel.MATERIAL;

        VertexConsumer vertexConsumer = material.buffer(pBufferSource, RenderType::entityTranslucent);
        pPoseStack.pushPose();
        pPoseStack.translate(0.5f, 1.5f, 0.5f);
        pPoseStack.mulPose(Vector3f.ZP.rotation(Mth.PI));
        pPoseStack.mulPose(Vector3f.YP.rotationDegrees(180 + facing.toYRot()));
        this.controllerModel.renderToBuffer(pPoseStack, vertexConsumer, pPackedLight, pPackedOverlay, 1, 1, 1, 1);
        pPoseStack.popPose();
    }
}
