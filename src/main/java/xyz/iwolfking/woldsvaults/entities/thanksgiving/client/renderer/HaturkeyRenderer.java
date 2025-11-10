package xyz.iwolfking.woldsvaults.entities.thanksgiving.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import xyz.iwolfking.woldsvaults.entities.thanksgiving.HaturkeyEntity;
import xyz.iwolfking.woldsvaults.models.entity.HaturkeyEntityModel;

public class HaturkeyRenderer extends GeoEntityRenderer<HaturkeyEntity> {
    public HaturkeyRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new HaturkeyEntityModel());
        this.shadowRadius = 0.6F;
    }

    @Override
    public ResourceLocation getTextureLocation(HaturkeyEntity instance) {
        return super.getTextureLocation(instance);
    }
}
