package xyz.iwolfking.woldsvaults.client.renderers.station;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;
import xyz.iwolfking.woldsvaults.blocks.ExtractorControllerBlock;
import xyz.iwolfking.woldsvaults.blocks.multiblock.ExtractorMultiblock;
import xyz.iwolfking.woldsvaults.blocks.tiles.ExtractorAssemblerTileEntity;
import xyz.iwolfking.woldsvaults.blocks.tiles.ExtractorForgeTileEntity;
import xyz.iwolfking.woldsvaults.blocks.tiles.ExtractorStationTile;
import xyz.iwolfking.woldsvaults.items.ExtractorItem;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * Draws the moving, translucent and full-bright pieces of a station on top of its baked shell,
 * plus the real extractor rig inside the Assembler's vitrine. Geometry and curves come from
 * {@link ExtractorStationRig}; this class only composes transforms.
 */
public class ExtractorStationRenderer<T extends BlockEntity & ExtractorStationTile> implements BlockEntityRenderer<T> {
    private static final float PX = 1.0F / 16.0F;
    private static final float PREVIEW_SCALE = 0.30F;

    private final ExtractorStationRig rig;
    @Nullable
    private ExtractorPreviewRenderer preview;

    public ExtractorStationRenderer(BlockEntityRendererProvider.Context context, ExtractorStationRig rig) {
        this.rig = rig;
    }

    /**
     * Renderer provider for the Extractor Assembler. Registered as a method reference: a lambda in
     * {@code ModBlocks} makes the verifier load client renderer classes on a dedicated server.
     */
    public static ExtractorStationRenderer<ExtractorAssemblerTileEntity> assembler(BlockEntityRendererProvider.Context context) {
        return new ExtractorStationRenderer<>(context, ExtractorStationRig.ASSEMBLER);
    }

    /** Renderer provider for the Extractor Forge; see {@link #assembler}. */
    public static ExtractorStationRenderer<ExtractorForgeTileEntity> forge(BlockEntityRendererProvider.Context context) {
        return new ExtractorStationRenderer<>(context, ExtractorStationRig.FORGE);
    }

    @Override
    public void render(T tile, float partialTick, PoseStack pose, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        BlockState state = tile.getBlockState();
        if (!(state.getBlock() instanceof ExtractorControllerBlock)) {
            return;
        }
        boolean working = tile.isWorking();
        float t = working ? (float) (((tile.getLevel() == null ? 0L : tile.getLevel().getGameTime()) % this.rig.cycleTicks + partialTick) / this.rig.cycleTicks) : 0.0F;
        Direction facing = state.getValue(ExtractorControllerBlock.FACING);

        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        pose.mulPose(Vector3f.YP.rotationDegrees(-ExtractorMultiblock.blockstateYaw(facing)));
        pose.translate(-0.5D, 0.0D, -0.5D);

        for (ExtractorStationRig.Part part : this.rig.parts) {
            if (part.translucent()) {
                continue;
            }
            this.renderPart(part, t, state, pose, buffers, part.fullbright() ? LightTexture.FULL_BRIGHT : packedLight, packedOverlay, RenderType.cutout());
        }
        if (this.rig.showsExtractorPreview) {
            this.renderPreview(tile, t, working, partialTick, pose, buffers, packedLight);
        }
        for (ExtractorStationRig.Part part : this.rig.parts) {
            if (part.translucent()) {
                this.renderPart(part, t, state, pose, buffers, part.fullbright() ? LightTexture.FULL_BRIGHT : packedLight, packedOverlay, RenderType.translucent());
            }
        }
        pose.popPose();
    }

    private void renderPart(ExtractorStationRig.Part part, float t, BlockState state, PoseStack pose, MultiBufferSource buffers, int light, int overlay, RenderType renderType) {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(part.model());
        if (model == null || model == Minecraft.getInstance().getModelManager().getMissingModel()) {
            WoldLogger.errorOnce("station_model." + part.model(), "Station renderer model {} is missing or unbaked; that piece will not draw.", part.model());
            return;
        }
        pose.pushPose();
        this.applyBoneChain(part.name(), t, pose);
        pose.translate(part.ox() * PX, part.oy() * PX, part.oz() * PX);
        VertexConsumer buffer = buffers.getBuffer(renderType);
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(pose.last(), buffer, state, model, 1.0F, 1.0F, 1.0F, light, overlay, EmptyModelData.INSTANCE);
        pose.popPose();
    }

    /** Applies the bone's ancestors outermost-first, then the bone itself: p' = Parent(R·(p − pivot) + pivot + translate). */
    private void applyBoneChain(String boneName, float t, PoseStack pose) {
        Deque<ExtractorStationRig.Bone> chain = new ArrayDeque<>();
        ExtractorStationRig.Bone bone = this.rig.bones.get(boneName);
        while (bone != null) {
            chain.push(bone);
            bone = bone.parent() == null ? null : this.rig.bones.get(bone.parent());
        }
        while (!chain.isEmpty()) {
            ExtractorStationRig.Bone current = chain.pop();
            ExtractorStationRig.Pose p = this.rig.poser.pose(current.name(), t);
            pose.translate((current.px() + p.tx()) * PX, (current.py() + p.ty()) * PX, (current.pz() + p.tz()) * PX);
            if (p.ry() != 0.0F) {
                pose.mulPose(Vector3f.YP.rotationDegrees(p.ry()));
            }
            if (p.rx() != 0.0F) {
                pose.mulPose(Vector3f.XP.rotationDegrees(p.rx()));
            }
            if (p.rz() != 0.0F) {
                pose.mulPose(Vector3f.ZP.rotationDegrees(p.rz()));
            }
            pose.translate(-current.px() * PX, -current.py() * PX, -current.pz() * PX);
        }
    }

    /** The displayed extractor at vitrine scale, centred on machine-local (24, 15, 24), spinning while the cycle runs. */
    private void renderPreview(T tile, float t, boolean working, float partialTick, PoseStack pose, MultiBufferSource buffers, int packedLight) {
        ItemStack shown = tile.getDisplayExtractor();
        if (shown.isEmpty() || tile.getLevel() == null) {
            return;
        }
        if (this.preview == null) {
            this.preview = new ExtractorPreviewRenderer();
        }
        pose.pushPose();
        pose.translate(8.0F * PX, 15.0F * PX, 8.0F * PX);
        pose.mulPose(Vector3f.YP.rotationDegrees(working ? 360.0F * t : 0.0F));
        pose.scale(PREVIEW_SCALE, PREVIEW_SCALE, PREVIEW_SCALE);
        this.preview.renderInWorld(ExtractorItem.getVariants(shown), pose, buffers, packedLight, partialTick);
        pose.popPose();
    }
}
