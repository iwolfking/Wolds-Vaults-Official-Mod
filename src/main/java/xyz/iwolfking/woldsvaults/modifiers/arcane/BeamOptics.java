package xyz.iwolfking.woldsvaults.modifiers.arcane;

import com.mojang.math.Vector3f;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * How Arcane beams travel. On their own they run straight, but something else can bend them by registering a
 * {@link Bender}, which decides the path a beam takes through whatever is near it. The same paths are worked out on the
 * server (what a beam hits) and on clients (what is drawn), so the two agree.
 */
public final class BeamOptics {
    // what a beam was bent by, which colours the light that leaves it
    public interface Tinted {
        Vector3f shownColor(float partialTicks);
    }

    // One straight piece of a beam's path. The first comes from the source with nothing bending it. Later pieces belong to
    // whatever bent the beam, and inside ones are the light crossing it.
    public record Ray(Vec3 from, Vec3 to, @Nullable Tinted bentBy, boolean inside) {
    }

    // how far bent light carries, and how wide a thing is to a beam at its widest
    public record Lens(double bentLength, double bentRadius) {
    }

    // the path of a beam from start along dir (a unit vector) for length
    @FunctionalInterface
    public interface Paths {
        List<Ray> trace(Vec3 start, Vec3 dir, double length, @Nullable Entity context);
    }

    public static final Paths STRAIGHT = (start, dir, length, context) -> List.of(new Ray(start, start.add(dir.scale(length)), null, false));

    // What can bend Arcane beams. Every part has a default that leaves them as they are.
    public interface Bender {
        // how far the beams of a Prism this caster places reach
        default float prismReach(ServerPlayer caster, float radius) {
            return radius;
        }

        default Lens lens() {
            return new Lens(0.0, 0.0);
        }

        // the share of a beam's damage that bent light deals
        default float bentDamage() {
            return 1.0F;
        }

        // the paths beams take from this spot out to reach, or STRAIGHT if nothing there bends them
        default Paths near(Level level, Vec3 at, double reach, Lens lens) {
            return STRAIGHT;
        }

        // an Arcane or Arcane Rail ray just fired from start to end
        default void rayFired(ServerPlayer player, Vec3 start, Vec3 end, float percentAbilityPower, int beamTicks, float beamWidth) {
        }
    }

    private static Bender bender = new Bender() {
    };

    private BeamOptics() {
    }

    public static void setBender(Bender replacement) {
        bender = replacement;
    }

    public static Bender bender() {
        return bender;
    }

    // Whether a beam piece from a to b touches the entity. It touches when the line passes within width of the
    // entity's middle across the level, plus however much wider than a player the entity is, and not too far above or
    // below its box. This is about what Vault Hunters' own Prism checks.
    public static boolean touches(Entity entity, Vec3 a, Vec3 b, double width) {
        AABB box = entity.getBoundingBox();
        double dx = b.x - a.x;
        double dz = b.z - a.z;
        double flatSqr = dx * dx + dz * dz;
        if (flatSqr < 1.0E-6) {
            AABB fat = box.inflate(width);
            return fat.contains(a) || fat.clip(a, b).isPresent();
        }
        Vec3 c = box.getCenter();
        double t = Mth.clamp(((c.x - a.x) * dx + (c.z - a.z) * dz) / flatSqr, 0.0, 1.0);
        double hx = c.x - (a.x + dx * t);
        double hz = c.z - (a.z + dz * t);
        double y = a.y + (b.y - a.y) * t;
        double reach = width + Math.max(0.0, box.getXsize() / 2.0 - 0.3);
        return hx * hx + hz * hz <= reach * reach && y >= box.minY - 0.75 && y <= box.maxY + 0.75;
    }
}
