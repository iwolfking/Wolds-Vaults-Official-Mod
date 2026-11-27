package xyz.iwolfking.woldsvaults.api.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class SecondChanceHelper {

    private static final String LAST_ACTIVATION_TAG = "woldsvaults_second_chance_last_activation";
    private static final long COOLDOWN_TICKS = 400L;

    /**
     * Whether the Second Chance cooldown since the player's last activation has elapsed, measured in
     * server ticks. A stored time ahead of the current tick count counts as elapsed.
     */
    public static boolean canActivate(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(LAST_ACTIVATION_TAG)) {
            return true;
        }
        long now = player.getServer().getTickCount();
        long last = data.getLong(LAST_ACTIVATION_TAG);
        return now < last || now - last >= COOLDOWN_TICKS;
    }

    public static void markActivated(ServerPlayer player) {
        player.getPersistentData().putLong(LAST_ACTIVATION_TAG, player.getServer().getTickCount());
    }
}
