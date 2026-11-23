package xyz.iwolfking.woldsvaults.loadingzone;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

// Where a loading zone transition ends. An arrival marker in the same room is ready at once; a pocket, or the way back out of
// one, may first need its chunks generated, so the transition keeps the screen black and asks again each tick.
public interface LoadingZoneDestination {
    record Landing(Vec3 pos, float yaw) {
    }

    // null while the landing is still loading
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
