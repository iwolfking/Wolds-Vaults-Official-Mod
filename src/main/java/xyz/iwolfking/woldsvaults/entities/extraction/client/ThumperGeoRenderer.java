package xyz.iwolfking.woldsvaults.entities.extraction.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.entities.extraction.ThumperEntity;

public class ThumperGeoRenderer extends GeoEntityRenderer<ThumperEntity> {
    public ThumperGeoRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Model());
        this.shadowRadius = 0.6F;
    }

    private static final class Model extends AnimatedGeoModel<ThumperEntity> {
        @Override
        public ResourceLocation getModelLocation(ThumperEntity entity) {
            return WoldsVaults.id("geo/thumper.geo.json");
        }

        @Override
        public ResourceLocation getTextureLocation(ThumperEntity entity) {
            return WoldsVaults.id("textures/entity/thumper.png");
        }

        @Override
        public ResourceLocation getAnimationFileLocation(ThumperEntity entity) {
            return WoldsVaults.id("animations/thumper.animation.json");
        }
    }
}
