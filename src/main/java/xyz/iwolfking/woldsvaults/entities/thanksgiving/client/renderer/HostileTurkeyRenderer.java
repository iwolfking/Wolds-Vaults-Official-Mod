package xyz.iwolfking.woldsvaults.entities.thanksgiving.client.renderer;

import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Chicken;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class HostileTurkeyRenderer extends MobRenderer<Chicken, ChickenModel<Chicken>> {
    private static final ResourceLocation TURKEY_LOCATION = WoldsVaults.id("textures/entity/hostile_turkey.png");

    public HostileTurkeyRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new ChickenModel<>(pContext.bakeLayer(ModelLayers.CHICKEN)), 0.3F);
    }

    /**
     * Returns the location of an entity's texture.
     */
    public ResourceLocation getTextureLocation(Chicken pEntity) {
        return TURKEY_LOCATION;
    }

    /**
     * Defines what float the third param in setRotationAngles of ModelBase is
     */
    protected float getBob(Chicken pLivingBase, float pPartialTicks) {
        float f = Mth.lerp(pPartialTicks, pLivingBase.oFlap, pLivingBase.flap);
        float f1 = Mth.lerp(pPartialTicks, pLivingBase.oFlapSpeed, pLivingBase.flapSpeed);
        return (Mth.sin(f) + 1.0F) * f1;
    }
}
