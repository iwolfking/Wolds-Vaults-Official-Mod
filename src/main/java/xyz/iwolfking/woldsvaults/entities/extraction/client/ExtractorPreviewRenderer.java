package xyz.iwolfking.woldsvaults.entities.extraction.client;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.core.util.Color;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import xyz.iwolfking.woldsvaults.entities.extraction.ExtractorEntity;
import xyz.iwolfking.woldsvaults.init.ModEntities;

import javax.annotation.Nullable;
import java.util.List;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * Draws the real extractor rig inside a screen. The rig is rendered up to three times per frame: the parts that
 * are actually installed at full strength, the slots that are empty as a translucent grey wooden silhouette, and
 * blueprint-previewed parts as a translucent blue ghost. Each pass sets the entity's variant list and a tint, so
 * the picture follows whatever is in the assembler's slots with no extra art.
 */
public class ExtractorPreviewRenderer extends ExtractorGeoRenderer {
    public enum Mode {
        SOLID,
        GHOST,
        BLUEPRINT
    }

    private static final Color GHOST_TINT = Color.ofRGBA(150, 165, 185, 115);
    private static final Color BLUEPRINT_TINT = Color.ofRGBA(143, 211, 255, 170);
    private static boolean warnedNoEntity;

    private Mode mode = Mode.SOLID;
    @Nullable
    private ExtractorEntity entity;

    public ExtractorPreviewRenderer() {
        super(new EntityRendererProvider.Context(Minecraft.getInstance().getEntityRenderDispatcher(), Minecraft.getInstance().getItemRenderer(),
                Minecraft.getInstance().getResourceManager(), Minecraft.getInstance().getEntityModels(), Minecraft.getInstance().font));
        this.shadowRadius = 0.0F;
    }

    @Override
    public RenderType getRenderType(ExtractorEntity animatable, float partialTicks, PoseStack stack, MultiBufferSource renderTypeBuffer,
                                    VertexConsumer vertexBuilder, int packedLightIn, ResourceLocation textureLocation) {
        if (this.mode == Mode.SOLID) {
            return super.getRenderType(animatable, partialTicks, stack, renderTypeBuffer, vertexBuilder, packedLightIn, textureLocation);
        }
        return RenderType.entityTranslucent(textureLocation);
    }

    @Override
    public Color getRenderColor(ExtractorEntity animatable, float partialTicks, PoseStack stack, MultiBufferSource renderTypeBuffer,
                                VertexConsumer vertexBuilder, int packedLightIn) {
        return switch (this.mode) {
            case GHOST -> GHOST_TINT;
            case BLUEPRINT -> BLUEPRINT_TINT;
            default -> Color.WHITE;
        };
    }

    @Override
    protected void renderLayer(PoseStack stack, MultiBufferSource bufferIn, int packedLightIn, ExtractorEntity entity, float limbSwing,
                               float limbSwingAmount, float partialTicks, float rotFloat, float netHeadYaw, float headPitch,
                               MultiBufferSource bufferIn2, GeoLayerRenderer<ExtractorEntity> layer) {
        if (this.mode == Mode.SOLID) {
            super.renderLayer(stack, bufferIn, packedLightIn, entity, limbSwing, limbSwingAmount, partialTicks, rotFloat, netHeadYaw, headPitch, bufferIn2, layer);
        }
    }

    @Nullable
    private ExtractorEntity entity() {
        Minecraft mc = Minecraft.getInstance();
        if (this.entity == null && mc.level != null) {
            this.entity = ModEntities.EXTRACTOR.create(mc.level);
            if (this.entity == null && !warnedNoEntity) {
                warnedNoEntity = true;
                WoldLogger.error("Could not create a display extractor for the Assembler screen; the diagram stays empty.");
            }
        }
        return this.entity;
    }

    /**
     * Draws the rig centred on screen point (x, y) at {@code pixelsPerBlock} scale, turned {@code yaw} degrees and
     * tipped {@code pitch} degrees. Each non-empty bone list is one pass in its own mode.
     */
    public void draw(int x, int y, float pixelsPerBlock, float yaw, float pitch, List<String> solidBones, List<String> ghostBones, List<String> blueprintBones) {
        ExtractorEntity target = this.entity();
        if (target == null) {
            return;
        }
        target.tickCount++;
        PoseStack modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        modelView.translate(x, y, 1050.0D);
        modelView.scale(1.0F, 1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();
        PoseStack pose = new PoseStack();
        pose.translate(0.0D, 0.0D, 1000.0D);
        pose.scale(pixelsPerBlock, pixelsPerBlock, pixelsPerBlock);
        pose.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
        pose.mulPose(Vector3f.XP.rotationDegrees(pitch));
        pose.mulPose(Vector3f.YP.rotationDegrees(yaw));
        pose.translate(0.0D, -2.0D, 0.0D);
        Lighting.setupForEntityInInventory();
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> {
            this.pass(Mode.SOLID, solidBones, target, pose, buffers);
            this.pass(Mode.GHOST, ghostBones, target, pose, buffers);
            this.pass(Mode.BLUEPRINT, blueprintBones, target, pose, buffers);
        });
        buffers.endBatch();
        modelView.popPose();
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
    }

    private void pass(Mode passMode, List<String> bones, ExtractorEntity target, PoseStack pose, MultiBufferSource.BufferSource buffers) {
        if (bones.isEmpty()) {
            return;
        }
        this.mode = passMode;
        target.setPreviewVariants(String.join(",", bones));
        this.render(target, 0.0F, Minecraft.getInstance().getFrameTime(), pose, buffers, LightTexture.FULL_BRIGHT);
        buffers.endBatch();
        this.mode = Mode.SOLID;
    }
}
