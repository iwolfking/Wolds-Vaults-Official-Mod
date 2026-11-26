package xyz.iwolfking.woldsvaults.client.simon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.client.model.data.EmptyModelData;
import xyz.iwolfking.woldsvaults.client.arcane.GlowRenderTypes;
import xyz.iwolfking.woldsvaults.client.arcane.GlowShapes;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.ColorUtil;
import xyz.iwolfking.woldsvaults.entities.simon.SimonCrystalEntity;

/**
 * Draws a prism crystal, see-through (its texture is partly transparent, drawn translucent), as its stage's block model (a void-core style column with a sunken rim on top, shards leaning
 * out from it and rubble round the base, in the crystal's colour; a Prismatic crystal uses the grey model tinted
 * through the hues), standing out from the face it grows on and turned a fixed amount per crystal. Dark crystals are
 * slightly dimmed, armed ones (waiting for the sequence) pulse, and lit ones are full bright with an additive halo; all
 * of them give off a little light of their own. Each new stage rises out of the ground. A Geode has a soft pulsing
 * halo (brighter for each facet) and a glowing sunken top. Each of its facets is a bright gem orbiting it, evenly
 * spaced on a glowing ring and trailing light, so the count reads at a glance. A Prismatic crystal wears a rainbow
 * aura: a glowing shell half the hue wheel away from its own colour, and rainbow rings turning round its base and its
 * middle.
 * <p>
 * A crystal lit in a sequence also shoots a pillar of light in its colour straight up into the sky: a soft wide glow,
 * a brighter band and a white-hot core, fading towards the top, with a flare at its foot and one riding up its head
 * as it shoots. When the crystal goes out, the pillar's foot lifts away and it fades. So it's easy to see which
 * crystal is lit, even behind others or from across the room.
 */
public class SimonCrystalRenderer extends EntityRenderer<SimonCrystalEntity> {
    private static final String[] STAGES = SimonCrystalEntity.STAGE_NAMES;
    private static final int WHITE_ROW = SimonCrystalEntity.Color.values().length;
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[WHITE_ROW + 1];
    private static final ResourceLocation[][] MODELS = new ResourceLocation[WHITE_ROW + 1][STAGES.length];
    private static final float[] LENGTHS = {0.3F, 0.6F, 1.2F, 2.0F};
    private static final float RISE_TICKS = 10.0F;
    private static final float GEODE_FLOOR = 30.0F / 16.0F;
    private static final float GEODE_INNER_HALF = 4.0F / 16.0F;
    private static final float PILLAR_HEIGHT = 14.0F;
    private static final float PILLAR_RISE_TICKS = 3.0F;
    private static final float PILLAR_FADE_TICKS = 5.0F;

    static {
        for (int row = 0; row <= WHITE_ROW; row++) {
            String color = row == WHITE_ROW ? "white" : SimonCrystalEntity.Color.values()[row].id;
            TEXTURES[row] = WoldsVaults.id("block/simon_crystal/" + color);
            for (int stage = 0; stage < STAGES.length; stage++) {
                MODELS[row][stage] = WoldsVaults.id("entity/simon_crystal/" + color + "_" + STAGES[stage]);
            }
        }
    }

    public SimonCrystalRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    private static int row(SimonCrystalEntity.Color color, boolean prismatic) {
        return prismatic ? WHITE_ROW : color.ordinal();
    }

