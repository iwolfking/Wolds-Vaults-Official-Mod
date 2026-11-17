package xyz.iwolfking.woldsvaults.client.magatsuhi;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.gear.sets.GearSetHelper;
import xyz.iwolfking.woldsvaults.client.arcane.GlowRenderTypes;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.message.MagatsuhiActionMessage;
import xyz.iwolfking.woldsvaults.network.message.MagatsuhiFxMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Ten Magatsuhi on the client. With the set's cut, a right click with either katana in the main hand goes to the server
 * instead of using the item; holding the button counts once. Draws what the server sends: each player's cut, a
 * dark square with a red rim on a block face or standing in the air or on an enemy; the pale translucent box left
 * where someone teleported from, fading out; and the crossed red slash of both katanas.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, value = Dist.CLIENT)
public final class ClientMagatsuhi {
    private static final Set<ResourceLocation> KATANAS = Set.of(WoldsVaults.id("daikyo"), WoldsVaults.id("shokyo"));

    private static boolean canCut;
    // a right click only counts once the button has been let go since the last
    private static boolean released = true;

    private static final Map<Integer, Mark> MARKS = new HashMap<>();
    private static final List<Fading> FADING = new ArrayList<>();

    private record Mark(Vec3 at, int face, float yaw, int entity, float width, float height, long until) {
    }

    private static final class Fading {
        final MagatsuhiFxMessage message;
        final int ticks;
        int age;

        Fading(MagatsuhiFxMessage message) {
            this.message = message;
            this.ticks = Math.max(1, message.ticks());
        }
    }

    private ClientMagatsuhi() {
    }

    public static void set(boolean canCut) {
        ClientMagatsuhi.canCut = canCut;
    }

    public static void fx(MagatsuhiFxMessage message) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        MagatsuhiFxMessage.Kind[] kinds = MagatsuhiFxMessage.Kind.values();
        if (message.kind() < 0 || message.kind() >= kinds.length) {
            return;
        }
        switch (kinds[message.kind()]) {
            case MARK -> MARKS.put(message.owner(), new Mark(new Vec3(message.x(), message.y(), message.z()), message.face(), message.yaw(), message.entity(),
                    message.width(), message.height(), level.getGameTime() + message.ticks()));
            case CLEAR -> MARKS.remove(message.owner());
            case AFTERIMAGE, SLASH, CROSS -> FADING.add(new Fading(message));
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getPlayer();
        if (!event.getWorld().isClientSide || event.getHand() != InteractionHand.MAIN_HAND || player != Minecraft.getInstance().player
                || player.isSpectator() || !canCut || !GearSetHelper.uniqueKey(event.getItemStack()).filter(KATANAS::contains).isPresent()) {
            return;
        }
        if (released) {
            released = false;
            ModNetwork.sendToServer(new MagatsuhiActionMessage());
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (!mc.options.keyUse.isDown()) {
            released = true;
        }
        if (mc.isPaused()) {
            return;
        }
        Iterator<Fading> it = FADING.iterator();
        while (it.hasNext()) {
            Fading fading = it.next();
            if (++fading.age > fading.ticks) {
                it.remove();
            }
        }
        ClientLevel level = mc.level;
        if (level != null) {
            MARKS.values().removeIf(mark -> level.getGameTime() > mark.until);
        }
    }

    @SubscribeEvent
    public static void onLoggedOut(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        canCut = false;
        MARKS.clear();
        FADING.clear();
    }

    // ---------- drawing ----------

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || MARKS.isEmpty() && FADING.isEmpty()) {
            return;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        Vec3 cam = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(-cam.x, -cam.y, -cam.z);
        Matrix4f matrix = pose.last().pose();
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        float partial = event.getPartialTick();

        VertexConsumer shade = buffers.getBuffer(GlowRenderTypes.SHADE);
        for (Mark mark : MARKS.values()) {
            Vec3[] corners = markCorners(level, mark, partial, 1.0);
            if (corners != null) {
                quad(shade, matrix, corners, 0.03F, 0.0F, 0.02F, 0.88F);
            }
        }
        for (Fading fading : FADING) {
            if (fading.message.kind() == MagatsuhiFxMessage.Kind.AFTERIMAGE.ordinal()) {
                afterimage(shade, matrix, fading, partial);
            }
        }
        buffers.endBatch(GlowRenderTypes.SHADE);

        VertexConsumer glow = buffers.getBuffer(GlowRenderTypes.GLOW);
        float pulse = 0.75F + 0.25F * Mth.sin((level.getGameTime() + partial) * 0.4F);
        for (Mark mark : MARKS.values()) {
            Vec3[] corners = markCorners(level, mark, partial, 1.0);
            if (corners != null) {
                rim(glow, matrix, corners, 0.06, 0.85F * pulse, 0.08F * pulse, 0.12F * pulse);
            }
        }
        for (Fading fading : FADING) {
            if (fading.message.kind() == MagatsuhiFxMessage.Kind.SLASH.ordinal()) {
                slash(glow, matrix, fading, partial);
            } else if (fading.message.kind() == MagatsuhiFxMessage.Kind.CROSS.ordinal()) {
                cross(glow, matrix, fading, partial);
            }
        }
        buffers.endBatch(GlowRenderTypes.GLOW);
        pose.popPose();
    }

