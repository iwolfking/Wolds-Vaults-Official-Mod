package xyz.iwolfking.woldsvaults.models.armor.layers;

import iskallia.vault.dynamodel.model.armor.ArmorLayers;
import iskallia.vault.dynamodel.model.armor.ArmorPieceModel;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.mixins.accessors.ModelAccessor;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.function.Supplier;

/**
 * Lightning Fiend set armor geometry (from the Blockbench build in handoff/lightning_fiend/blockbench). MainLayer
 * carries the helmet (bolt horns, lightning ears, spike crown, the floating orb ring), the chestplate (sleeves, arm
 * arcs, volt claws) and the boots (ankle bolts, heel streaks, toe talons) on armor_layer1; LeggingsLayer carries the
 * belt and thigh marks on armor_layer2. Most boxes are skin overlays and zero-thickness planes, so the skin shows.
 * The textures may be 128x128: the UV layout stays 64x64 and Minecraft samples it at double density.
 * Both layers draw with alpha blending rather than cutout, so the textures' see-through parts show as painted: VH's
 * armor layer reads isTranslucent(), and the first-person sleeve goes through the model's own render type. The flat
 * parts are made single-sided and chain joints lose their doubled face (see MainLayer#fixOverlappingFaces).
 */
public class LightningFiendArmorLayers extends ArmorLayers {
    @Override
    public Supplier<LayerDefinition> getGeometrySupplier(EquipmentSlot equipmentSlot) {
        return equipmentSlot == EquipmentSlot.LEGS ? LeggingsLayer::createBodyLayer : MainLayer::createBodyLayer;
    }

    @Override
    public VaultArmorLayerSupplier<? extends BaseLayer> getLayerSupplier(EquipmentSlot equipmentSlot) {
        return equipmentSlot == EquipmentSlot.LEGS ? LeggingsLayer::new : MainLayer::new;
    }

    @OnlyIn(Dist.CLIENT)
    public static class MainLayer extends ArmorLayers.MainLayer {
        private static final Pattern CHAIN_SEGMENT = Pattern.compile(".+_(\\d+)");
        private static Field polygonNormal;

        public MainLayer(ArmorPieceModel definition, ModelPart root) {
            super(definition, root);
            ((ModelAccessor) this).setRenderType(RenderType::entityTranslucent);
            fixOverlappingFaces(root);
        }

        @SuppressWarnings("unchecked")
        private static void fixOverlappingFaces(ModelPart root) {
            try {
                Field childrenField = ObfuscationReflectionHelper.findField(ModelPart.class, "f_104213_");
                Field cubesField = ObfuscationReflectionHelper.findField(ModelPart.class, "f_104212_");
                Field polygonsField = ObfuscationReflectionHelper.findField(ModelPart.Cube.class, "f_104341_");
                Deque<Map.Entry<String, ModelPart>> parts = new ArrayDeque<>();
                parts.add(Map.entry("root", root));
                while (!parts.isEmpty()) {
                    Map.Entry<String, ModelPart> entry = parts.pop();
                    Matcher segment = CHAIN_SEGMENT.matcher(entry.getKey());
                    boolean chained = segment.matches() && Integer.parseInt(segment.group(1)) > 0;
                    for (ModelPart.Cube cube : (List<ModelPart.Cube>) cubesField.get(entry.getValue())) {
                        float[] size = { cube.maxX - cube.minX, cube.maxY - cube.minY, cube.maxZ - cube.minZ };
                        int thin = Math.abs(size[0]) < 1.0E-4F ? 0 : Math.abs(size[1]) < 1.0E-4F ? 1 : Math.abs(size[2]) < 1.0E-4F ? 2 : -1;
                        Object[] polygons = (Object[]) polygonsField.get(cube);
                        List<Object> kept = new ArrayList<>();
                        for (Object polygon : polygons) {
                            Vector3f normal = normal(polygon);
                            if (thin >= 0) {
                                float along = thin == 0 ? normal.x() : thin == 1 ? normal.y() : normal.z();
                                if (Math.abs(along) > 0.5F) {
                                    kept.add(polygon);
                                    break;
                                }
                            } else if (!chained || normal.y() < 0.5F) {
                                kept.add(polygon);
                            }
                        }
                        if (kept.size() != polygons.length) {
                            polygonsField.set(cube, kept.toArray((Object[]) Array.newInstance(polygons.getClass().getComponentType(), 0)));
                        }
                    }
                    parts.addAll(((Map<String, ModelPart>) childrenField.get(entry.getValue())).entrySet());
                }
            } catch (ReflectiveOperationException | RuntimeException e) {
                WoldsVaults.LOGGER.warn("Lightning Fiend armor: couldn't remove its overlapping faces, so they may flicker.", e);
            }
        }

