package xyz.iwolfking.woldsvaults.spellcraft.hit;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import xyz.iwolfking.woldsvaults.spellcraft.scope.ProjectileGlyphTags;
import xyz.iwolfking.woldsvaults.spellcraft.scope.TickingEntity;

import javax.annotation.Nullable;

public final class HitAttribution {
    private HitAttribution() {
    }

    /** The glyph-tagged projectile of {@code owner} whose tick is running right now, or null. */
    @Nullable
    static Projectile tickingProjectileOf(Entity owner) {
        return TickingEntity.current() instanceof Projectile projectile && projectile.getOwner() == owner
                && ProjectileGlyphTags.isTagged(projectile) ? projectile : null;
    }
}
