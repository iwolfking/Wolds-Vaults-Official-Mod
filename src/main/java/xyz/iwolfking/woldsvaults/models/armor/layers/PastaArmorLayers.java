package xyz.iwolfking.woldsvaults.models.armor.layers;

// Made with Blockbench 5.1.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import iskallia.vault.dynamodel.model.armor.ArmorLayers;
import iskallia.vault.dynamodel.model.armor.ArmorPieceModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Supplier;

public class PastaArmorLayers extends ArmorLayers {

    @Override
    public Supplier<LayerDefinition> getGeometrySupplier(EquipmentSlot equipmentSlot) {
        return MainLayer::createBodyLayer;
    }

    @Override
    public ArmorLayers.VaultArmorLayerSupplier<? extends ArmorLayers.BaseLayer> getLayerSupplier(EquipmentSlot equipmentSlot) {
        return MainLayer::new;
    }


    @OnlyIn(Dist.CLIENT)
    public static class MainLayer extends ArmorLayers.MainLayer {

        public MainLayer(ArmorPieceModel definition, ModelPart root) {
            super(definition, root);
        }

        public static LayerDefinition createBodyLayer() {
            MeshDefinition meshdefinition = createBaseLayer();
            PartDefinition partdefinition = meshdefinition.getRoot();

            PartDefinition bipedHead = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

            PartDefinition strainer = bipedHead.addOrReplaceChild("strainer", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -8.0F, -16.0F, 10.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
                    .texOffs(16, 14).addBox(-3.0F, -9.5F, -14.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 11.0F));

            PartDefinition bone = strainer.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 23).addBox(-3.0F, -5.0F, -14.5F, 0.5F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                    .texOffs(0, 29).addBox(-9.0F, -5.0F, -8.0F, 6.0F, 2.0F, 0.5F, new CubeDeformation(0.0F))
                    .texOffs(0, 29).addBox(-9.5F, -5.0F, -14.5F, 6.5F, 2.0F, 0.5F, new CubeDeformation(0.0F))
                    .texOffs(0, 23).addBox(-9.5F, -5.0F, -14.5F, 0.5F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, -6.5F, 0.0F));

            PartDefinition spaghetti = bipedHead.addOrReplaceChild("spaghetti", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, 11.0F));

            PartDefinition right = spaghetti.addOrReplaceChild("right", CubeListBuilder.create(), PartPose.offset(-5.25F, -3.0F, -9.0F));

            PartDefinition long1 = right.addOrReplaceChild("long", CubeListBuilder.create().texOffs(36, 24).addBox(0.5F, -3.5F, 0.5F, 0.5F, 7.5F, 0.5F, new CubeDeformation(0.0F))
                    .texOffs(21, 30).addBox(0.5F, -3.5F, -0.5F, 0.5F, 7.5F, 0.5F, new CubeDeformation(0.0F))
                    .texOffs(23, 31).addBox(0.5F, -3.5F, -1.5F, 0.5F, 7.5F, 0.5F, new CubeDeformation(0.0F))
                    .texOffs(30, 34).addBox(0.5F, -3.5F, -2.5F, 0.5F, 7.5F, 0.5F, new CubeDeformation(0.0F))
                    .texOffs(22, 30).addBox(0.5F, -3.5F, -3.5F, 0.5F, 7.5F, 0.5F, new CubeDeformation(0.0F))
                    .texOffs(22, 30).addBox(0.5F, -3.5F, -4.5F, 0.5F, 7.5F, 0.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

            PartDefinition short1 = right.addOrReplaceChild("short", CubeListBuilder.create().texOffs(32, 38).addBox(0.5F, -3.5F, 0.0F, 0.5F, 6.5F, 0.5F, new CubeDeformation(0.0F))
                    .texOffs(36, 38).addBox(0.5F, -3.5F, -1.0F, 0.5F, 6.5F, 0.5F, new CubeDeformation(0.0F))
                    .texOffs(40, 0).addBox(0.5F, -3.5F, -2.0F, 0.5F, 6.5F, 0.5F, new CubeDeformation(0.0F))
                    .texOffs(41, 10).addBox(0.5F, -3.5F, -3.0F, 0.5F, 6.5F, 0.5F, new CubeDeformation(0.0F))
                    .texOffs(9, 39).addBox(0.5F, -3.5F, -4.0F, 0.5F, 6.5F, 0.5F, new CubeDeformation(0.0F)), PartPose.offset(-0.2F, 0.0F, 0.0F));

            PartDefinition left = spaghetti.addOrReplaceChild("left", CubeListBuilder.create(), PartPose.offset(5.25F, -3.0F, -13.0F));

            PartDefinition long2 = left.addOrReplaceChild("long2", CubeListBuilder.create().texOffs(33, 31).addBox(-1.0F, -3.5F, -1.0F, 0.5F, 7.5F, 0.5F, new CubeDeformation(0.0F))
                    .texOffs(1, 38).addBox(-1.0F, -3.5F, 0.0F, 0.5F, 7.5F, 0.5F, new CubeDeformation(0.0F))
                    .texOffs(5, 38).addBox(-1.0F, -3.5F, 1.0F, 0.5F, 7.5F, 0.5F, new CubeDeformation(0.0F))
                    .texOffs(9, 38).addBox(-1.0F, -3.5F, 2.0F, 0.5F, 7.5F, 0.5F, new CubeDeformation(0.0F))
                    .texOffs(37, 22).addBox(-1.0F, -3.5F, 3.0F, 0.5F, 7.5F, 0.5F, new CubeDeformation(0.0F))
                    .texOffs(37, 31).addBox(-1.0F, -3.5F, 4.0F, 0.5F, 7.5F, 0.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

            PartDefinition short2 = left.addOrReplaceChild("short2", CubeListBuilder.create().texOffs(16, 40).addBox(-1.0F, -3.5F, -0.5F, 0.5F, 6.5F, 0.5F, new CubeDeformation(0.0F))
                    .texOffs(5, 37).addBox(-1.0F, -3.5F, 0.5F, 0.5F, 6.5F, 0.5F, new CubeDeformation(0.0F))
                    .texOffs(24, 37).addBox(-1.0F, -3.5F, 1.5F, 0.5F, 6.5F, 0.5F, new CubeDeformation(0.0F))
                    .texOffs(24, 39).addBox(-1.0F, -3.5F, 2.5F, 0.5F, 6.5F, 0.5F, new CubeDeformation(0.0F))
                    .texOffs(38, 26).addBox(-1.0F, -3.5F, 3.5F, 0.5F, 6.5F, 0.5F, new CubeDeformation(0.0F)), PartPose.offset(0.2F, 0.0F, 0.0F));

            PartDefinition top = spaghetti.addOrReplaceChild("top", CubeListBuilder.create(), PartPose.offset(5.25F, -3.0F, -13.0F));

            PartDefinition long3 = top.addOrReplaceChild("long3", CubeListBuilder.create().texOffs(0, 38).addBox(-1.0F, -4.5F, 4.5F, 0.5F, 7.5F, 0.75F, new CubeDeformation(0.0F))
                    .texOffs(1, 38).addBox(-1.0F, -2.5F, -1.25F, 0.5F, 6.5F, 0.75F, new CubeDeformation(0.0F))
                    .texOffs(1, 33).addBox(-1.0F, -4.75F, -0.5F, 0.5F, 9.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -4.0F, 0.0F, 0.0F, 0.0F, -1.5708F));


            return LayerDefinition.create(meshdefinition, 64, 64);
        }
    }
}