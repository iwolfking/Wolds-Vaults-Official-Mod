package xyz.iwolfking.woldsvaults.client.fx;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.network.message.RiptideFxMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * Client side of the Tidebreaker riptide visuals: turns {@link RiptideFxMessage}s into shock cone pulses, lightning
 * arcs, the hover stance and halo, and arcane particles, ticks them, and draws them after particles each frame.
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, value = Dist.CLIENT)
public final class RiptideFx {
    static final float[] TEAL = {0.30F, 1.0F, 0.85F};
    static final float[] VIOLET = {0.62F, 0.28F, 1.0F};
    static final float[] WHITE = {0.92F, 1.0F, 1.0F};
    static final float[] ICE = {0.45F, 0.72F, 1.0F};
    static final float[] FROST = {0.78F, 0.95F, 1.0F};

    private static final Vector3f DUST_TEAL = new Vector3f(0.25F, 0.95F, 0.8F);
    private static final Vector3f DUST_VIOLET = new Vector3f(0.55F, 0.2F, 0.95F);
    private static final Vector3f DUST_ICE = new Vector3f(0.7F, 0.9F, 1.0F);
    private static final int MAX_EFFECTS = 256;
    private static final int ARC_LIFETIME = 10;
    private static final int ARC_GROW_TICKS = 3;
    private static final int MAX_LIVE_BOLTS = 4;
    private static final int BURSTS_PER_TICK = 6;

    private static final List<Effect> EFFECTS = new ArrayList<>();
    private static final List<Effect> PENDING = new ArrayList<>();
    private static boolean ticking;
    private static final Random RANDOM = new Random();
    private static ClientLevel trackedLevel;
    private static int burstBudget = BURSTS_PER_TICK;

    private RiptideFx() {
    }

    interface Effect {
        boolean tick(ClientLevel level);

        void renderGlow(VertexConsumer consumer, Matrix4f pose, Vec3 camera, float partialTick);
    }

