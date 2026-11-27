package xyz.iwolfking.woldsvaults.spellcraft.cast;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.spellcraft.ability.CastOrigin;
import xyz.iwolfking.woldsvaults.spellcraft.projectile.BundledSpellOrb;
import xyz.iwolfking.woldsvaults.spellcraft.projectile.ProjectileSteering;
import xyz.iwolfking.woldsvaults.spellcraft.projectile.ShotgunPattern;
import xyz.iwolfking.woldsvaults.spellcraft.scope.CastModifiers;
import xyz.iwolfking.woldsvaults.spellcraft.spell.SpellModel;

import javax.annotation.Nullable;

/**
 * Throws a Bundled Spell from the player, or from a landing point for a bundle inside another bundle's payload. The
 * glyphs right after it modify the orb, and Shotgun throws one orb per copy, each carrying the whole payload at the
 * copies' damage. The orb flies for the Bundled Spell's rolled flight time, or lands its payload where it is when its
 * orbit ends.
 */
public final class BundleLauncher {
    private BundleLauncher() {
    }

    public static void launch(SpellcraftConfig config, ServerPlayer player, SpellModel.Bundle bundle, @Nullable CastOrigin origin,
                              float damageFactor) {
        Vec3 start = origin != null ? origin.position() : player.getEyePosition().subtract(0, 0.1, 0);
        Vec3 direction = SpellCaster.baseDirection(player, origin);
        CastModifiers orb = CastModifiers.of(config, bundle.glyphs());
        double speed = config.tuning.bundleSpeed * orb.speed();
        int lifetime = Math.max(1, Math.round(bundle.glyph().potency() * 20.0F));
        float payloadDamage = damageFactor * orb.damage();
        for (Vec3 heading : ShotgunPattern.fan(direction, orb.shotgunCopies(), config.tuning.shotgunSpreadDegrees)) {
            BundledSpellOrb projectile = new BundledSpellOrb(player.level, player, bundle.payload(), payloadDamage, lifetime,
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
