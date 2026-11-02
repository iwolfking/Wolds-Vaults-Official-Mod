package xyz.iwolfking.woldsvaults.client.screens.gods;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import iskallia.vault.client.gui.helper.ScreenDrawHelper;
import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.client.renderer.ShaderInstance;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public final class GodTreeBackdrop {
    private static final String DIR = "textures/gui/gods/backdrop/";
    private static final float DRIFT_SPEED = 3.0F;
    private static final float DRIFT_ANGLE = 12.0F;
    private static final float FAN_SPREAD = 12.0F;
    private static final float METEORS_PER_MINUTE = 10.0F;
    private static final int GALAXY_COUNT = 6;
    private static final int GALAXY_SCALE = 2;
    private static final int SPAN_W = 768, SPAN_H = 432;

    private static final int CLOUDS_PER_SHEET = 3;

    private record Tile(ResourceLocation texture, int size, float depth, float alpha) {}
    private record Clouds(ResourceLocation texture, int cell, float depth, float alpha, float fan, float spacing,
                          float fill, float minScale, float maxScale, long seed) {}
    private record Sheet(ResourceLocation texture, int width, int height) {}
    private record Sprite(float x, float y, int u, int v, int w, int h, int scale, float phase) {}
    private record Look(String secondHaze, int[] galaxies, int cluster, int[][] stars, float nebulaAlpha) {}

    private static final Sheet GALAXIES = new Sheet(tex("galaxies"), 224, 32);
    private static final Sheet CLUSTERS = new Sheet(tex("clusters"), 96, 32);
    private static final Sheet STARS = new Sheet(tex("star_sprites"), 128, 80);
    private static final Sheet COMETS = new Sheet(tex("comets"), 96, 16);
    private static final Map<VaultGod, Look> LOOKS = new EnumMap<>(Map.of(
            VaultGod.IDONA, new Look("neutral", new int[]{3, 4}, 0, new int[][]{{3, 5}, {2, 3}, {3, 7}, {2, 4}}, 0.45F),
            VaultGod.VELARA, new Look("tenos", new int[]{0, 6}, 2, new int[][]{{0, 5}, {0, 7}, {1, 3}, {0, 6}}, 0.72F),
            VaultGod.WENDARR, new Look("neutral", new int[]{2, 4}, 0, new int[][]{{2, 5}, {2, 7}, {0, 3}, {2, 6}}, 0.72F),
            VaultGod.TENOS, new Look("velara", new int[]{1, 5}, 1, new int[][]{{1, 5}, {1, 7}, {0, 4}, {1, 6}}, 0.72F)));
    private static final Map<VaultGod, GodTreeBackdrop> CACHE = new EnumMap<>(VaultGod.class);

    private final Clouds hazeGod, hazeSecond, nebulaBack, nebulaGod, dust, wisps;
    private final Tile starsFar, starsMid, starsNear;
    private final List<Sprite> galaxies, clusters, brightStars;
    private final List<Meteor> meteors = new ArrayList<>();
    private final Random random = new Random();
    private float lastSeconds = -1.0F, spawnIn = 2.0F;
    private final Map<Long, Float> voidCells = new HashMap<>();
    private final List<Mote> motes = new ArrayList<>();
    private BlackHole hole;
    private float holeIn, moteIn;

    private static final float ZOOM = 0.5F;
    private static final float BRIGHTNESS = .25F;

    private static final float GRADE_BRIGHTNESS = 45, GRADE_CONTRAST = 37, GRADE_GAMMA = 1.14F, GRADE_VIBRANCE = 90;

    private static final float BLACK_HOLES_PER_MINUTE = 0.5F;
    private static final float HOLE_DEPTH = 0.2F;
    private static final float HOLE_CROSSING = 14.0F;
    private static final float HOLE_CELL = 1.0F;
    private static final float HOLE_EAT_RADIUS = 9.0F;
    private static final float HOLE_PULL_RADIUS = 24.0F;
    private static final float HOLE_MOTE_SPEED = 8.0F;
    private static final float HOLE_MOTES_PER_SECOND = 70.0F;
    private static final float HOLE_VOID_HOLD = 3.0F;
    private static final float HOLE_VOID_FADE = 2.5F;
    private static final float HOLE_FADE_JITTER = 2.0F;
    private static final int[] MOTE_COLORS = {0xFFFFFFFF, 0xFFBFD4FF, 0xFFFFE2A8, 0xFF8A6FB8, 0xFF6B5A3A};

    public static GodTreeBackdrop forGod(VaultGod god) {
        return CACHE.computeIfAbsent(god, GodTreeBackdrop::new);
    }

    private GodTreeBackdrop(VaultGod god) {
        String name = god.getName().toLowerCase(Locale.ROOT);
        Look look = LOOKS.get(god);
        Random layers = new Random(god.ordinal() * 1000L + 99);
        this.hazeGod = new Clouds(tex("clouds_haze_" + name), 128, 0.0F, 0.20F, 0, 380, 0.85F, 2.5F, 4.0F, layers.nextLong());
        this.hazeSecond = new Clouds(tex("clouds_haze_" + look.secondHaze()), 128, 0.0F, 0.14F, 0, 440, 0.8F, 2.5F, 4.0F, layers.nextLong());
        this.nebulaBack = new Clouds(tex("clouds_nebula_neutral"), 128, 0.10F, 0.50F, -1, 230, 0.65F, 1.2F, 2.0F, layers.nextLong());
        this.nebulaGod = new Clouds(tex("clouds_nebula_" + name), 128, 0.26F, look.nebulaAlpha(), 0, 190, 0.75F, 1.0F, 1.7F, layers.nextLong());
        this.dust = new Clouds(tex("clouds_dust"), 64, 0.34F, 0.85F, 0.5F, 170, 0.55F, 1.5F, 2.5F, layers.nextLong());
        this.wisps = new Clouds(tex("clouds_wisps_" + name), 64, 0.48F, 0.32F, 1, 150, 0.5F, 1.2F, 2.2F, layers.nextLong());
        this.starsFar = new Tile(tex("stars_far"), 64, 0.03F, 1.0F);
        this.starsMid = new Tile(tex("stars_mid"), 64, 0.16F, 1.0F);
        this.starsNear = new Tile(tex("stars_near"), 128, 0.55F, 1.0F);

        Random placement = new Random(god.ordinal() * 1000L + 17);
        List<int[]> galaxyCells = new ArrayList<>();
        for (int i = 0; i < GALAXY_COUNT; i++) {
            int type = i < look.galaxies().length || placement.nextFloat() < 0.55F
                    ? look.galaxies()[i % look.galaxies().length] : placement.nextInt(7);
            galaxyCells.add(new int[]{type * 32, 0, 32, 32});
        }
        this.galaxies = scatter(placement, galaxyCells, GALAXY_SCALE);
        this.clusters = scatter(placement, List.of(new int[]{look.cluster() * 32, 0, 32, 32}), 1);
        List<int[]> starCells = new ArrayList<>();
        for (int[] rowCol : look.stars()) {
            starCells.add(new int[]{rowCol[1] * 16, rowCol[0] * 16, 16, 16});
        }
        this.brightStars = scatter(placement, starCells, 1);
        this.holeIn = nextWait(this.random, BLACK_HOLES_PER_MINUTE);
    }

    private static void useGradeShader() {
        ShaderInstance shader = GodTreeShaders.backdropGrade;
        if (shader == null) {
            GodTreeLog.errorOnce("backdrop_grade", "God tree backdrop grade shader did not load; drawing the sky ungraded.");
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            return;
        }
        shader.safeGetUniform("Brightness").set((GRADE_BRIGHTNESS - 50) / 100.0F);
        shader.safeGetUniform("Contrast").set(GRADE_CONTRAST / 50.0F);
        shader.safeGetUniform("Gamma").set(GRADE_GAMMA);
        shader.safeGetUniform("Vibrance").set((GRADE_VIBRANCE - 50) / 50.0F);
        RenderSystem.setShader(() -> shader);
    }

    public void render(PoseStack pose, Rectangle bounds, Vec2 translation, float scale, float tick) {
        float seconds = tick / 20.0F;
        float dt = this.lastSeconds < 0 ? 0.0F : Mth.clamp(seconds - this.lastSeconds, 0.0F, 0.1F);
        this.lastSeconds = seconds;
        float angle = (float) Math.toRadians(DRIFT_ANGLE);
        float panX = (scale * translation.x - Mth.cos(angle) * DRIFT_SPEED * seconds) / ZOOM;
        float panY = (scale * translation.y - Mth.sin(angle) * DRIFT_SPEED * seconds) / ZOOM;
        Rectangle b = new Rectangle(0, 0, Mth.ceil(bounds.width / ZOOM), Mth.ceil(bounds.height / ZOOM));

        pose.pushPose();
        pose.translate(bounds.x, bounds.y, 0.0D);
        pose.scale(ZOOM, ZOOM, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        useGradeShader();
        clouds(pose, b, this.hazeGod, panX, panY);
        clouds(pose, b, this.hazeSecond, panX, panY);
        clouds(pose, b, this.nebulaBack, panX, panY);
        sprites(pose, b, GALAXIES, this.galaxies, 0.07F, panX, panY, false, seconds);
        sprites(pose, b, CLUSTERS, this.clusters, 0.13F, panX, panY, false, seconds);
        clouds(pose, b, this.nebulaGod, panX, panY);
        clouds(pose, b, this.dust, panX, panY);
        clouds(pose, b, this.wisps, panX, panY);
        starTile(pose, b, this.starsFar, panX, panY, 1L);
        starTile(pose, b, this.starsMid, panX, panY, 2L);
        starTile(pose, b, this.starsNear, panX, panY, 3L);

        sprites(pose, b, STARS, this.brightStars, 0.62F, panX, panY, true, seconds);
        this.meteors(pose, b, dt);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.blackHole(pose, b, panX, panY, seconds, dt);
        shade(pose, b);
        pose.popPose();
    }

    private static void shade(PoseStack pose, Rectangle b) {
        float amount = Mth.clamp(Math.abs(BRIGHTNESS - 1.0F), 0.0F, 1.0F);
        if (amount <= 0.0F) {
            return;
        }
        int rgb = BRIGHTNESS < 1.0F ? 0x000000 : 0xFFFFFF;
        GuiComponent.fill(pose, b.x, b.y, b.x + b.width, b.y + b.height, (Mth.floor(amount * 255.0F) << 24) | rgb);
    }

    private static void clouds(PoseStack pose, Rectangle b, Clouds c, float panX, float panY) {
        float x = panX * c.depth(), y = panY * c.depth();
        if (c.fan() != 0) {
            float a = (float) Math.toRadians(c.fan() * FAN_SPREAD), cos = Mth.cos(a), sin = Mth.sin(a);
            float rx = x * cos - y * sin;
            y = x * sin + y * cos;
            x = rx;
        }
        float offX = x, offY = y, spacing = c.spacing();
        float reach = c.cell() * c.maxScale() * 0.75F;
        int col0 = Mth.floor((b.x - offX - reach) / spacing), col1 = Mth.floor((b.x + b.width - offX + reach) / spacing);
        int row0 = Mth.floor((b.y - offY - reach) / spacing), row1 = Mth.floor((b.y + b.height - offY + reach) / spacing);
        useGradeShader();
        RenderSystem.setShaderTexture(0, c.texture());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, c.alpha());
        Matrix4f matrix = pose.last().pose();
        ScreenDrawHelper.draw(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX, buf -> {
            for (int col = col0; col <= col1; col++) {
                for (int row = row0; row <= row1; row++) {
                    long key = mix(c.seed() ^ (col * 0x9E3779B97F4A7C15L) ^ (row * 0xC2B2AE3D27D4EB4FL));
                    if (fraction(key, 0) >= c.fill()) {
                        continue;
                    }
                    float cx = (col + fraction(key, 1)) * spacing + offX, cy = (row + fraction(key, 2)) * spacing + offY;
                    int index = Math.min((int) (fraction(key, 3) * CLOUDS_PER_SHEET), CLOUDS_PER_SHEET - 1);
                    float half = c.cell() * Mth.lerp(fraction(key, 4), c.minScale(), c.maxScale()) / 2.0F;
                    float angle = fraction(key, 5) * Mth.TWO_PI, cos = Mth.cos(angle) * half, sin = Mth.sin(angle) * half;
                    float u0 = (float) index / CLOUDS_PER_SHEET, u1 = (index + 1.0F) / CLOUDS_PER_SHEET;
                    if (fraction(key, 6) < 0.5F) {
                        float swap = u0;
                        u0 = u1;
                        u1 = swap;
                    }
                    buf.vertex(matrix, cx - cos - sin, cy - sin + cos, 0.0F).uv(u0, 1.0F).endVertex();
                    buf.vertex(matrix, cx + cos - sin, cy + sin + cos, 0.0F).uv(u1, 1.0F).endVertex();
                    buf.vertex(matrix, cx + cos + sin, cy + sin - cos, 0.0F).uv(u1, 0.0F).endVertex();
                    buf.vertex(matrix, cx - cos + sin, cy - sin - cos, 0.0F).uv(u0, 0.0F).endVertex();
                }
            }
        });
    }

    private static void starTile(PoseStack pose, Rectangle b, Tile t, float panX, float panY, long seed) {
        float x = panX * t.depth(), y = panY * t.depth();
        int size = t.size(), half = size / 2;
        RenderSystem.setShaderTexture(0, t.texture());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, t.alpha());
        RenderSystem.disableCull();
        for (int col = Mth.floor(-x / size); col * size + x < b.width; col++) {
            for (int row = Mth.floor(-y / size); row * size + y < b.height; row++) {
                long h = mix(seed ^ (col * 0x9E3779B97F4A7C15L) ^ (row * 0xC2B2AE3D27D4EB4FL));
                pose.pushPose();
                pose.translate(b.x + col * size + x + half, b.y + row * size + y + half, 0.0D);
                pose.mulPose(Vector3f.ZP.rotationDegrees((h & 3) * 90.0F));
                if ((h & 4) != 0) {
                    pose.scale(-1.0F, 1.0F, 1.0F);
                }
                GuiComponent.blit(pose, -half, -half, 0.0F, 0.0F, size, size, size, size);
                pose.popPose();
            }
        }
        RenderSystem.enableCull();
    }

    private static long mix(long h) {
        h ^= h >>> 33;
        h *= 0xFF51AFD7ED558CCDL;
        h ^= h >>> 33;
        h *= 0xC4CEB9FE1A85EC53L;
        return h ^ (h >>> 33);
    }

    private static void sprites(PoseStack pose, Rectangle b, Sheet sheet, List<Sprite> list, float depth,
                                float panX, float panY, boolean twinkle, float seconds) {
        RenderSystem.setShaderTexture(0, sheet.texture());
        for (Sprite s : list) {
            float alpha = twinkle ? 0.72F + 0.28F * Mth.sin(seconds * 1.8F + s.phase()) : 1.0F;
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
            int w = s.w() * s.scale(), h = s.h() * s.scale();
            float x = wrap(s.x() + panX * depth, SPAN_W), y = wrap(s.y() + panY * depth, SPAN_H);
            for (float dx = x - SPAN_W; dx < b.width; dx += SPAN_W) {
                for (float dy = y - SPAN_H; dy < b.height; dy += SPAN_H) {
                    if (dx + w < 0 || dy + h < 0) {
                        continue;
                    }

                    pose.pushPose();
                    pose.translate(b.x + dx, b.y + dy, 0.0D);
                    GuiComponent.blit(pose, 0, 0, w, h, s.u(), s.v(), s.w(), s.h(), sheet.width(), sheet.height());
                    pose.popPose();
                }
            }
        }
    }

    private void meteors(PoseStack pose, Rectangle b, float dt) {
        this.spawnIn -= dt;
        if (METEORS_PER_MINUTE > 0 && this.spawnIn <= 0) {
            this.spawn(b);
            this.spawnIn = (float) (-Math.log(1 - this.random.nextDouble()) * 60.0 / METEORS_PER_MINUTE);
        }
        RenderSystem.setShaderTexture(0, COMETS.texture());
        RenderSystem.disableCull();
        for (Iterator<Meteor> it = this.meteors.iterator(); it.hasNext(); ) {
            Meteor m = it.next();
            m.age += dt;
            m.x += m.vx * dt;
            m.y += m.vy * dt;
            if (m.age >= m.life) {
                it.remove();
                continue;
            }
            float fade = Math.min(Math.min(1.0F, m.age / 0.15F), Math.min(1.0F, (m.life - m.age) / 0.5F));
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, fade);
            pose.pushPose();
            pose.translate(b.x + m.x, b.y + m.y, 0.0D);
            if (m.flip) {
                pose.scale(-1.0F, 1.0F, 1.0F);
            }
            GuiComponent.blit(pose, m.comet ? -42 : -44, m.comet ? -7 : -3, m.comet ? 48 : 0, 0, 48, 16,
                    COMETS.width(), COMETS.height());
            pose.popPose();
        }
        RenderSystem.enableCull();
    }

    private void spawn(Rectangle b) {
        boolean comet = this.random.nextFloat() < 0.3F, flip = this.random.nextBoolean();
        float dirX = comet ? 0.998F : 0.963F, dirY = comet ? 0.06F : -0.267F;
        if (flip) {
            dirX = -dirX;
        }
        float speed = comet ? 85 + this.random.nextFloat() * 40 : 210 + this.random.nextFloat() * 70;
        float tx = b.width * (0.2F + 0.6F * this.random.nextFloat());
        float ty = b.height * (0.25F + 0.5F * this.random.nextFloat());
        this.meteors.add(new Meteor(comet, flip, tx - dirX * 260, ty - dirY * 260, dirX * speed, dirY * speed,
                comet ? 7.0F : 3.0F));
    }

    private void blackHole(PoseStack pose, Rectangle b, float panX, float panY, float seconds, float dt) {
        float unit = 1.0F / ZOOM, cell = HOLE_CELL * unit, eat = HOLE_EAT_RADIUS * unit, pull = HOLE_PULL_RADIUS * unit;
        float offX = panX * HOLE_DEPTH, offY = panY * HOLE_DEPTH;
        this.holeIn -= dt;
        if (this.hole == null && this.holeIn <= 0) {
            this.hole = this.newHole(b, offX, offY, seconds, pull);
        }
        float t = this.hole == null ? -1.0F : (seconds - this.hole.born()) / HOLE_CROSSING;
        if (this.hole != null && (t < 0 || t >= 1)) {
            this.hole = null;
            this.motes.clear();
            this.holeIn = nextWait(this.random, BLACK_HOLES_PER_MINUTE);
        }
        if (this.hole == null && this.voidCells.isEmpty()) {
            return;
        }

        float hx = 0, hy = 0;
        if (this.hole != null) {
            BlackHole h = this.hole;
            float dx = h.endX() - h.startX(), dy = h.endY() - h.startY(), len = Mth.sqrt(dx * dx + dy * dy);
            float sway = Mth.sin(t * Mth.TWO_PI + h.phase()) * 10.0F * unit;
            hx = h.startX() + dx * t - dy / len * sway;
            hy = h.startY() + dy * t + dx / len * sway;

            int reach = Mth.ceil(eat * 1.25F / cell), hcx = Mth.floor(hx / cell), hcy = Mth.floor(hy / cell);
            for (int cx = hcx - reach; cx <= hcx + reach; cx++) {
                for (int cy = hcy - reach; cy <= hcy + reach; cy++) {
                    long key = (long) cx << 32 | (cy & 0xFFFFFFFFL);
                    float ragged = eat * (0.75F + 0.5F * fraction(key, 1));
                    float ex = (cx + 0.5F) * cell - hx, ey = (cy + 0.5F) * cell - hy;
                    if (ex * ex + ey * ey <= ragged * ragged) {
                        this.voidCells.put(key, seconds);
                    }
                }
            }

            for (this.moteIn -= dt; this.moteIn <= 0; this.moteIn += 1.0F / HOLE_MOTES_PER_SECOND) {
                this.motes.add(new Mote(this.random.nextFloat() * Mth.TWO_PI, pull * (0.8F + 0.4F * this.random.nextFloat()),
                        MOTE_COLORS[this.random.nextInt(MOTE_COLORS.length)]));
            }
            for (Iterator<Mote> it = this.motes.iterator(); it.hasNext(); ) {
                Mote m = it.next();
                m.age += dt;
                m.radius -= HOLE_MOTE_SPEED * unit * (1.0F + 3.0F * (1.0F - m.radius / pull)) * dt;
                m.angle += 1.2F * pull / Math.max(m.radius, cell) * dt;
                if (m.radius < eat * 0.3F) {
                    it.remove();
                }
            }
        }

        float sx = hx + offX, sy = hy + offY;
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        Matrix4f matrix = pose.last().pose();
        ScreenDrawHelper.draw(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR, buf -> {
            for (Iterator<Map.Entry<Long, Float>> it = this.voidCells.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<Long, Float> e = it.next();
                long key = e.getKey();
                float age = seconds - e.getValue();
                float back = (age - HOLE_VOID_HOLD - HOLE_FADE_JITTER * fraction(key, 2)) / HOLE_VOID_FADE;
                if (age < 0 || back >= 1) {
                    it.remove();
                    continue;
                }
                float x = (int) (key >> 32) * cell + offX, y = (int) key * cell + offY;
                if (x + cell < b.x || y + cell < b.y || x > b.x + b.width || y > b.y + b.height) {
                    continue;
                }
                square(buf, matrix, x, y, cell, Mth.floor((1.0F - Mth.clamp(back, 0.0F, 1.0F)) * 255.0F) << 24);
            }
            if (this.hole == null) {
                return;
            }
            for (Mote m : this.motes) {
                int alpha = Mth.floor(Math.min(1.0F, m.age / 0.3F) * 255.0F);
                square(buf, matrix, sx + Mth.cos(m.angle) * m.radius - cell / 2, sy + Mth.sin(m.angle) * m.radius - cell / 2,
                        cell, alpha << 24 | m.color & 0xFFFFFF);
            }
            int core = Mth.ceil(eat * 0.7F / cell);
            for (int i = -core; i <= core; i++) {
                for (int j = -core; j <= core; j++) {
                    if (i * i + j * j <= core * core) {
                        square(buf, matrix, sx + (i - 0.5F) * cell, sy + (j - 0.5F) * cell, cell, 0xFF000000);
                    }
                }
            }
            for (int i = 0; i < 12; i++) {
                float a = i * Mth.TWO_PI / 12 + seconds * (2.0F + (i % 3) * 0.8F);
                float r = eat * (1.0F + 0.12F * (i % 4)), size = cell * (1 + i % 2);
                square(buf, matrix, sx + Mth.cos(a) * r - size / 2, sy + Mth.sin(a) * r - size / 2, size, 0xFF000000);
            }
        });
        RenderSystem.enableTexture();
    }

    private BlackHole newHole(Rectangle b, float offX, float offY, float seconds, float margin) {
        float left = b.x - offX - margin, right = b.x + b.width - offX + margin;
        float y0 = b.y - offY + b.height * (0.2F + 0.6F * this.random.nextFloat());
        float y1 = b.y - offY + b.height * (0.2F + 0.6F * this.random.nextFloat());
        boolean leftward = this.random.nextBoolean();
        return new BlackHole(leftward ? right : left, y0, leftward ? left : right, y1, seconds,
                this.random.nextFloat() * Mth.TWO_PI);
    }

    private static void square(BufferBuilder buf, Matrix4f matrix, float x, float y, float size, int argb) {
        int a = argb >>> 24, r = argb >> 16 & 0xFF, g = argb >> 8 & 0xFF, bl = argb & 0xFF;
        buf.vertex(matrix, x, y + size, 0.0F).color(r, g, bl, a).endVertex();
        buf.vertex(matrix, x + size, y + size, 0.0F).color(r, g, bl, a).endVertex();
        buf.vertex(matrix, x + size, y, 0.0F).color(r, g, bl, a).endVertex();
        buf.vertex(matrix, x, y, 0.0F).color(r, g, bl, a).endVertex();
    }

    private static float fraction(long key, long salt) {
        return (mix(key * 31 + salt) >>> 40) / (float) (1L << 24);
    }

    private static float nextWait(Random random, float perMinute) {
        return (float) (-Math.log(1 - random.nextDouble()) * 60.0 / perMinute);
    }

    private static List<Sprite> scatter(Random random, List<int[]> cells, int scale) {
        List<Sprite> placed = new ArrayList<>();
        for (int[] cell : cells) {
            float bestX = 0, bestY = 0, bestDist = -1;
            for (int attempt = 0; attempt < 60; attempt++) {
                float x = random.nextFloat() * SPAN_W, y = random.nextFloat() * SPAN_H;
                float dist = Float.MAX_VALUE;
                for (Sprite s : placed) {
                    float dx = Math.abs(s.x() - x), dy = Math.abs(s.y() - y);
                    dx = Math.min(dx, SPAN_W - dx);
                    dy = Math.min(dy, SPAN_H - dy);
                    dist = Math.min(dist, Mth.sqrt(dx * dx + dy * dy));
                }
                if (dist > bestDist) {
                    bestX = x;
                    bestY = y;
                    bestDist = dist;
                }
            }
            placed.add(new Sprite(bestX, bestY, cell[0], cell[1], cell[2], cell[3], scale,
                    random.nextFloat() * (float) (Math.PI * 2)));
        }
        return placed;
    }

    private static float wrap(float v, float span) {
        return ((v % span) + span) % span;
    }

    private static ResourceLocation tex(String name) {
        return WoldsVaults.id(DIR + name + ".png");
    }

    private record BlackHole(float startX, float startY, float endX, float endY, float born, float phase) {}

    private static final class Mote {
        final int color;
        float angle, radius, age;

        Mote(float angle, float radius, int color) {
            this.angle = angle;
            this.radius = radius;
            this.color = color;
        }
    }

    private static final class Meteor {
        final boolean comet, flip;
        final float vx, vy, life;
        float x, y, age;

        Meteor(boolean comet, boolean flip, float x, float y, float vx, float vy, float life) {
            this.comet = comet;
            this.flip = flip;
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.life = life;
        }
    }
}