    // The cut's four corners, counterclockwise from bottom left; null while its enemy is out of sight.
    private static Vec3[] markCorners(ClientLevel level, Mark mark, float partial, double scale) {
        Vec3 at = mark.at;
        if (mark.entity >= 0) {
            Entity entity = level.getEntity(mark.entity);
            if (entity == null) {
                return null;
            }
            at = entity.getPosition(partial).add(0.0, entity.getBbHeight() * 0.5, 0.0);
        }
        double hw = mark.width * 0.5 * scale;
        double hh = mark.height * 0.5 * scale;
        Vec3 across;
        Vec3 up;
        if (mark.face >= 0 && mark.face < Direction.values().length) {
            Direction face = Direction.values()[mark.face];
            if (face.getAxis().isHorizontal()) {
                Direction side = face.getClockWise();
                across = new Vec3(side.getStepX(), 0.0, side.getStepZ());
                up = new Vec3(0.0, 1.0, 0.0);
            } else {
                across = new Vec3(1.0, 0.0, 0.0);
                up = new Vec3(0.0, 0.0, 1.0);
            }
        } else {
            float rad = mark.yaw * Mth.DEG_TO_RAD;
            across = new Vec3(Mth.cos(rad), 0.0, Mth.sin(rad));
            up = new Vec3(0.0, 1.0, 0.0);
        }
        Vec3 a = across.scale(hw);
        Vec3 u = up.scale(hh);
        return new Vec3[] {at.subtract(a).subtract(u), at.add(a).subtract(u), at.add(a).add(u), at.subtract(a).add(u)};
    }

    // a glowing border of the given thickness just inside the corners
    private static void rim(VertexConsumer consumer, Matrix4f matrix, Vec3[] c, double thickness, float r, float g, float b) {
        for (int i = 0; i < 4; i++) {
            Vec3 from = c[i];
            Vec3 to = c[(i + 1) % 4];
            Vec3 inFrom = c[(i + 3) % 4].subtract(from).normalize().scale(thickness);
            Vec3 inTo = c[(i + 2) % 4].subtract(to).normalize().scale(thickness);
            quad(consumer, matrix, new Vec3[] {from, to, to.add(inTo), from.add(inFrom)}, r, g, b, 1.0F);
        }
    }

    // A pale box where someone stood, fading as it rises a little.
    private static void afterimage(VertexConsumer consumer, Matrix4f matrix, Fading fading, float partial) {
        MagatsuhiFxMessage m = fading.message;
        float t = Mth.clamp((fading.age + partial) / fading.ticks, 0.0F, 1.0F);
        float alpha = 0.45F * (1.0F - t);
        if (alpha <= 0.0F) {
            return;
        }
        double hw = m.width() * 0.5 * (1.0 + 0.15 * t);
        double lift = 0.15 * t;
        double x0 = m.x() - hw, x1 = m.x() + hw, z0 = m.z() - hw, z1 = m.z() + hw;
        double y0 = m.y() + lift, y1 = m.y() + m.height() + lift;
        float r = 0.82F, g = 0.84F, b = 0.95F;
        quad(consumer, matrix, box(x0, y0, z0, x1, y0, z1, 'y'), r, g, b, alpha);
        quad(consumer, matrix, box(x0, y1, z0, x1, y1, z1, 'y'), r, g, b, alpha);
        quad(consumer, matrix, box(x0, y0, z0, x1, y1, z0, 'z'), r, g, b, alpha);
        quad(consumer, matrix, box(x0, y0, z1, x1, y1, z1, 'z'), r, g, b, alpha);
        quad(consumer, matrix, box(x0, y0, z0, x0, y1, z1, 'x'), r, g, b, alpha);
        quad(consumer, matrix, box(x1, y0, z0, x1, y1, z1, 'x'), r, g, b, alpha);
    }

