package xyz.iwolfking.woldsvaults.client.screens.gods;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import iskallia.vault.client.gui.helper.ScreenDrawHelper;
import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class GodIdolCloud {
    private static final ResourceLocation SETTINGS = WoldsVaults.id("god_tree/idol_cloud.json");
    private static final ResourceLocation CLOUD = WoldsVaults.id("textures/gui/gods/idol_cloud.png");
    private static final ResourceLocation RAMPS = WoldsVaults.id("textures/gui/gods/idol_cloud_ramps.png");
    public static final float BASE = 256.0F;
    private static final int MAX_OPENINGS = 3;
    private static final List<Opening> DEFAULT_OPENINGS = List.of(
            new Opening(104.0F, 167.5F, 21.5F), new Opening(152.0F, 167.5F, 21.5F), new Opening(128.0F, 213.5F, 21.5F));

    private static Settings settings;

    public record Opening(float x, float y, float r) {}

    private record Render(int resolution, float frameRate, boolean smooth, float godPhase) {}

    private record Cloud(float opacity, float levelStart, float levelStep, float clearScale, float clearSoftness) {}

    private record Warp(boolean enabled, float strength, float scale, float speed, int detail) {}

    private record Swirl(boolean enabled, float angle, float rate, float reach) {}

    private record Breathe(boolean enabled, float amount, float rate) {}

    private record Flicker(boolean enabled, float amount, float scale, float speed) {}

    private record Wisps(boolean enabled, float amount, float spread, float scale, float rise, float threshold) {}

    private record Colour(float brightness, float highlights, int toneSteps, int alphaSteps) {}

    private record Settings(Render render, List<Opening> openings, Cloud cloud, Warp warp, Swirl swirl, Breathe breathe,
                            Flicker flicker, Wisps wisps, Colour colour) {}

    private GodIdolCloud() {
    }

    @Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ReloadHook {
        private ReloadHook() {
        }

        @SubscribeEvent
        public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener((ResourceManagerReloadListener) manager -> settings = null);
        }
    }

    public static List<Opening> openings() {
        return settings().openings();
    }

    public static float levelFade(int level) {
        Cloud cloud = settings().cloud();
        return Math.max(0.0F, Math.min(1.0F, cloud.levelStart() + cloud.levelStep() * level));
    }

    public static void render(PoseStack poseStack, VaultGod god, int phaseIndex, float x, float y, float size, float alpha) {
        ShaderInstance shader = GodTreeShaders.idolCloud;
        if (shader == null) {
            GodTreeLog.errorOnce("idol_cloud", "God idol cloud shader did not load; drawing the idols without their cloud.");
            return;
        }
        if (alpha <= 0.0F) {
            return;
        }
        Settings s = settings();
        double seconds = (Util.getMillis() % 3_600_000L) / 1000.0D;
        double step = 1.0D / Math.max(1.0F, s.render().frameRate());
        float time = (float) (Math.floor(seconds / step) * step + s.render().godPhase() * phaseIndex);

        shader.safeGetUniform("CloudTime").set(time, (float) god.ordinal(),
                s.render().smooth() ? 0.0F : (float) s.render().resolution(), 0.0F);
        for (int i = 0; i < MAX_OPENINGS; i++) {
            Opening o = i < s.openings().size() ? s.openings().get(i) : null;
            shader.safeGetUniform("Opening" + i).set(o == null ? 0.0F : o.x(), o == null ? 0.0F : o.y(),
                    o == null ? 0.0F : o.r(), o == null ? 0.0F : 1.0F);
        }
        Cloud cloud = s.cloud();
        shader.safeGetUniform("CloudShape").set(cloud.opacity(), cloud.clearScale(), cloud.clearSoftness(), 0.0F);
        Warp warp = s.warp();
        shader.safeGetUniform("Warp").set(warp.enabled() ? warp.strength() : 0.0F, warp.scale(), warp.speed(), (float) warp.detail());
        Swirl swirl = s.swirl();
        shader.safeGetUniform("Swirl").set(swirl.enabled() ? (float) Math.toRadians(swirl.angle()) : 0.0F, swirl.rate(),
                swirl.reach(), 0.0F);
        Breathe breathe = s.breathe();
        shader.safeGetUniform("Breathe").set(breathe.enabled() ? breathe.amount() : 0.0F, breathe.rate(), 0.0F, 0.0F);
        Flicker flicker = s.flicker();
        shader.safeGetUniform("Flicker").set(flicker.enabled() ? flicker.amount() : 0.0F, flicker.scale(), flicker.speed(), 0.0F);
        Wisps wisps = s.wisps();
        shader.safeGetUniform("Wisps").set(wisps.enabled() ? wisps.amount() : 0.0F, wisps.spread(), wisps.scale(), wisps.rise());
        shader.safeGetUniform("WispThinness").set(wisps.threshold());
        Colour colour = s.colour();
        shader.safeGetUniform("CloudColour").set(colour.brightness(), colour.highlights(), (float) colour.toneSteps(),
                (float) colour.alphaSteps());

        Minecraft.getInstance().getTextureManager().getTexture(CLOUD).setFilter(true, false);
        RenderSystem.setShader(() -> shader);
        RenderSystem.setShaderTexture(0, CLOUD);
        RenderSystem.setShaderTexture(1, RAMPS);
        RenderSystem.enableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        Matrix4f pose = poseStack.last().pose();
        ScreenDrawHelper.draw(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX, buf -> {
            buf.vertex(pose, x, y + size, 0.0F).uv(0.0F, 1.0F).endVertex();
            buf.vertex(pose, x + size, y + size, 0.0F).uv(1.0F, 1.0F).endVertex();
            buf.vertex(pose, x + size, y, 0.0F).uv(1.0F, 0.0F).endVertex();
            buf.vertex(pose, x, y, 0.0F).uv(0.0F, 0.0F).endVertex();
        });
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static Settings settings() {
        if (settings == null) {
            JsonObject json = new JsonObject();
            try (Resource resource = Minecraft.getInstance().getResourceManager().getResource(SETTINGS);
                 Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                json = JsonParser.parseReader(reader).getAsJsonObject();
            } catch (IOException | RuntimeException e) {
                WoldsVaults.LOGGER.warn("God idol cloud {} could not be read; using the tuning page's defaults", SETTINGS, e);
            }
            settings = settings(json);
        }
        return settings;
    }

    private static Settings settings(JsonObject json) {
        JsonObject render = part(json, "render");
        JsonObject cloud = part(json, "cloud");
        JsonObject warp = part(json, "warp");
        JsonObject swirl = part(json, "swirl");
        JsonObject breathe = part(json, "breathe");
        JsonObject flicker = part(json, "flicker");
        JsonObject wisps = part(json, "wisps");
        JsonObject colour = part(json, "colour");
        return new Settings(
                new Render(Math.max(8, i(render, "resolution", 128)), f(render, "frameRate", 30.0F),
                        b(render, "smooth", false), f(render, "godPhase", 1.7F)),
                openings(json),
                new Cloud(f(cloud, "opacity", 1.0F), f(cloud, "levelStart", 0.0F), f(cloud, "levelStep", 0.1F),
                        f(cloud, "clearScale", 1.0F), f(cloud, "clearSoftness", 2.0F)),
                new Warp(b(warp, "enabled", true), f(warp, "strength", 4.0F), Math.max(0.1F, f(warp, "scale", 26.0F)),
                        f(warp, "speed", 0.15F), Math.max(1, Math.min(4, i(warp, "detail", 2)))),
                new Swirl(b(swirl, "enabled", true), f(swirl, "angle", 8.0F), f(swirl, "rate", 0.1F),
                        Math.max(0.1F, f(swirl, "reach", 30.0F))),
                new Breathe(b(breathe, "enabled", true), f(breathe, "amount", 0.03F), f(breathe, "rate", 0.16F)),
                new Flicker(b(flicker, "enabled", true), f(flicker, "amount", 0.3F), Math.max(0.1F, f(flicker, "scale", 14.0F)),
                        f(flicker, "speed", 0.3F)),
                new Wisps(b(wisps, "enabled", true), f(wisps, "amount", 0.6F), f(wisps, "spread", 0.16F),
                        Math.max(0.1F, f(wisps, "scale", 10.0F)), f(wisps, "rise", 6.0F),
                        Math.min(0.99F, f(wisps, "threshold", 0.55F))),
                new Colour(f(colour, "brightness", 1.0F), f(colour, "highlights", 0.25F), i(colour, "toneSteps", 0),
                        i(colour, "alphaSteps", 0)));
    }

    private static List<Opening> openings(JsonObject json) {
        JsonElement element = json.get("openings");
        if (element == null || !element.isJsonArray() || element.getAsJsonArray().isEmpty()) {
            return DEFAULT_OPENINGS;
        }
        JsonArray array = element.getAsJsonArray();
        List<Opening> openings = new ArrayList<>();
        for (int k = 0; k < Math.min(MAX_OPENINGS, array.size()); k++) {
            JsonObject o = array.get(k).isJsonObject() ? array.get(k).getAsJsonObject() : new JsonObject();
            Opening fallback = DEFAULT_OPENINGS.get(k);
            openings.add(new Opening(f(o, "x", fallback.x()), f(o, "y", fallback.y()), f(o, "r", fallback.r())));
        }
        return List.copyOf(openings);
    }

    private static JsonObject part(JsonObject json, String key) {
        JsonElement element = json.get(key);
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : new JsonObject();
    }

    private static float f(JsonObject json, String key, float fallback) {
        JsonElement element = json.get(key);
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()
                ? element.getAsFloat() : fallback;
    }

    private static int i(JsonObject json, String key, int fallback) {
        return Math.round(f(json, key, fallback));
    }

    private static boolean b(JsonObject json, String key, boolean fallback) {
        JsonElement element = json.get(key);
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()
                ? element.getAsBoolean() : fallback;
    }
}
