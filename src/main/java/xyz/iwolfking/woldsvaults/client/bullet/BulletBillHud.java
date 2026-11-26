package xyz.iwolfking.woldsvaults.client.bullet;

import iskallia.vault.init.ModKeybinds;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.ColorUtil;
import xyz.iwolfking.woldsvaults.client.hud.AbilityHud;

/**
 * While the player is a Bullet Bill the hotbar and the held item's name give way to the form's own solid bar, in the
 * hotbar's place: the jets in the left slot (lit while they fire), Bullet Dash in the middle and the detonation in the
 * right (dimmed without the full set, flashing red once armed), each with its key, and the time the form has left.
 */
public final class BulletBillHud {
    private static final ResourceLocation JETS = WoldsVaults.id("textures/gui/bullet_bill/jets.png");
    private static final ResourceLocation DASH = WoldsVaults.id("textures/gui/bullet_bill/dash.png");
    private static final ResourceLocation DETONATE = WoldsVaults.id("textures/gui/bullet_bill/detonate.png");
    private static final int LIT = 0xFFE8622A;
    private static final int RED = 0xFFD2202A;
    private static final int TEXT = 0xE8E0F0;
    private static final int DIM = 0x8A8494;
    private static final int ARMED_TEXT = 0xFF5A4A;

    private BulletBillHud() {
    }

    public static void onHudLayer(RenderGameOverlayEvent.PreLayer event) {
        Minecraft mc = Minecraft.getInstance();
        ClientBulletBill.Bill bill = mc.player == null ? null : ClientBulletBill.bill(mc.player);
        if (bill != null) {
            AbilityHud.replaceHotbar(event, hud -> draw(hud, bill));
        }
    }

    private static void draw(AbilityHud hud, ClientBulletBill.Bill bill) {
        hud.panel();
        boolean armed = bill.armedAge >= 0;
        float flash = armed ? bill.redPulse(hud.partialTick) : 0.0F;
        int idle = AbilityHud.TOOLTIP_BORDER;
        hud.slot(AbilityHud.Slot.LEFT, JETS, bill.firing() ? LIT : idle, AbilityHud.BACKGROUND, 1.0F, hud.mc.options.keyJump);
        hud.slot(AbilityHud.Slot.MIDDLE, DASH, bill.boosting() ? LIT : idle, AbilityHud.BACKGROUND, 1.0F, ModKeybinds.abilityKey);
        hud.slot(AbilityHud.Slot.RIGHT, DETONATE, armed ? RED : idle, 0xF0000000 | ColorUtil.mix(AbilityHud.BACKGROUND, RED, flash), bill.tuning.detonation() ? 1.0F : 0.35F, hud.mc.options.keyUse);

        hud.labelLeft(bill.firing() ? "Jets firing" : "Jets", bill.firing() ? 0xFFC27A : TEXT);
        hud.labelRight(armed ? "ARMED" : "Detonate", armed ? ARMED_TEXT : bill.tuning.detonation() ? TEXT : DIM);

        float remaining = bill.totalTicks <= 0 ? 0.0F : Mth.clamp((bill.ticksLeft - hud.partialTick) / bill.totalTicks, 0.0F, 1.0F);
        hud.timeBar(remaining, 0xFF0C0A0E, LIT, 0xFFFFC27A);
    }
}
