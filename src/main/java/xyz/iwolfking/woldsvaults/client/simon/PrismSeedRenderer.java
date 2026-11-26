package xyz.iwolfking.woldsvaults.client.simon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import xyz.iwolfking.woldsvaults.entities.simon.PrismSeedEntity;
import xyz.iwolfking.woldsvaults.entities.simon.SimonCrystalEntity;

/** Draws a prism seed in flight as a tumbling, full-bright Seed-stage crystal in its colour. */
public class PrismSeedRenderer extends EntityRenderer<PrismSeedEntity> {
    public PrismSeedRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(PrismSeedEntity seed, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light) {
        float time = seed.tickCount + partialTicks;
        Vector3f tint = new Vector3f(1.0F, 1.0F, 1.0F);
        if (seed.isPrismatic()) {
            int rgb = Mth.hsvToRgb(time * 0.02F % 1.0F, 0.75F, 1.0F);
            tint = new Vector3f((rgb >> 16 & 255) / 255.0F, (rgb >> 8 & 255) / 255.0F, (rgb & 255) / 255.0F);
        }
        pose.pushPose();
        pose.translate(0.0, 0.1, 0.0);
        pose.mulPose(Vector3f.YP.rotationDegrees(time * 25.0F));
        pose.mulPose(Vector3f.XP.rotationDegrees(time * 17.0F));
        pose.translate(0.0, -0.1, 0.0);
        SimonCrystalRenderer.renderModel(pose, buffers, Sheets.translucentCullBlockSheet(), seed.getColor(), seed.isPrismatic(), tint,
                SimonCrystalEntity.SEED, 1.0F, LightTexture.FULL_BRIGHT);
        pose.popPose();
        super.render(seed, yaw, partialTicks, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(PrismSeedEntity seed) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