    public static void accept(RiptideFxMessage message) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        switch (message.getKind()) {
            case RiptideFxMessage.CONE -> cone(level, message);
            case RiptideFxMessage.ARC -> arc(level, message);
            case RiptideFxMessage.AURA -> aura(level, message);
            case RiptideFxMessage.BURST -> impactBurst(level, message.getFirst(), message.getAhead() * 0.6F, message.getBehind() * 0.6F, message.isChilled());
            case RiptideFxMessage.HOVER -> hover(level, message);
            default -> WoldLogger.error("Unknown riptide fx kind {}; message ignored.", message.getKind());
        }
    }

    static void add(Effect effect) {
        if (ticking) {
            PENDING.add(effect);
            return;
        }
        if (EFFECTS.size() >= MAX_EFFECTS) {
            EFFECTS.remove(0);
        }
        EFFECTS.add(effect);
    }

    private static void hover(ClientLevel level, RiptideFxMessage message) {
        int id = message.getSourceId();
        if (message.getTicks() <= 0) {
            HoverStance.stop(id);
            return;
        }
        HoverStance.start(level, id, message.getTicks());
        boolean ringed = EFFECTS.stream().anyMatch(effect -> effect instanceof HoverRingEffect ring && ring.getEntityId() == id);
        if (!ringed) {
            add(new HoverRingEffect(id, RANDOM.nextFloat()));
        }
    }

    private static void cone(ClientLevel level, RiptideFxMessage message) {
        Vec3 origin = message.getFirst();
        Vec3 axis = message.getSecond();
        add(new ShockConeEffect(origin, axis, message.getAhead(), message.getBehind(), message.getRadius(), RANDOM.nextFloat(), RANDOM.nextLong()));
        int count = particleCount(10);
        if (count == 0 || axis.lengthSqr() < 1.0E-6) {
            return;
        }
        Vec3 dir = axis.normalize();
        Vec3[] basis = FxGeometry.basis(dir);
        double length = message.getAhead() + message.getBehind();
        for (int i = 0; i < count; i++) {
            double s = 0.3 + 0.7 * RANDOM.nextDouble();
            double theta = RANDOM.nextDouble() * Math.PI * 2.0;
            Vec3 radial = basis[0].scale(Math.cos(theta)).add(basis[1].scale(Math.sin(theta)));
            Vec3 p = origin.add(dir.scale(message.getAhead() - s * length)).add(radial.scale(message.getRadius() * s));
            Vec3 v = radial.scale(0.05 + 0.05 * RANDOM.nextDouble()).subtract(dir.scale(0.04));
            switch (i % 5) {
                case 0 -> level.addParticle(ParticleTypes.ENCHANT, p.x, p.y, p.z, radial.x * 1.4, radial.y * 1.4 + 0.6, radial.z * 1.4);
                case 1 -> level.addParticle(ParticleTypes.REVERSE_PORTAL, p.x, p.y, p.z, v.x, v.y, v.z);
                case 2 -> level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y, p.z, v.x * 0.6, v.y * 0.6, v.z * 0.6);
                case 3 -> level.addParticle(dust(false), p.x, p.y, p.z, v.x, v.y, v.z);
                default -> level.addParticle(ParticleTypes.GLOW, p.x, p.y, p.z, v.x, v.y, v.z);
            }
        }
    }

    /**
     * Draws a strike arc unless {@value #MAX_LIVE_BOLTS} strike arcs are already on screen, in which case the strike
     * shows only its impact burst. Purely visual: the server has already dealt the strike's damage.
     */
    private static void arc(ClientLevel level, RiptideFxMessage message) {
        if (liveBolts() >= MAX_LIVE_BOLTS) {
            Entity target = level.getEntity(message.getTargetId());
            impactBurst(level, message.getSecond(), target == null ? 0.6F : target.getBbWidth(), target == null ? 1.8F : target.getBbHeight(), message.isChilled());
            return;
        }
        Entity source = level.getEntity(message.getSourceId());
        Entity target = level.getEntity(message.getTargetId());
        Vec3 from = message.getFirst();
        Vec3 to = message.getSecond();
        Vec3 sourceOffset = source == null ? Vec3.ZERO : from.subtract(source.position());
        Vec3 targetOffset = target == null ? Vec3.ZERO : to.subtract(target.position());
        add(new LightningArcEffect(message.getSourceId(), sourceOffset, from, message.getTargetId(), targetOffset, to,
                message.isChilled(), ARC_LIFETIME, ARC_GROW_TICKS, 1.0F, true, RANDOM.nextLong()));
        int count = particleCount(5);
        for (int i = 0; i < count; i++) {
            Vec3 d = randomUnit().scale(0.9);
            level.addParticle(ParticleTypes.ENCHANT, from.x, from.y, from.z, d.x, d.y, d.z);
        }
    }

    private static int liveBolts() {
        int live = 0;
        for (Effect effect : EFFECTS) {
            if (effect instanceof LightningArcEffect arc && arc.isStrike()) {
                live++;
            }
        }
        for (Effect effect : PENDING) {
            if (effect instanceof LightningArcEffect arc && arc.isStrike()) {
                live++;
            }
        }
        return live;
    }

    private static void aura(ClientLevel level, RiptideFxMessage message) {
        Entity source = level.getEntity(message.getSourceId());
        Vec3 chest = message.getFirst();
        Vec3 base = source == null ? chest : source.position();
        for (int i = 0; i < 2; i++) {
            Vec3 a = randomUnit().scale(0.55);
            Vec3 b = a.add(randomUnit().scale(0.7)).normalize().scale(0.75);
            Vec3 offsetA = chest.subtract(base).add(a);
            Vec3 offsetB = chest.subtract(base).add(b);
            add(new LightningArcEffect(message.getSourceId(), offsetA, base.add(offsetA), message.getSourceId(), offsetB, base.add(offsetB),
                    false, 4, 1, 0.35F, false, RANDOM.nextLong()));
        }
        int count = particleCount(4);
        for (int i = 0; i < count; i++) {
            Vec3 d = randomUnit().scale(1.5);
            switch (i % 4) {
                case 0, 1 -> level.addParticle(ParticleTypes.ENCHANT, chest.x, chest.y, chest.z, d.x, d.y, d.z);
                case 2 -> level.addParticle(ParticleTypes.REVERSE_PORTAL, chest.x + d.x * 0.4, chest.y + d.y * 0.4, chest.z + d.z * 0.4, 0.0, 0.02, 0.0);
                default -> level.addParticle(ParticleTypes.GLOW, chest.x + d.x * 0.4, chest.y + d.y * 0.4, chest.z + d.z * 0.4, 0.0, 0.0, 0.0);
            }
        }
    }

    /** Flash, converging glyphs, sparks and motes where an arc or shock lands on a mob, at most {@value #BURSTS_PER_TICK} a tick. */
    static void impactBurst(ClientLevel level, Vec3 center, float width, float height, boolean chilled) {
        int count = particleCount(14);
        if (count == 0 || burstBudget <= 0) {
            return;
        }
        burstBudget--;
        float spread = Math.max(0.6F, Math.max(width, height) * 0.5F);
        level.addParticle(ParticleTypes.FLASH, center.x, center.y, center.z, 0.0, 0.0, 0.0);
        for (int i = 0; i < count; i++) {
            Vec3 d = randomUnit();
            switch (i % 4) {
                case 0 -> level.addParticle(ParticleTypes.ENCHANT, center.x, center.y, center.z, d.x * spread * 2.2, d.y * spread * 2.2, d.z * spread * 2.2);
                case 1 -> level.addParticle(ParticleTypes.ELECTRIC_SPARK, center.x, center.y, center.z, d.x * 0.45, d.y * 0.45, d.z * 0.45);
                case 2 -> level.addParticle(chilled ? ParticleTypes.SNOWFLAKE : ParticleTypes.REVERSE_PORTAL,
                        center.x + d.x * spread, center.y + d.y * spread, center.z + d.z * spread, d.x * 0.05, d.y * 0.05, d.z * 0.05);
                default -> level.addParticle(dust(chilled), center.x + d.x * spread * 0.6, center.y + d.y * spread * 0.6, center.z + d.z * spread * 0.6,
                        d.x * 0.1, d.y * 0.1, d.z * 0.1);
            }
        }
    }

    private static ParticleOptions dust(boolean chilled) {
        return new DustColorTransitionOptions(chilled ? DUST_ICE : DUST_TEAL, chilled ? DUST_TEAL : DUST_VIOLET, 1.3F);
    }

    private static Vec3 randomUnit() {
        double z = RANDOM.nextDouble() * 2.0 - 1.0;
        double theta = RANDOM.nextDouble() * Math.PI * 2.0;
        double r = Math.sqrt(1.0 - z * z);
        return new Vec3(r * Math.cos(theta), z, r * Math.sin(theta));
    }

    static int particleCount(int full) {
        ParticleStatus status = Minecraft.getInstance().options.particles;
        return switch (status) {
            case ALL -> full;
            case DECREASED -> full / 2;
            case MINIMAL -> 0;
        };
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level != trackedLevel) {
            trackedLevel = level;
            EFFECTS.clear();
            PENDING.clear();
            HoverStance.clear();
        }
        burstBudget = BURSTS_PER_TICK;
        if (level == null || minecraft.isPaused()) {
            return;
        }
        try {
            HoverStance.tick(level);
            ticking = true;
            EFFECTS.removeIf(effect -> !effect.tick(level));
        } catch (RuntimeException e) {
            WoldLogger.error("Riptide fx tick failed; dropping {} active effects instead of crashing.", EFFECTS.size(), e);
            EFFECTS.clear();
            PENDING.clear();
            HoverStance.clear();
        } finally {
            ticking = false;
        }
        PENDING.forEach(RiptideFx::add);
        PENDING.clear();
    }

    @SubscribeEvent
    public static void hideOffhandWhileRiptiding(RenderHandEvent event) {
        if (event.getHand() == InteractionHand.OFF_HAND && Minecraft.getInstance().player != null && Minecraft.getInstance().player.isAutoSpinAttack()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || EFFECTS.isEmpty()) {
            return;
        }
        Matrix4f pose = event.getPoseStack().last().pose();
        Vec3 camera = event.getCamera().getPosition();
        float partialTick = event.getPartialTick();
        List<Effect> snapshot = new ArrayList<>(EFFECTS);
        try {
            MultiBufferSource.BufferSource source = Minecraft.getInstance().renderBuffers().bufferSource();
            VertexConsumer consumer = source.getBuffer(RenderType.lightning());
            try {
                for (Effect effect : snapshot) {
                    effect.renderGlow(consumer, pose, camera, partialTick);
                }
            } finally {
                source.endBatch(RenderType.lightning());
            }
        } catch (RuntimeException e) {
            WoldLogger.error("Riptide fx render failed; dropping {} active effects instead of crashing.", EFFECTS.size(), e);
            EFFECTS.clear();
        }
    }
}
