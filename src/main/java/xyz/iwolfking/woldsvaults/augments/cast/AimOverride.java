package xyz.iwolfking.woldsvaults.augments.cast;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.HashMap;
import java.util.function.Supplier;

/**
 * Turns the player's aim for one cast (Shotgun copies). VH aims ability projectiles with the player's rotation, so
 * each copy is cast with the rotation of its direction and the real rotation is restored in the same tick. Server
 * side only; nothing is sent to the client.
 */
public final class AimOverride {
    private static final Map<UUID, Vec3> REAL_LOOK = new HashMap<>();

    private AimOverride() {
    }

    /** Where the player is really looking: the crosshair, even while a copy is cast with a turned aim. */
    public static Vec3 realLook(ServerPlayer player) {
        Vec3 look = REAL_LOOK.get(player.getUUID());
        return look != null ? look : player.getLookAngle();
    }

    public static <T> T call(ServerPlayer player, Vec3 direction, Supplier<T> action) {
        float yRot = player.getYRot();
        float xRot = player.getXRot();
        float yRotO = player.yRotO;
        float xRotO = player.xRotO;
        float yHeadRot = player.getYHeadRot();
        Vec3 unit = direction.normalize();
        float newYRot = (float) Math.toDegrees(Math.atan2(-unit.x, unit.z));
        float newXRot = (float) Math.toDegrees(Math.asin(-unit.y));
        Vec3 outer = REAL_LOOK.get(player.getUUID());
        if (outer == null) {
            REAL_LOOK.put(player.getUUID(), player.getLookAngle());
        }
        try {
            player.setYRot(newYRot);
            player.setXRot(newXRot);
            player.yRotO = newYRot;
            player.xRotO = newXRot;
            player.setYHeadRot(newYRot);
            return action.get();
        } finally {
            if (outer == null) {
                REAL_LOOK.remove(player.getUUID());
            }
            player.setYRot(yRot);
            player.setXRot(xRot);
            player.yRotO = yRotO;
            player.xRotO = xRotO;
            player.setYHeadRot(yHeadRot);
        }
    }
}
