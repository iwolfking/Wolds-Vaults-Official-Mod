package xyz.iwolfking.woldsvaults.client.lightning;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.modifiers.gear.lightning.LightningFiend;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Draws a lightning dash (Lightning Fiend 6-piece): while the player crosses the path they are hidden and drawn as a
 * knot of crackling bolts, trailing one big bolt back to where they set off; once they reappear the trail fades out
 * over {@link #FADE} ticks. The bolts are jagged channels with rounded corners and forks, each layer (a wide blue
 * glow, a pale body about as wide as a player, a white-hot core) one soft-edged strip, drawn with vanilla
 * lightning's additive render type and re-forking every other tick to flicker. Also draws the form's short arcs
 * (Arc Lash, Lightning Rod) as thin whips that fade over {@link #ARC_LIFE} ticks, and Thunderstrikes: a bolt
 * {@link #STRIKE_SCALE} times a dash trail's width with a ring of arcs racing out across the ground.
 * A player who dashed sideways or back is drawn facing the way they went when they reappear, for {@link #FACE_HOLD}
 * ticks, then eases back over {@link #FACE_BLEND}; only the drawn body and head turn, so the camera, which follows
 * the player's own facing even in third person, stays put.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class LightningDashRenderer {
    private static final int FADE = 12;
    private static final int LINGER = 2;
    private static final int FACE_HOLD = 6;
    private static final int FACE_BLEND = 6;
    private static final int FORKS = 5;
    private static final int ARC_LIFE = 5;
    private static final int STRIKE_LIFE = 16;
    private static final float STRIKE_SCALE = 10.0F;
    private static final double STRIKE_INSIDE = 3.0;
    private static final List<Streak> STREAKS = new ArrayList<>();
    private static final List<Arcs> ARCS = new ArrayList<>();
    private static final List<Strike> STRIKES = new ArrayList<>();
    private static int turnedId = -1;
    private static final float[] SAVED = new float[4];

    private static final class Streak {
        final int entityId;
        final List<Vec3> path;
        final int ticks;
        final long seed;
        int age;

        Streak(int entityId, List<Vec3> path, int ticks, long seed) {
            this.entityId = entityId;
            this.path = path;
            this.ticks = ticks;
            this.seed = seed;
        }
    }

    private static final class Arcs {
        final List<Vec3> points;
        final long seed;
        int age;

        Arcs(List<Vec3> points, long seed) {
            this.points = points;
            this.seed = seed;
        }
    }

    private static final class Strike {
        final Vec3 at;
        final float radius;
        final long seed;
        int age;

        Strike(Vec3 at, float radius, long seed) {
            this.at = at;
            this.radius = radius;
            this.seed = seed;
        }
    }

    private LightningDashRenderer() {
    }

    public static void add(int entityId, List<Vec3> path, int ticks, long seed) {
        STREAKS.add(new Streak(entityId, path, ticks, seed));
    }

    public static void addStrike(Vec3 at, float radius, long seed) {
        STRIKES.add(new Strike(at, radius, seed));
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            mc.level.setSkyFlashTime(3);
        }
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        if (horizontalDistanceSqr(cam, at) < STRIKE_INSIDE * STRIKE_INSIDE) {
            LightningSpeedLines.flash();
        }
    }

    private static double horizontalDistanceSqr(Vec3 a, Vec3 b) {
        double dx = a.x - b.x, dz = a.z - b.z;
        return dx * dx + dz * dz;
    }

    public static void addArcs(List<Vec3> points, long seed) {
        if (points.size() >= 2) {
            ARCS.add(new Arcs(points, seed));
        }
    }

    /** Whether the entity is crossing a dash as lightning right now, and so not drawn as itself. */
    public static boolean isTravelling(int entityId) {
        for (Streak streak : STREAKS) {
            if (streak.entityId == entityId && streak.age < streak.ticks) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            STREAKS.removeIf(streak -> ++streak.age > streak.ticks + FADE);
            ARCS.removeIf(arcs -> ++arcs.age > ARC_LIFE);
            STRIKES.removeIf(strike -> ++strike.age > STRIKE_LIFE);
        }
    }

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (STREAKS.isEmpty()) {
            return;
        }
        Player player = event.getPlayer();
        if (isTravelling(player.getId())) {
            event.setCanceled(true);
            return;
        }
        Streak streak = latest(player.getId());
        if (streak == null) {
            return;
        }
        Vec3 way = streak.path.get(streak.path.size() - 1).subtract(streak.path.get(0));
        double flat = way.x * way.x + way.z * way.z;
        float after = streak.age + event.getPartialTick() - streak.ticks;
        float weight = after <= FACE_HOLD ? 1.0F : 1.0F - (after - FACE_HOLD) / FACE_BLEND;
        if (flat < 0.25 || weight <= 0.0F) {
            return;
        }
        float yaw = (float) (Mth.atan2(way.z, way.x) * (180.0 / Math.PI)) - 90.0F;
        float partialTick = event.getPartialTick();
        float body = Mth.rotLerp(weight, Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot), yaw);
        float head = Mth.rotLerp(weight, Mth.rotLerp(partialTick, player.yHeadRotO, player.yHeadRot), yaw);
        turnedId = player.getId();
        SAVED[0] = player.yBodyRotO;
        SAVED[1] = player.yBodyRot;
        SAVED[2] = player.yHeadRotO;
        SAVED[3] = player.yHeadRot;
        player.yBodyRotO = player.yBodyRot = body;
        player.yHeadRotO = player.yHeadRot = head;
    }

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        Player player = event.getPlayer();
        if (turnedId == player.getId()) {
            player.yBodyRotO = SAVED[0];
            player.yBodyRot = SAVED[1];
            player.yHeadRotO = SAVED[2];
            player.yHeadRot = SAVED[3];
            turnedId = -1;
        }
    }

    private static Streak latest(int entityId) {
        Streak latest = null;
        for (Streak streak : STREAKS) {
            if (streak.entityId == entityId && (latest == null || streak.age < latest.age)) {
                latest = streak;
            }
        }
        return latest;
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Player player = Minecraft.getInstance().player;
        if (!STREAKS.isEmpty() && player != null && isTravelling(player.getId())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || STREAKS.isEmpty() && ARCS.isEmpty() && STRIKES.isEmpty()) {
            return;
        }
        Vec3 cam = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(-cam.x, -cam.y, -cam.z);
        Matrix4f matrix = pose.last().pose();
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer consumer = buffers.getBuffer(RenderType.lightning());
        for (Streak streak : STREAKS) {
            draw(consumer, matrix, cam, streak, event.getPartialTick());
        }
        for (Arcs arcs : ARCS) {
            draw(consumer, matrix, cam, arcs, event.getPartialTick());
        }
        for (Strike strike : STRIKES) {
            draw(consumer, matrix, cam, strike, event.getPartialTick());
        }
        buffers.endBatch(RenderType.lightning());
        pose.popPose();
    }

    private static void draw(VertexConsumer consumer, Matrix4f matrix, Vec3 cam, Streak streak, float partialTick) {
        float time = streak.age + partialTick;
        float life = time <= streak.ticks ? 1.0F : 1.0F - (time - streak.ticks) / FADE;
        if (life <= 0.0F) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Entity entity = mc.level == null ? null : mc.level.getEntity(streak.entityId);
        Vec3 body = new Vec3(0.0, (entity == null ? 1.8F : entity.getBbHeight()) * 0.5, 0.0);
        Vec3 end = streak.path.get(streak.path.size() - 1);
        Vec3 feet = entity != null ? entity.getPosition(partialTick) : LightningFiend.along(streak.path, time / streak.ticks);
        Vec3 from = streak.path.get(0).add(body);
        Vec3 head = (time < streak.ticks ? feet : end).add(body);

        Random random = new Random(streak.seed + streak.age / 2);
        if (from.distanceToSqr(head) > 0.04) {
            List<Vec3> channel = smooth(jagged(from, head, random, 4, 0.15), 2);
            layers(consumer, matrix, cam, channel, life, 1.0F, false);
            Vec3 along = head.subtract(from).normalize();
            for (int i = 0; i < FORKS && channel.size() > 4; i++) {
                Vec3 at = channel.get(1 + random.nextInt(channel.size() - 2));
                Vec3 out = along.scale(0.6).add(perpendicular(along, random)).normalize().scale(1.5 + random.nextDouble() * 2.0);
                layers(consumer, matrix, cam, smooth(jagged(at, at.add(out), random, 3, 0.2), 1), life, 0.4F, true);
            }
        }

        boolean firstPerson = entity == mc.getCameraEntity() && mc.options.getCameraType().isFirstPerson();
        if (time < streak.ticks + LINGER && !firstPerson) {
            float bodyLife = time < streak.ticks ? 1.0F : 1.0F - (time - streak.ticks) / LINGER;
            bodyBolts(consumer, matrix, cam, feet, body.y * 2.0, new Random(streak.seed * 31L + streak.age), bodyLife);
        }
    }

    private static void draw(VertexConsumer consumer, Matrix4f matrix, Vec3 cam, Arcs arcs, float partialTick) {
        float life = 1.0F - (arcs.age + partialTick) / ARC_LIFE;
        if (life <= 0.0F) {
            return;
        }
        Random random = new Random(arcs.seed + arcs.age);
        for (int i = 0; i + 1 < arcs.points.size(); i += 2) {
            Vec3 from = arcs.points.get(i), to = arcs.points.get(i + 1);
            if (from.distanceToSqr(to) < 0.01) {
                continue;
            }
            List<Vec3> channel = smooth(jagged(from, to, random, 4, 0.12), 1);
            layers(consumer, matrix, cam, channel, life, 0.35F, false);
            Vec3 at = channel.get(channel.size() / 2);
            Vec3 along = to.subtract(from).normalize();
            Vec3 out = along.scale(0.5).add(perpendicular(along, random)).normalize().scale(0.8 + random.nextDouble());
            layers(consumer, matrix, cam, smooth(jagged(at, at.add(out), random, 2, 0.2), 1), life, 0.2F, true);
        }
    }

    private static void draw(VertexConsumer consumer, Matrix4f matrix, Vec3 cam, Strike strike, float partialTick) {
        float time = strike.age + partialTick;
        float life = 1.0F - time / STRIKE_LIFE;
        if (life <= 0.0F) {
            return;
        }
        Random random = new Random(strike.seed + strike.age / 2);
        Vec3 ground = strike.at.add(0.0, 0.3, 0.0);
        if (horizontalDistanceSqr(cam, strike.at) >= STRIKE_INSIDE * STRIKE_INSIDE) {
            Vec3 top = strike.at.add((random.nextDouble() - 0.5) * 8.0, 90.0, (random.nextDouble() - 0.5) * 8.0);
            List<Vec3> channel = smooth(jagged(top, strike.at, random, 6, 0.06), 2);
            layers(consumer, matrix, cam, channel, life, STRIKE_SCALE, false);
            Vec3 down = strike.at.subtract(top).normalize();
            for (int i = 0; i < 6; i++) {
                Vec3 at = channel.get(channel.size() * 2 / 5 + random.nextInt(channel.size() / 2));
                Vec3 out = down.scale(0.5).add(perpendicular(down, random)).normalize().scale(6.0 + random.nextDouble() * 6.0);
                layers(consumer, matrix, cam, smooth(jagged(at, at.add(out), random, 3, 0.2), 1), life, 3.0F, true);
            }
        }
        float reach = strike.radius * Math.min(1.0F, time / 5.0F);
        if (reach < 0.5F) {
            return;
        }
        int segments = 24;
        double turn = random.nextDouble() * Math.PI * 2.0;
        for (int i = 0; i < segments; i++) {
            double a0 = turn + i * Math.PI * 2.0 / segments, a1 = turn + (i + 1) * Math.PI * 2.0 / segments;
            Vec3 p = ground.add(Math.cos(a0) * reach, 0.0, Math.sin(a0) * reach);
            Vec3 q = ground.add(Math.cos(a1) * reach, 0.0, Math.sin(a1) * reach);
            layers(consumer, matrix, cam, smooth(jagged(p, q, random, 2, 0.25), 1), life, 1.2F, false);
            if (i % 3 == 0) {
                layers(consumer, matrix, cam, smooth(jagged(ground, p, random, 3, 0.2), 1), life, 0.6F, true);
            }
        }
    }

    private static void bodyBolts(VertexConsumer consumer, Matrix4f matrix, Vec3 cam, Vec3 feet, double height, Random random, float life) {
        for (int i = 0; i < 3; i++) {
            Vec3 bottom = feet.add((random.nextDouble() - 0.5) * 0.5, -0.1, (random.nextDouble() - 0.5) * 0.5);
            Vec3 top = feet.add((random.nextDouble() - 0.5) * 0.5, height + 0.1, (random.nextDouble() - 0.5) * 0.5);
            layers(consumer, matrix, cam, smooth(jagged(bottom, top, random, 3, 0.15), 1), life, 0.45F, true);
        }
    }

    private static void layers(VertexConsumer consumer, Matrix4f matrix, Vec3 cam, List<Vec3> path, float life, float scale, boolean fork) {
        ribbon(consumer, matrix, cam, path, 1.0F * scale, fork, 0.25F, 0.55F, 1.0F, 0.3F * life);
        ribbon(consumer, matrix, cam, path, 0.5F * scale, fork, 0.55F, 0.85F, 1.0F, 0.55F * life);
        ribbon(consumer, matrix, cam, path, 0.18F * scale, fork, 1.0F, 1.0F, 1.0F, 1.0F * life);
    }

    private static List<Vec3> smooth(List<Vec3> points, int passes) {
        for (int pass = 0; pass < passes; pass++) {
            List<Vec3> next = new ArrayList<>(points.size() * 2);
            next.add(points.get(0));
            for (int i = 0; i < points.size() - 1; i++) {
                Vec3 a = points.get(i), b = points.get(i + 1);
                next.add(a.lerp(b, 0.25));
                next.add(a.lerp(b, 0.75));
            }
            next.add(points.get(points.size() - 1));
            points = next;
        }
        return points;
    }

    private static List<Vec3> jagged(Vec3 from, Vec3 to, Random random, int passes, double roughness) {
        List<Vec3> points = new ArrayList<>(List.of(from, to));
        double amplitude = from.distanceTo(to) * roughness;
        for (int pass = 0; pass < passes; pass++) {
            List<Vec3> next = new ArrayList<>(points.size() * 2);
            for (int i = 0; i < points.size() - 1; i++) {
                Vec3 a = points.get(i), b = points.get(i + 1);
                next.add(a);
                Vec3 mid = a.add(b).scale(0.5);
                next.add(mid.add(perpendicular(b.subtract(a), random).scale((random.nextDouble() * 2.0 - 1.0) * amplitude)));
            }
            next.add(points.get(points.size() - 1));
            points = next;
            amplitude *= 0.55;
        }
        return points;
    }

    private static Vec3 perpendicular(Vec3 direction, Random random) {
        Vec3 dir = direction.lengthSqr() < 1.0E-8 ? new Vec3(1.0, 0.0, 0.0) : direction.normalize();
        Vec3 up = Math.abs(dir.y) > 0.9 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
        Vec3 a = dir.cross(up).normalize();
        Vec3 b = dir.cross(a).normalize();
        double angle = random.nextDouble() * Math.PI * 2.0;
        return a.scale(Math.cos(angle)).add(b.scale(Math.sin(angle)));
    }

    private static void ribbon(VertexConsumer consumer, Matrix4f matrix, Vec3 cam, List<Vec3> path, float halfWidth, boolean fork, float r, float g, float b, float a) {
        int n = path.size();
        Vec3[] sides = new Vec3[n];
        for (int i = 0; i < n; i++) {
            Vec3 p = path.get(i);
            Vec3 side = path.get(Math.min(i + 1, n - 1)).subtract(path.get(Math.max(i - 1, 0))).cross(p.subtract(cam));
            if (side.lengthSqr() < 1.0E-10) {
                sides[i] = i > 0 ? sides[i - 1] : Vec3.ZERO;
                continue;
            }
            side = side.normalize();
            if (i > 0 && side.dot(sides[i - 1]) < 0.0) {
                side = side.scale(-1.0);
            }
            sides[i] = side;
        }
        for (int i = 0; i < n; i++) {
            float t = n < 2 ? 1.0F : (float) i / (n - 1);
            float width = halfWidth * (fork ? 1.0F - 0.85F * t : Math.min(1.0F, t / 0.08F));
            sides[i] = sides[i].normalize().scale(width);
        }
        for (int i = 0; i < n - 1; i++) {
            Vec3 p = path.get(i), q = path.get(i + 1);
            Vec3 ps = sides[i], qs = sides[i + 1];
            soft(consumer, matrix, p.add(ps), p, q, q.add(qs), r, g, b, a);
            soft(consumer, matrix, p.subtract(ps), p, q, q.subtract(qs), r, g, b, a);
        }
    }

    private static void soft(VertexConsumer consumer, Matrix4f matrix, Vec3 edge1, Vec3 middle1, Vec3 middle2, Vec3 edge2, float r, float g, float b, float a) {
        vertex(consumer, matrix, edge1, r, g, b, 0.0F);
        vertex(consumer, matrix, middle1, r, g, b, a);
        vertex(consumer, matrix, middle2, r, g, b, a);
        vertex(consumer, matrix, edge2, r, g, b, 0.0F);
        vertex(consumer, matrix, edge2, r, g, b, 0.0F);
        vertex(consumer, matrix, middle2, r, g, b, a);
        vertex(consumer, matrix, middle1, r, g, b, a);
        vertex(consumer, matrix, edge1, r, g, b, 0.0F);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, Vec3 v, float r, float g, float b, float a) {
        consumer.vertex(matrix, (float) v.x, (float) v.y, (float) v.z).color(r, g, b, a).endVertex();
    }
}
