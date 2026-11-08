package xyz.iwolfking.woldsvaults.augments.cast;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

/**
 * Turns the player's aim for one cast (Shotgun copies). VH aims ability projectiles with the player's rotation, so
 * each copy is cast with the rotation of its direction and the real rotation is restored in the same tick. Server
 * side only; nothing is sent to the client.
 */
public final class AimOverride {
    private AimOverride() {
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
        try {
            player.setYRot(newYRot);
            player.setXRot(newXRot);
            player.yRotO = newYRot;
            player.xRotO = newXRot;
            player.setYHeadRot(newYRot);
            return action.get();
        } finally {
            player.setYRot(yRot);
            player.setXRot(xRot);
            player.yRotO = yRotO;
            player.xRotO = xRotO;
            player.setYHeadRot(yHeadRot);
        }
    }
}
