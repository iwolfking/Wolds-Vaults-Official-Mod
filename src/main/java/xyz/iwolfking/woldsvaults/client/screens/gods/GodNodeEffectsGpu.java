package xyz.iwolfking.woldsvaults.client.screens.gods;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.math.Matrix4f;
import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeType;

import java.util.List;
import java.util.Map;

/**
 * GPU renderer for {@link GodNodeEffects}. Each (node type, phase) canvas owns a two-channel float texture and
 * framebuffer, red for the light behind the star and green for the light over it. The accumulation pass draws every
 * primitive {@link GodNodeEffects#paint} places as one quad, additively (GL_ONE, GL_ONE), in one batch per canvas;
 * shaders/core/node_effect_accum.fsh evaluates the exact pixel coverage and values of the CPU path's dot, stroke and
 * glow. The tone pass draws each star's quad through shaders/core/node_effect_tone.fsh, which reproduces
 * GodNodeEffects#tone from the summed light and a 256 x gods ramp texture built once from GodNodeEffects#ramps().
 */
final class GodNodeEffectsGpu {
    static final VertexFormat ACCUM_FORMAT = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder()
            .put("Position", DefaultVertexFormat.ELEMENT_POSITION)
            .put("Seg", new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.UV, 4))
            .put("Val", new VertexFormatElement(1, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.UV, 4))
            .put("Kind", new VertexFormatElement(2, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.UV, 4))
            .build());

    private static final int DOT = 0;
    private static final int STROKE = 1;
    private static final int GLOW = 2;
    private static final int[] FORMATS = {GL30.GL_RG32F, GL30.GL_RG16F};
    private static final String[] FORMAT_NAMES = {"RG32F", "RG16F"};
    private static final float[] ZERO = new float[4];
    private static final Target[] TARGETS = new Target[GodNodeType.values().length * GodNodeEffects.PHASE_COUNT];
    private static final Accumulator ACCUMULATOR = new Accumulator();

    private static DynamicTexture rampTexture;
    private static int format;
    private static String failure;
    private static boolean shadersWarned;

    private GodNodeEffectsGpu() {
    }

    private static final class Target {
        private final int framebuffer;
        private final int texture;
        private final int size;
        private boolean painted;

        Target(int framebuffer, int texture, int size) {
            this.framebuffer = framebuffer;
            this.texture = texture;
            this.size = size;
        }

        void delete() {
            GlStateManager._glDeleteFramebuffers(this.framebuffer);
            GlStateManager._deleteTexture(this.texture);
        }
    }

    static boolean isUnavailable() {
        return failure != null || GodTreeShaders.nodeEffectAccum == null || GodTreeShaders.nodeEffectTone == null;
    }

    /**
     * Frees every canvas target and the ramp texture and clears any earlier failure, so the next frame retries the
     * GPU path with freshly loaded shaders and ramps. Render thread only.
     */
    static void release() {
        for (int i = 0; i < TARGETS.length; i++) {
            if (TARGETS[i] != null) {
                TARGETS[i].delete();
                TARGETS[i] = null;
            }
        }
        if (rampTexture != null) {
            rampTexture.close();
            rampTexture = null;
        }
        format = 0;
        failure = null;
        shadersWarned = false;
    }

    /**
     * Draws one layer of the effects for the grouped stars, as GodNodeEffects#render describes. Returns false, having
     * drawn nothing, when the GPU path is unavailable; the reason is logged once and the caller falls back to the CPU
     * path.
     */
    static boolean render(PoseStack poseStack, VaultGod god, Map<GodNodeType, GodNodeEffects.Look> settings,
                          List<GodNodeWidget>[] groups, boolean front, double seconds) {
        if (failure != null) {
            return false;
        }
        ShaderInstance accum = GodTreeShaders.nodeEffectAccum;
        ShaderInstance tone = GodTreeShaders.nodeEffectTone;
        if (accum == null || tone == null) {
            if (!shadersWarned) {
                shadersWarned = true;
                WoldsVaults.LOGGER.error("God node effects: GPU path unavailable, shader node_effect_accum or"
                        + " node_effect_tone is not loaded; using the CPU path");
            }
            return false;
        }
        DynamicTexture ramps = rampTexture();
        if (ramps == null || !paint(accum, settings, groups, front, seconds)) {
            return false;
        }

        Matrix4f pose = poseStack.last().pose();
        if (tone.MODEL_VIEW_MATRIX != null) {
            tone.MODEL_VIEW_MATRIX.set(RenderSystem.getModelViewMatrix());
        }
        if (tone.PROJECTION_MATRIX != null) {
            tone.PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());
        }
        if (tone.COLOR_MODULATOR != null) {
            tone.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
        }
        tone.setSampler("Sampler1", ramps.getId());
        tone.safeGetUniform("RampRow").set((float) god.ordinal());
        GlStateManager.SourceFactor src = GlStateManager.SourceFactor.SRC_ALPHA;
        GlStateManager.DestFactor dst = front ? GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA : GlStateManager.DestFactor.ONE;
        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        for (GodNodeType type : GodNodeType.values()) {
            GodNodeEffects.Look look = settings.get(type);
            for (int phase = 0; phase < GodNodeEffects.PHASE_COUNT; phase++) {
                int index = type.ordinal() * GodNodeEffects.PHASE_COUNT + phase;
                List<GodNodeWidget> group = groups[index];
                if (group == null) {
                    continue;
                }
                Target target = TARGETS[index];
                GodNodeEffects.Light light = look.light();
                tone.setSampler("Sampler0", target.texture);
                tone.safeGetUniform("Tone").set((float) light.gain(), (float) light.toneFloor(),
                        (float) light.toneSteps(), front ? 1.0F : 0.0F);
                buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
                GodNodeEffects.quads(buf, pose, group, target.size / 2.0F, 0.0F, 1.0F);
                draw(tone, buf, src, dst);
            }
        }
        RenderSystem.defaultBlendFunc();
        return true;
    }

    /**
     * Accumulation pass: sums this frame's light into the target of every group that needs it (every group behind the
     * stars; over the stars only a target never painted). Saves and restores the framebuffer bindings, viewport,
     * scissor, cull and depth test it changes. Returns false if a target cannot be made.
     */
    private static boolean paint(ShaderInstance accum, Map<GodNodeType, GodNodeEffects.Look> settings,
                                 List<GodNodeWidget>[] groups, boolean front, double seconds) {
        int phases = GodNodeEffects.PHASE_COUNT;
        boolean needed = false;
        for (int index = 0; index < groups.length && !needed; index++) {
            needed = groups[index] != null && (!front || TARGETS[index] == null || !TARGETS[index].painted);
        }
        if (!needed) {
            return true;
        }
        int drawFramebuffer = GlStateManager.getBoundFramebuffer();
        int readFramebuffer = GlStateManager._getInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        int viewX = GlStateManager.Viewport.x();
        int viewY = GlStateManager.Viewport.y();
        int viewWidth = GlStateManager.Viewport.width();
        int viewHeight = GlStateManager.Viewport.height();
        boolean scissor = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        boolean cull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        GlStateManager._disableScissorTest();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        try {
            BufferBuilder buf = Tesselator.getInstance().getBuilder();
            for (GodNodeType type : GodNodeType.values()) {
                GodNodeEffects.Look look = settings.get(type);
                for (int phase = 0; phase < phases; phase++) {
                    int index = type.ordinal() * phases + phase;
                    if (groups[index] == null) {
                        continue;
                    }
                    Target target = target(index, look.size());
                    if (target == null) {
                        return false;
                    }
                    if (front && target.painted) {
                        continue;
                    }
                    GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, target.framebuffer);
                    GlStateManager._viewport(0, 0, target.size, target.size);
                    GL30.glClearBufferfv(GL11.GL_COLOR, 0, ZERO);
                    accum.safeGetUniform("CanvasSize").set((float) target.size, (float) target.size);
                    buf.begin(VertexFormat.Mode.QUADS, ACCUM_FORMAT);
                    ACCUMULATOR.buf = buf;
                    GodNodeEffects.paint(look, GodNodeEffects.phaseTime(seconds, phase), target.size / 2.0D, ACCUMULATOR);
                    ACCUMULATOR.buf = null;
                    draw(accum, buf, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
                    target.painted = true;
                }
            }
            return true;
        } finally {
            GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFramebuffer);
            GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFramebuffer);
            GlStateManager._viewport(viewX, viewY, viewWidth, viewHeight);
            if (scissor) {
                GlStateManager._enableScissorTest();
            }
            if (cull) {
                RenderSystem.enableCull();
            }
            if (depth) {
                RenderSystem.enableDepthTest();
            }
            RenderSystem.defaultBlendFunc();
        }
    }

    /**
     * Ends buf and draws it with shader under the given blend. The blend is set after ShaderInstance#apply, whose
     * cached BlendMode would otherwise keep or override it depending on the previously applied shader.
     */
    private static void draw(ShaderInstance shader, BufferBuilder buf, GlStateManager.SourceFactor src,
                             GlStateManager.DestFactor dst) {
        buf.end();
        shader.apply();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(src, dst);
        BufferUploader._endInternal(buf);
        shader.clear();
    }

    private static Target target(int index, int size) {
        Target target = TARGETS[index];
        if (target != null && target.size == size) {
            return target;
        }
        if (target != null) {
            target.delete();
            TARGETS[index] = null;
        }
        target = create(size);
        TARGETS[index] = target;
        return target;
    }

    /**
     * Allocates a size x size two-channel float texture and a framebuffer around it: RG32F, which sums like the CPU
     * path's float buffers, or RG16F where the driver cannot render to RG32F. Leaves the new framebuffer bound;
     * the caller restores the binding. Returns null, with the GPU path marked failed, when neither is complete.
     */
    private static Target create(int size) {
        int previousTexture = GlStateManager._getInteger(GL11.GL_TEXTURE_BINDING_2D);
        for (; format < FORMATS.length; format++) {
            int texture = GlStateManager._genTexture();
            GlStateManager._bindTexture(texture);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL30.GL_CLAMP_TO_EDGE);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL30.GL_CLAMP_TO_EDGE);
            GlStateManager._texImage2D(GL11.GL_TEXTURE_2D, 0, FORMATS[format], size, size, 0, GL30.GL_RG, GL11.GL_FLOAT, null);
            GlStateManager._bindTexture(previousTexture);
            int framebuffer = GlStateManager.glGenFramebuffers();
            GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);
            GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, texture, 0);
            int status = GlStateManager.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
            if (status == GL30.GL_FRAMEBUFFER_COMPLETE) {
                return new Target(framebuffer, texture, size);
            }
            GlStateManager._glDeleteFramebuffers(framebuffer);
            GlStateManager._deleteTexture(texture);
            if (format + 1 < FORMATS.length) {
                WoldsVaults.LOGGER.warn("God node effects: {} framebuffer incomplete (status 0x{}); trying {}, which sums"
                                + " light at half precision", FORMAT_NAMES[format], Integer.toHexString(status),
                        FORMAT_NAMES[format + 1]);
            } else {
                fail(FORMAT_NAMES[format] + " framebuffer incomplete (status 0x" + Integer.toHexString(status) + ")", null);
            }
        }
        return null;
    }

    /**
     * The ramp of every god as a 256 x gods RGBA8 texture, row = god ordinal, uploaded once per resource load.
     */
    private static DynamicTexture rampTexture() {
        if (rampTexture == null) {
            int[][] ramps = GodNodeEffects.ramps();
            NativeImage image = null;
            try {
                image = new NativeImage(256, ramps.length, false);
                for (int y = 0; y < ramps.length; y++) {
                    for (int x = 0; x < 256; x++) {
                        int rgb = ramps[y][x];
                        image.setPixelRGBA(x, y, 0xFF000000 | (rgb & 0xFF) << 16 | (rgb >> 8 & 0xFF) << 8 | (rgb >> 16 & 0xFF));
                    }
                }
                rampTexture = new DynamicTexture(image);
            } catch (RuntimeException e) {
                if (image != null) {
                    image.close();
                }
                fail("the ramp texture could not be built", e);
            }
        }
        return rampTexture;
    }

    private static void fail(String reason, Throwable cause) {
        failure = reason;
        if (cause == null) {
            WoldsVaults.LOGGER.error("God node effects: GPU path unavailable, {}; using the CPU path", reason);
        } else {
            WoldsVaults.LOGGER.error("God node effects: GPU path unavailable, {}; using the CPU path", reason, cause);
        }
    }

    /**
     * Turns the primitives GodNodeEffects#paint places into ACCUM_FORMAT quads. Each quad carries its primitive
     * whole (kind, channel, position split into an integer origin and a fraction, values, widths, stamp count) and
     * covers every pixel the primitive can reach; node_effect_accum.fsh decides coverage per pixel exactly as the CPU
     * path does.
     */
    private static final class Accumulator implements GodNodeEffects.Splat {
        private BufferBuilder buf;
        private float segX;
        private float segY;
        private float segDx;
        private float segDy;
        private float v0;
        private float v1;
        private float w0;
        private float w1;
        private float code;
        private float steps;
        private float originX;
        private float originY;

        @Override
        public void dot(boolean front, double x, double y, double v, double w) {
            if (w <= 1.25D) {
                double px = Math.floor(x);
                double py = Math.floor(y);
                this.primitive(DOT, front, px, py, 0.5D, 0.5D, 0.0D, 0.0D, v, 0.0D, 0.0F, 0.0F, 0);
                this.square(px + 0.5D, py + 0.5D, 1.0D);
                return;
            }
            float width = (float) w;
            if (width <= 1.25F) {
                width = Math.nextUp(1.25F);
            }
            double ox = Math.floor(x);
            double oy = Math.floor(y);
            this.primitive(DOT, front, ox, oy, x - ox, y - oy, 0.0D, 0.0D, v, 0.0D, width, 0.0F, 0);
            this.square(x, y, w / 2.0D + 1.0D);
        }

        @Override
        public void stroke(boolean front, double x0, double y0, double x1, double y1, double v0, double v1, double w0, double w1) {
            double dx = x1 - x0;
            double dy = y1 - y0;
            double length = Math.sqrt(dx * dx + dy * dy);
            int steps = Math.max(1, (int) Math.ceil(length * 2.0D));
            double ox = Math.floor(x0);
            double oy = Math.floor(y0);
            this.primitive(STROKE, front, ox, oy, x0 - ox, y0 - oy, dx, dy, v0, v1, (float) w0, (float) w1, steps);
            double margin = Math.max(Math.max(w0, w1) / 2.0D, 1.5D) + 1.0D;
            if (length < 1.0E-6D) {
                this.square(x0, y0, margin);
                return;
            }
            double ux = dx / length * margin;
            double uy = dy / length * margin;
            this.vertex(x0 - ux + uy, y0 - uy - ux);
            this.vertex(x1 + ux + uy, y1 + uy - ux);
            this.vertex(x1 + ux - uy, y1 + uy + ux);
            this.vertex(x0 - ux - uy, y0 - uy + ux);
        }

        @Override
        public void glow(double centre, double radius, double k) {
            double origin = Math.floor(centre);
            this.primitive(GLOW, false, origin, origin, centre - origin, centre - origin, 0.0D, 0.0D, k, 0.0D,
                    (float) radius, 0.0F, 0);
            this.square(centre, centre, radius + 1.0D);
        }

        private void primitive(int kind, boolean front, double ox, double oy, double fx, double fy, double dx, double dy,
                               double v0, double v1, float w0, float w1, int steps) {
            this.code = kind * 2 + (front ? 1 : 0);
            this.originX = (float) ox;
            this.originY = (float) oy;
            this.segX = (float) fx;
            this.segY = (float) fy;
            this.segDx = (float) dx;
            this.segDy = (float) dy;
            this.v0 = (float) v0;
            this.v1 = (float) v1;
            this.w0 = w0;
            this.w1 = w1;
            this.steps = steps;
        }

        private void square(double x, double y, double half) {
            this.vertex(x - half, y - half);
            this.vertex(x + half, y - half);
            this.vertex(x + half, y + half);
            this.vertex(x - half, y + half);
        }

        private void vertex(double x, double y) {
            BufferBuilder b = this.buf;
            b.vertex(x, y, 0.0D);
            b.putFloat(0, this.segX);
            b.putFloat(4, this.segY);
            b.putFloat(8, this.segDx);
            b.putFloat(12, this.segDy);
            b.nextElement();
            b.putFloat(0, this.v0);
            b.putFloat(4, this.v1);
            b.putFloat(8, this.w0);
            b.putFloat(12, this.w1);
            b.nextElement();
            b.putFloat(0, this.code);
            b.putFloat(4, this.steps);
            b.putFloat(8, this.originX);
            b.putFloat(12, this.originY);
            b.nextElement();
            b.endVertex();
        }
    }
}
