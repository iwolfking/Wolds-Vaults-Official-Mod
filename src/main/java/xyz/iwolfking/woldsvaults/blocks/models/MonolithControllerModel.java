package xyz.iwolfking.woldsvaults.blocks.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import javax.annotation.Nonnull;


public class MonolithControllerModel extends Model {
    public static final ModelLayerLocation MODEL_LOCATION = new ModelLayerLocation(WoldsVaults.id("monolith_controller"), "main");
    public static final ResourceLocation TEXTURE_LOCATION = WoldsVaults.id("block/monolith_controller");
    public static final Material MATERIAL = new Material(TextureAtlas.LOCATION_BLOCKS, TEXTURE_LOCATION);;
    public static final ResourceLocation FILLED_TEXTURE_LOCATION = WoldsVaults.id("block/monolith_controller_filled");
    public static final Material FILLED_MATERIAL = new Material(TextureAtlas.LOCATION_BLOCKS, FILLED_TEXTURE_LOCATION);
    protected ModelPart root;

    public MonolithControllerModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition group = partdefinition.addOrReplaceChild("group", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-11.0F, 11.0F, -7.0F, 22.0F, 3.0F, 14.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 17).addBox(-9.0F, -24.0F, -5.0F, 18.0F, 14.0F, 10.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 72).addBox(-5.0F, -27.0F, -9.0F, 10.0F, 9.0F, 8.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 41).addBox(4.0F, -30.0F, -8.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(48, 54).addBox(4.0F, -34.0F, -4.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 17).addBox(-6.0F, -30.0F, -8.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-6.0F, -34.0F, -4.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 41).addBox(-7.0F, -10.0F, -5.0F, 14.0F, 21.0F, 10.0F, new CubeDeformation(0.0F))
                        .texOffs(48, 54).addBox(9.0F, -28.0F, -7.0F, 2.0F, 14.0F, 14.0F, new CubeDeformation(0.0F))
                        .texOffs(48, 29).addBox(11.0F, -28.0F, -6.0F, 10.0F, 13.0F, 12.0F, new CubeDeformation(0.0F))
                        .texOffs(73, 75).addBox(-20.0F, -25.0F, -3.5F, 11.0F, 9.0F, 7.0F, new CubeDeformation(0.0F))
                        .texOffs(86, 48).addBox(-16.0F, -16.0F, -3.0F, 7.0F, 18.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(36, 82).addBox(9.0F, -16.0F, -3.0F, 7.0F, 18.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 10.0F, 0.0F)
        );

        group.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(64, 9).addBox(-8.0F, 1.6F, -4.0F, 10.0F, 10.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.0F, -29.0F, 0.0F, 0.0F, 0.0F, -0.3927F));
        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    public void renderToBuffer(@Nonnull PoseStack poseStack, @Nonnull VertexConsumer vertexConsumer, int packedLight, int packetOverlay, float red, float green, float blue, float alpha) {
        this.root.render(poseStack, vertexConsumer, packedLight, packetOverlay, red, green, blue, alpha);
    }

    static {

    }
}