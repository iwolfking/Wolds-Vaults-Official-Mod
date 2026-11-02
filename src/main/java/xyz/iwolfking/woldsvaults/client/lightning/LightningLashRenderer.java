package xyz.iwolfking.woldsvaults.client.lightning;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Draws Arc Lash's bolts (Lightning Fiend 6-piece): each flies out along its line at the speed the server gave and
 * spins about that line as it goes, a {@link #LENGTH}-block bolt turning like a blade. It's drawn the way vanilla draws
 * lightning (LightningBoltRenderer): a jagged channel of eight segments with two branches off its upper part, every
 * segment a square tube in four widening layers of faint blue-white added together. The shape is vanilla's shrunk
 * down, but the tubes are much thicker than vanilla's proportions, which would be a hairline at this size. A bolt
 * re-forks every other tick and fades over {@link #FADE} ticks where it lands.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class LightningLashRenderer {
    private static final float LENGTH = 3.0F;
    private static final float WIDTH = 0.1F;
    private static final float SPIN = 50.0F;
    private static final int FADE = 3;
    private static final List<Bolt> BOLTS = new ArrayList<>();

    private static final class Bolt {
        final Vec3 origin;
        final Vec3 direction;
        final float length;
        final float speed;
        final long seed;
        final int flightTicks;
        int age;

        Bolt(Vec3 origin, Vec3 direction, float length, float speed, long seed) {
            this.origin = origin;
            this.direction = direction;
            this.length = length;
            this.speed = speed;
            this.seed = seed;
            this.flightTicks = (int) Math.ceil(length / speed);
        }
    }

    private LightningLashRenderer() {
    }

    public static void add(Vec3 origin, List<Vec3> directions, List<Float> lengths, float speed, long seed) {
        for (int i = 0; i < directions.size(); i++) {
            Vec3 direction = directions.get(i);
            if (direction.lengthSqr() > 1.0E-6) {
                BOLTS.add(new Bolt(origin, direction.normalize(), lengths.get(i), speed, seed + i * 341873128712L));
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !BOLTS.isEmpty()) {
            BOLTS.removeIf(bolt -> ++bolt.age > bolt.flightTicks + FADE);
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || BOLTS.isEmpty()) {
            return;
        }
        Vec3 cam = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(-cam.x, -cam.y, -cam.z);
        Matrix4f matrix = pose.last().pose();
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer consumer = buffers.getBuffer(RenderType.lightning());
        for (Bolt bolt : BOLTS) {
            draw(consumer, matrix, bolt, event.getPartialTick());
        }
        buffers.endBatch(RenderType.lightning());
        pose.popPose();
    }

    private static void draw(VertexConsumer consumer, Matrix4f matrix, Bolt bolt, float partialTick) {
        float time = bolt.age + partialTick;
        float alpha = time <= bolt.flightTicks ? 1.0F : 1.0F - (time - bolt.flightTicks) / FADE;
        if (alpha <= 0.0F) {
            return;
        }
        Vec3 center = bolt.origin.add(bolt.direction.scale(Math.min(bolt.length, bolt.speed * time)));
        Vec3 up = Math.abs(bolt.direction.y) > 0.9 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
        Vec3 a = bolt.direction.cross(up).normalize();
        Vec3 b = a.cross(bolt.direction).normalize();
        double angle = Math.toRadians(time * SPIN + (bolt.seed & 255));
        Vec3 along = a.scale(Math.cos(angle)).add(b.scale(Math.sin(angle)));
        vanillaBolt(consumer, matrix, center, bolt.direction, along, bolt.direction.cross(along), bolt.seed + bolt.age / 2, alpha);
    }

    private static void vanillaBolt(VertexConsumer consumer, Matrix4f matrix, Vec3 center, Vec3 x, Vec3 y, Vec3 z, long seed, float alpha) {
        float segment = LENGTH / 8.0F;
        float step = segment / 16.0F;
        float[] downX = new float[8];
        float[] downZ = new float[8];
        float totalX = 0.0F;
        float totalZ = 0.0F;
        Random random = new Random(seed);
        for (int i = 7; i >= 0; --i) {
            downX[i] = totalX;
            downZ[i] = totalZ;
            totalX += random.nextInt(11) - 5;
            totalZ += random.nextInt(11) - 5;
        }
        float shiftX = totalX * 0.5F;
        float shiftZ = totalZ * 0.5F;
        Frame frame = new Frame(matrix, consumer, center, x, y, z, segment, step, shiftX, shiftZ, alpha);
        for (int layer = 0; layer < 4; ++layer) {
            Random shape = new Random(seed);
            for (int branch = 0; branch < 3; ++branch) {
                int top = branch > 0 ? 7 - branch : 7;
                int bottom = branch > 0 ? top - 2 : 0;
                float lowX = downX[top] - totalX;
                float lowZ = downZ[top] - totalZ;
                for (int seg = top; seg >= bottom; --seg) {
                    float highX = lowX;
                    float highZ = lowZ;
                    if (branch == 0) {
                        lowX += shape.nextInt(11) - 5;
                        lowZ += shape.nextInt(11) - 5;
                    } else {
                        lowX += shape.nextInt(31) - 15;
                        lowZ += shape.nextInt(31) - 15;
                    }
                    float highWidth = 0.1F + layer * 0.2F;
                    float lowWidth = 0.1F + layer * 0.2F;
                    if (branch == 0) {
                        highWidth *= seg * 0.1F + 1.0F;
                        lowWidth *= (seg - 1.0F) * 0.1F + 1.0F;
                    }
                    frame.quad(lowX, lowZ, seg, highX, highZ, highWidth, lowWidth, false, false, true, false);
                    frame.quad(lowX, lowZ, seg, highX, highZ, highWidth, lowWidth, true, false, true, true);
                    frame.quad(lowX, lowZ, seg, highX, highZ, highWidth, lowWidth, true, true, false, true);
                    frame.quad(lowX, lowZ, seg, highX, highZ, highWidth, lowWidth, false, true, false, false);
                }
            }
        }
    }

    private record Frame(Matrix4f matrix, VertexConsumer consumer, Vec3 center, Vec3 x, Vec3 y, Vec3 z,
                         float segment, float step, float shiftX, float shiftZ, float alpha) {
        void quad(float lowX, float lowZ, int seg, float highX, float highZ, float highWidth, float lowWidth,
                  boolean a, boolean b, boolean c, boolean d) {
            vertex(lowX, lowZ, a ? lowWidth : -lowWidth, b ? lowWidth : -lowWidth, seg);
            vertex(highX, highZ, a ? highWidth : -highWidth, b ? highWidth : -highWidth, seg + 1);
            vertex(highX, highZ, c ? highWidth : -highWidth, d ? highWidth : -highWidth, seg + 1);
            vertex(lowX, lowZ, c ? lowWidth : -lowWidth, d ? lowWidth : -lowWidth, seg);
        }

        private void vertex(float offsetX, float offsetZ, float widthX, float widthZ, int seg) {
            double lx = (offsetX + this.shiftX) * this.step + widthX * WIDTH;
            double ly = seg * this.segment - LENGTH * 0.5;
            double lz = (offsetZ + this.shiftZ) * this.step + widthZ * WIDTH;
            Vec3 at = this.center.add(this.x.scale(lx)).add(this.y.scale(ly)).add(this.z.scale(lz));
            this.consumer.vertex(this.matrix, (float) at.x, (float) at.y, (float) at.z).color(0.45F, 0.45F, 0.5F, 0.3F * this.alpha).endVertex();
        }
    }
}
