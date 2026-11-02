package xyz.iwolfking.woldsvaults.client.lightning;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;

/**
 * While the player is lightning (Lightning Fiend 6-piece) the hotbar and the held item's name give way to the form's
 * own bar, in the hotbar's place: Arc Lash in the left slot, Thunderstrike in the middle and Lightning Rod in the
 * right, each with its key (and the lash and rod a cooldown veil like an item's), the lash's and rod's names (the
 * rod's target once it has one) and the time the form has left. The Thunderstrike slot shows the form's charge: a glowing
 * yellow ring around the slot filling clockwise from the top centre, and the strike's multiplier at that charge as a
 * small yellow number in the slot's top-right corner.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class LightningFormHud {
    private static final ResourceLocation WIDGETS = new ResourceLocation("textures/gui/widgets.png");
    private static final ResourceLocation ARC_LASH = new ResourceLocation(WoldsVaults.MOD_ID, "textures/gui/lightning_form/arc_lash.png");
    private static final ResourceLocation LIGHTNING_ROD = new ResourceLocation(WoldsVaults.MOD_ID, "textures/gui/lightning_form/lightning_rod.png");
    private static final ResourceLocation THUNDERSTRIKE = new ResourceLocation(WoldsVaults.MOD_ID, "textures/gui/lightning_form/thunderstrike.png");
    private static final int TEXT = 0xC6F8FF;
    private static final int SET_COLOUR = 0x6FCFFF;
    private static final int CHARGE = 0xFFFFD84A;
    private static final int CHARGE_GLOW = 0x60FFD84A;
    private static final float LABEL_SCALE = 0.75F;

    private LightningFormHud() {
    }

    @SubscribeEvent
    public static void onHudLayer(RenderGameOverlayEvent.PreLayer event) {
        if (!ClientLightningForm.isActive()) {
            return;
        }
        if (event.getOverlay() == ForgeIngameGui.ITEM_NAME_ELEMENT) {
            event.setCanceled(true);
        } else if (event.getOverlay() == ForgeIngameGui.HOTBAR_ELEMENT) {
            event.setCanceled(true);
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && !mc.player.isSpectator()) {
                draw(mc, event.getMatrixStack(), event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight(), event.getPartialTicks());
            }
        }
    }

    private static void draw(Minecraft mc, PoseStack pose, int width, int height, float partialTick) {
        int left = width / 2 - 91;
        int right = left + 182;
        int middle = left + 91;
        int top = height - 22;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        GuiComponent.fill(pose, left + 22, top + 1, right - 22, top + 21, 0xC0101B33);
        GuiComponent.fill(pose, left + 22, top + 1, right - 22, top + 2, 0xFF0D2C4A);
        GuiComponent.fill(pose, left + 22, top + 20, right - 22, top + 21, 0xFF0D2C4A);

        slot(mc, pose, left, top, ARC_LASH, ClientLightningForm.lashCooldown(partialTick), mc.options.keyAttack);
        slot(mc, pose, middle - 11, top, THUNDERSTRIKE, 0.0F, mc.options.keyPickItem);
        charge(mc, pose, middle - 11, top);
        slot(mc, pose, right - 22, top, LIGHTNING_ROD, ClientLightningForm.rodCooldown(partialTick), mc.options.keyUse);

        Font font = mc.font;
        int gap = middle - 11 - (left + 22);
        int textWidth = Mth.floor((gap - 6) / LABEL_SCALE);
        label(pose, font, "Arc Lash", left + 25, top + 4, TEXT, false);
        Entity marked = mc.level == null || ClientLightningForm.markId() < 0 ? null : mc.level.getEntity(ClientLightningForm.markId());
        String rod = marked == null ? "Lightning Rod" : font.plainSubstrByWidth(marked.getDisplayName().getString(), textWidth);
        label(pose, font, rod, right - 25, top + 4, marked == null ? TEXT : SET_COLOUR, true);

        int half = gap - 6;
        int filled = Math.round(half * 2 * ClientLightningForm.formLeft(partialTick));
        timeBar(pose, left + 25, top, half, Math.min(filled, half));
        timeBar(pose, middle + 14, top, half, Math.max(0, filled - half));
        RenderSystem.disableBlend();
    }

    private static void charge(Minecraft mc, PoseStack pose, int x, int y) {
        float filled = ClientLightningForm.chargeFilled();
        ring(pose, x - 1, y - 1, 24, filled, CHARGE_GLOW);
        ring(pose, x, y, 22, filled, CHARGE);
        String multiplier = Integer.toString(Mth.floor(ClientLightningForm.thunderMultiplier() + 1.0E-6F));
        pose.pushPose();
        pose.translate(x + 20, y + 2, 0.0);
        pose.scale(0.5F, 0.5F, 1.0F);
        mc.font.drawShadow(pose, multiplier, -mc.font.width(multiplier), 0, CHARGE);
        pose.popPose();
    }

    /** A 1 px square outline from (x, y), size px across, drawn clockwise from the top centre for {@code filled} of its length. */
    private static void ring(PoseStack pose, int x, int y, int size, float filled, int colour) {
        int half = size / 2;
        int left = Math.round(4 * size * filled);
        int[][] segments = {{x + half, y, 1, 0, size - half}, {x + size - 1, y, 0, 1, size}, {x + size - 1, y + size - 1, -1, 0, size},
                {x, y + size - 1, 0, -1, size}, {x, y, 1, 0, half}};
        for (int[] s : segments) {
            int k = Math.min(s[4], left);
            if (k <= 0) {
                return;
            }
            if (s[2] != 0) {
                int x0 = s[2] > 0 ? s[0] : s[0] - k + 1;
                GuiComponent.fill(pose, x0, s[1], x0 + k, s[1] + 1, colour);
            } else {
                int y0 = s[3] > 0 ? s[1] : s[1] - k + 1;
                GuiComponent.fill(pose, s[0], y0, s[0] + 1, y0 + k, colour);
            }
            left -= k;
        }
    }

    private static void timeBar(PoseStack pose, int x, int top, int length, int filled) {
        GuiComponent.fill(pose, x, top + 13, x + length, top + 18, 0xFF060B17);
        if (filled > 2) {
            GuiComponent.fill(pose, x + 1, top + 14, x + filled - 1, top + 17, 0xFF2BA6D4);
            GuiComponent.fill(pose, x + 1, top + 14, x + filled - 1, top + 15, 0xFFC6F8FF);
        }
    }

    private static void slot(Minecraft mc, PoseStack pose, int x, int y, ResourceLocation icon, float cooldown, KeyMapping key) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, WIDGETS);
        GuiComponent.blit(pose, x, y, 0, 0, 21, 22, 256, 256);
        GuiComponent.blit(pose, x + 21, y, 181, 0, 1, 22, 256, 256);
        RenderSystem.setShaderTexture(0, icon);
        GuiComponent.blit(pose, x + 3, y + 3, 0, 0, 16, 16, 16, 16);
        if (cooldown > 0.0F) {
            GuiComponent.fill(pose, x + 3, y + 3 + Mth.floor(16.0F * (1.0F - cooldown)), x + 19, y + 19, 0x7FFFFFFF);
        }
        String name = keyName(key);
        pose.pushPose();
        pose.translate(x + 20, y + 15, 0.0);
        pose.scale(0.5F, 0.5F, 1.0F);
        mc.font.drawShadow(pose, name, -mc.font.width(name), 0, 0xFFFFFF);
        pose.popPose();
    }

    private static void label(PoseStack pose, Font font, String text, int x, int y, int colour, boolean alignRight) {
        pose.pushPose();
        pose.translate(alignRight ? x - font.width(text) * LABEL_SCALE : x, y, 0.0);
        pose.scale(LABEL_SCALE, LABEL_SCALE, 1.0F);
        font.drawShadow(pose, text, 0, 0, colour);
        pose.popPose();
    }

    private static String keyName(KeyMapping key) {
        InputConstants.Key bound = key.getKey();
        if (bound.getType() == InputConstants.Type.MOUSE) {
            return switch (bound.getValue()) {
                case 0 -> "LMB";
                case 1 -> "RMB";
                case 2 -> "MMB";
                default -> "M" + (bound.getValue() + 1);
            };
        }
        String name = key.getTranslatedKeyMessage().getString();
        return name.length() > 4 ? name.substring(0, 4) : name;
    }
}