        private static Vector3f normal(Object polygon) throws IllegalAccessException {
            if (polygonNormal == null) {
                polygonNormal = ObfuscationReflectionHelper.findField(polygon.getClass(), "f_104360_");
            }
            return (Vector3f) polygonNormal.get(polygon);
        }

        @Override
        public boolean isTranslucent() {
            return true;
        }

        public static LayerDefinition createBodyLayer() {
            MeshDefinition meshdefinition = createBaseLayer();
            PartDefinition partdefinition = meshdefinition.getRoot();

            PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.25F))
                    .texOffs(33, 10).addBox(-5.0F, -11.0F, -4.4F, 10.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

            PartDefinition horn_left_0 = head.addOrReplaceChild("horn_left_0", CubeListBuilder.create().texOffs(48, 1).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -8.0F, 1.5F, 0.0F, 0.0F, 0.4363F));

            PartDefinition horn_left_1 = horn_left_0.addOrReplaceChild("horn_left_1", CubeListBuilder.create().texOffs(53, 1).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, 0.0F, 0.0F, -0.6109F));

            PartDefinition horn_left_2 = horn_left_1.addOrReplaceChild("horn_left_2", CubeListBuilder.create().texOffs(58, 1).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.0F, 0.0F, 0.4363F));

            PartDefinition horn_left_3 = horn_left_2.addOrReplaceChild("horn_left_3", CubeListBuilder.create().texOffs(54, 10).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.0F, 0.0F, -0.3491F));

            PartDefinition ear_left = head.addOrReplaceChild("ear_left", CubeListBuilder.create().texOffs(33, 1).addBox(-1.0F, -7.0F, 0.0F, 7.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, -6.0F, 0.5F, 0.0F, -0.3491F, 0.0F));

            PartDefinition horn_right_0 = head.addOrReplaceChild("horn_right_0", CubeListBuilder.create().texOffs(48, 1).mirror().addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.0F, -8.0F, 1.5F, 0.0F, 0.0F, -0.4363F));

            PartDefinition horn_right_1 = horn_right_0.addOrReplaceChild("horn_right_1", CubeListBuilder.create().texOffs(53, 1).mirror().addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, 0.0F, 0.0F, 0.6109F));

            PartDefinition horn_right_2 = horn_right_1.addOrReplaceChild("horn_right_2", CubeListBuilder.create().texOffs(58, 1).mirror().addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.0F, 0.0F, -0.4363F));

            PartDefinition horn_right_3 = horn_right_2.addOrReplaceChild("horn_right_3", CubeListBuilder.create().texOffs(54, 10).mirror().addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.0F, 0.0F, 0.3491F));

            PartDefinition ear_right = head.addOrReplaceChild("ear_right", CubeListBuilder.create().texOffs(33, 1).mirror().addBox(-6.0F, -7.0F, 0.0F, 7.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-3.5F, -6.0F, 0.5F, 0.0F, 0.3491F, 0.0F));

            PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F))
                    .texOffs(1, 45).addBox(8.0F, -17.5F, 0.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                    .texOffs(14, 45).addBox(7.0F, -18.5F, 2.0F, 5.0F, 5.0F, 0.0F, new CubeDeformation(0.0F))
                    .texOffs(14, 40).addBox(9.5F, -18.5F, -0.5F, 0.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
                    .texOffs(1, 45).addBox(12.0F, -10.0F, 0.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                    .texOffs(14, 45).addBox(11.0F, -11.0F, 2.0F, 5.0F, 5.0F, 0.0F, new CubeDeformation(0.0F))
                    .texOffs(14, 40).addBox(13.5F, -11.0F, -0.5F, 0.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
                    .texOffs(1, 45).addBox(11.5F, -4.0F, 0.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                    .texOffs(14, 45).addBox(10.5F, -5.0F, 2.0F, 5.0F, 5.0F, 0.0F, new CubeDeformation(0.0F))
                    .texOffs(14, 40).addBox(13.0F, -5.0F, -0.5F, 0.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
                    .texOffs(1, 45).addBox(-1.5F, -20.5F, 0.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                    .texOffs(14, 45).addBox(-2.5F, -21.5F, 2.0F, 5.0F, 5.0F, 0.0F, new CubeDeformation(0.0F))
                    .texOffs(14, 40).addBox(0.0F, -21.5F, -0.5F, 0.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
                    .texOffs(1, 45).mirror().addBox(-11.0F, -17.5F, 0.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
                    .texOffs(14, 45).mirror().addBox(-12.0F, -18.5F, 2.0F, 5.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false)
                    .texOffs(14, 40).mirror().addBox(-9.5F, -18.5F, -0.5F, 0.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
                    .texOffs(1, 45).mirror().addBox(-15.0F, -10.0F, 0.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
                    .texOffs(14, 45).mirror().addBox(-16.0F, -11.0F, 2.0F, 5.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false)
                    .texOffs(14, 40).mirror().addBox(-13.5F, -11.0F, -0.5F, 0.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
                    .texOffs(1, 45).mirror().addBox(-14.5F, -4.0F, 0.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
                    .texOffs(14, 45).mirror().addBox(-15.5F, -5.0F, 2.0F, 5.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false)
                    .texOffs(14, 40).mirror().addBox(-13.0F, -5.0F, -0.5F, 0.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

            PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F))
                    .texOffs(14, 33).addBox(-1.0F, 6.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.5F))
                    .texOffs(1, 33).addBox(3.25F, 0.0F, 0.0F, 6.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, 2.0F, 0.0F));

            PartDefinition claw_left_a_0 = left_arm.addOrReplaceChild("claw_left_a_0", CubeListBuilder.create().texOffs(31, 33).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, 10.0F, -1.5F, 2.618F, 0.0F, 0.0F));

            PartDefinition claw_left_a_1 = claw_left_a_0.addOrReplaceChild("claw_left_a_1", CubeListBuilder.create().texOffs(36, 33).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.3491F, 0.0F, 0.0F));

            PartDefinition claw_left_a_2 = claw_left_a_1.addOrReplaceChild("claw_left_a_2", CubeListBuilder.create().texOffs(41, 33).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.4363F, 0.0F, 0.0F));

            PartDefinition claw_left_b_0 = left_arm.addOrReplaceChild("claw_left_b_0", CubeListBuilder.create().texOffs(46, 33).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 10.0F, -1.5F, 2.5307F, 0.0F, 0.0F));

            PartDefinition claw_left_b_1 = claw_left_b_0.addOrReplaceChild("claw_left_b_1", CubeListBuilder.create().texOffs(51, 33).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, 0.3491F, 0.0F, 0.0F));

            PartDefinition claw_left_b_2 = claw_left_b_1.addOrReplaceChild("claw_left_b_2", CubeListBuilder.create().texOffs(56, 33).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, 0.4363F, 0.0F, 0.0F));

            PartDefinition claw_left_c_0 = left_arm.addOrReplaceChild("claw_left_c_0", CubeListBuilder.create().texOffs(1, 41).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 10.0F, -1.5F, 2.618F, 0.0F, 0.0F));

            PartDefinition claw_left_c_1 = claw_left_c_0.addOrReplaceChild("claw_left_c_1", CubeListBuilder.create().texOffs(6, 41).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.3491F, 0.0F, 0.0F));

            PartDefinition claw_left_c_2 = claw_left_c_1.addOrReplaceChild("claw_left_c_2", CubeListBuilder.create().texOffs(11, 41).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.4363F, 0.0F, 0.0F));

            PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)).mirror(false)
                    .texOffs(14, 33).mirror().addBox(-3.0F, 6.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.5F)).mirror(false)
                    .texOffs(1, 33).mirror().addBox(-9.25F, 0.0F, 0.0F, 6.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-5.0F, 2.0F, 0.0F));

            PartDefinition claw_right_a_0 = right_arm.addOrReplaceChild("claw_right_a_0", CubeListBuilder.create().texOffs(31, 33).mirror().addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.5F, 10.0F, -1.5F, 2.618F, 0.0F, 0.0F));

            PartDefinition claw_right_a_1 = claw_right_a_0.addOrReplaceChild("claw_right_a_1", CubeListBuilder.create().texOffs(36, 33).mirror().addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.3491F, 0.0F, 0.0F));

            PartDefinition claw_right_a_2 = claw_right_a_1.addOrReplaceChild("claw_right_a_2", CubeListBuilder.create().texOffs(41, 33).mirror().addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.4363F, 0.0F, 0.0F));

            PartDefinition claw_right_b_0 = right_arm.addOrReplaceChild("claw_right_b_0", CubeListBuilder.create().texOffs(46, 33).mirror().addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.0F, 10.0F, -1.5F, 2.5307F, 0.0F, 0.0F));

            PartDefinition claw_right_b_1 = claw_right_b_0.addOrReplaceChild("claw_right_b_1", CubeListBuilder.create().texOffs(51, 33).mirror().addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, 0.3491F, 0.0F, 0.0F));

            PartDefinition claw_right_b_2 = claw_right_b_1.addOrReplaceChild("claw_right_b_2", CubeListBuilder.create().texOffs(56, 33).mirror().addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, 0.4363F, 0.0F, 0.0F));

            PartDefinition claw_right_c_0 = right_arm.addOrReplaceChild("claw_right_c_0", CubeListBuilder.create().texOffs(1, 41).mirror().addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.5F, 10.0F, -1.5F, 2.618F, 0.0F, 0.0F));

            PartDefinition claw_right_c_1 = claw_right_c_0.addOrReplaceChild("claw_right_c_1", CubeListBuilder.create().texOffs(6, 41).mirror().addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.3491F, 0.0F, 0.0F));

            PartDefinition claw_right_c_2 = claw_right_c_1.addOrReplaceChild("claw_right_c_2", CubeListBuilder.create().texOffs(11, 41).mirror().addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.4363F, 0.0F, 0.0F));

            PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-1.9F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F))
                    .texOffs(1, 52).addBox(2.35F, 4.0F, 0.0F, 5.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(1.9F, 12.0F, 0.0F));

            PartDefinition ankle_spur_left_0 = left_leg.addOrReplaceChild("ankle_spur_left_0", CubeListBuilder.create().texOffs(12, 52).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.35F, 11.5F, 1.5F, 0.0F, 0.0F, 1.2217F));

            PartDefinition ankle_spur_left_1 = ankle_spur_left_0.addOrReplaceChild("ankle_spur_left_1", CubeListBuilder.create().texOffs(17, 52).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.0F, 0.0F, -0.5236F));

            PartDefinition toe_talon_left_a_0 = left_leg.addOrReplaceChild("toe_talon_left_a_0", CubeListBuilder.create().texOffs(47, 52).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.6F, 11.0F, -2.5F, 1.7453F, 0.0F, 0.0F));

            PartDefinition toe_talon_left_a_1 = toe_talon_left_a_0.addOrReplaceChild("toe_talon_left_a_1", CubeListBuilder.create().texOffs(52, 52).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.3491F, 0.0F, 0.0F));

            PartDefinition toe_talon_left_b_0 = left_leg.addOrReplaceChild("toe_talon_left_b_0", CubeListBuilder.create().texOffs(57, 52).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.9F, 11.0F, -2.5F, 1.7453F, 0.0F, 0.0F));

            PartDefinition toe_talon_left_b_1 = toe_talon_left_b_0.addOrReplaceChild("toe_talon_left_b_1", CubeListBuilder.create().texOffs(1, 58).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.3491F, 0.0F, 0.0F));

            PartDefinition heel_streak_left_a_0 = left_leg.addOrReplaceChild("heel_streak_left_a_0", CubeListBuilder.create().texOffs(22, 52).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.1F, 11.0F, 2.5F, -1.6581F, 0.0F, 0.0F));

            PartDefinition heel_streak_left_a_1 = heel_streak_left_a_0.addOrReplaceChild("heel_streak_left_a_1", CubeListBuilder.create().texOffs(27, 52).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, 0.5236F, 0.0F, 0.0F));

            PartDefinition heel_streak_left_a_2 = heel_streak_left_a_1.addOrReplaceChild("heel_streak_left_a_2", CubeListBuilder.create().texOffs(32, 52).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

            PartDefinition heel_streak_left_b_0 = left_leg.addOrReplaceChild("heel_streak_left_b_0", CubeListBuilder.create().texOffs(37, 52).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.9F, 9.0F, 2.5F, -1.7453F, 0.0F, 0.0F));

            PartDefinition heel_streak_left_b_1 = heel_streak_left_b_0.addOrReplaceChild("heel_streak_left_b_1", CubeListBuilder.create().texOffs(42, 52).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.5236F, 0.0F, 0.0F));

            PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.1F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)).mirror(false)
                    .texOffs(1, 52).mirror().addBox(-7.35F, 4.0F, 0.0F, 5.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-1.9F, 12.0F, 0.0F));

            PartDefinition ankle_spur_right_0 = right_leg.addOrReplaceChild("ankle_spur_right_0", CubeListBuilder.create().texOffs(12, 52).mirror().addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.35F, 11.5F, 1.5F, 0.0F, 0.0F, -1.2217F));

            PartDefinition ankle_spur_right_1 = ankle_spur_right_0.addOrReplaceChild("ankle_spur_right_1", CubeListBuilder.create().texOffs(17, 52).mirror().addBox(-0.5F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.0F, 0.0F, 0.5236F));

            PartDefinition toe_talon_right_a_0 = right_leg.addOrReplaceChild("toe_talon_right_a_0", CubeListBuilder.create().texOffs(47, 52).mirror().addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.6F, 11.0F, -2.5F, 1.7453F, 0.0F, 0.0F));

            PartDefinition toe_talon_right_a_1 = toe_talon_right_a_0.addOrReplaceChild("toe_talon_right_a_1", CubeListBuilder.create().texOffs(52, 52).mirror().addBox(-0.5F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.3491F, 0.0F, 0.0F));

            PartDefinition toe_talon_right_b_0 = right_leg.addOrReplaceChild("toe_talon_right_b_0", CubeListBuilder.create().texOffs(57, 52).mirror().addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.9F, 11.0F, -2.5F, 1.7453F, 0.0F, 0.0F));

            PartDefinition toe_talon_right_b_1 = toe_talon_right_b_0.addOrReplaceChild("toe_talon_right_b_1", CubeListBuilder.create().texOffs(1, 58).mirror().addBox(-0.5F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.3491F, 0.0F, 0.0F));

            PartDefinition heel_streak_right_a_0 = right_leg.addOrReplaceChild("heel_streak_right_a_0", CubeListBuilder.create().texOffs(22, 52).mirror().addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.1F, 11.0F, 2.5F, -1.6581F, 0.0F, 0.0F));

            PartDefinition heel_streak_right_a_1 = heel_streak_right_a_0.addOrReplaceChild("heel_streak_right_a_1", CubeListBuilder.create().texOffs(27, 52).mirror().addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, 0.5236F, 0.0F, 0.0F));

            PartDefinition heel_streak_right_a_2 = heel_streak_right_a_1.addOrReplaceChild("heel_streak_right_a_2", CubeListBuilder.create().texOffs(32, 52).mirror().addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

            PartDefinition heel_streak_right_b_0 = right_leg.addOrReplaceChild("heel_streak_right_b_0", CubeListBuilder.create().texOffs(37, 52).mirror().addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.9F, 9.0F, 2.5F, -1.7453F, 0.0F, 0.0F));

            PartDefinition heel_streak_right_b_1 = heel_streak_right_b_0.addOrReplaceChild("heel_streak_right_b_1", CubeListBuilder.create().texOffs(42, 52).mirror().addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.5236F, 0.0F, 0.0F));

            return LayerDefinition.create(meshdefinition, 64, 64);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class LeggingsLayer extends ArmorLayers.LeggingsLayer {
        public LeggingsLayer(ArmorPieceModel definition, ModelPart root) {
            super(definition, root);
            ((ModelAccessor) this).setRenderType(RenderType::entityTranslucent);
        }

        @Override
        public boolean isTranslucent() {
            return true;
        }

        public static LayerDefinition createBodyLayer() {
            MeshDefinition meshdefinition = createBaseLayer();
            PartDefinition partdefinition = meshdefinition.getRoot();

            PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 9.0F, -2.0F, 8.0F, 3.0F, 4.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));

            PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-1.9F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)), PartPose.offset(1.9F, 12.0F, 0.0F));

            PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.1F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)).mirror(false), PartPose.offset(-1.9F, 12.0F, 0.0F));

            return LayerDefinition.create(meshdefinition, 64, 64);
        }
    }
}
