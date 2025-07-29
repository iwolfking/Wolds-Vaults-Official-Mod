package xyz.iwolfking.woldsvaults.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import iskallia.vault.block.entity.MonolithTileEntity;
import iskallia.vault.init.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import vazkii.quark.content.tools.module.ColorRunesModule;
import xyz.iwolfking.woldsvaults.blocks.BrewingAltar;
import xyz.iwolfking.woldsvaults.blocks.tiles.BrewingAltarTileEntity;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.items.alchemy.AlchemyIngredientItem;
import xyz.iwolfking.woldsvaults.items.alchemy.DecoPotionItem;

import java.util.*;
import java.util.List;
import java.util.function.IntFunction;


public class BrewingAltarRenderer implements BlockEntityRenderer<BrewingAltarTileEntity> {
    private static final Minecraft mc = Minecraft.getInstance();
    private float currentAngle = 0f;
    private float currentHover = 0f;
    private float currentRotationSpeed = 0f;

    public BrewingAltarRenderer(BlockEntityRendererProvider.Context ignored) {
    }

    @Override
    public void render(@NotNull BrewingAltarTileEntity pBlockEntity, float pPartialTick, @NotNull PoseStack pPoseStack, @NotNull MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        Level level = mc.level;

        if (level == null) return;
        if (pBlockEntity.hasExpired()) return;

        if (pBlockEntity.getBlockState().getValue(BrewingAltar.USES) == 0 && !pBlockEntity.hasExpired()) {
            playExpireEffects(level, pBlockEntity);
            return;
        }




        NonNullList<ItemStack> items = pBlockEntity.getAllIngredients();

        renderText(pBlockEntity, pPoseStack, pBufferSource);
        renderPotion(level, pBlockEntity, pPoseStack, pBufferSource, pPackedOverlay, AlchemyIngredientItem.filterForAlchemyIngredients(items));
        renderFloatingItems(pPoseStack, pBufferSource, pPackedOverlay, items.size(), items::get,
                i -> ((float) i / items.size()) * 2 * Math.PI + (Math.PI * (System.currentTimeMillis() / 3000D) % (2 * Math.PI)));
        renderCatalysts(pPoseStack, pBufferSource, pPackedOverlay, pBlockEntity.getCatalyst());
    }

    private void renderCatalysts(@NotNull PoseStack pPoseStack, @NotNull MultiBufferSource pBufferSource, int pPackedOverlay, ItemStack catalyst) {
        if (catalyst.getItem() == ModItems.INGREDIENT_TEMPLATE) return;
        catalyst.enchant(Enchantments.UNBREAKING, 3);
        BakedModel model = mc.getItemRenderer().getModel(catalyst, null, null, 0);

        Vec3[] positions = new Vec3[] {
                new Vec3(0.5, 0.35, 0.0), // North
                new Vec3(1, 0.35, 0.5), // East
                new Vec3(0.5, 0.35, 1), // South
                new Vec3(0, 0.35, 0.5)  // West
        };

        float rot = 0F;
        for (Vec3 pos : positions) {
            pPoseStack.pushPose();
            pPoseStack.translate(pos.x, pos.y, pos.z);
            pPoseStack.scale(0.5F, 0.5F, 0.5F);
            pPoseStack.mulPose(Vector3f.YP.rotationDegrees(rot));
            mc.getItemRenderer().render(catalyst, ItemTransforms.TransformType.GROUND, true, pPoseStack, pBufferSource, 0xFFFFFF, pPackedOverlay, model);
            pPoseStack.popPose();
            rot += 90F;
        }

//        pPoseStack.pushPose();
//        pPoseStack.translate(0.5, 0.35, 0);
//        pPoseStack.scale(0.5F, 0.5F, 0.5F);
//
//        BakedModel ibakedmodel = mc.getItemRenderer().getModel(catalyst, null, null, 0);
//        mc.getItemRenderer().render(catalyst, ItemTransforms.TransformType.GROUND, true, pPoseStack, pBufferSource, 0xFFFFFF, pPackedOverlay, ibakedmodel);
//        pPoseStack.popPose();
    }