    /** Renders one crystal model, base centred on the pose's origin, growing up its y axis. */
    static void renderModel(PoseStack pose, MultiBufferSource buffers, RenderType type, SimonCrystalEntity.Color color, boolean prismatic,
                            Vector3f tint, int stage, float shade, int light) {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(MODELS[row(color, prismatic)][Mth.clamp(stage, 0, STAGES.length - 1)]);
        ModelBlockRenderer renderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();
        float r = prismatic ? tint.x() * shade : shade;
        float g = prismatic ? tint.y() * shade : shade;
        float b = prismatic ? tint.z() * shade : shade;
        pose.pushPose();
        pose.translate(-0.5, 0.0, -0.5);
        renderer.renderModel(pose.last(), buffers.getBuffer(type), null, model, r, g, b, light, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
        pose.popPose();
    }

    @Override
    public void render(SimonCrystalEntity crystal, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light) {
        byte state = crystal.getLight();
        int stage = crystal.getStage();
        float time = crystal.tickCount + partialTicks;
        float shade = state == SimonCrystalEntity.LIT ? 1.0F
                : state == SimonCrystalEntity.ARMED ? 0.9F + 0.1F * Mth.sin(time * 0.3F) : 0.88F;
        int ownLight = stage == SimonCrystalEntity.GEODE ? 13 : 9;
        int packed = state == SimonCrystalEntity.LIT ? LightTexture.FULL_BRIGHT
                : LightTexture.pack(Math.max(LightTexture.block(light), ownLight), LightTexture.sky(light));
        Vector3f tint = crystal.shownColor(partialTicks);
        RenderType glow = RenderType.eyes(TextureAtlas.LOCATION_BLOCKS);
        float rise = Mth.clamp((crystal.ticksSinceGrown() + partialTicks) / RISE_TICKS, 0.0F, 1.0F);
        rise = 1.0F - (1.0F - rise) * (1.0F - rise);

        pose.pushPose();
        standOn(pose, crystal.getFace());
        pose.mulPose(Vector3f.YP.rotationDegrees(crystal.getId() * 47 % 360));
        pose.translate(0.0, -(1.0F - rise) * LENGTHS[Mth.clamp(stage, 0, LENGTHS.length - 1)] * 0.7F, 0.0);
        renderModel(pose, buffers, Sheets.translucentCullBlockSheet(), crystal.getColor(), crystal.isPrismatic(), tint, stage, shade, packed);
        if (stage == SimonCrystalEntity.GEODE) {
            float pulse = Mth.sin(time * 0.15F);
            renderCoreGlow(pose, buffers.getBuffer(glow), crystal, tint, 0.5F + 0.2F * pulse);
            if (state != SimonCrystalEntity.LIT) {
                pose.pushPose();
                pose.scale(1.07F, 1.03F, 1.07F);
                renderModel(pose, buffers, glow, crystal.getColor(), crystal.isPrismatic(), tint, stage,
                        0.2F + 0.07F * pulse + 0.08F * crystal.getFacets(), LightTexture.FULL_BRIGHT);
                pose.popPose();
            }
        }
        if (crystal.isPrismatic() && state != SimonCrystalEntity.LIT) {
            Vector3f aura = hue(time * 0.02F + crystal.getId() * 0.13F + 0.5F);
            pose.pushPose();
            pose.scale(1.14F, 1.06F, 1.14F);
            renderModel(pose, buffers, glow, crystal.getColor(), true, aura, stage, 0.35F + 0.1F * Mth.sin(time * 0.2F), LightTexture.FULL_BRIGHT);
            pose.popPose();
        }
        if (state == SimonCrystalEntity.LIT) {
            pose.pushPose();
            pose.scale(1.15F, 1.08F, 1.15F);
            pose.translate(0.0, -0.02, 0.0);
            renderModel(pose, buffers, glow, crystal.getColor(), crystal.isPrismatic(), tint, stage, 0.55F, LightTexture.FULL_BRIGHT);
            pose.popPose();
        }
        pose.popPose();
        renderFacets(crystal, tint, partialTicks, pose, buffers);
        renderPrismaticAura(crystal, partialTicks, pose, buffers);
        renderPillar(crystal, tint, partialTicks, pose, buffers);
        super.render(crystal, yaw, partialTicks, pose, buffers, light);
    }

    @Override
    public boolean shouldRender(SimonCrystalEntity crystal, Frustum frustum, double x, double y, double z) {
        return super.shouldRender(crystal, frustum, x, y, z)
                || pillarShowing(crystal) && frustum.isVisible(crystal.getBoundingBox().expandTowards(0.0, PILLAR_HEIGHT + 2.0, 0.0));
    }

    private static boolean pillarShowing(SimonCrystalEntity crystal) {
        return crystal.getLight() == SimonCrystalEntity.LIT || crystal.ticksSinceUnlit() < PILLAR_FADE_TICKS;
    }

    /** Renders a pillar of light in the crystal's colour, shooting straight up from the top of it while it is lit. */
    private void renderPillar(SimonCrystalEntity crystal, Vector3f tint, float partialTicks, PoseStack pose, MultiBufferSource buffers) {
        if (!pillarShowing(crystal)) {
            return;
        }
        boolean on = crystal.getLight() == SimonCrystalEntity.LIT;
        float fade = on ? 1.0F : 1.0F - (crystal.ticksSinceUnlit() + partialTicks) / PILLAR_FADE_TICKS;
        if (fade <= 0.0F) {
            return;
        }
        float lit = crystal.ticksSinceLit() + partialTicks;
        float rise = Mth.clamp(lit / PILLAR_RISE_TICKS, 0.0F, 1.0F);
        rise = 1.0F - (1.0F - rise) * (1.0F - rise);
        Vec3 middle = crystal.center().subtract(crystal.position());
        float x = (float) middle.x;
        float z = (float) middle.z;
        float top = (float) (crystal.getBoundingBox().maxY - crystal.getY());
        float foot = top + (1.0F - fade) * PILLAR_HEIGHT * 0.6F;
        float head = top + PILLAR_HEIGHT * rise + (1.0F - fade) * 2.0F;
        if (head <= foot) {
            return;
        }
        Camera camera = this.entityRenderDispatcher.camera;
        double dx = camera.getPosition().x - (crystal.getX() + x);
        double dz = camera.getPosition().z - (crystal.getZ() + z);
        double flat = Math.sqrt(dx * dx + dz * dz);
        Vec3 side = flat < 1.0E-3 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(-dz / flat, 0.0, dx / flat);
        float time = crystal.tickCount + partialTicks;
        float width = (0.4F + 0.3F * crystal.widthScale()) * (1.0F + 0.06F * Mth.sin(time * 1.3F));
        VertexConsumer consumer = buffers.getBuffer(GlowRenderTypes.GLOW);
        Matrix4f matrix = pose.last().pose();
        Vec3 footAt = new Vec3(x, foot, z);
        Vec3 headAt = new Vec3(x, head, z);
        Vec3 midAt = new Vec3(x, foot + (head - foot) * 0.35F, z);
        Vector3f[] colors = {tint, ColorUtil.lighten(tint, 0.35F), GlowShapes.WHITE};
        float[] halves = {width, width * 0.4F, width * 0.1F};
        float[] alphas = {0.4F * fade, 0.65F * fade, 0.9F * fade};
        for (int i = 0; i < colors.length; i++) {
            GlowShapes.band(consumer, matrix, footAt, midAt, side, halves[i], colors[i], alphas[i], alphas[i] * 0.75F);
            GlowShapes.band(consumer, matrix, midAt, headAt, side, halves[i], colors[i], alphas[i] * 0.75F, 0.0F);
        }
        Vector3f left = camera.getLeftVector();
        Vector3f up = camera.getUpVector();
        GlowShapes.flare(consumer, matrix, footAt, left, up, width * 1.8F * (1.0F + 0.1F * Mth.sin(time * 0.9F)), tint, 0.7F * fade);
        GlowShapes.flare(consumer, matrix, footAt, left, up, width * 0.6F, GlowShapes.WHITE, 0.8F * fade);
        if (on && rise < 1.0F) {
            GlowShapes.flare(consumer, matrix, headAt, left, up, width * 1.5F, ColorUtil.lighten(tint, 0.5F), 0.9F);
        }
    }

    private static void glow(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, Vector3f color, float a) {
        consumer.vertex(matrix, x, y, z).color(color.x(), color.y(), color.z(), a).endVertex();
    }

    /**
     * Draws the Geode's sunken floor glowing inside its rim, additively just above its texture's inner-floor square.
     */
    private static void renderCoreGlow(PoseStack pose, VertexConsumer consumer, SimonCrystalEntity crystal, Vector3f tint, float strength) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(TEXTURES[row(crystal.getColor(), crystal.isPrismatic())]);
        Matrix4f matrix = pose.last().pose();
        Matrix3f normal = pose.last().normal();
        float y = GEODE_FLOOR + 0.01F;
        float h = GEODE_INNER_HALF;
        float r = (crystal.isPrismatic() ? tint.x() : 1.0F) * strength;
        float g = (crystal.isPrismatic() ? tint.y() : 1.0F) * strength;
        float b = (crystal.isPrismatic() ? tint.z() : 1.0F) * strength;
        float u0 = sprite.getU(8.0), u1 = sprite.getU(12.0), v0 = sprite.getV(0.0), v1 = sprite.getV(4.0);
        float[][] corners = {{-h, -h, u0, v0}, {-h, h, u0, v1}, {h, h, u1, v1}, {h, -h, u1, v0}};
        for (float[] c : corners) {
            consumer.vertex(matrix, c[0], y, c[1]).color(r, g, b, 1.0F).uv(c[2], c[3]).overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(LightTexture.FULL_BRIGHT).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        }
    }

