package xyz.iwolfking.woldsvaults.models.entity;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.entities.thanksgiving.HaturkeyEntity;

public class HaturkeyEntityModel extends AnimatedGeoModel<HaturkeyEntity> {
    @Override
    public ResourceLocation getModelLocation(HaturkeyEntity haturkeyEntity) {
        return WoldsVaults.id("geo/haturkey.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(HaturkeyEntity haturkeyEntity) {
        return WoldsVaults.id("textures/entity/haturkey.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(HaturkeyEntity haturkeyEntity) {
        return WoldsVaults.id("animations/haturkey.animation.json");
    }
}