    private void renderPotion(Level level, BrewingAltarTileEntity pBlockEntity, PoseStack poseStack, MultiBufferSource buffer, int overlay, List<ItemStack> items) {
        ItemStack item = ModItems.DECO_POTION.getDefaultInstance();

        if (item.getItem() instanceof DecoPotionItem it) {
            it.setTypeBasedOnContents(items);
            int mixedColor = AlchemyIngredientItem.getMixedColor(items);
            DecoPotionItem.setColor(item, mixedColor);

            float partialTicks = Minecraft.getInstance().getFrameTime();
            long gameTime = level.getGameTime();

            boolean hasIngredients = !items.isEmpty();
            float targetHover = hasIngredients ? (float) Math.sin((gameTime + partialTicks) * 0.1f) * 0.1f : 0f;
            float targetRotationSpeed = hasIngredients ? 2.0f * Math.max(0.2f, items.size()) * 0.5f : 0.5f;

            currentHover = Mth.lerp(0.01F, currentHover, targetHover);
            currentRotationSpeed = Mth.lerp(0.01F, currentRotationSpeed, targetRotationSpeed);

            float deltaAngle = currentRotationSpeed;
            currentAngle = (currentAngle + deltaAngle) % 360f;

            poseStack.pushPose();
            poseStack.translate(0.5, 1.25 + currentHover, 0.5); // smooth hover
            poseStack.mulPose(Vector3f.YP.rotationDegrees(currentAngle));

            BakedModel ibakedmodel = mc.getItemRenderer().getModel(item, null, null, 0);
            mc.getItemRenderer().render(item, ItemTransforms.TransformType.GROUND, true, poseStack, buffer, 0xFFFFFF, overlay, ibakedmodel);
            poseStack.popPose();

            if (items.size() == 3 && level.random.nextFloat() < 0.01f) {
                spawnParticles(pBlockEntity.getBlockPos(), level.random, mixedColor);
            }
        }
    }

    private static void playExpireEffects(Level level, BrewingAltarTileEntity pBlockEntity) {
        double x = pBlockEntity.getBlockPos().getX() + 0.5;
        double y = pBlockEntity.getBlockPos().getY() + 1.2;
        double z = pBlockEntity.getBlockPos().getZ() + 0.5;

        ParticleEngine pm = mc.particleEngine;

        for (int i = 0; i < 200; i++) {
            double angle = level.random.nextDouble() * 2 * Math.PI;
            double speed = 0.2 + level.random.nextDouble() * 0.3;
            double dx = Math.cos(angle) * speed;
            double dz = Math.sin(angle) * speed;
            double dy = 0.1 + level.random.nextDouble() * 0.1;

            Particle p = pm.createParticle(ModParticles.BONK.get(), x, y, z, dx, dy, dz);
            if (p != null) p.setColor(1f, 0f, 0f);
        }

        MonolithTileEntity.spawnIgniteParticles(pBlockEntity.getBlockPos());

        mc.level.playLocalSound(
                x, y, z,
                SoundEvents.GENERIC_EXPLODE, // pick your sound here
                SoundSource.BLOCKS,
                1.0f, // volume
                1.5f, // pitch
                false
        );

        pBlockEntity.setHasExpired(true);
    }

