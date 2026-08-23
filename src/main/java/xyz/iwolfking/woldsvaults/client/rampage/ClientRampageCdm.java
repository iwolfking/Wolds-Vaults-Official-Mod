package xyz.iwolfking.woldsvaults.client.rampage;

/**
 * Client-side mirror of the Rampage damage bonus the HUD indicator shows.
 *
 * <p>Held as the rendered percentage rather than as a factor, because the server only sends a
 * packet when that integer changes; storing anything finer would imply a precision the sync does
 * not have.
 *
 * <p>No staleness timeout, unlike the Champion HUD mirror. That one is driven by a periodic push
 * and can tell "stopped arriving" from "still zero"; this one is edge-triggered, so a timeout
 * would clear a perfectly current value during any quiet stretch. Disconnect is handled explicitly
 * by {@code GreedClientDisconnectEvents} instead.
 */
public final class ClientRampageCdm {
    private static int percent;

    private ClientRampageCdm() {
    }

    public static void update(int percent) {
        ClientRampageCdm.percent = percent;
    }

    public static void clear() {
        percent = 0;
    }

    public static int getPercent() {
        return percent;
    }

    /**
     * The value the overlay renders, in the form the base mod's indicator expects: a damage
     * multiplier whose distance from 1.0 is the percentage shown. Returning exactly 1.0 when there
     * is no bonus is what makes the base overlay's own early-return hide the row.
     */
    public static float getMeleeFactor() {
        return 1.0F + percent / 100.0F;
    }
}
