package xyz.iwolfking.woldsvaults.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import iskallia.vault.VaultMod;
import iskallia.vault.block.model.VaultChestModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import xyz.iwolfking.woldsvaults.blocks.ExtractionCacheBlock;
import xyz.iwolfking.woldsvaults.blocks.tiles.ExtractionCacheTileEntity;

import javax.annotation.Nonnull;

public class ExtractionCacheRenderer implements BlockEntityRenderer<ExtractionCacheTileEntity> {
    private static final Material FLESH_CHEST_MATERIAL = new Material(Sheets.CHEST_SHEET, VaultMod.id("entity/chest/vault_flesh_chest"));

    private final VaultChestModel model;

    public ExtractionCacheRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new VaultChestModel(context.bakeLayer(VaultChestModel.FLESH_LOCATION));
    }

    @Override
    public void render(@Nonnull ExtractionCacheTileEntity tile, float partialTicks, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        BlockState state = tile.getBlockState();
        Direction facing = state.hasProperty(ExtractionCacheBlock.FACING) ? state.getValue(ExtractionCacheBlock.FACING) : Direction.SOUTH;
        this.model.setLidAngle(0.0F);
        this.model.setOpened(false);
        VertexConsumer consumer = FLESH_CHEST_MATERIAL.buffer(buffer, RenderType::entityCutout);
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-facing.toYRot()));
        poseStack.translate(-0.5, -0.5, -0.5);
        this.model.renderToBuffer(poseStack, consumer, combinedLight, combinedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }
}
