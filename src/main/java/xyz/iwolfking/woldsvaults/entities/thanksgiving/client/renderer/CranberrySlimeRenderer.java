package xyz.iwolfking.woldsvaults.entities.thanksgiving.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.SlimeOuterLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Slime;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class CranberrySlimeRenderer extends MobRenderer<Slime, SlimeModel<Slime>> {
    public CranberrySlimeRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new SlimeModel<>(pContext.bakeLayer(ModelLayers.SLIME)), 0.25F);
        this.addLayer(new SlimeOuterLayer<>(this, pContext.getModelSet()));
    }

    public void render(Slime pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
        this.shadowRadius = 0.25F * (float)pEntity.getSize();
        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }

    protected void scale(Slime pLivingEntity, PoseStack pMatrixStack, float pPartialTickTime) {
        float f = 0.999F;
        pMatrixStack.scale(0.999F, 0.999F, 0.999F);
        pMatrixStack.translate(0.0D, (double)0.001F, 0.0D);
        float f1 = (float)pLivingEntity.getSize();
        float f2 = Mth.lerp(pPartialTickTime, pLivingEntity.oSquish, pLivingEntity.squish) / (f1 * 0.5F + 1.0F);
        float f3 = 1.0F / (f2 + 1.0F);
        pMatrixStack.scale(f3 * f1, 1.0F / f3 * f1, f3 * f1);
    }

    @Override
    public ResourceLocation getTextureLocation(Slime pEntity) {
        return WoldsVaults.id("textures/entity/cranberry_slime.png");
    }
}
