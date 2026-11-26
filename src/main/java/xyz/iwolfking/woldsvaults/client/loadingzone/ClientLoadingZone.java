package xyz.iwolfking.woldsvaults.client.loadingzone;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.Input;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;

/**
 * The client half of a loading zone: a fade to black and back with the HUD and hand hidden and movement held still,
 * timed on its own clock from the server's message.
 */
public final class ClientLoadingZone {
    private static final long MILLIS_PER_TICK = 50L;

    private static long start = -1L;
    private static long fadeOut;
    private static long hold;
    private static long fadeIn;

    private ClientLoadingZone() {
    }

    public static void begin(int fadeOutTicks, int holdTicks, int fadeInTicks) {
        start = Util.getMillis();
        fadeOut = fadeOutTicks * MILLIS_PER_TICK;
        hold = holdTicks * MILLIS_PER_TICK;
        fadeIn = fadeInTicks * MILLIS_PER_TICK;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.screen != null) {
            minecraft.player.closeContainer();
        }
    }

    private static boolean active() {
        if (start >= 0L && Util.getMillis() - start >= fadeOut + hold + fadeIn) {
            start = -1L;
        }
        return start >= 0L;
    }

    private static float darkness() {
        long elapsed = Util.getMillis() - start;
        if (elapsed < fadeOut) {
            return elapsed / (float) fadeOut;
        }
        if (elapsed < fadeOut + hold) {
            return 1.0F;
        }
        return 1.0F - (elapsed - fadeOut - hold) / (float) fadeIn;
    }

    /** Draws the black over the world and cancels the whole overlay, hiding every HUD layer. */
    public static void onOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL || !active()) {
            return;
        }
        int alpha = Math.round(Math.max(0.0F, Math.min(1.0F, darkness())) * 255.0F);
        GuiComponent.fill(event.getMatrixStack(), 0, 0, event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight(), alpha << 24);
        event.setCanceled(true);
    }

    public static void onRenderHand(RenderHandEvent event) {
        if (active()) {
            event.setCanceled(true);
        }
    }

    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (active()) {
            Input input = event.getInput();
            input.forwardImpulse = 0.0F;
            input.leftImpulse = 0.0F;
            input.up = false;
            input.down = false;
            input.left = false;
            input.right = false;
            input.jumping = false;
            input.shiftKeyDown = false;
        }
    }

    public static void onLoggedOut(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        start = -1L;
    }
}
