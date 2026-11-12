package xyz.iwolfking.woldsvaults.entities.extraction.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;
import xyz.iwolfking.woldsvaults.entities.extraction.ExtractorEntity;

/**
 * Draws the rig again through {@code RenderType.eyes} using a mask sheet, so the glowing
 * texels are full-bright rather than lit by the room.
 *
 * <p>Two of these are installed. The <b>steady</b> sheet holds every emissive that is
 * simply "on" and is drawn at full strength. The <b>pulse</b> sheet holds the ones that
 * breathe — radiator coolant, vent slits, the scanner lamp — and is drawn at a brightness
 * that is nearly dark while the rig idles and swings on a 0.9 s cycle while it drills.
 * That contrast is what makes a running extractor read as running.
 *
 * <p>Splitting them by SHEET rather than by bone is deliberate: {@code eyes} takes one
 * colour per draw, so per-bone brightness would mean toggling bone visibility between
 * passes. Two mostly-transparent masks cost two cheap draws and no bookkeeping.
 */
public class ExtractorEmissiveLayer extends GeoLayerRenderer<ExtractorEntity> {
    private final ResourceLocation texture;
    private final boolean pulsing;

    public ExtractorEmissiveLayer(IGeoRenderer<ExtractorEntity> renderer, ResourceLocation texture, boolean pulsing) {
        super(renderer);
        this.texture = texture;
        this.pulsing = pulsing;
    }

    @Override
    public RenderType getRenderType(ResourceLocation textureLocation) {
        return RenderType.eyes(textureLocation);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       ExtractorEntity entity, float limbSwing, float limbSwingAmount, float partialTicks,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        float brightness = 1.0F;
        if (this.pulsing) {
            float seconds = (entity.tickCount + partialTicks) / 20.0F;
            brightness = ExtractorMotion.pulseBrightness(seconds, entity.getDrillRamp());
        }
        this.renderCopyModel(this.getEntityModel(), this.texture, poseStack, bufferSource,
                packedLight, entity, partialTicks, brightness, brightness, brightness);
    }
}
