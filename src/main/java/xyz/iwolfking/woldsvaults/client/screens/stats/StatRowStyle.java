package xyz.iwolfking.woldsvaults.client.screens.stats;

import net.minecraft.network.chat.TextColor;

import javax.annotation.Nullable;

/**
 * Extra state carried by an H-screen stat row builder: the key the Statistics tab layout table knows it by, and the
 * colour its value is drawn in. A row with no value colour keeps the base look.
 */
public interface StatRowStyle {
    void woldsvaults$setStatKey(String key);

    @Nullable
    String woldsvaults$getStatKey();

    void woldsvaults$setValueColor(@Nullable TextColor color);

    @Nullable
    TextColor woldsvaults$getValueColor();

    /** Shows the server-synced value for this key (and {@code cap:} key) while one is recent. */
    void woldsvaults$syncFrom(String key);
}
