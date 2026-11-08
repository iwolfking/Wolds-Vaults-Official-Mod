package xyz.iwolfking.woldsvaults.augments.bundle;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import xyz.iwolfking.woldsvaults.augments.AugmentType;
import xyz.iwolfking.woldsvaults.augments.cast.CastContext;
import xyz.iwolfking.woldsvaults.augments.cast.CastModifiers;
import xyz.iwolfking.woldsvaults.augments.cast.ShotgunPattern;
import xyz.iwolfking.woldsvaults.augments.cast.SpellPlan;
import xyz.iwolfking.woldsvaults.augments.effect.ProjectileSteering;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;

/**
 * Throws a Bundled Spell from the player, or from a landing point for a bundle inside another bundle's payload.
 * Shotgun copies share one landing tracker, so only the first landing of each ability casts natively; each copy's
 * payload gets the Shotgun damage factor per split.
 */
public final class BundleLauncher {
    private BundleLauncher() {
    }

    public static void launch(FocusAugmentsConfig config, ServerPlayer player, SpellPlan.Bundle bundle, CastContext context) {
        Vec3 start;
        Vec3 direction;
        if (context.origin() != null) {
            start = context.origin().position();
            direction = context.origin().direction().lengthSqr() > 1.0E-6 ? context.origin().direction() : player.getLookAngle();
        } else {
            start = player.getEyePosition().subtract(0, 0.1, 0);
            direction = player.getLookAngle();
        }
        int splits = bundle.count(AugmentType.SHOTGUN);
        float scale = CastModifiers.scale(config, bundle.count(AugmentType.SIZE_UP), bundle.count(AugmentType.SIZE_DOWN));
        double speed = config.tuning.bundleBaseSpeed * (1 + bundle.count(AugmentType.ACCELERATE) * config.strength(AugmentType.ACCELERATE));
        boolean ethereal = bundle.count(AugmentType.ETHEREAL) > 0;
        CastContext.Landings landings = context.landings() != null ? context.landings() : CastContext.Landings.firstNative();
        float splitFactor = (float) Math.pow(config.strength(AugmentType.SHOTGUN), splits);
        CastContext payloadContext = new CastContext(null, landings, context.damageMultiplier() * splitFactor);

        for (Vec3 heading : ShotgunPattern.directions(direction, splits, config.tuning.shotgunSpreadDegrees)) {
            AugmentBundleEntity projectile = new AugmentBundleEntity(player.level, player, bundle.payload(), payloadContext, scale, ethereal);
            projectile.setPos(start);
            projectile.setDeltaMovement(heading.scale(speed));
            player.level.addFreshEntity(projectile);
            if (bundle.count(AugmentType.ORBIT) > 0) {
                ProjectileSteering.orbit(projectile, player);
            } else if (bundle.count(AugmentType.HOMING) > 0) {
                ProjectileSteering.home(projectile, player);
            }
        }
    }
}
