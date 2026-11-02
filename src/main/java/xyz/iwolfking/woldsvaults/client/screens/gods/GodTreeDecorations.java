package xyz.iwolfking.woldsvaults.client.screens.gods;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import iskallia.vault.client.gui.helper.ScreenDrawHelper;
import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class GodTreeDecorations {
    private static final ResourceLocation SETTINGS = WoldsVaults.id("god_tree/decorations.json");
    private static final ResourceLocation SPRITES = WoldsVaults.id("god_tree/decoration_sprites.json");
    private static final ResourceLocation GLOW = WoldsVaults.id("textures/gui/gods/decoration_glow.png");
    private static final float TAU = (float) (Math.PI * 2.0D);

    private static Map<VaultGod, List<Decoration>> decorations;
    private static Sheet sheet;

    private record Sprite(float u0, float v0, float u1, float v1, float aspect) {}

    private record Sheet(ResourceLocation texture, Map<String, Sprite> sprites) {}

    private record Decoration(boolean glow, String sprite, float x, float y, float size, float rotation, String color,
                              float opacity, boolean front, boolean additive, float spin, float pulse, float pulseRate,
                              float phase) {}

    private GodTreeDecorations() {
    }

    @Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ReloadHook {
        private ReloadHook() {
        }

        @SubscribeEvent
        public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener((ResourceManagerReloadListener) manager -> {
                decorations = null;
                sheet = null;
            });
        }
    }

    public static void render(PoseStack poseStack, VaultGod god, boolean front, float minX, float minY, float maxX, float maxY) {
        List<Decoration> list = decorations().get(god);
        if (list == null || list.isEmpty()) {
            return;
        }
        Sheet sprites = sheet();
        float seconds = (Util.getMillis() % 3_600_000L) / 1000.0F;
        Matrix4f pose = poseStack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.enableTexture();
        RenderSystem.enableBlend();
        for (Decoration d : list) {
            if (d.front() != front) {
                continue;
            }
            Sprite sprite = d.glow() ? new Sprite(0.0F, 0.0F, 1.0F, 1.0F, 1.0F) : sprites.sprites().get(d.sprite());
            if (sprite == null) {
                GodTreeLog.warnOnce("decoration_sprite:" + d.sprite(), "God tree decoration sprite {} is not in decoration_sprites.json; skipping it.", d.sprite());
                continue;
            }
            float halfW = d.size() / 2.0F;
            float halfH = halfW * sprite.aspect();
            float reach = (float) Math.sqrt(halfW * halfW + halfH * halfH);
            if (d.x() + reach < minX || d.x() - reach > maxX || d.y() + reach < minY || d.y() - reach > maxY) {
                continue;
            }
            float alpha = d.opacity() * (1.0F - d.pulse() + d.pulse() * (0.5F + 0.5F * (float) Math.sin(TAU * (d.pulseRate() * seconds + d.phase()))));
            if (alpha <= 0.0F) {
                continue;
            }
            int rgb = color(god, d.color());
            int r = rgb >> 16 & 0xFF;
            int g = rgb >> 8 & 0xFF;
            int b = rgb & 0xFF;
            int a = Math.round(Math.min(1.0F, alpha) * 255.0F);
            double angle = Math.toRadians(d.rotation() + d.spin() * seconds);
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);
            if (d.glow()) {
                Minecraft.getInstance().getTextureManager().getTexture(GLOW).setFilter(true, false);
            }
            RenderSystem.setShaderTexture(0, d.glow() ? GLOW : sprites.texture());
            if (d.additive()) {
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            } else {
                RenderSystem.defaultBlendFunc();
            }
            float[][] corners = {{-halfW, halfH, sprite.u0(), sprite.v1()}, {halfW, halfH, sprite.u1(), sprite.v1()},
                    {halfW, -halfH, sprite.u1(), sprite.v0()}, {-halfW, -halfH, sprite.u0(), sprite.v0()}};
            ScreenDrawHelper.draw(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
                for (float[] c : corners) {
                    float x = d.x() + c[0] * cos - c[1] * sin;
                    float y = d.y() + c[0] * sin + c[1] * cos;
                    buf.vertex(pose, x, y, 0.0F).color(r, g, b, a).uv(c[2], c[3]).endVertex();
                }
            });
        }
        RenderSystem.defaultBlendFunc();
    }

    private static int color(VaultGod god, String name) {
        return switch (name) {
            case "accent" -> GodTreeTheme.accent(god) & 0xFFFFFF;
            case "dim" -> GodTreeTheme.accentDim(god) & 0xFFFFFF;
            case "deep" -> GodTreeTheme.accentDeep(god) & 0xFFFFFF;
            case "white" -> 0xFFFFFF;
            default -> {
                try {
                    yield Integer.parseInt(name.startsWith("#") ? name.substring(1) : name, 16) & 0xFFFFFF;
                } catch (NumberFormatException e) {
                    GodTreeLog.warnOnce("decoration_color:" + name, "God tree decoration colour {} is not a colour name or hex value; using white.", name);
                    yield 0xFFFFFF;
                }
            }
        };
    }

    private static Map<VaultGod, List<Decoration>> decorations() {
        if (decorations == null) {
            decorations = new EnumMap<>(VaultGod.class);
            try {
                JsonObject gods = part(read(SETTINGS), "gods");
                for (VaultGod god : VaultGod.values()) {
                    JsonElement element = gods.get(god.getName().toLowerCase(Locale.ROOT));
                    if (element == null || !element.isJsonArray()) {
                        continue;
                    }
                    List<Decoration> list = new ArrayList<>();
                    for (JsonElement entry : element.getAsJsonArray()) {
                        if (entry.isJsonObject()) {
                            list.add(decoration(entry.getAsJsonObject()));
                        }
                    }
                    decorations.put(god, list);
                }
            } catch (IOException | RuntimeException e) {
                WoldsVaults.LOGGER.warn("God tree decorations {} could not be read; trees draw without them", SETTINGS, e);
            }
        }
        return decorations;
    }

    private static Decoration decoration(JsonObject json) {
        boolean glow = "glow".equals(s(json, "type", "sprite"));
        return new Decoration(glow, s(json, "sprite", "sparkle"), f(json, "x", 0.0F), f(json, "y", 0.0F),
                Math.max(0.5F, f(json, "size", glow ? 80.0F : 24.0F)), f(json, "rotation", 0.0F), s(json, "color", "accent"),
                f(json, "opacity", glow ? 0.5F : 1.0F), "front".equals(s(json, "layer", "back")),
                "add".equals(s(json, "blend", glow ? "add" : "normal")), f(json, "spin", 0.0F),
                Math.max(0.0F, Math.min(1.0F, f(json, "pulse", 0.0F))), f(json, "pulseRate", 0.5F), f(json, "phase", 0.0F));
    }

    private static Sheet sheet() {
        if (sheet == null) {
            Map<String, Sprite> sprites = new HashMap<>();
            ResourceLocation texture = WoldsVaults.id("textures/gui/gods/decorations.png");
            try {
                JsonObject json = read(SPRITES);
                ResourceLocation named = json.has("sheet") ? ResourceLocation.tryParse(json.get("sheet").getAsString()) : null;
                if (named != null) {
                    texture = named;
                }
                float width = f(json, "width", 256.0F);
                float height = f(json, "height", 128.0F);
                for (Map.Entry<String, JsonElement> entry : part(json, "sprites").entrySet()) {
                    JsonArray rect = entry.getValue().getAsJsonArray();
                    float x = rect.get(0).getAsFloat();
                    float y = rect.get(1).getAsFloat();
                    float w = rect.get(2).getAsFloat();
                    float h = rect.get(3).getAsFloat();
                    sprites.put(entry.getKey(), new Sprite(x / width, y / height, (x + w) / width, (y + h) / height, h / w));
                }
            } catch (IOException | RuntimeException e) {
                WoldsVaults.LOGGER.warn("God tree decoration sprites {} could not be read; sprite decorations are skipped", SPRITES, e);
            }
            sheet = new Sheet(texture, sprites);
        }
        return sheet;
    }

    private static JsonObject read(ResourceLocation location) throws IOException {
        try (Resource resource = Minecraft.getInstance().getResourceManager().getResource(location);
             Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
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

    private static String s(JsonObject json, String key, String fallback) {
        JsonElement element = json.get(key);
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()
                ? element.getAsString() : fallback;
    }
}
