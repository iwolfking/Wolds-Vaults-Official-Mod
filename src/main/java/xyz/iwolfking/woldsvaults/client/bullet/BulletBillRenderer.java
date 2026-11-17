package xyz.iwolfking.woldsvaults.client.bullet;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.client.arcane.GlowRenderTypes;
import xyz.iwolfking.woldsvaults.modifiers.gear.bullet.BulletBill;

import javax.annotation.Nullable;

/**
 * Draws a Bullet Bill in place of a player who is one: the model pointing where they look (never nose down into the
 * ground they slide along), banking into turns, its tail burning with a white-hot glow that flares while the jets fire,
 * and during a Bullet Dash boost corkscrewing through a spin, its fire turning into a pinwheel so the roll shows from
 * straight behind, while it pulses red, both as the hurt tint and as a red glow over its body. The Banzai and King Bill helmets draw it many times
 * larger, resting on the ground, with the eye and jaw painted on its hull in place of the small Bill's eyes and fists;
 * a King Bill is light red and glows with heat. A giant Bill isn't drawn while the camera is inside it, so being caught
 * in a corridor doesn't black out the view.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class BulletBillRenderer {
    private static final ResourceLocation TEXTURE = WoldsVaults.id("textures/entity/bullet_bill.png");
    private static final ResourceLocation BANZAI_TEXTURE = WoldsVaults.id("textures/entity/banzai_bill.png");
    private static final ResourceLocation KING_TEXTURE = WoldsVaults.id("textures/entity/king_bill.png");
    // white over the body and black elsewhere, so the red glow lights the body but not the eyes and fists
    private static final ResourceLocation GLOW = WoldsVaults.id("textures/entity/bullet_bill_glow.png");
    private static final float SCALE = 1.15F;
    private static final float GLOW_SCALE = 1.04F;
    // how high the Bill's middle sits above the player's feet, in blocks (about its radius, so it rests on the ground)
    public static final double CENTER = 0.55;
    private static final float MAX_BANK = 35.0F;
    // the bank is how far the Bill's heading has run ahead of a lagging copy of itself
    private static final float BANK_PER_LAG = 0.8F;
    private static final float BANK_LAG_TICKS = 3.0F;
    // the tail's back face, and the rings of its glow from the middle out, in model units (a block at scale 1)
    private static final float TAIL_FACE = -0.64F;
    private static final float[] FLAME_RADII = {0.0F, 0.15F, 0.29F, 0.46F};
    private static final float[][] FLAME_COLOURS = {{1.0F, 0.96F, 0.8F, 1.0F}, {1.0F, 0.62F, 0.2F, 0.9F}, {1.0F, 0.36F, 0.58F, 0.55F}, {1.0F, 0.2F, 0.5F, 0.0F}};
    private static final int FLAME_SEGMENTS = 20;
    // the spin: how many degrees the nose swings off the line of flight at most, and the pinwheel's blades, in model units
    // and radians, each running from the hub out to an elbow and swept round to a tip beyond the hull
    private static final float SPIN_LEAN = 14.0F;
    private static final int PINWHEEL_BLADES = 4;
    private static final float PINWHEEL_ELBOW = 0.34F;
    private static final float PINWHEEL_TIP = 0.8F;
    private static final float PINWHEEL_WIDTH = 0.62F;
    private static final float PINWHEEL_SWEEP = 1.15F;
    // how far the model reaches from its middle, in blocks at scale 1: to the tail, the nose, and out to the side
    private static final double TAIL_REACH = 10.0 / 16.0;
    private static final double NOSE_REACH = 13.0 / 16.0;
    private static final double SIDE_REACH = 7.5 / 16.0;
    // the chase view: blocks behind the Bill (a base plus some per unit of its size), the share of that it sits above the
    // Bill's line, which puts the Bill tail on and a little below the middle of the screen with no tilt, and how many
    // ticks the view trails its turns by
    private static final double CHASE_BASE = 2.5;
    private static final double CHASE_PER_SCALE = 2.2;
    private static final double CHASE_RISE = 0.11;
    private static final float CHASE_LAG_TICKS = 3.0F;
    // and a giant Bill's front view: how far out, how far ahead of its middle it orbits (about where the face is), and the degrees
    // round to its side and down that bring the eye and teeth on its flank into view
    private static final double FRONT_DISTANCE = 1.9;
    private static final double FRONT_AHEAD = 0.35;
    private static final float FRONT_ORBIT = 40.0F;
    private static final float FRONT_TILT = 8.0F;
    private static final float VIEW_EASE_TICKS = 12.0F;

    // Where a Bill's camera goes: the point it orbits, its rotation, how far above that it sits, how far out it wants to be,
    // and how far it has eased from wherever the camera was as the form began (0 to 1).
    public record BillView(Vec3 pivot, float yaw, float pitch, double rise, double distance, float blend) {
    }

    private static ModelPart model;

    private BulletBillRenderer() {
    }

    private static ModelPart model() {
        if (model == null) {
            model = Minecraft.getInstance().getEntityModels().bakeLayer(BulletBillModel.LAYER);
        }
        return model;
    }

    private static ResourceLocation texture(int variant) {
        return variant == BulletBill.VARIANT_KING ? KING_TEXTURE : variant == BulletBill.VARIANT_BANZAI ? BANZAI_TEXTURE : TEXTURE;
    }

    // the Bill's middle, from the player's feet, in blocks
    static double center(ClientBulletBill.Bill bill) {
        return CENTER * bill.tuning.scale();
    }

    // an angle eased towards a target with a lag of tau ticks, or snapped to it if it wasn't followed a moment ago
    private static float follow(float current, float target, float now, float last, float tau) {
        float dt = now - last;
        if (last < 0.0F || dt < 0.0F || dt > 5.0F) {
            return target;
        }
        return current + Mth.wrapDegrees(target - current) * (1.0F - (float) Math.exp(-dt / tau));
    }

    // The third person view of a Bill, from the camera's rotation so far, or null for anyone who isn't one (and for a normal
    // Bill's front view, which stays vanilla's). The chase view keeps whatever way the camera was turned, so Shoulder
    // Surfing's free look still works.
    @Nullable
    public static BillView billView(Player player, float cameraYaw, float cameraPitch, boolean front, float partialTick) {
        ClientBulletBill.Bill bill = ClientBulletBill.bill(player);
        if (bill == null || (front && bill.tuning.scale() <= 1.0F)) {
            return null;
        }
        float scale = bill.tuning.scale();
        Vec3 middle = player.getPosition(partialTick).add(0.0, center(bill), 0.0);
        float eased = Mth.clamp((bill.totalTicks - bill.ticksLeft + partialTick) / VIEW_EASE_TICKS, 0.0F, 1.0F);
        float blend = eased * eased * (3.0F - 2.0F * eased);
        if (front) {
            Vec3 heading = Vec3.directionFromRotation(player.getViewXRot(partialTick), player.getViewYRot(partialTick));
            return new BillView(middle.add(heading.scale(FRONT_AHEAD * scale)), cameraYaw + FRONT_ORBIT, Mth.clamp(cameraPitch + FRONT_TILT, -90.0F, 90.0F), 0.0, FRONT_DISTANCE * scale, blend);
        }
        float now = player.tickCount + partialTick;
        bill.cameraYaw = follow(bill.cameraYaw, cameraYaw, now, bill.cameraTime, CHASE_LAG_TICKS);
        bill.cameraPitch = follow(bill.cameraPitch, cameraPitch, now, bill.cameraTime, CHASE_LAG_TICKS);
        bill.cameraTime = now;
        double distance = CHASE_BASE + CHASE_PER_SCALE * scale;
        return new BillView(middle, bill.cameraYaw, Mth.clamp(bill.cameraPitch, -90.0F, 90.0F), distance * CHASE_RISE, distance, blend);
    }

    // the tail's fire, drawn in the model's own space: a white-hot disc ringed orange then pink, facing back, and a tongue
    // of flame streaming out of it, which together flare while the jets fire and most of all in a boost
    private static void flame(PoseStack pose, MultiBufferSource buffers, ClientBulletBill.Bill bill, float time, float spinAmount) {
        Matrix4f matrix = pose.last().pose();
        VertexConsumer consumer = buffers.getBuffer(GlowRenderTypes.GLOW);
        float power = bill.boosting() ? 1.3F : bill.firing() ? 1.0F : 0.6F;
        float pulse = 1.0F + 0.12F * Mth.sin(time * 1.7F);
        float reach = (0.6F + 0.4F * power) * pulse;
        float strength = Math.min(1.0F, 0.45F + 0.55F * power);
        for (int ring = 0; ring < FLAME_RADII.length - 1; ring++) {
            float inner = FLAME_RADII[ring] * reach;
            float outer = FLAME_RADII[ring + 1] * reach;
            float[] from = FLAME_COLOURS[ring];
            float[] to = FLAME_COLOURS[ring + 1];
            for (int i = 0; i < FLAME_SEGMENTS; i++) {
                float a = i * Mth.TWO_PI / FLAME_SEGMENTS;
                float b = (i + 1) * Mth.TWO_PI / FLAME_SEGMENTS;
                consumer.vertex(matrix, Mth.cos(a) * inner, Mth.sin(a) * inner, TAIL_FACE).color(from[0], from[1], from[2], from[3] * strength).endVertex();
                consumer.vertex(matrix, Mth.cos(b) * inner, Mth.sin(b) * inner, TAIL_FACE).color(from[0], from[1], from[2], from[3] * strength).endVertex();
                consumer.vertex(matrix, Mth.cos(b) * outer, Mth.sin(b) * outer, TAIL_FACE).color(to[0], to[1], to[2], to[3] * strength).endVertex();
                consumer.vertex(matrix, Mth.cos(a) * outer, Mth.sin(a) * outer, TAIL_FACE).color(to[0], to[1], to[2], to[3] * strength).endVertex();
            }
        }
        float length = 0.3F + 0.6F * power * pulse;
        float width = 0.17F * reach;
        tongue(consumer, matrix, width, 0.0F, length, strength);
        tongue(consumer, matrix, 0.0F, width, length, strength);
        if (spinAmount > 0.0F) {
            pinwheel(consumer, matrix, Math.min(1.0F, spinAmount * 2.0F));
        }
    }

    // a Bill is round from behind, so the fire turns with its roll to show the spin
    private static void pinwheel(VertexConsumer consumer, Matrix4f matrix, float strength) {
        for (int blade = 0; blade < PINWHEEL_BLADES; blade++) {
            float a = blade * Mth.TWO_PI / PINWHEEL_BLADES;
            float b = a + PINWHEEL_WIDTH;
            float tip = a + PINWHEEL_SWEEP;
            consumer.vertex(matrix, 0.0F, 0.0F, TAIL_FACE).color(1.0F, 0.85F, 0.45F, strength).endVertex();
            consumer.vertex(matrix, Mth.cos(a) * PINWHEEL_ELBOW, Mth.sin(a) * PINWHEEL_ELBOW, TAIL_FACE).color(1.0F, 0.6F, 0.15F, strength).endVertex();
            consumer.vertex(matrix, Mth.cos(tip) * PINWHEEL_TIP, Mth.sin(tip) * PINWHEEL_TIP, TAIL_FACE).color(1.0F, 0.2F, 0.08F, 0.0F).endVertex();
            consumer.vertex(matrix, Mth.cos(b) * PINWHEEL_ELBOW, Mth.sin(b) * PINWHEEL_ELBOW, TAIL_FACE).color(1.0F, 0.6F, 0.15F, strength).endVertex();
        }
    }

    private static void tongue(VertexConsumer consumer, Matrix4f matrix, float x, float y, float length, float strength) {
        consumer.vertex(matrix, -x, -y, TAIL_FACE).color(1.0F, 0.8F, 0.35F, 0.9F * strength).endVertex();
        consumer.vertex(matrix, x, y, TAIL_FACE).color(1.0F, 0.8F, 0.35F, 0.9F * strength).endVertex();
        consumer.vertex(matrix, 0.0F, 0.0F, TAIL_FACE - length).color(1.0F, 0.3F, 0.2F, 0.0F).endVertex();
        consumer.vertex(matrix, 0.0F, 0.0F, TAIL_FACE - length).color(1.0F, 0.3F, 0.2F, 0.0F).endVertex();
    }

    // A box round all of a giant Bill, or null for anyone who isn't one.
    @Nullable
    public static AABB giantBounds(Player player) {
        ClientBulletBill.Bill bill = ClientBulletBill.bill(player);
        if (bill == null || bill.tuning.scale() <= 1.0F) {
            return null;
        }
        Vec3 middle = player.position().add(0.0, center(bill), 0.0);
        return new AABB(middle, middle).inflate(NOSE_REACH * SCALE * bill.tuning.scale());
    }

    private static boolean cameraInside(Vec3 middle, float yaw, float pitch, float size) {
        Vec3 offset = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().subtract(middle);
        Vec3 heading = Vec3.directionFromRotation(pitch, yaw);
        double along = offset.dot(heading);
        return along > -TAIL_REACH * size && along < NOSE_REACH * size && offset.subtract(heading.scale(along)).length() < SIDE_REACH * size;
    }

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        Player player = event.getPlayer();
        ClientBulletBill.Bill bill = ClientBulletBill.bill(player);
        if (bill == null) {
            return;
        }
        event.setCanceled(true);
        float partialTick = event.getPartialTick();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource buffers = event.getMultiBufferSource();
        int variant = bill.tuning.variant();
        float size = SCALE * bill.tuning.scale();

        float yaw = player.getViewYRot(partialTick);
        float pitch = player.getViewXRot(partialTick);
        if (player.isOnGround()) {
            pitch = Math.min(pitch, 0.0F);
        }
        if (bill.tuning.scale() > 1.0F && cameraInside(player.getPosition(partialTick).add(0.0, center(bill), 0.0), yaw, pitch, size)) {
            return;
        }
        float time = player.tickCount + partialTick;
        bill.bankYaw = follow(bill.bankYaw, yaw, time, bill.bankTime, BANK_LAG_TICKS);
        bill.bankTime = time;
        // turning right raises the yaw, and a positive roll here dips the right side
        float bank = Mth.clamp(Mth.wrapDegrees(yaw - bill.bankYaw) * BANK_PER_LAG, -MAX_BANK, MAX_BANK);
        float red = bill.redPulse(partialTick);
        // a King Bill's steady, slowly breathing heat
        float heat = variant == BulletBill.VARIANT_KING ? 0.2F + 0.1F * Mth.sin((player.tickCount + partialTick) * 0.25F) : 0.0F;

        pose.pushPose();
        pose.translate(0.0, center(bill), 0.0);
        pose.mulPose(Vector3f.YP.rotationDegrees(-yaw));
        pose.mulPose(Vector3f.XP.rotationDegrees(pitch));
        // the spin corkscrews: the nose circles the line of flight as the Bill rolls, swinging its flanks into view
        float spin = bill.spin(partialTick);
        float spinAmount = bill.spinAmount(partialTick);
        float lean = spinAmount * SPIN_LEAN;
        pose.mulPose(Vector3f.YP.rotationDegrees(lean * Mth.cos(spin * Mth.DEG_TO_RAD)));
        pose.mulPose(Vector3f.XP.rotationDegrees(lean * Mth.sin(spin * Mth.DEG_TO_RAD)));
        pose.mulPose(Vector3f.ZP.rotationDegrees(bank + spin));
        pose.scale(size, size, size);
        // vanilla's entity model flip (the model is laid out y down)
        pose.scale(-1.0F, -1.0F, 1.0F);

        ModelPart part = model();
        part.getChild("details").visible = variant == BulletBill.VARIANT_BULLET;
        int overlay = red > 0.5F ? OverlayTexture.pack(OverlayTexture.u(0.0F), OverlayTexture.v(true)) : OverlayTexture.NO_OVERLAY;
        part.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(texture(variant))), event.getPackedLight(), overlay);
        flame(pose, buffers, bill, time, spinAmount);
        if (red > 0.0F || heat > 0.0F) {
            pose.scale(GLOW_SCALE, GLOW_SCALE, GLOW_SCALE);
            part.render(pose, buffers.getBuffer(RenderType.eyes(GLOW)), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                    Math.max(red, heat), red * 0.12F + heat * 0.4F, red * 0.08F + heat * 0.08F, 1.0F);
        }
        pose.popPose();
    }
}
