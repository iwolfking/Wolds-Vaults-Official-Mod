package xyz.iwolfking.woldsvaults.client.lightning;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.client.hud.AbilityHud;

/**
 * While the player is lightning (Lightning Fiend 6-piece) the hotbar and the held item's name give way to the form's
 * ability bar ({@link AbilityHud}), in the hotbar's place: Arc Lash in the left slot, Thunderstrike in the middle and
 * Lightning Rod in the right, each with its key (and the lash and rod a cooldown veil like an item's), the lash's and
 * rod's names (the rod's target once it has one) and the time the form has left. The Thunderstrike slot shows the
 * form's charge: a glowing yellow ring around the slot filling clockwise from the top centre, and the strike's
 * multiplier at that charge as a small yellow number in the slot's top-right corner.
 */
public final class LightningFormHud {
    private static final ResourceLocation ARC_LASH = new ResourceLocation(WoldsVaults.MOD_ID, "textures/gui/lightning_form/arc_lash.png");
    private static final ResourceLocation LIGHTNING_ROD = new ResourceLocation(WoldsVaults.MOD_ID, "textures/gui/lightning_form/lightning_rod.png");
    private static final ResourceLocation THUNDERSTRIKE = new ResourceLocation(WoldsVaults.MOD_ID, "textures/gui/lightning_form/thunderstrike.png");
    private static final int TEXT = 0xC6F8FF;
    private static final int SET_COLOUR = 0x6FCFFF;
    private static final int CHARGE = 0xFFFFD84A;
    private static final int CHARGE_GLOW = 0x60FFD84A;

    private LightningFormHud() {
    }

    public static void onHudLayer(RenderGameOverlayEvent.PreLayer event) {
        if (ClientLightningForm.isActive()) {
            AbilityHud.replaceHotbar(event, LightningFormHud::draw);
        }
    }

    private static void draw(AbilityHud hud) {
        Minecraft mc = hud.mc;
        float partialTick = hud.partialTick;
        hud.panel();
        hud.cooldownSlot(AbilityHud.Slot.LEFT, ARC_LASH, ClientLightningForm.lashCooldown(partialTick), mc.options.keyAttack);
        hud.cooldownSlot(AbilityHud.Slot.MIDDLE, THUNDERSTRIKE, 0.0F, mc.options.keyPickItem);
        charge(hud);
        hud.cooldownSlot(AbilityHud.Slot.RIGHT, LIGHTNING_ROD, ClientLightningForm.rodCooldown(partialTick), mc.options.keyUse);

        hud.labelLeft("Arc Lash", TEXT);
        Entity marked = mc.level == null || ClientLightningForm.markId() < 0 ? null : mc.level.getEntity(ClientLightningForm.markId());
        String rod = marked == null ? "Lightning Rod" : mc.font.plainSubstrByWidth(marked.getDisplayName().getString(), hud.labelRoom());
        hud.labelRight(rod, marked == null ? TEXT : SET_COLOUR);

        hud.timeBar(ClientLightningForm.formLeft(partialTick), 0xFF060B17, 0xFF2BA6D4, 0xFFC6F8FF);
    }

    /** Draws the charge ring round the Thunderstrike slot and the strike's multiplier in its corner. */
    private static void charge(AbilityHud hud) {
        PoseStack pose = hud.pose();
        int x = hud.slotX(AbilityHud.Slot.MIDDLE);
        int y = hud.top();
        float filled = ClientLightningForm.chargeFilled();
        ring(pose, x - 1, y - 1, 24, filled, CHARGE_GLOW);
        ring(pose, x, y, 22, filled, CHARGE);
        String multiplier = Integer.toString(Mth.floor(ClientLightningForm.thunderMultiplier() + 1.0E-6F));
        pose.pushPose();
        pose.translate(x + 20, y + 2, 0.0);
        pose.scale(0.5F, 0.5F, 1.0F);
        hud.mc.font.drawShadow(pose, multiplier, -hud.mc.font.width(multiplier), 0, CHARGE);
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
}
