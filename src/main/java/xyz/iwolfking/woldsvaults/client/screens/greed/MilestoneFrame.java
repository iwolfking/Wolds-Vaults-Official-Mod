package xyz.iwolfking.woldsvaults.client.screens.greed;

import xyz.iwolfking.woldsvaults.client.screens.ScreenAnimationClock;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import iskallia.vault.client.gui.helper.ScreenDrawHelper;
import iskallia.vault.gear.VaultGearRarity;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.client.init.ModShaders;

/**
 * The frames greed milestone rows wear once they have a tier done, by rarity: common, rare, epic, omega and mythic,
 * set by how far through its tiers the milestone is, so finishing one is always mythic. Each rarity takes its colour
 * from Vault Hunters' own gear rarities (mythic is Wold's), and gets more ornate and more alive going up: corner
 * pieces from common, a crest on the top edge from rare, a hanging tail from epic, and the shader's running light,
 * breathing glow, sparkles and drifting colour. Everything is drawn on the GPU by the milestone_frame shader: one quad
 * for the frame and one batch of flourish sprites (from a greyscale atlas the shader recolours) per row. The flourishes
 * reach outside the row, so the lists leave room for them with {@link #topOverhang}, {@link #bottomOverhang} and
 * {@link #SIDE_OVERHANG}.
 */
public final class MilestoneFrame {
    public static final int NONE = 0;
    public static final int MYTHIC = 5;
    public static final int SIDE_OVERHANG = 6;

    private static final ResourceLocation ATLAS = WoldsVaults.id("textures/gui/greed/milestone_ornaments.png");
    private static final int ATLAS_SIZE = 64;
    private static final int GLOW = 4;
    private static final int[] FALLBACK_COLOURS = {0, 0xD4D4D4, 0xFFE24A, 0xC56BFF, 0x4EF08A, 0xFF4E9A};

    private record Sprite(int u, int v, int w, int h) {
    }

    private static final Sprite[] CRESTS = {null, null, new Sprite(32, 22, 13, 7), new Sprite(0, 22, 25, 9),
            new Sprite(0, 12, 33, 10), new Sprite(0, 0, 45, 12)};
    private static final Sprite[] TAILS = {null, null, null, new Sprite(42, 32, 11, 5), new Sprite(24, 32, 15, 6),
            new Sprite(0, 32, 21, 7)};
    private static final Sprite[] CORNERS = {null, new Sprite(34, 40, 4, 4), new Sprite(27, 40, 6, 6),
            new Sprite(19, 40, 7, 7), new Sprite(10, 40, 8, 8), new Sprite(0, 40, 9, 9)};

    private MilestoneFrame() {
    }

    /** The rarity a milestone's frame shows: none before its first tier, mythic once every tier is done. */
    public static int rarity(int completed, int tierCount) {
        if (completed <= 0 || tierCount <= 0) {
            return NONE;
        }
        return Math.min(MYTHIC, (int) Math.ceil(completed * (double) MYTHIC / tierCount));
    }

    public static int colour(int rarity) {
        String name = switch (rarity) {
            case 1 -> "COMMON";
            case 2 -> "RARE";
            case 3 -> "EPIC";
            case 4 -> "OMEGA";
            default -> "MYTHIC";
        };
        try {
            return VaultGearRarity.valueOf(name).getColor().getValue();
        } catch (IllegalArgumentException | NullPointerException e) {
            WoldLogger.errorOnce("milestone_frame_colour_" + name, "Gear rarity {} has no colour; milestone frames use a built-in one", name, e);
            return FALLBACK_COLOURS[Math.max(1, Math.min(MYTHIC, rarity))];
        }
    }

    /** How many pixels in from the row's edge a frame's border runs, bevel included. */
    public static int borderWidth(int rarity) {
        if (rarity <= NONE) {
            return 1;
        }
        return Math.max(2, Math.min(5, rarity)) + 1;
    }

    /** How far a frame's flourishes reach above the row. */
    public static int topOverhang(int rarity) {
        if (rarity <= NONE) {
            return 0;
        }
        int corner = CORNERS[rarity].h() / 2;
        return CRESTS[rarity] == null ? corner : Math.max(corner, CRESTS[rarity].h() - 2);
    }

    /** How far a frame's flourishes reach below the row. */
    public static int bottomOverhang(int rarity) {
        if (rarity <= NONE) {
            return 0;
        }
        int corner = CORNERS[rarity].h() / 2;
        return TAILS[rarity] == null ? corner : Math.max(corner, TAILS[rarity].h() - 2);
    }

    public static int crestWidth(int rarity) {
        return rarity <= NONE || CRESTS[rarity] == null ? 0 : CRESTS[rarity].w();
    }

    /**
     * Draws a frame of the rarity over a row at (x, y) and depth z, seeded so neighbouring rows don't move in step.
     * Vault Hunters' screens depth test their elements by z, so the frame has to sit at the row's own z or the list
     * behind it hides it.
     */
    public static void draw(PoseStack poseStack, int x, int y, int z, int width, int height, int rarity, int seed) {
        draw(poseStack, x, y, z, width, height, rarity, seed, false, true);
    }