    /**
     * Renders each facet as a bright gem orbiting the crystal round its middle, evenly spaced on one glowing ring and
     * trailing light.
     */
    private void renderFacets(SimonCrystalEntity crystal, Vector3f tint, float partialTicks, PoseStack pose, MultiBufferSource buffers) {
        int facets = crystal.getFacets();
        if (facets <= 0) {
            return;
        }
        AABB box = crystal.getBoundingBox();
        Vec3 middle = crystal.center().subtract(crystal.position());
        float cx = (float) middle.x;
        float cy = (float) middle.y;
        float cz = (float) middle.z;
        float radius = (float) Math.max(box.getXsize(), box.getZsize()) * 0.5F + 0.5F;
        float time = crystal.tickCount + partialTicks;
        Vector3f bright = ColorUtil.lighten(tint, 0.3F);
        VertexConsumer consumer = buffers.getBuffer(GlowRenderTypes.GLOW);
        Matrix4f matrix = pose.last().pose();
        Camera camera = this.entityRenderDispatcher.camera;
        Vector3f left = camera.getLeftVector();
        Vector3f up = camera.getUpVector();
        ring(consumer, matrix, cx, cy, cz, radius, 0.06F, 0.0F, bright, 0.2F + 0.08F * facets, false);
        float spin = time * 0.07F;
        for (int i = 0; i < facets; i++) {
            float angle = spin + i * Mth.TWO_PI / facets;
            float y = cy + 0.08F * Mth.sin(time * 0.15F + i * 2.1F);
            for (int trail = 4; trail >= 1; trail--) {
                float back = angle - trail * 0.1F;
                GlowShapes.flare(consumer, matrix, new Vec3(cx + Mth.cos(back) * radius, y, cz + Mth.sin(back) * radius), left, up,
                        0.24F - trail * 0.03F, bright, 0.4F * (1.0F - trail / 5.0F));
            }
            float x = cx + Mth.cos(angle) * radius;
            float z = cz + Mth.sin(angle) * radius;
            gem(consumer, matrix, x, y, z, 0.15F, time * 0.2F + i, bright);
            Vec3 at = new Vec3(x, y, z);
            GlowShapes.flare(consumer, matrix, at, left, up, 0.5F + 0.06F * Mth.sin(time * 0.5F + i), bright, 0.55F);
            GlowShapes.flare(consumer, matrix, at, left, up, 0.2F, GlowShapes.WHITE, 0.8F);
        }
    }

