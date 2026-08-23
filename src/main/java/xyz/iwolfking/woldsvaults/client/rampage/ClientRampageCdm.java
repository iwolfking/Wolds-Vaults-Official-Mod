package xyz.iwolfking.woldsvaults.client.rampage;

/**
 * Client-side mirror of the Rampage damage bonus the HUD shows, as the rendered integer percentage; the server
 * pushes it only on change.
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

    public static float getMeleeFactor() {
        return 1.0F + percent / 100.0F;
    }
}
