package xyz.iwolfking.woldsvaults.loadingzone;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public interface LoadingZoneDestination {
    record Landing(Vec3 pos, float yaw) {
    }

    /** Returns the landing, or null while it is still loading. */
    @Nullable
    Landing poll(ServerPlayer player);

    boolean instant();

    static LoadingZoneDestination at(Vec3 pos, float yaw) {
        Landing landing = new Landing(pos, yaw);
        return new LoadingZoneDestination() {
            @Override
            public Landing poll(ServerPlayer player) {
                return landing;
            }

            @Override
            public boolean instant() {
                return true;
            }
        };
    }
}
