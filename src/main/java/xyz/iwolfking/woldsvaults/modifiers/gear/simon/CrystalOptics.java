package xyz.iwolfking.woldsvaults.modifiers.gear.simon;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import xyz.iwolfking.woldsvaults.api.gear.sets.GearSetHelper;
import xyz.iwolfking.woldsvaults.entities.simon.SimonCrystalEntity;
import xyz.iwolfking.woldsvaults.abilities.arcane.BeamOptics.Lens;
import xyz.iwolfking.woldsvaults.abilities.arcane.BeamOptics.Ray;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Beams bouncing between Simon's Set crystals. The same paths are worked out on the server (what a beam hits) and on
 * clients (what is drawn), so the two agree.
 * <p>
 * Each crystal is an upright column round its middle, as wide as it has grown (a Geode out to the set's
 * reflectRadius). A beam that enters one stops there. Its light gathers into the crystal's middle and bounces on to
 * the nearest crystal it can see that this beam hasn't visited yet, and so on. From the last crystal it bounces to
 * the nearest enemy it can see.
 * <p>
 * Where the beam hits a crystal nudges the bounce. A beam through the middle lands on the middle of the next crystal.
 * One that hits a crystal off to one side lands that far off to the same side of the next crystal's face, or of the
 * enemy's body. So a beam sweeping across a crystal sweeps its bounce across the next one, and that one's bounce
 * across the one after.
 */
public final class CrystalOptics {
    private static final int MAX_CRYSTALS = 6;
    // how far below and above a crystal's box a beam still catches it
    private static final double BELOW = 0.3;
    private static final double ABOVE = 0.6;
    private static final double MIN_RADIUS = 0.4;
    // beams steeper than this run along the columns rather than through them
    private static final double MIN_FLAT = 0.1;
    // how far across the next crystal's face (or the enemy's body) an edge hit lands, kept a little inside its edge
    private static final double SWAY = 0.85;

    // where a line enters a crystal, and how far across its face (-1 to 1, positive to the right of its middle as the
    // line travels)
    private record Hit(SimonCrystalEntity crystal, Vec3 in, double sway) {
    }

    private CrystalOptics() {
    }

    public static List<SimonCrystalEntity> crystalsNear(Level level, Vec3 at, double range) {
        return level.getEntitiesOfClass(SimonCrystalEntity.class, new AABB(at, at).inflate(range), crystal -> !crystal.isRemoved());
    }

    // The path of a beam from start along dir (a unit vector) for length, bouncing from the first crystal it enters.
    // The beam passes through blocks (Vault Hunters' own Prism beams do, and Arcane's ray is already cut short at the
    // wall). Each bounce needs a clear line to where it goes.
    public static List<Ray> trace(Level level, List<SimonCrystalEntity> crystals, Vec3 start, Vec3 dir, double length,
                                  Lens lens, @Nullable Entity context) {
        Vec3 end = start.add(dir.scale(length));
        if (crystals.isEmpty()) {
            return List.of(new Ray(start, end, null, false));
        }
        List<Ray> rays = new ArrayList<>(4);
        Set<SimonCrystalEntity> visited = new HashSet<>();
        Hit hit = firstHit(crystals, visited, start, dir, length, lens.bentRadius());
        if (hit == null) {
            rays.add(new Ray(start, end, null, false));
            return rays;
        }
        rays.add(new Ray(start, hit.in(), null, false));
        while (true) {
            SimonCrystalEntity crystal = hit.crystal();
            visited.add(crystal);
            Vec3 core = crystal.center();
            rays.add(new Ray(hit.in(), core, crystal, true));
            SimonCrystalEntity next = visited.size() >= MAX_CRYSTALS ? null
                    : nearestSeen(level, crystals, other -> !other.isRemoved() && !visited.contains(other), core, lens.bentLength(), context);
            if (next == null) {
                List<Mob> mobs = level.getEntitiesOfClass(Mob.class, new AABB(core, core).inflate(lens.bentLength()));
                Mob enemy = nearestSeen(level, mobs, GearSetHelper::isEnemy, core, lens.bentLength(), context);
                if (enemy != null) {
                    AABB box = enemy.getBoundingBox();
                    rays.add(new Ray(core, swayed(core, box.getCenter(), hit.sway() * SWAY * box.getXsize() / 2.0), crystal, false));
                }
                return rays;
            }
            Vec3 aim = swayed(core, next.center(), hit.sway() * SWAY * radius(next, lens.bentRadius()));
            double reach = core.distanceTo(aim);
            Vec3 heading = aim.subtract(core).scale(1.0 / Math.max(1.0E-6, reach));
            // what it enters first on the way (the crystal aimed at, unless another is in the way)
            Hit onward = firstHit(crystals, visited, core, heading, reach + 2.0, lens.bentRadius());
            if (onward == null) {
                rays.add(new Ray(core, aim, crystal, false));
                return rays;
            }
            rays.add(new Ray(core, onward.in(), crystal, false));
            hit = onward;
        }
    }