    /**
     * Renders a Prismatic crystal's rainbow rings: one turning round its base and a narrower one turning the other way
     * round its middle, the colours chasing round them.
     */
    private void renderPrismaticAura(SimonCrystalEntity crystal, float partialTicks, PoseStack pose, MultiBufferSource buffers) {
        if (!crystal.isPrismatic()) {
            return;
        }
        AABB box = crystal.getBoundingBox();
        Vec3 middle = crystal.center().subtract(crystal.position());
        float cx = (float) middle.x;
        float cz = (float) middle.z;
        float base = (float) (box.minY - crystal.getY()) + 0.05F;
        float half = (float) Math.max(box.getXsize(), box.getZsize()) * 0.5F;
        float time = crystal.tickCount + partialTicks;
        VertexConsumer consumer = buffers.getBuffer(GlowRenderTypes.GLOW);
        Matrix4f matrix = pose.last().pose();
        float breath = 0.5F + 0.1F * Mth.sin(time * 0.2F);
        ring(consumer, matrix, cx, base, cz, half + 0.4F, 0.25F, time * 0.04F, GlowShapes.WHITE, breath, true);
        ring(consumer, matrix, cx, (float) middle.y, cz, half + 0.3F, 0.1F, -time * 0.06F, GlowShapes.WHITE, breath * 0.7F, true);
    }