    // one face of a box: the corners of the rectangle between two points that share the given axis' value
    private static Vec3[] box(double x0, double y0, double z0, double x1, double y1, double z1, char flat) {
        return switch (flat) {
            case 'y' -> new Vec3[] {new Vec3(x0, y0, z0), new Vec3(x1, y0, z0), new Vec3(x1, y0, z1), new Vec3(x0, y0, z1)};
            case 'z' -> new Vec3[] {new Vec3(x0, y0, z0), new Vec3(x1, y0, z0), new Vec3(x1, y1, z0), new Vec3(x0, y1, z0)};
            default -> new Vec3[] {new Vec3(x0, y0, z0), new Vec3(x0, y0, z1), new Vec3(x0, y1, z1), new Vec3(x0, y1, z0)};
        };
    }

    // Both katanas' strokes crossing in front of the wearer, sweeping out and fading.
    private static void slash(VertexConsumer consumer, Matrix4f matrix, Fading fading, float partial) {
        MagatsuhiFxMessage m = fading.message;
        float t = Mth.clamp((fading.age + partial) / fading.ticks, 0.0F, 1.0F);
        float fade = 1.0F - t;
        if (fade <= 0.0F) {
            return;
        }
        float rad = m.yaw() * Mth.DEG_TO_RAD;
        Vec3 forward = new Vec3(-Mth.sin(rad), 0.0, Mth.cos(rad));
        Vec3 across = new Vec3(Mth.cos(rad), 0.0, Mth.sin(rad));
        Vec3 up = new Vec3(0.0, 1.0, 0.0);
        Vec3 center = new Vec3(m.x(), m.y(), m.z()).add(forward.scale(0.8));
        double half = m.width() * 0.5 * (0.35 + 0.65 * Math.min(1.0F, t * 3.0F));
        for (int stroke = 0; stroke < 2; stroke++) {
            Vec3 dir = across.scale(stroke == 0 ? 1.0 : -1.0).add(up.scale(0.55)).normalize();
            blade(consumer, matrix, center.subtract(dir.scale(half)), center.add(dir.scale(half)), forward, 0.09 * fade + 0.02, fade);
        }
    }

    // The finisher's X on the enemy: the first katana's stroke cuts in from the top corner on one side, the second from the
    // other a beat later, each sweeping down through the middle; the X holds a moment, then fades.
    private static void cross(VertexConsumer consumer, Matrix4f matrix, Fading fading, float partial) {
        MagatsuhiFxMessage m = fading.message;
        float t = Mth.clamp((fading.age + partial) / fading.ticks, 0.0F, 1.0F);
        float fade = 1.0F - Mth.clamp((t - 0.45F) / 0.55F, 0.0F, 1.0F);
        if (fade <= 0.0F) {
            return;
        }
        float rad = m.yaw() * Mth.DEG_TO_RAD;
        Vec3 forward = new Vec3(-Mth.sin(rad), 0.0, Mth.cos(rad));
        Vec3 across = new Vec3(Mth.cos(rad), 0.0, Mth.sin(rad));
        Vec3 up = new Vec3(0.0, 1.0, 0.0);
        // just in front of the enemy, so the X isn't buried in its body
        Vec3 center = new Vec3(m.x(), m.y(), m.z()).subtract(forward.scale(m.width() * 0.25));
        double half = m.width() * 0.5;
        for (int stroke = 0; stroke < 2; stroke++) {
            float swept = Mth.clamp((t - stroke * 0.2F) / 0.25F, 0.0F, 1.0F);
            if (swept <= 0.0F) {
                continue;
            }
            Vec3 dir = across.scale(stroke == 0 ? 1.0 : -1.0).add(up).normalize();
            Vec3 start = center.add(dir.scale(half));
            Vec3 end = start.subtract(dir.scale(half * 2.0 * swept));
            blade(consumer, matrix, start, end, forward, 0.07 * fade + 0.02, fade);
        }
    }

    // one glowing katana stroke from a to b: a red edge round a pale core
    private static void blade(VertexConsumer consumer, Matrix4f matrix, Vec3 a, Vec3 b, Vec3 forward, double width, float fade) {
        Vec3 side = b.subtract(a).cross(forward).normalize().scale(width);
        quad(consumer, matrix, new Vec3[] {a.subtract(side), b.subtract(side), b.add(side), a.add(side)}, 0.95F * fade, 0.12F * fade, 0.15F * fade, 1.0F);
        Vec3 core = side.scale(0.35);
        quad(consumer, matrix, new Vec3[] {a.subtract(core), b.subtract(core), b.add(core), a.add(core)}, fade, 0.85F * fade, 0.85F * fade, 1.0F);
    }

    private static void quad(VertexConsumer consumer, Matrix4f matrix, Vec3[] c, float r, float g, float b, float a) {
        for (Vec3 v : c) {
            consumer.vertex(matrix, (float) v.x, (float) v.y, (float) v.z).color(r, g, b, a).endVertex();
        }
    }
}
