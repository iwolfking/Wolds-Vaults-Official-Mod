package xyz.iwolfking.woldsvaults.models.armor;

import iskallia.vault.dynamodel.model.armor.ArmorLayers;
import iskallia.vault.dynamodel.model.armor.ArmorPieceModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.function.Supplier;

public class HeatwaveArmorModel extends ArmorLayers {
    @Override
    public Supplier<LayerDefinition> getGeometrySupplier(EquipmentSlot equipmentSlot) {
        return equipmentSlot == EquipmentSlot.LEGS ? LeggingsLayer::createBodyLayer : MainLayer::createBodyLayer;
    }

    @Override
    public VaultArmorLayerSupplier<? extends BaseLayer> getLayerSupplier(EquipmentSlot equipmentSlot) {
        return equipmentSlot == EquipmentSlot.LEGS ? LeggingsLayer::new : MainLayer::new;
    }

    public static class LeggingsLayer extends ArmorLayers.LeggingsLayer {

        public LeggingsLayer(ArmorPieceModel definition, ModelPart root) {
            super(definition, root);
        }

        public static LayerDefinition createBodyLayer() {
            MeshDefinition meshdefinition = new MeshDefinition();
            PartDefinition partdefinition = meshdefinition.getRoot();

            PartDefinition Body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, 9.0F, -4.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.11F))
                    .texOffs(2, 9).addBox(-4.0F, 10.0F, -3.0F, 8.0F, 4.0F, 6.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 0.0F, 0.0F));

            PartDefinition RightLeg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 19).mirror().addBox(-3.0F, 0.0F, -3.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
                    .texOffs(0, 31).addBox(-2.0F, 5.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

            PartDefinition LeftLeg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 19).addBox(1.2F, 0.0F, -3.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                    .texOffs(0, 31).mirror().addBox(-1.8F, 5.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offset(1.9F, 12.0F, 0.0F));

            return LayerDefinition.create(meshdefinition, 64, 64);
        }
    }

    public static class MainLayer extends ArmorLayers.MainLayer {

        public MainLayer(ArmorPieceModel definition, ModelPart root) {
            super(definition, root);
        }

        public static LayerDefinition createBodyLayer() {
            MeshDefinition meshdefinition = new MeshDefinition();
            PartDefinition partdefinition = meshdefinition.getRoot();

            PartDefinition Head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(28, 38).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 4.0F, 9.0F, new CubeDeformation(0.11F))
                    .texOffs(28, 51).addBox(-4.5F, -4.5F, -4.5F, 9.0F, 4.0F, 9.0F, new CubeDeformation(0.1F))
                    .texOffs(0, 46).addBox(-3.5F, -6.5F, -5.75F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.1F))
                    .texOffs(16, 46).addBox(-1.25F, -10.5F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 0.0F, 0.0F));

            PartDefinition Body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 33).addBox(-5.0F, -3.0F, -1.0F, 10.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                    .texOffs(2, 41).addBox(-5.0F, -2.0F, -5.0F, 10.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                    .texOffs(0, 0).addBox(-5.5F, -1.0F, -5.5F, 11.0F, 7.0F, 11.0F, new CubeDeformation(0.1F))
                    .texOffs(48, 16).addBox(5.0F, -1.0F, -3.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                    .texOffs(48, 16).addBox(-7.0F, -1.0F, -3.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                    .texOffs(0, 18).addBox(-6.0F, 6.0F, -6.0F, 12.0F, 3.0F, 12.0F, new CubeDeformation(0.0F))
                    .texOffs(44, 0).addBox(0.5F, -1.0F, 4.0F, 5.0F, 11.0F, 5.0F, new CubeDeformation(0.0F))
                    .texOffs(44, 0).addBox(-5.5F, -1.0F, 4.0F, 5.0F, 11.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

            PartDefinition RightArm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -2.0F, -2.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

            PartDefinition LeftArm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 0).addBox(3.0F, -2.0F, -2.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, 2.0F, 0.0F));

            PartDefinition Hat = partdefinition.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 0).addBox(3.0F, -2.0F, -2.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, 2.0F, 0.0F));

            PartDefinition RightLeg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 56).addBox(-3.95F, 7.25F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                    .texOffs(0, 50).addBox(-2.95F, 9.25F, -2.5F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                    .texOffs(0, 56).addBox(-3.95F, 10.25F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

            PartDefinition LeftLeg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 56).addBox(-2.25F, 10.25F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                    .texOffs(0, 50).addBox(-2.25F, 9.25F, -2.5F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                    .texOffs(0, 56).addBox(-2.25F, 7.25F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(1.9F, 12.0F, 0.0F));

            return LayerDefinition.create(meshdefinition, 64, 64);
        }
    }
}

