package xyz.iwolfking.woldsvaults.entities.extraction.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.entities.extraction.ExtractorEntity;

public class ExtractorGeoRenderer extends GeoEntityRenderer<ExtractorEntity> {
    private static final Component FUEL_PROMPT = new TextComponent("Right-click with Power Cells to fuel the extractor").withStyle(ChatFormatting.YELLOW);
    private static final Component ARMED_PROMPT = new TextComponent("Right-click to insert greed cards, then press Start").withStyle(ChatFormatting.GOLD);
    private static final double PROMPT_RANGE_SQ = 12.0D * 12.0D;

    public ExtractorGeoRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ExtractorGeoModel());
        this.shadowRadius = 0.0F;
        this.addLayer(new ExtractorEmissiveLayer(this,
                WoldsVaults.id("textures/entity/extractor_emissive.png"), false));
        this.addLayer(new ExtractorEmissiveLayer(this,
                WoldsVaults.id("textures/entity/extractor_emissive_pulse.png"), true));
    }

    /**
     * A prompt floats over the extractor for players standing near it: the card prompt while a Greed Infuser waits to be
     * started, the fuel prompt while it works, and the glue left on board (with the pickup countdown) once a fluid job
     * is done. It is drawn here rather than stored as the entity's custom name so Jade and death messages keep calling
     * the machine "Extractor".
     */
    @Override
    public void render(ExtractorEntity entity, float entityYaw, float partialTicks, PoseStack stack, MultiBufferSource buffers, int packedLight) {
        super.render(entity, entityYaw, partialTicks, stack, buffers, packedLight);
        if (!entity.isAddedToWorld() || this.entityRenderDispatcher.distanceToSqr(entity) > PROMPT_RANGE_SQ) {
            return;
        }
        Component prompt = null;
        if (entity.isArmed()) {
            prompt = ARMED_PROMPT;
        } else if (!entity.isJobDone()) {
            prompt = FUEL_PROMPT;
        } else if (entity.getFluidAmount() > 0) {
            int lock = entity.getFluidLockSeconds();
            prompt = new TextComponent(String.format("Prismatic Glue: %,d mB - right-click with a fluid tank%s", entity.getFluidAmount(),
                    lock > 0 ? String.format(" (pickup in %ds)", lock) : " (pickup discards it)")).withStyle(ChatFormatting.LIGHT_PURPLE);
        }
        if (prompt != null) {
            this.renderNameTag(entity, prompt, stack, buffers, packedLight);
        }
    }
}