    /**
     * Draws a flat ring round (x, y, z), half wide either side of its radius, brightest along its middle; in one
     * colour, or with rainbow set, running once round the hue wheel from phase.
     */
    private static void ring(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, float radius, float half, float phase,
                             Vector3f color, float alpha, boolean rainbow) {
        int segments = 48;
        Vector3f c1 = rainbow ? hue(phase / Mth.TWO_PI) : color;
        for (int k = 0; k < segments; k++) {
            float a0 = k * Mth.TWO_PI / segments;
            float a1 = (k + 1) * Mth.TWO_PI / segments;
            Vector3f c0 = c1;
            if (rainbow) {
                c1 = hue(phase / Mth.TWO_PI + (float) (k + 1) / segments);
            }
            float cos0 = Mth.cos(a0);
            float sin0 = Mth.sin(a0);
            float cos1 = Mth.cos(a1);
            float sin1 = Mth.sin(a1);
            float in = radius - half;
            float out = radius + half;
            glow(consumer, matrix, x + cos0 * in, y, z + sin0 * in, c0, 0.0F);
            glow(consumer, matrix, x + cos1 * in, y, z + sin1 * in, c1, 0.0F);
            glow(consumer, matrix, x + cos1 * radius, y, z + sin1 * radius, c1, alpha);
            glow(consumer, matrix, x + cos0 * radius, y, z + sin0 * radius, c0, alpha);
            glow(consumer, matrix, x + cos0 * radius, y, z + sin0 * radius, c0, alpha);
            glow(consumer, matrix, x + cos1 * radius, y, z + sin1 * radius, c1, alpha);
            glow(consumer, matrix, x + cos1 * out, y, z + sin1 * out, c1, 0.0F);
            glow(consumer, matrix, x + cos0 * out, y, z + sin0 * out, c0, 0.0F);
        }
    }

    /**
     * Draws a small cut gem (two four-sided pyramids base to base) turned by spin: tinted round its girdle, white at
     * its points.
     */
    private static void gem(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, float size, float spin, Vector3f color) {
        float[][] girdle = new float[4][];
        for (int k = 0; k < 4; k++) {
            float a = spin + k * Mth.HALF_PI;
            girdle[k] = new float[]{x + Mth.cos(a) * size, y, z + Mth.sin(a) * size};
        }
        float tip = size * 1.5F;
        for (int k = 0; k < 4; k++) {
            float[] p = girdle[k];
            float[] q = girdle[(k + 1) % 4];
            for (float point : new float[]{y + tip, y - tip}) {
                glow(consumer, matrix, p[0], p[1], p[2], color, 0.75F);
                glow(consumer, matrix, q[0], q[1], q[2], color, 0.75F);
                glow(consumer, matrix, x, point, z, GlowShapes.WHITE, 0.95F);
                glow(consumer, matrix, x, point, z, GlowShapes.WHITE, 0.95F);
            }
        }
    }

    private static Vector3f hue(float hue) {
        return ColorUtil.vector(Mth.hsvToRgb(((hue % 1.0F) + 1.0F) % 1.0F, 0.8F, 1.0F));
    }

    /** Turns the model's up to point out of the face the crystal grows on. */
    private static void standOn(PoseStack pose, Direction face) {
        switch (face) {
            case DOWN -> pose.mulPose(Vector3f.XP.rotationDegrees(180.0F));
            case NORTH -> pose.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
            case SOUTH -> pose.mulPose(Vector3f.XP.rotationDegrees(90.0F));
            case EAST -> pose.mulPose(Vector3f.ZP.rotationDegrees(-90.0F));
            case WEST -> pose.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
        }
    }

    @Override
    public ResourceLocation getTextureLocation(SimonCrystalEntity crystal) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    /** Registers every colour and stage of the crystal model as a special model. */
    public static void registerModels() {
        for (ResourceLocation[] stages : MODELS) {
            for (ResourceLocation model : stages) {
                ForgeModelBakery.addSpecialModel(model);
            }
        }
    }
}
