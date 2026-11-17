package xyz.iwolfking.woldsvaults.client.bullet;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import xyz.iwolfking.woldsvaults.WoldsVaults;

/**
 * The Bullet Bill a Bullet Bill set wearer turns into, after the build it was drawn from: a stepped round body with a
 * dome nose (+z) and a wider dark cap at the tail, an angry eye on each flank with the pupil looking forward, and a
 * white fist below and behind each eye. Laid out like vanilla's entity models (y down), 128 by 128 texture. The eyes
 * and fists are their own part, so the giant variants can leave them off and paint a face on the hull instead.
 */
public final class BulletBillModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(WoldsVaults.id("bullet_bill"), "main");

    private BulletBillModel() {
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(71, 71).addBox(-3.0F, -6.0F, -10.0F, 6.0F, 1.0F, 1.0F)
                .texOffs(58, 66).addBox(-5.0F, -5.0F, -10.0F, 10.0F, 2.0F, 1.0F)
                .texOffs(100, 34).addBox(-6.0F, -3.0F, -10.0F, 12.0F, 6.0F, 1.0F)
                .texOffs(81, 66).addBox(-5.0F, 3.0F, -10.0F, 10.0F, 2.0F, 1.0F)
                .texOffs(86, 71).addBox(-3.0F, 5.0F, -10.0F, 6.0F, 1.0F, 1.0F)
                .texOffs(0, 61).addBox(-4.5F, -7.5F, -9.0F, 9.0F, 1.0F, 3.0F)
                .texOffs(80, 47).addBox(-6.5F, -6.5F, -9.0F, 13.0F, 2.0F, 3.0F)
                .texOffs(0, 34).addBox(-7.5F, -4.5F, -9.0F, 15.0F, 9.0F, 3.0F)
                .texOffs(0, 55).addBox(-6.5F, 4.5F, -9.0F, 13.0F, 2.0F, 3.0F)
                .texOffs(25, 61).addBox(-4.5F, 6.5F, -9.0F, 9.0F, 1.0F, 3.0F)
                .texOffs(47, 20).addBox(-4.0F, -7.0F, -6.0F, 8.0F, 1.0F, 11.0F)
                .texOffs(51, 0).addBox(-6.0F, -6.0F, -6.0F, 12.0F, 2.0F, 11.0F)
                .texOffs(0, 0).addBox(-7.0F, -4.0F, -6.0F, 14.0F, 8.0F, 11.0F)
                .texOffs(0, 20).addBox(-6.0F, 4.0F, -6.0F, 12.0F, 2.0F, 11.0F)
                .texOffs(86, 20).addBox(-4.0F, 6.0F, -6.0F, 8.0F, 1.0F, 11.0F)
                .texOffs(96, 61).addBox(-3.5F, -6.5F, 5.0F, 7.0F, 1.0F, 3.0F)
                .texOffs(33, 55).addBox(-5.5F, -5.5F, 5.0F, 11.0F, 2.0F, 3.0F)
                .texOffs(37, 34).addBox(-6.5F, -3.5F, 5.0F, 13.0F, 7.0F, 3.0F)
                .texOffs(62, 55).addBox(-5.5F, 3.5F, 5.0F, 11.0F, 2.0F, 3.0F)
                .texOffs(0, 66).addBox(-3.5F, 5.5F, 5.0F, 7.0F, 1.0F, 3.0F)
                .texOffs(104, 66).addBox(-3.0F, -5.5F, 8.0F, 6.0F, 1.0F, 2.0F)
                .texOffs(50, 61).addBox(-4.5F, -4.5F, 8.0F, 9.0F, 2.0F, 2.0F)
                .texOffs(0, 47).addBox(-5.5F, -2.5F, 8.0F, 11.0F, 5.0F, 2.0F)
                .texOffs(73, 61).addBox(-4.5F, 2.5F, 8.0F, 9.0F, 2.0F, 2.0F)
                .texOffs(0, 71).addBox(-3.0F, 4.5F, 8.0F, 6.0F, 1.0F, 2.0F)
                .texOffs(101, 71).addBox(-2.5F, -4.5F, 10.0F, 5.0F, 1.0F, 1.0F)
                .texOffs(37, 71).addBox(-3.5F, -3.5F, 10.0F, 7.0F, 1.0F, 1.0F)
                .texOffs(43, 47).addBox(-4.5F, -2.5F, 10.0F, 9.0F, 5.0F, 1.0F)
                .texOffs(54, 71).addBox(-3.5F, 2.5F, 10.0F, 7.0F, 1.0F, 1.0F)
                .texOffs(114, 71).addBox(-2.5F, 3.5F, 10.0F, 5.0F, 1.0F, 1.0F)
                .texOffs(0, 75).addBox(-1.5F, -3.0F, 11.0F, 3.0F, 1.0F, 1.0F)
                .texOffs(91, 55).addBox(-3.0F, -2.0F, 11.0F, 6.0F, 4.0F, 1.0F)
                .texOffs(9, 75).addBox(-1.5F, 2.0F, 11.0F, 3.0F, 1.0F, 1.0F)
                .texOffs(21, 66).addBox(-1.5F, -1.5F, 12.0F, 3.0F, 3.0F, 1.0F),
                PartPose.ZERO);
        root.addOrReplaceChild("details", CubeListBuilder.create()
                .texOffs(27, 47).addBox(6.0F, -5.5F, 0.5F, 1.5F, 4.5F, 2.0F)
                .texOffs(35, 47).addBox(-7.5F, -5.5F, 0.5F, 1.5F, 4.5F, 2.0F)
                .texOffs(64, 47).addBox(6.0F, -4.5F, 2.5F, 1.5F, 3.5F, 2.0F)
                .texOffs(72, 47).addBox(-7.5F, -4.5F, 2.5F, 1.5F, 3.5F, 2.0F)
                .texOffs(46, 66).addBox(7.0F, -4.0F, 2.8F, 0.8F, 2.5F, 1.5F)
                .texOffs(52, 66).addBox(-7.8F, -4.0F, 2.8F, 0.8F, 2.5F, 1.5F)
                .texOffs(70, 34).addBox(7.0F, 0.0F, -3.0F, 3.5F, 4.0F, 3.5F)
                .texOffs(85, 34).addBox(-10.5F, 0.0F, -3.0F, 3.5F, 4.0F, 3.5F)
                .texOffs(17, 71).addBox(7.5F, -1.0F, -1.5F, 2.5F, 1.0F, 2.0F)
                .texOffs(27, 71).addBox(-10.0F, -1.0F, -1.5F, 2.5F, 1.0F, 2.0F)
                .texOffs(30, 66).addBox(6.5F, 1.0F, -4.5F, 2.0F, 2.5F, 1.5F)
                .texOffs(38, 66).addBox(-8.5F, 1.0F, -4.5F, 2.0F, 2.5F, 1.5F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 128);
    }
}
