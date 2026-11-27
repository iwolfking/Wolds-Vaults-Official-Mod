package xyz.iwolfking.woldsvaults.spellcraft.projectile;

import net.minecraft.world.phys.Vec3;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;

final class OrbitMotion {
    private OrbitMotion() {
    }

    static void steerOrbit(Steered steered, SpellcraftConfig config) {
        double radius = config.tuning.orbitRadius;
        double angularSpeed = Math.max(0.15, Math.min(0.6, steered.speed / radius));
        double angle = steered.angleOffset + steered.age * angularSpeed;
        Vec3 center = steered.owner.position().add(0, config.tuning.orbitHeight, 0);
        Vec3 target = center.add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
        ProjectileSteering.move(steered, target.subtract(steered.projectile.position()));
    }
}
