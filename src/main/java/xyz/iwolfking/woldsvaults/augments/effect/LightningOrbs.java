package xyz.iwolfking.woldsvaults.augments.effect;

import iskallia.vault.entity.entity.LightningOrbEntity;
import net.minecraft.world.phys.Vec3;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.LightningOrbEntityAccessor;

import java.util.Set;

/**
 * Homing and Orbit on VH's Ball of Lightning. The orb sets its own motion every tick from a private direction and
 * its synced speed, so steering sets those instead of its motion, on the server and on tracking clients.
 */
public final class LightningOrbs {
    private LightningOrbs() {
    }

    public static void steer(LightningOrbEntity orb, Vec3 direction, float speed) {
        if (direction.lengthSqr() < 1.0E-8) {
            return;
        }
        ((LightningOrbEntityAccessor) orb).woldsvaults$setDirection(direction.normalize());
        orb.getEntityData().set(LightningOrbEntityAccessor.woldsvaults$getSpeed(), speed);
    }

    /** Entity ids the orb already damaged; it never hits them again, so Homing skips them. */
    public static Set<Integer> alreadyHit(LightningOrbEntity orb) {
        return ((LightningOrbEntityAccessor) orb).woldsvaults$getHitEntities();
    }
}
