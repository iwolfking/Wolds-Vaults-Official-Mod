package xyz.iwolfking.woldsvaults.augments.cast;

import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Shotgun directions: the base direction plus one per copy, fanned horizontally {@code spreadDegrees} apart and
 * centred on the aim. The direction closest to the aim comes first; it is the native cast.
 */
public final class ShotgunPattern {
    private ShotgunPattern() {
    }

    public static List<Vec3> fan(Vec3 base, int copies, double spreadDegrees) {
        int count = Math.max(1, copies + 1);
        List<Double> offsets = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            offsets.add((i - (count - 1) / 2.0) * spreadDegrees);
        }
        offsets.sort(Comparator.comparingDouble(Math::abs));
        Vec3 unit = base.normalize();
        float yaw = (float) Math.toDegrees(Math.atan2(-unit.x, unit.z));
        float pitch = (float) Math.toDegrees(Math.asin(-unit.y));
        List<Vec3> result = new ArrayList<>(count);
        for (double offset : offsets) {
            result.add(Vec3.directionFromRotation(pitch, (float) (yaw + offset)));
        }
        return result;
    }
}
