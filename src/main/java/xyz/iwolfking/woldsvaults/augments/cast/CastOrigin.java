package xyz.iwolfking.woldsvaults.augments.cast;

import net.minecraft.world.phys.Vec3;

/**
 * Where a Bundled Spell's payload is cast: point abilities happen at {@code position} (VH's skill source position)
 * and projectile abilities relaunch from it along {@code direction}.
 */
public record CastOrigin(Vec3 position, Vec3 direction) {
}
