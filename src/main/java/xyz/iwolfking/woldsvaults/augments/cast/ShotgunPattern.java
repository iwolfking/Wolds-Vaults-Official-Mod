package xyz.iwolfking.woldsvaults.augments.cast;

import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Shotgun directions. Every split doubles the copies, the halves {@code spreadDegrees} apart: the first split is
 * horizontal, the second vertical, every later one outward along each copy's own diagonal from the centre.
 */
public final class ShotgunPattern {
    private ShotgunPattern() {
    }

    public static List<Vec3> directions(Vec3 base, int splits, double spreadDegrees) {
        List<Vec2> offsets = List.of(Vec2.ZERO);
        double half = spreadDegrees / 2;
        for (int split = 1; split <= splits; split++) {
            List<Vec2> next = new ArrayList<>(offsets.size() * 2);
            for (Vec2 offset : offsets) {
                Vec2 axis = splitAxis(split, offset);
                next.add(new Vec2((float) (offset.x + axis.x * half), (float) (offset.y + axis.y * half)));
                next.add(new Vec2((float) (offset.x - axis.x * half), (float) (offset.y - axis.y * half)));
            }
            offsets = next;
        }
        Vec3 unit = base.normalize();
        float yaw = (float) Math.toDegrees(Math.atan2(-unit.x, unit.z));
        float pitch = (float) Math.toDegrees(Math.asin(-unit.y));
        List<Vec3> result = new ArrayList<>(offsets.size());
        for (Vec2 offset : offsets) {
            result.add(Vec3.directionFromRotation(pitch - offset.y, yaw + offset.x));
        }
        return result;
    }

    private static Vec2 splitAxis(int split, Vec2 offset) {
        if (split == 1) {
            return new Vec2(1, 0);
        }
        if (split == 2) {
            return new Vec2(0, 1);
        }
        double length = Math.sqrt(offset.x * offset.x + offset.y * offset.y);
        return length < 1.0E-6 ? new Vec2((float) Math.sqrt(0.5), (float) Math.sqrt(0.5))
                : new Vec2((float) (offset.x / length), (float) (offset.y / length));
    }
}
