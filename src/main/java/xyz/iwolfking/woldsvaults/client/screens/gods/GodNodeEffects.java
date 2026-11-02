package xyz.iwolfking.woldsvaults.client.screens.gods;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import iskallia.vault.client.gui.helper.ScreenDrawHelper;
import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeType;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Light around the god tree's stars: spiral arms, motes swirling in, rays beaming out and a core glow, tuned per node
 * type in god_tree/node_effects.json (the export of the God Node Vortex tuning page). Light is summed per pixel of the
 * 64px star cell, then soft-clipped, banded and coloured through the god's row of star_ramps.png, so overlapping light
 * saturates instead of stacking. Each node type and phase is painted once a frame (light behind the star and light
 * over it) and drawn at every star of that type, pixel for pixel with the star sprite.
 * <p>
 * The primitives (dots, strokes, glow) are placed by {@link #paint}. They are summed either on the GPU into a float
 * target and tone-mapped by a shader ({@link GodNodeEffectsGpu}, the default), or on the CPU into float buffers and
 * uploaded as a texture (the original path, kept behind {@link #togglePath()} for comparison).
 */
public final class GodNodeEffects {
    private static final ResourceLocation SETTINGS = new ResourceLocation(WoldsVaults.MOD_ID, "god_tree/node_effects.json");
    private static final ResourceLocation RAMPS = new ResourceLocation(WoldsVaults.MOD_ID, "textures/gui/gods/star_ramps.png");
    static final float CELL = GodTreeTheme.STAR_CELL;
    private static final double TAU = Math.PI * 2.0D;
    private static final int MAX_SIZE = 192;
    private static final int ARM_SAMPLES = 600;
    private static final int MAX_MOTES = 200;
    private static final int MAX_RAYS = 32;
    private static final double[] PHASES = {0.0D, 1.7D, 3.9D};
    static final int PHASE_COUNT = PHASES.length;
    private static final float MIN_NODE_PIXELS = 6.0F;

    private static final double[][] MOTES = randomTable(1337, MAX_MOTES, 5);
    private static final double[][] RAYS = randomTable(4242, MAX_RAYS, 3);
    private static final Map<GodNodeType, Layer[]> LAYERS = new EnumMap<>(GodNodeType.class);

    private static Map<GodNodeType, Look> looks;
    private static int[][] ramps;
    private static boolean gpu = true;

    record Light(double gain, double toneFloor, int toneSteps) {}

    record Arms(boolean enabled, int count, double inner, double outer, double twist, double spin,
                double widthCore, double widthTip, double brightness, int segments, double fill, double flow) {}

    record Motes(boolean enabled, int count, double start, double end, double trip, double swirl,
                 boolean rideArms, int trail, double size, double brightness, boolean overStar) {}

    record Rays(boolean enabled, int count, double start, double length, double lengthSpread, double widthBase,
                double widthTip, double spin, double lean, double pulseRate, double stretchPulse,
                double brightnessPulse, double waves, double randomness, double brightness, boolean fadeToTip,
                boolean overStar) {}

    record Glow(boolean enabled, double radius, double brightness, double pulse, double pulseRate) {}

    record Look(Light light, Arms arms, Motes motes, Rays rays, Glow glow) {
        /**
         * Whether any light sits over the star, which needs the second pass.
         */
        boolean hasFront() {
            return this.motes.enabled() && this.motes.overStar() || this.rays.enabled() && this.rays.overStar();
        }

        /**
         * Side of the square of cell pixels that holds all this light, with the star at its centre.
         */
        int size() {
            double reach = 0.0D;
            if (this.arms.enabled()) {
                reach = Math.max(reach, Math.max(this.arms.inner(), this.arms.outer())
                        + Math.max(this.arms.widthCore(), this.arms.widthTip()) / 2.0D);
            }
            if (this.motes.enabled()) {
                reach = Math.max(reach, Math.max(this.motes.start() * 1.15D, this.motes.end()) + this.motes.size() / 2.0D);
            }
            if (this.rays.enabled()) {
                reach = Math.max(reach, this.rays.start() + this.rays.length() + Math.abs(this.rays.lengthSpread())
                        + Math.max(this.rays.widthBase(), this.rays.widthTip()) / 2.0D);
            }
            if (this.glow.enabled()) {
                reach = Math.max(reach, this.glow.radius());
            }
            return Math.min(MAX_SIZE, 2 * (int) Math.ceil(reach + 2.0D));
        }
    }

    /**
     * Receives the light primitives {@link #paint} places, in canvas pixels with y down. front selects the light
     * over the star instead of the light behind it.
     */
    interface Splat {
        /**
         * Adds v to every pixel whose centre lies within w/2 of (x, y); at w &lt;= 1.25 only to the pixel holding (x, y).
         */
        void dot(boolean front, double x, double y, double v, double w);

        /**
         * Stamps a dot every half pixel from (x0, y0) to (x1, y1), value and width interpolated along the way, each
         * stamp carrying half its value split across its width.
         */
        void stroke(boolean front, double x0, double y0, double x1, double y1, double v0, double v1, double w0, double w1);

        /**
         * Adds k * (1 - d / radius)^2 behind the star to every pixel whose centre lies at d &lt; radius from the centre.
         */
        void glow(double centre, double radius, double k);
    }

    private GodNodeEffects() {
    }

    @Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ReloadHook {
        private ReloadHook() {
        }

        @SubscribeEvent
        public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener((ResourceManagerReloadListener) manager -> {
                looks = null;
                ramps = null;
                if (RenderSystem.isOnRenderThread()) {
                    GodNodeEffectsGpu.release();
                } else {
                    RenderSystem.recordRenderCall(GodNodeEffectsGpu::release);
                }
            });
        }
    }

    /**
     * Switches between the GPU and the CPU renderer and logs which one is now active. Temporary, for comparing the
     * two in game.
     */
    public static void togglePath() {
        gpu = !gpu;
        if (!gpu) {
            WoldsVaults.LOGGER.info("God node effects: CPU path active");
        } else if (GodNodeEffectsGpu.isUnavailable()) {
            WoldsVaults.LOGGER.info("God node effects: GPU path selected but unavailable (see the earlier warning); CPU path still in use");
        } else {
            WoldsVaults.LOGGER.info("God node effects: GPU path active");
        }
    }

    /**
     * Draws one layer of effects for every star in the given tree-space rectangle. With front false it draws what sits
     * behind the stars (glow, arms, and motes or rays not set over the star) and paints this frame's light; with front
     * true, the motes and rays set over the star. Light behind a star adds to what is under it; light over a star
     * covers it as strongly as it shines. Each star's state darkens its light's colour
     * ({@link GodNodeWidget#effectBrightness()}) without making it see-through. viewportScale is the pan region's zoom.
     */
    public static void render(PoseStack poseStack, VaultGod god, Collection<GodNodeWidget> widgets, boolean front,
                              float minX, float minY, float maxX, float maxY, float viewportScale) {
        Map<GodNodeType, Look> settings = looks();
        if (settings.isEmpty()) {
            return;
        }
        float screenPerUnit = viewportScale * (float) Minecraft.getInstance().getWindow().getGuiScale();
        GodNodeType[] types = GodNodeType.values();
        @SuppressWarnings("unchecked")
        List<GodNodeWidget>[] groups = new List[types.length * PHASES.length];
        boolean any = false;
        for (GodNodeWidget widget : widgets) {
            Look look = settings.get(widget.getNode().type());
            if (look == null || (front && !look.hasFront()) || widget.getWidth() * screenPerUnit < MIN_NODE_PIXELS) {
                continue;
            }
            float half = look.size() / 2.0F * widget.getWidth() / CELL;
            float cx = widget.x + widget.getWidth() / 2.0F;
            float cy = widget.y + widget.getHeight() / 2.0F;
            if (cx + half < minX || cx - half > maxX || cy + half < minY || cy - half > maxY) {
                continue;
            }
            int group = widget.getNode().type().ordinal() * PHASES.length + phase(widget);
            if (groups[group] == null) {
                groups[group] = new ArrayList<>();
            }
            groups[group].add(widget);
            any = true;
        }
        if (!any) {
            return;
        }
        double seconds = (Util.getMillis() % 86_400_000L) / 1000.0D;
        if (gpu && GodNodeEffectsGpu.render(poseStack, god, settings, groups, front, seconds)) {
            return;
        }
        renderCpu(poseStack, god, settings, groups, front, seconds);
    }

    static double phaseTime(double seconds, int phase) {
        return seconds + PHASES[phase];
    }

    private static void renderCpu(PoseStack poseStack, VaultGod god, Map<GodNodeType, Look> settings,
                                  List<GodNodeWidget>[] groups, boolean front, double seconds) {
        int[] ramp = ramps()[god.ordinal()];
        Matrix4f pose = poseStack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.enableTexture();
        RenderSystem.enableBlend();
        if (front) {
            RenderSystem.defaultBlendFunc();
        } else {
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        }
        for (GodNodeType type : GodNodeType.values()) {
            Look look = settings.get(type);
            for (int phase = 0; phase < PHASES.length; phase++) {
                List<GodNodeWidget> group = groups[type.ordinal() * PHASES.length + phase];
                if (group == null) {
                    continue;
                }
                Layer layer = layer(type, phase, look.size());
                if (!front || !layer.painted) {
                    layer.paint(look, ramp, phaseTime(seconds, phase));
                }
                float halfCells = layer.canvas.size / 2.0F;
                RenderSystem.setShaderTexture(0, layer.id);
                ScreenDrawHelper.draw(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX,
                        buf -> {
                            quads(buf, pose, group, halfCells, front ? 0.5F : 0.0F, 0.5F);
                        });
            }
        }
        RenderSystem.defaultBlendFunc();
    }

    /**
     * Writes one POSITION_COLOR_TEX quad per star of a group: halfCells cell pixels either side of the star's centre,
     * shaded by its state, reading u from u0 to u0 + uSpan of the light texture. Starting the texture's corners
     * further round turns each star's light by its own quarter turns.
     */
    static void quads(BufferBuilder buf, Matrix4f pose, List<GodNodeWidget> group, float halfCells, float u0, float uSpan) {
        for (GodNodeWidget widget : group) {
            float half = halfCells * widget.getWidth() / CELL;
            float cx = widget.x + widget.getWidth() / 2.0F;
            float cy = widget.y + widget.getHeight() / 2.0F;
            int shade = Math.round(widget.effectBrightness() * 255.0F);
            int turn = widget.effectSeed() >>> 30;
            for (int corner = 0; corner < 4; corner++) {
                float x = corner == 0 || corner == 3 ? cx - half : cx + half;
                float y = corner < 2 ? cy + half : cy - half;
                int uv = (corner + turn) & 3;
                float u = uv == 1 || uv == 2 ? u0 + uSpan : u0;
                float v = uv < 2 ? 1.0F : 0.0F;
                buf.vertex(pose, x, y, 0.0F).color(shade, shade, shade, 255).uv(u, v).endVertex();
            }
        }
    }

    private static int phase(GodNodeWidget widget) {
        return (widget.effectSeed() >>> 4) % PHASES.length;
    }

    private static Layer layer(GodNodeType type, int phase, int size) {
        Layer[] row = LAYERS.computeIfAbsent(type, t -> new Layer[PHASES.length]);
        if (row[phase] == null || row[phase].canvas.size != size) {
            row[phase] = new Layer(new ResourceLocation(WoldsVaults.MOD_ID,
                    "god_node_effects/" + type.name().toLowerCase(Locale.ROOT) + "_" + phase), size);
        }
        return row[phase];
    }

    private static final class Layer {
        private final ResourceLocation id;
        private final Canvas canvas;
        private final DynamicTexture texture;
        private boolean painted;

        Layer(ResourceLocation id, int size) {
            this.id = id;
            this.canvas = new Canvas(size);
            this.texture = new DynamicTexture(size * 2, size, true);
            Minecraft.getInstance().getTextureManager().register(id, this.texture);
        }

        void paint(Look look, int[] ramp, double time) {
            this.canvas.paint(look, time);
            NativeImage image = this.texture.getPixels();
            if (image == null) {
                return;
            }
            int size = this.canvas.size;
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    int i = y * size + x;
                    image.setPixelRGBA(x, y, tone(look.light(), ramp, this.canvas.behind[i]));
                    image.setPixelRGBA(size + x, y, tone(look.light(), ramp, this.canvas.front[i]));
                }
            }
            this.texture.upload();
            this.painted = true;
        }
    }

    /**
     * The tuning page's tone map, as an ABGR pixel: light soft-clips, bands into tone steps and takes its colour from
     * the ramp. Alpha carries the strength, so additive blending adds colour times strength, as the page does.
     * shaders/core/node_effect_tone.fsh reproduces this line for line; keep the two in step.
     */
    private static int tone(Light light, int[] ramp, double value) {
        if (value <= 0.0D) {
            return 0;
        }
        double a = 1.0D - Math.exp(-value * light.gain());
        if (light.toneSteps() > 0) {
            a = Math.round(a * light.toneSteps()) / (double) light.toneSteps();
        }
        if (a <= 0.0D) {
            return 0;
        }
        int rgb = ramp[Math.min(255, (int) Math.floor(light.toneFloor() + (255.0D - light.toneFloor()) * a))];
        int alpha = (int) Math.round(Math.min(1.0D, a) * 255.0D);
        return alpha << 24 | (rgb & 0xFF) << 16 | (rgb >> 8 & 0xFF) << 8 | (rgb >> 16 & 0xFF);
    }

    private static final class Canvas implements Splat {
        private final int size;
        private final double centre;
        private final float[] behind;
        private final float[] front;

        Canvas(int size) {
            this.size = size;
            this.centre = size / 2.0D;
            this.behind = new float[size * size];
            this.front = new float[size * size];
        }

        void paint(Look look, double time) {
            Arrays.fill(this.behind, 0.0F);
            Arrays.fill(this.front, 0.0F);
            GodNodeEffects.paint(look, time, this.centre, this);
        }

        @Override
        public void glow(double centre, double radius, double k) {
            for (int y = (int) Math.floor(centre - radius); y <= (int) Math.ceil(centre + radius); y++) {
                for (int x = (int) Math.floor(centre - radius); x <= (int) Math.ceil(centre + radius); x++) {
                    double dx = x + 0.5D - centre;
                    double dy = y + 0.5D - centre;
                    double d = Math.sqrt(dx * dx + dy * dy);
                    if (d < radius) {
                        this.add(this.behind, x, y, k * Math.pow(1.0D - d / radius, 2.0D));
                    }
                }
            }
        }

        @Override
        public void dot(boolean front, double x, double y, double v, double w) {
            this.stamp(front ? this.front : this.behind, x, y, v, w);
        }

        @Override
        public void stroke(boolean front, double x0, double y0, double x1, double y1, double v0, double v1, double w0, double w1) {
            float[] buf = front ? this.front : this.behind;
            double length = Math.sqrt((x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0));
            int steps = Math.max(1, (int) Math.ceil(length * 2.0D));
            for (int i = 0; i <= steps; i++) {
                double t = (double) i / steps;
                double w = w0 + (w1 - w0) * t;
                this.stamp(buf, x0 + (x1 - x0) * t, y0 + (y1 - y0) * t, (v0 + (v1 - v0) * t) * 0.5D / Math.max(1.0D, w), w);
            }
        }

        private void add(float[] buf, double x, double y, double v) {
            int ix = (int) Math.floor(x);
            int iy = (int) Math.floor(y);
            if (ix < 0 || iy < 0 || ix >= this.size || iy >= this.size) {
                return;
            }
            buf[iy * this.size + ix] += v;
        }

        private void stamp(float[] buf, double x, double y, double v, double w) {
            if (w <= 1.25D) {
                this.add(buf, x, y, v);
                return;
            }
            double r = w / 2.0D;
            for (int py = (int) Math.floor(y - r); py <= (int) Math.floor(y + r); py++) {
                for (int px = (int) Math.floor(x - r); px <= (int) Math.floor(x + r); px++) {
                    double dx = px + 0.5D - x;
                    double dy = py + 0.5D - y;
                    if (dx * dx + dy * dy <= r * r) {
                        this.add(buf, px + 0.5D, py + 0.5D, v);
                    }
                }
            }
        }
    }

    /**
     * The tuning page's drawing code, line for line: places one star's glow, arms, motes and rays at time seconds
     * into out, around the canvas centre. Both renderers sum exactly these primitives, so their geometry is the same.
     */
    static void paint(Look look, double time, double centre, Splat out) {
        glow(look.glow(), time, centre, out);
        arms(look.arms(), time, centre, out);
        motes(look.motes(), look.arms(), time, centre, out);
        rays(look.rays(), time, centre, out);
    }

    private static void glow(Glow glow, double t, double centre, Splat out) {
        double radius = glow.radius();
        if (!glow.enabled() || radius <= 0.0D || glow.brightness() <= 0.0D) {
            return;
        }
        out.glow(centre, radius, glow.brightness() * (1.0D + glow.pulse() * Math.sin(TAU * glow.pulseRate() * t)));
    }

    private static void arms(Arms arms, double t, double centre, Splat out) {
        int n = arms.count();
        if (!arms.enabled() || n <= 0 || arms.brightness() <= 0.0D) {
            return;
        }
        double inner = arms.inner();
        double length = Math.max(inner + 1.0D, arms.outer()) - inner;
        for (int arm = 0; arm < n; arm++) {
            double phi = armAngle(arms, arm, n, t);
            double lastX = Double.NaN;
            double lastY = Double.NaN;
            for (int k = 0; k <= ARM_SAMPLES; k++) {
                double s = (double) k / ARM_SAMPLES;
                double r = inner + length * s;
                double th = phi + arms.twist() * s;
                double x = centre + Math.cos(th) * r;
                double y = centre + Math.sin(th) * r;
                if (!Double.isNaN(lastX) && Math.sqrt((x - lastX) * (x - lastX) + (y - lastY) * (y - lastY)) < 0.5D) {
                    continue;
                }
                lastX = x;
                lastY = y;
                double m = 1.0D;
                if (arms.segments() > 0) {
                    double d = fract(s * arms.segments() + t * arms.flow());
                    m = d < arms.fill() ? 0.55D + 0.45D * (1.0D - d / arms.fill()) : 0.0D;
                }
                if (m == 0.0D) {
                    continue;
                }
                double w = arms.widthCore() + (arms.widthTip() - arms.widthCore()) * s;
                double profile = Math.pow(Math.sin(Math.PI * s), 0.7D);
                out.dot(false, x, y, arms.brightness() * profile * m * 0.5D / Math.max(1.0D, w), w);
            }
        }
    }

    private static void motes(Motes motes, Arms arms, double t, double centre, Splat out) {
        if (!motes.enabled()) {
            return;
        }
        boolean over = motes.overStar();
        double life = Math.max(0.1D, motes.trip());
        int count = Math.min(MAX_MOTES, motes.count());
        double[] head = new double[2];
        double[] point = new double[2];
        for (int i = 0; i < count; i++) {
            double[] mote = MOTES[i];
            double q = fract(t / life + mote[0]);
            double fade = smooth(0.0D, 0.15D, q) * (1.0D - smooth(0.72D, 1.0D, q));
            double v = motes.brightness() * fade;
            if (v <= 0.0D) {
                continue;
            }
            motePosition(motes, arms, mote, q, t, centre, head);
            out.dot(over, head[0], head[1], v, motes.size());
            for (int j = 1; j <= motes.trail(); j++) {
                double earlier = q - j * 0.018D;
                if (earlier < 0.0D) {
                    break;
                }
                motePosition(motes, arms, mote, earlier, t - j * 0.018D * life, centre, point);
                double f = 1.0D - (double) j / (motes.trail() + 1);
                out.stroke(over, head[0], head[1], point[0], point[1], v * (f + 1.0D / (motes.trail() + 1)), v * f,
                        motes.size(), motes.size());
                head[0] = point[0];
                head[1] = point[1];
            }
        }
    }

    private static void motePosition(Motes motes, Arms arms, double[] mote, double q, double t, double centre, double[] out) {
        double start = motes.start() * (0.85D + mote[2] * 0.3D);
        double r = start + (motes.end() - start) * Math.pow(q, 1.35D);
        double th;
        if (motes.rideArms()) {
            int n = Math.max(1, arms.count());
            double inner = arms.inner();
            double outer = Math.max(inner + 1.0D, arms.outer());
            double s = (r - inner) / (outer - inner);
            th = armAngle(arms, (int) Math.floor(mote[3] * 64.0D) % n, n, t) + arms.twist() * s + (mote[4] - 0.5D) * 0.35D;
        } else {
            th = mote[1] * TAU + motes.swirl() * TAU * Math.pow(q, 1.6D);
        }
        out[0] = centre + Math.cos(th) * r;
        out[1] = centre + Math.sin(th) * r;
    }

    private static void rays(Rays rays, double t, double centre, Splat out) {
        if (!rays.enabled()) {
            return;
        }
        boolean over = rays.overStar();
        int n = Math.min(MAX_RAYS, rays.count());
        double lean = rays.lean() * Math.PI / 180.0D;
        for (int i = 0; i < n; i++) {
            double[] ray = RAYS[i];
            double u = ray[0];
            double spread = ray[1] * 2.0D - 1.0D;
            double jitter = ray[2] - 0.5D;
            double angle = (double) i / n * TAU + rays.spin() * t * TAU + jitter * (TAU / n) * 0.3D;
            double pulse = 0.5D + 0.5D * Math.sin(TAU * rays.pulseRate() * t + (double) i / n * TAU * rays.waves()
                    + u * TAU * rays.randomness());
            double full = Math.max(1.0D, rays.length() + spread * rays.lengthSpread());
            double length = full * (1.0D - rays.stretchPulse() + rays.stretchPulse() * pulse);
            double v = rays.brightness() * (1.0D - rays.brightnessPulse() + rays.brightnessPulse() * pulse);
            if (v <= 0.0D || length <= 0.0D) {
                continue;
            }
            double sx = centre + Math.cos(angle) * rays.start();
            double sy = centre + Math.sin(angle) * rays.start();
            double ex = sx + Math.cos(angle + lean) * length;
            double ey = sy + Math.sin(angle + lean) * length;
            if (!rays.fadeToTip()) {
                out.stroke(over, sx, sy, ex, ey, v * 1.5D, v * 1.5D, rays.widthBase(), rays.widthTip());
                continue;
            }
            int pieces = Math.max(2, (int) Math.ceil(length / 3.0D));
            for (int k = 0; k < pieces; k++) {
                double a = (double) k / pieces;
                double b = (double) (k + 1) / pieces;
                out.stroke(over, sx + (ex - sx) * a, sy + (ey - sy) * a, sx + (ex - sx) * b, sy + (ey - sy) * b,
                        v * 1.8D * Math.pow(1.0D - a, 1.4D), v * 1.8D * Math.pow(1.0D - b, 1.4D),
                        rays.widthBase() + (rays.widthTip() - rays.widthBase()) * a,
                        rays.widthBase() + (rays.widthTip() - rays.widthBase()) * b);
            }
        }
    }

    private static double armAngle(Arms arms, int arm, int count, double t) {
        return (double) arm / count * TAU + arms.spin() * t * TAU;
    }

    private static Map<GodNodeType, Look> looks() {
        if (looks == null) {
            looks = new EnumMap<>(GodNodeType.class);
            try (Resource resource = Minecraft.getInstance().getResourceManager().getResource(SETTINGS);
                 Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                JsonObject nodes = GsonHelper.getAsJsonObject(JsonParser.parseReader(reader).getAsJsonObject(), "nodes");
                for (GodNodeType type : GodNodeType.values()) {
                    if (nodes.has(type.name())) {
                        looks.put(type, look(nodes.getAsJsonObject(type.name())));
                    }
                }
            } catch (IOException | RuntimeException e) {
                WoldsVaults.LOGGER.warn("God node effects {} could not be read; stars draw without effects", SETTINGS, e);
            }
        }
        return looks;
    }

    private static Look look(JsonObject node) {
        JsonObject light = part(node, "light");
        JsonObject arms = part(node, "arms");
        JsonObject motes = part(node, "motes");
        JsonObject rays = part(node, "rays");
        JsonObject glow = part(node, "glow");
        return new Look(
                new Light(d(light, "brightness", 1.2D), d(light, "toneFloor", 70.0D), i(light, "toneSteps", 4)),
                new Arms(b(arms, "enabled", true), i(arms, "count", 5), d(arms, "innerRadius", 14.0D),
                        d(arms, "outerRadius", 48.0D), d(arms, "twist", 2.6D), d(arms, "spin", 0.18D),
                        d(arms, "widthCore", 1.0D), d(arms, "widthTip", 3.0D), d(arms, "brightness", 0.55D),
                        i(arms, "segments", 3), d(arms, "segmentFill", 0.6D), d(arms, "segmentFlow", 0.8D)),
                new Motes(b(motes, "enabled", true), i(motes, "count", 45), d(motes, "startRadius", 52.0D),
                        d(motes, "fadeRadius", 9.0D), d(motes, "tripTime", 2.6D), d(motes, "swirl", 1.1D),
                        b(motes, "rideArms", false), i(motes, "trail", 6), d(motes, "size", 1.0D),
                        d(motes, "brightness", 1.1D), b(motes, "overStar", false)),
                new Rays(b(rays, "enabled", true), i(rays, "count", 8), d(rays, "startRadius", 4.0D),
                        d(rays, "length", 30.0D), d(rays, "lengthSpread", 4.0D), d(rays, "widthBase", 2.0D),
                        d(rays, "widthTip", 1.0D), d(rays, "orbitSpeed", -0.04D), d(rays, "angle", 0.0D),
                        d(rays, "pulseRate", 0.8D), d(rays, "stretchPulse", 0.45D), d(rays, "brightnessPulse", 0.6D),
                        d(rays, "pulseWaves", 1.0D), d(rays, "pulseRandomness", 0.3D), d(rays, "brightness", 1.0D),
                        b(rays, "fadeToTip", true), b(rays, "overStar", false)),
                new Glow(b(glow, "enabled", true), d(glow, "radius", 20.0D), d(glow, "brightness", 0.35D),
                        d(glow, "pulse", 0.25D), d(glow, "pulseRate", 0.5D)));
    }

    private static JsonObject part(JsonObject node, String name) {
        return GsonHelper.getAsJsonObject(node, name, new JsonObject());
    }

    private static double d(JsonObject json, String key, double fallback) {
        JsonElement element = json.get(key);
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()
                ? element.getAsDouble() : fallback;
    }

    private static int i(JsonObject json, String key, int fallback) {
        return (int) Math.round(d(json, key, fallback));
    }

    private static boolean b(JsonObject json, String key, boolean fallback) {
        JsonElement element = json.get(key);
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()
                ? element.getAsBoolean() : fallback;
    }

    /**
     * One 256-entry 0xRRGGBB ramp per god, indexed by {@link VaultGod#ordinal()}: the god's row of star_ramps.png, or
     * {@link #themeRamp} where the image or its row is missing or unreadable. Never holds a null row.
     */
    static int[][] ramps() {
        if (ramps == null) {
            VaultGod[] gods = VaultGod.values();
            int[][] rows = new int[gods.length][];
            try (Resource resource = Minecraft.getInstance().getResourceManager().getResource(RAMPS);
                 NativeImage image = NativeImage.read(resource.getInputStream())) {
                if (image.getWidth() < 256) {
                    WoldsVaults.LOGGER.warn("God star ramps {} is {} px wide, not 256; its last column repeats",
                            RAMPS, image.getWidth());
                }
                for (VaultGod god : gods) {
                    if (god.ordinal() >= image.getHeight()) {
                        WoldsVaults.LOGGER.warn("God star ramps {} has no row {} for {}; using the theme colours",
                                RAMPS, god.ordinal(), god);
                        continue;
                    }
                    int[] row = new int[256];
                    for (int t = 0; t < 256; t++) {
                        int abgr = image.getPixelRGBA(Math.min(t, image.getWidth() - 1), god.ordinal());
                        row[t] = (abgr & 0xFF) << 16 | (abgr >> 8 & 0xFF) << 8 | (abgr >> 16 & 0xFF);
                    }
                    rows[god.ordinal()] = row;
                }
            } catch (IOException | RuntimeException e) {
                WoldsVaults.LOGGER.warn("God star ramps {} could not be read; using the theme colours instead", RAMPS, e);
            }
            for (VaultGod god : gods) {
                if (rows[god.ordinal()] == null) {
                    rows[god.ordinal()] = themeRamp(god);
                }
            }
            ramps = rows;
        }
        return ramps;
    }

    /**
     * Black through the god's deep, dim and accent colours to white, when the ramp texture is missing.
     */
    private static int[] themeRamp(VaultGod god) {
        int[] stops = {0x000000, GodTreeTheme.accentDeep(god) & 0xFFFFFF, GodTreeTheme.accentDim(god) & 0xFFFFFF,
                GodTreeTheme.accent(god) & 0xFFFFFF, 0xFFFFFF};
        int[] row = new int[256];
        for (int t = 0; t < 256; t++) {
            double at = t / 255.0D * (stops.length - 1);
            int low = Math.min(stops.length - 2, (int) at);
            double mix = at - low;
            int from = stops[low];
            int to = stops[low + 1];
            int r = (int) lerp(from >> 16 & 0xFF, to >> 16 & 0xFF, mix);
            int g = (int) lerp(from >> 8 & 0xFF, to >> 8 & 0xFF, mix);
            int b = (int) lerp(from & 0xFF, to & 0xFF, mix);
            row[t] = r << 16 | g << 8 | b;
        }
        return row;
    }

    /**
     * The tuning page's random table: mulberry32 with the page's seeds, so a star there matches the star here.
     */
    private static double[][] randomTable(int seed, int rows, int columns) {
        double[][] table = new double[rows][columns];
        int a = seed;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                a += 0x6D2B79F5;
                int t = (a ^ (a >>> 15)) * (1 | a);
                t = (t + (t ^ (t >>> 7)) * (61 | t)) ^ t;
                table[r][c] = ((t ^ (t >>> 14)) & 0xFFFFFFFFL) / 4294967296.0D;
            }
        }
        return table;
    }

    private static double fract(double value) {
        return value - Math.floor(value);
    }

    private static double smooth(double from, double to, double value) {
        double t = Math.min(1.0D, Math.max(0.0D, (value - from) / (to - from)));
        return t * t * (3.0D - 2.0D * t);
    }

    private static double lerp(double from, double to, double t) {
        return from + (to - from) * t;
    }
}