    // the first crystal along the line that it enters, where, and how far across its face
    @Nullable
    private static Hit firstHit(List<SimonCrystalEntity> crystals, Set<SimonCrystalEntity> visited, Vec3 from, Vec3 heading, double length, double geodeRadius) {
        double flat = Math.sqrt(heading.x * heading.x + heading.z * heading.z);
        if (flat < MIN_FLAT) {
            return null;
        }
        double ux = heading.x / flat;
        double uz = heading.z / flat;
        Hit best = null;
        double bestAlong = length;
        for (SimonCrystalEntity crystal : crystals) {
            if (crystal.isRemoved() || visited.contains(crystal)) {
                continue;
            }
            AABB box = crystal.getBoundingBox();
            double radius = radius(crystal, geodeRadius);
            double cx = (box.minX + box.maxX) / 2.0 - from.x;
            double cz = (box.minZ + box.maxZ) / 2.0 - from.z;
            // how far along (level) to the column's nearest point, and how far the column's middle is to the line's
            // right (so the line passes that far to the middle's left)
            double along = cx * ux + cz * uz;
            double offset = ux * cz - uz * cx;
            if (Math.abs(offset) >= radius) {
                continue;
            }
            double t = (along - Math.sqrt(radius * radius - offset * offset)) / flat;
            if (t <= 1.0E-3 || t >= bestAlong) {
                continue;
            }
            double y = from.y + heading.y * t;
            if (y < box.minY - BELOW || y > box.maxY + ABOVE) {
                continue;
            }
            bestAlong = t;
            best = new Hit(crystal, from.add(heading.scale(t)), -offset / radius);
        }
        return best;
    }

    // the nearest of the candidates (by their middles) that passes the filter and can be seen from here, within range
    @Nullable
    private static <T extends Entity> T nearestSeen(Level level, List<T> candidates, Predicate<T> filter, Vec3 from, double range, @Nullable Entity context) {
        T best = null;
        double bestDistance = range * range;
        for (T candidate : candidates) {
            Vec3 at = candidate.getBoundingBox().getCenter();
            double distance = at.distanceToSqr(from);
            if (filter.test(candidate) && distance < bestDistance && canSee(level, from, at, context)) {
                best = candidate;
                bestDistance = distance;
            }
        }
        return best;
    }

    public static boolean canSee(Level level, Vec3 from, Vec3 to, @Nullable Entity context) {
        return level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, context)).getType() == HitResult.Type.MISS;
    }

    // the point side across from target, to the right (as seen travelling from from) for a positive side
    private static Vec3 swayed(Vec3 from, Vec3 target, double side) {
        double dx = target.x - from.x;
        double dz = target.z - from.z;
        double flat = Math.sqrt(dx * dx + dz * dz);
        if (flat < 1.0E-6) {
            return target;
        }
        // with x east and z south, a quarter turn from (dx, dz) towards +z is to the right
        return target.add(-dz / flat * side, 0.0, dx / flat * side);
    }

    // How wide a column the crystal is to a beam: its share of a Geode's.
    public static double radius(SimonCrystalEntity crystal, double geodeRadius) {
        return Math.max(MIN_RADIUS, geodeRadius * crystal.widthScale());
    }
}