    /** As above, on the navy row styles' plate if {@code navy}, and leaving out the flourishes unless {@code ornaments}. */
    public static void draw(PoseStack poseStack, int x, int y, int z, int width, int height, int rarity, int seed,
                            boolean navy, boolean ornaments) {
        if (rarity <= NONE || width <= 2 || height <= 2) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0, 0.0, z);
        drawAtDepth(poseStack, x, y, width, height, rarity, seed, navy, ornaments);
        poseStack.popPose();
    }

    private static void drawAtDepth(PoseStack poseStack, int x, int y, int width, int height, int rarity, int seed,
                                    boolean navy, boolean ornaments) {
        int rgb = colour(rarity);
        ShaderInstance shader = ModShaders.milestoneFrame;
        if (shader == null) {
            GuiComponent.fill(poseStack, x, y, x + width, y + height, 0xFF000000 | rgb);
            GuiComponent.fill(poseStack, x + 1, y + 1, x + width - 1, y + height - 1, navy ? GreedPixelArt.NAVY_ROW : GreedTheme.PLATE);
            return;
        }
        float time = ScreenAnimationClock.seconds();
        float r = (rgb >> 16 & 255) / 255.0F;
        float g = (rgb >> 8 & 255) / 255.0F;
        float b = (rgb & 255) / 255.0F;
        shader.safeGetUniform("FrameStyle").set((float) rarity, time, (seed & 1023) * 0.37F, navy ? 1.0F : 0.0F);
        shader.safeGetUniform("FrameColour").set(r, g, b, 1.0F);

        RenderSystem.setShader(() -> shader);
        RenderSystem.setShaderTexture(0, ATLAS);
        RenderSystem.enableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        Matrix4f pose = poseStack.last().pose();

        shader.safeGetUniform("FrameSize").set((float) width, (float) height, (float) GLOW, 0.0F);
        ScreenDrawHelper.draw(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
            quad(buf, pose, x - GLOW, y - GLOW, width + 2 * GLOW, height + 2 * GLOW, 0.0F, 0.0F, 1.0F, 1.0F, width, height, x, y);
        });

        if (!ornaments) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            return;
        }
        shader.safeGetUniform("FrameSize").set((float) width, (float) height, (float) GLOW, 1.0F);
        int bob = rarity >= MYTHIC ? (int) Math.round(Math.sin(time * 1.6 + seed) * 0.6) : 0;
        ScreenDrawHelper.draw(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
            Sprite corner = CORNERS[rarity];
            int left = x - corner.w() / 2;
            int right = x + width - corner.w() + corner.w() / 2;
            int upper = y - corner.h() / 2;
            int lower = y + height - corner.h() + corner.h() / 2;
            sprite(buf, pose, corner, left, upper, false, false, width, height, x, y);
            sprite(buf, pose, corner, right, upper, true, false, width, height, x, y);
            sprite(buf, pose, corner, left, lower, false, true, width, height, x, y);
            sprite(buf, pose, corner, right, lower, true, true, width, height, x, y);
            Sprite crest = CRESTS[rarity];
            if (crest != null) {
                sprite(buf, pose, crest, x + (width - crest.w()) / 2, y + 2 - crest.h() + bob, false, false, width, height, x, y);
            }
            Sprite tail = TAILS[rarity];
            if (tail != null) {
                sprite(buf, pose, tail, x + (width - tail.w()) / 2, y + height - 2, false, false, width, height, x, y);
            }
        });
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void sprite(BufferBuilder buf, Matrix4f pose, Sprite s, int x, int y, boolean flipX, boolean flipY,
                               int width, int height, int frameX, int frameY) {
        float u0 = s.u() / (float) ATLAS_SIZE;
        float v0 = s.v() / (float) ATLAS_SIZE;
        float u1 = (s.u() + s.w()) / (float) ATLAS_SIZE;
        float v1 = (s.v() + s.h()) / (float) ATLAS_SIZE;
        quad(buf, pose, x, y, s.w(), s.h(), flipX ? u1 : u0, flipY ? v1 : v0, flipX ? u0 : u1, flipY ? v0 : v1,
                width, height, frameX, frameY);
    }

    private static void quad(BufferBuilder buf, Matrix4f pose, float x, float y, float w, float h,
                             float u0, float v0, float u1, float v1, int width, int height, int frameX, int frameY) {
        vertex(buf, pose, x, y + h, u0, v1, width, height, frameX, frameY);
        vertex(buf, pose, x + w, y + h, u1, v1, width, height, frameX, frameY);
        vertex(buf, pose, x + w, y, u1, v0, width, height, frameX, frameY);
        vertex(buf, pose, x, y, u0, v0, width, height, frameX, frameY);
    }

    private static void vertex(BufferBuilder buf, Matrix4f pose, float x, float y, float u, float v,
                               int width, int height, int frameX, int frameY) {
        float lx = Math.max(0.0F, Math.min(1.0F, (x - frameX + 16.0F) / (width + 32.0F)));
        float ly = Math.max(0.0F, Math.min(1.0F, (y - frameY + 16.0F) / (height + 32.0F)));
        buf.vertex(pose, x, y, 0.0F).color(lx, ly, 0.0F, 1.0F).uv(u, v).endVertex();
    }
}
