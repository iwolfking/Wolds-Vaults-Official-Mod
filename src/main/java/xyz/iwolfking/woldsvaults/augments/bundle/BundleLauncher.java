package xyz.iwolfking.woldsvaults.augments.bundle;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import xyz.iwolfking.woldsvaults.augments.cast.CastModifiers;
import xyz.iwolfking.woldsvaults.augments.cast.CastOrigin;
import xyz.iwolfking.woldsvaults.augments.cast.ShotgunPattern;
import xyz.iwolfking.woldsvaults.augments.effect.ProjectileSteering;
import xyz.iwolfking.woldsvaults.augments.spell.SpellModel;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;

import javax.annotation.Nullable;

/**
 * Throws a Bundled Spell from the player, or from a landing point for a bundle inside another bundle's payload. The
 * glyphs right after it modify the orb: speed, size, Homing or Orbit, Ethereal, and Shotgun (one orb per copy, each
 * carrying the whole payload at the copies' damage). The orb flies for the Bundled Spell's rolled flight time; an
 * orbiting orb lasts its whole orbit and lands its payload where it is when the orbit ends.
 */
public final class BundleLauncher {
    private BundleLauncher() {
    }

    public static void launch(FocusAugmentsConfig config, ServerPlayer player, SpellModel.Bundle bundle, @Nullable CastOrigin origin,
                              float damageFactor) {
        Vec3 start;
        Vec3 direction;
        if (origin != null) {
            start = origin.position();
            direction = origin.direction().lengthSqr() > 1.0E-6 ? origin.direction() : player.getLookAngle();
        } else {
            start = player.getEyePosition().subtract(0, 0.1, 0);
            direction = player.getLookAngle();
        }
        CastModifiers orb = CastModifiers.of(config, bundle.glyphs());
        double speed = config.tuning.bundleSpeed * orb.speed();
        int lifetime = Math.max(1, Math.round(bundle.glyph().potency() * 20.0F));
        float payloadDamage = damageFactor * orb.damage();
        for (Vec3 heading : ShotgunPattern.fan(direction, orb.shotgunCopies(), config.tuning.shotgunSpreadDegrees)) {
            AugmentBundleEntity projectile = new AugmentBundleEntity(player.level, player, bundle.payload(), payloadDamage, lifetime,
                    orb.scale(), orb.ethereal());
            projectile.setPos(start);
            projectile.setDeltaMovement(heading.scale(speed));
            player.level.addFreshEntity(projectile);
            if (orb.orbit()) {
                ProjectileSteering.orbit(projectile, player, orb.orbitSeconds());
            } else if (orb.homing()) {
                ProjectileSteering.home(projectile, player, orb.homingRange(), orb.homingTurn());
            }
        }
    }
}