    private static void renderFloatingItems(PoseStack poseStack, MultiBufferSource buffer, int pPackedOverlay, int count, IntFunction<ItemStack> getItem, IntFunction<Double> getAngle) {
        poseStack.pushPose();
        poseStack.translate(0.5, 1.2, 0.5);
        poseStack.scale(1.2F, 1.2F, 1.2F);

        for (int i = 0; i < count; i++) {
            ItemStack item = getItem.apply(i);
            if (item.isEmpty()) continue;

            poseStack.pushPose();
            double angle = getAngle.apply(i);
            double y = Math.sin(angle) * 1.2;
            double x = Math.cos(angle) * 1.2;
            poseStack.translate(x, Math.sin(angle * 2) * 0.1, y);
            poseStack.mulPose(Vector3f.YN.rotation((float) (angle + Math.PI / 2f)));
            BakedModel ibakedmodel = mc.getItemRenderer().getModel(item, null, null, 0);
            mc.getItemRenderer().render(item, ItemTransforms.TransformType.GROUND, true, poseStack, buffer, 0xFFFFFF, pPackedOverlay, ibakedmodel);
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private static void renderText(BrewingAltarTileEntity tileEntity, PoseStack poseStack, MultiBufferSource bufferSource) {
        List<MutableComponent> lines = new ArrayList<>();
        Map<AlchemyIngredientItem.AlchemyIngredientType, Integer> typeCounts = new HashMap<>();
        Font fontRenderer = mc.font;

        BrewingAltarTileEntity.PercentageResult result = tileEntity.getProgressIncrease();
        List<ItemStack> filtered = new ArrayList<>();

        for (ItemStack stack : tileEntity.getAllIngredients()) {
            if (stack.getItem() instanceof AlchemyIngredientItem ingredient) {
                filtered.add(stack);
                AlchemyIngredientItem.AlchemyIngredientType type = ingredient.getType();
                typeCounts.put(type, typeCounts.getOrDefault(type, 0) + 1);
            }
        }

        int totalRelevant = 0;

        // Count non-neutral ingredient total
        for (Map.Entry<AlchemyIngredientItem.AlchemyIngredientType, Integer> entry : typeCounts.entrySet()) {
            if (entry.getKey() != AlchemyIngredientItem.AlchemyIngredientType.NEUTRAL) {
                totalRelevant += entry.getValue();
            }
        }

        for (Map.Entry<AlchemyIngredientItem.AlchemyIngredientType, Integer> entry : typeCounts.entrySet()) {
            AlchemyIngredientItem.AlchemyIngredientType type = entry.getKey();
            int count = entry.getValue();

            if (type != AlchemyIngredientItem.AlchemyIngredientType.NEUTRAL) {
                int percentage = (int) ((count / (float) totalRelevant) * 100);
                lines.add(AlchemyIngredientItem.getEffectComponent(type, percentage));
            }


        }

        if (!filtered.isEmpty()) {
            TextComponent display = new TextComponent("Adds ");
            String val1 = String.format("%.1f%%", result.min() * 100);

            if (result.isRange()) {
                String val2 = String.format("%.1f%%", result.max() * 100);
                display.append(new TextComponent("between "))
                        .append(new TextComponent(val1).withStyle(Style.EMPTY.withColor(0xF0E68C)))
                        .append(new TextComponent(" and "))
                        .append(new TextComponent(val2).withStyle(Style.EMPTY.withColor(0xF0E68C)));
            } else {
                display.append(new TextComponent(val1).withStyle(Style.EMPTY.withColor(0xF0E68C)));
            }

            display.append(new TextComponent(" to the Objective Progression"));
            lines.add(display);
        }


        if (tileEntity.isFilled()) {
            lines.add(new TextComponent("Right click to brew!").withStyle(Style.EMPTY.withColor(0x00ff04)));
        }

        int length = lines.size();
        for(int i = length - 1; i >= 0; --i) {
            MutableComponent text = lines.get(i);
            if (text != null) {
                float scale = 0.02F;
                int color = -1;
                int opacity = 1711276032;
                poseStack.pushPose();
                Matrix4f matrix4f = poseStack.last().pose();
                float offset = (float)(-fontRenderer.width(text) / 2);
                poseStack.translate(0.5F, 1.7F + 0.25F * (float)(length - i), 0.5F);
                poseStack.scale(scale, scale, scale);
                poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
                fontRenderer.drawInBatch(text, offset, 0.0F, color, false, matrix4f, bufferSource, true, opacity, 0xFFFFFF);
                fontRenderer.drawInBatch(text, offset, 0.0F, -1, false, matrix4f, bufferSource, false, 0xFFFFFF, 0xFFFFFF);
                poseStack.popPose();
            }
        }
    }

    private void spawnParticles(BlockPos pos, Random random, int color) {
        double centerX = pos.getX() + 0.5;
        double centerY = pos.getY() + 1.25;
        double centerZ = pos.getZ() + 0.5;

        ParticleEngine pm = mc.particleEngine;
        int particleCount = 3 + random.nextInt(3); // spawn 3 to 5 particles
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        for (int i = 0; i < particleCount; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = 0.05 + random.nextDouble() * 0.1; // 0.05 to 0.15
            double dx = Math.cos(angle) * speed;
            double dz = Math.sin(angle) * speed;
            double dy = 0.02 + random.nextDouble() * 0.03; // 0.02 to 0.05 upward

            Particle p = pm.createParticle(ModParticles.BONK.get(), centerX, centerY, centerZ, dx, dy, dz);
            if (p != null) p.setColor(r, g, b);
        }

    }



}
