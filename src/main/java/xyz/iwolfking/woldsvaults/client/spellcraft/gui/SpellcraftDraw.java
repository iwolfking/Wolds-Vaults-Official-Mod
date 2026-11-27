package xyz.iwolfking.woldsvaults.client.spellcraft.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.atlas.TextureAtlasRegion;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.helper.FontHelper;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModTextureAtlases;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.client.screens.CompactButton;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphType;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public final class SpellcraftDraw {
    private static final Map<GlyphType, ResourceLocation> RUNES = new EnumMap<>(GlyphType.class);
    private static final Set<String> MISSING_ICONS = new HashSet<>();

    static {
        for (GlyphType type : GlyphType.values()) {
            RUNES.put(type, WoldsVaults.id("textures/gui/spellcraft/glyph/" + type.id() + ".png"));
        }
    }

    private SpellcraftDraw() {
    }

    public static void window(PoseStack pose, int x0, int y0, int x1, int y1) {
        ScreenTextures.DEFAULT_WINDOW_BACKGROUND.blit(pose, x0, y0, 0, x1 - x0, y1 - y0);
    }

    public static void insetBlack(PoseStack pose, int x0, int y0, int x1, int y1) {
        ScreenTextures.INSET_BLACK_BACKGROUND.blit(pose, x0, y0, 0, x1 - x0, y1 - y0);
    }

    public static void insetGrey(PoseStack pose, int x0, int y0, int x1, int y1) {
        ScreenTextures.INSET_GREY_BACKGROUND.blit(pose, x0, y0, 0, x1 - x0, y1 - y0);
    }

    public static void insetDarkGrey(PoseStack pose, int x0, int y0, int x1, int y1) {
        ScreenTextures.INSET_DARK_GREY_BACKGROUND.blit(pose, x0, y0, 0, x1 - x0, y1 - y0);
    }

    /** (x, y) is the top left of the 16x16 content area, with the frame one pixel outside it. */
    public static void slot(PoseStack pose, int x, int y, boolean disabled) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        (disabled ? ScreenTextures.INSET_DISABLED_ITEM_SLOT_BACKGROUND : ScreenTextures.INSET_ITEM_SLOT_BACKGROUND).blit(pose, x - 1, y - 1);
    }

    public static void ring(PoseStack pose, int x0, int y0, int x1, int y1, int color) {
        GuiComponent.fill(pose, x0, y0, x1, y0 + 1, color);
        GuiComponent.fill(pose, x0, y1 - 1, x1, y1, color);
        GuiComponent.fill(pose, x0, y0, x0 + 1, y1, color);
        GuiComponent.fill(pose, x1 - 1, y0, x1, y1, color);
    }

    public static void rune(PoseStack pose, GlyphType type, int x, int y, int size, boolean dim) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, RUNES.get(type));
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (dim) {
            RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 0.6F);
        } else {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
        GuiComponent.blit(pose, x, y, size, size, 0.0F, 0.0F, 16, 16, 16, 16);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Nullable
    private static TextureAtlasRegion abilityIcon(@Nullable String specOrGroup) {
        if (specOrGroup == null || specOrGroup.isEmpty()) {
            return null;
        }
        ResourceLocation icon = ModConfigs.ABILITIES_GUI.getIconOrFirst(specOrGroup);
        if (icon == null) {
            if (MISSING_ICONS.add(specOrGroup)) {
                WoldLogger.error("Spellcraft UI: no ability icon for {}; drawing an empty cell", specOrGroup);
            }
            return null;
        }
        return TextureAtlasRegion.of(ModTextureAtlases.ABILITIES, icon);
    }

    public static void ability(PoseStack pose, @Nullable String specOrGroup, int x, int y, int size, float alpha) {
        TextureAtlasRegion icon = abilityIcon(specOrGroup);
        if (icon == null) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        icon.blit(pose, x, y, 0, size, size);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /** Draws bordered text at a scale and returns its scaled width. */
    public static float smallText(PoseStack pose, Font font, String text, float x, float y, int color, float scale) {
        pose.pushPose();
        pose.translate(x, y, 0.0);
        pose.scale(scale, scale, 1.0F);
        FontHelper.drawStringWithBorder(pose, text, 0.0F, 0.0F, color, SpellcraftPalette.BORDER);
        pose.popPose();
        return font.width(text) * scale;
    }

    /**
     * Draws {@code used} arcana dim and the edited spell's {@code pending} arcana bright, tinted red when their sum
     * exceeds {@code total}.
     */
    public static void arcanaBar(PoseStack pose, int x, int y, int w, int h, float used, float pending, float total) {
        insetBlack(pose, x, y, x + w, y + h);
        int inner = w - 2;
        float budget = Math.max(1.0F, total);
        int u = (int) (inner * Math.min(used, total) / budget);
        int p = (int) (inner * Math.min(used + pending, total) / budget) - u;
        if (u > 0) {
            GuiComponent.fill(pose, x + 1, y + 1, x + 1 + u, y + h - 1, SpellcraftPalette.ARCANA_DIM);
        }
        if (p > 0) {
            GuiComponent.fill(pose, x + 1 + u, y + 1, x + 1 + u + p, y + h - 1, SpellcraftPalette.ARCANA);
        }
        for (int k = 1; k < 10; k++) {
            int tx = x + 1 + inner * k / 10;
            GuiComponent.fill(pose, tx, y + 1, tx + 1, y + 2, SpellcraftPalette.BAR_TICK);
        }
        if (Math.round(used + pending) > total) {
            GuiComponent.fill(pose, x + 1, y + 1, x + w - 1, y + h - 1, SpellcraftPalette.OVER_TINT);
        }
    }

    /** The stars come from a fixed seed so they stay put from frame to frame. */
    public static void starfield(PoseStack pose, int x0, int y0, int x1, int y1) {
        GuiComponent.fill(pose, x0, y0, x1, y1, SpellcraftPalette.ARCANE_DEEP);
        Random random = new Random(11L);
        int count = (x1 - x0) * (y1 - y0) / 80;
        int[] colors = {0xFFFFFF, 0xD8C8FF, 0xB8E0FF};
        int[] alphas = {0x50, 0x80, 0xC0};
        for (int i = 0; i < count; i++) {
            int x = x0 + 1 + random.nextInt(Math.max(1, x1 - x0 - 3));
            int y = y0 + 1 + random.nextInt(Math.max(1, y1 - y0 - 3));
            int color = (alphas[random.nextInt(alphas.length)] << 24) | colors[random.nextInt(colors.length)];
            GuiComponent.fill(pose, x, y, x + 1, y + 1, color);
        }
    }

    public static void spellStar(PoseStack pose, int x, int y) {
        GuiComponent.fill(pose, x + 11, y, x + 16, y + 5, 0xFF2A1840);
        GuiComponent.fill(pose, x + 13, y, x + 14, y + 5, SpellcraftPalette.ARCANE_GLOW);
        GuiComponent.fill(pose, x + 11, y + 2, x + 16, y + 3, SpellcraftPalette.ARCANE_GLOW);
    }

    public static void redX(PoseStack pose, int x, int y) {
        for (int d = 0; d < 10; d++) {
            GuiComponent.fill(pose, x + 3 + d, y + 3 + d, x + 5 + d, y + 4 + d, SpellcraftPalette.BAD);
            GuiComponent.fill(pose, x + 12 - d, y + 3 + d, x + 14 - d, y + 4 + d, SpellcraftPalette.BAD);
        }
    }

    public static void strike(PoseStack pose, int x, int y) {
        GuiComponent.fill(pose, x, y, x + 16, y + 16, 0x90200000);
        for (int d = 0; d < 16; d++) {
            GuiComponent.fill(pose, x + d, y + 15 - d, x + d + 1, y + 16 - d, SpellcraftPalette.BAD);
        }
    }

    /** A vanilla button at any height, drawn from the texture's top rows plus its bottom edge row. */
    public static void button(PoseStack pose, Font font, int x, int y, int w, int h, String label, boolean hovered) {
        button(pose, font, x, y, w, h, label, hovered, 1.0F, false);
    }

    /** A compact button, its label drawn at {@code scale}; a lit button is tinted yellow. */
    public static void button(PoseStack pose, Font font, int x, int y, int w, int h, String label, boolean hovered, float scale,
                              boolean lit) {
        CompactButton.draw(pose, font, x, y, w, h, label, hovered ? CompactButton.STATE_HOVERED : CompactButton.STATE_NORMAL,
                hovered ? 0xFFFFFFA0 : 0xFFE0E0E0, scale, lit);
    }

    public static void click() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    public static boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }
}
