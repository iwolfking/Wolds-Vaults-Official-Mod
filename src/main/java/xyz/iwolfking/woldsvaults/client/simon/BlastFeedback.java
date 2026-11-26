package xyz.iwolfking.woldsvaults.client.simon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import xyz.iwolfking.woldsvaults.api.util.ColorUtil;

import java.util.Random;

/**
 * The flash and the jolt a Simon's Set blast gives nearby players, each running on its own clock.
 * <p>
 * The flash is the chain's blended colour (cycling through the rainbow when a Prismatic Geode was in it) over the whole
 * view, fading out over half a second. It's at full strength within the blast and a little past it, fading to nothing
 * three times as far out.
 * <p>
 * In the jolt the view dips at the blast, then wobbles (roll, pitch and a little yaw, out of step with each other) and
 * settles over as long as the ground ripple lasts. It's stronger the nearer the blast, half as strong again for each
 * Prismatic Geode in the chain (at most twice), and scaled by the player's Screen Effect Scale setting.
 */
public final class BlastFeedback {
    private static final float FLASH_TICKS = 10.0F;
    private static final float PEAK = 0.4F;
    private static final float RAINBOW_PEAK = 0.5F;
    private static final float SHAKE_TICKS = 16.0F;
    private static final float ROLL = 2.5F;
    private static final float PITCH = 1.4F;
    private static final float YAW = 1.0F;
    private static final float DIP = 1.6F;
    private static final Random RANDOM = new Random();
    private static int flashAge = -1;
    private static float flashStrength;
    private static int flashRgb;
    private static boolean rainbow;
    private static int shakeAge = -1;
    private static float shakeStrength;
    private static float phase;

    private BlastFeedback() {
    }

    /**
     * How strongly the local player feels a blast at this point with this radius: fully within it and a little past it,
     * fading to nothing three times as far out (0 with no player).
     */
    private static float felt(Vec3 at, float radius) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return 0.0F;
        }
        double distance = mc.player.getEyePosition().distanceTo(at);
        double full = radius + 8.0;
        double none = radius * 3.0 + 16.0;
        return (float) Mth.clamp((none - distance) / (none - full), 0.0, 1.0);
    }

    /** Whether a new blast this strong outdoes what is left of the one still playing. */
    private static boolean outdoes(float power, int age, float strength, float ticks) {
        return power > 0.0F && (age < 0 || power >= strength * (1.0F - age / ticks));
    }

    /**
     * Starts the flash for a blast at this point and radius, in this colour, with this many Prismatic Geodes, if it
     * outdoes the one playing.
     */
    public static void flash(Vec3 at, float radius, int color, int prismaticGeodes) {
        float felt = felt(at, radius);
        if (outdoes(felt, flashAge, flashStrength, FLASH_TICKS)) {
            flashAge = 0;
            flashStrength = felt;
            flashRgb = color;
            rainbow = prismaticGeodes > 0;
        }
    }

    /**
     * Starts the jolt for a blast at this point and radius, with this many Prismatic Geodes, if it outdoes the one
     * playing.
     */
    public static void shake(Vec3 at, float radius, int prismaticGeodes) {
        float power = felt(at, radius) * Math.min(2.0F, 1.0F + 0.5F * prismaticGeodes);
        if (outdoes(power, shakeAge, shakeStrength, SHAKE_TICKS)) {
            shakeAge = 0;
            shakeStrength = power;
            phase = RANDOM.nextFloat() * Mth.TWO_PI;
        }
    }

    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !Minecraft.getInstance().isPaused()) {
            flashAge = older(flashAge, FLASH_TICKS);
            shakeAge = older(shakeAge, SHAKE_TICKS);
        }
    }

    /** One tick on, or -1 once it is over (or was not playing). */
    private static int older(int age, float ticks) {
        return age < 0 || age + 1 > ticks ? -1 : age + 1;
    }

    public static void onOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL || flashAge < 0) {
            return;
        }
        float time = flashAge + event.getPartialTicks();
        float fade = 1.0F - time / FLASH_TICKS;
        if (fade <= 0.0F) {
            return;
        }
        float alpha = flashStrength * (rainbow ? RAINBOW_PEAK : PEAK) * fade * fade;
        int color = rainbow ? Mth.hsvToRgb((time * 0.08F) % 1.0F, 0.6F, 1.0F) : flashRgb;
        int argb = Mth.clamp((int) (alpha * 255.0F), 0, 255) << 24 | ColorUtil.mix(color, 0xFFFFFF, 0.2F);
        GuiComponent.fill(event.getMatrixStack(), 0, 0, event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight(), argb);
    }

    public static void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        if (shakeAge < 0) {
            return;
        }
        float time = shakeAge + (float) event.getPartialTicks();
        float left = 1.0F - time / SHAKE_TICKS;
        if (left <= 0.0F) {
            return;
        }
        float scale = Minecraft.getInstance().options.screenEffectScale;
        float amount = shakeStrength * left * left * scale;
        float dip = Math.max(0.0F, 1.0F - time / 3.0F) * DIP * shakeStrength * scale;
        float roll = (0.7F * Mth.sin(time * 2.1F + phase) + 0.3F * Mth.sin(time * 3.7F + phase * 2.0F)) * ROLL * amount;
        float pitch = (0.6F * Mth.sin(time * 2.9F + phase * 1.3F) + 0.4F * Mth.sin(time * 4.3F)) * PITCH * amount;
        float yaw = Mth.sin(time * 2.4F + phase * 0.7F) * YAW * amount;
        event.setRoll(event.getRoll() + roll);
        event.setPitch(event.getPitch() + pitch + dip);
        event.setYaw(event.getYaw() + yaw);
    }

    public static void onLoggedOut(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        flashAge = -1;
        shakeAge = -1;
    }
}
