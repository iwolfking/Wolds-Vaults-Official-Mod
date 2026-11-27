package xyz.iwolfking.woldsvaults.spellcraft.hit;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xyz.iwolfking.woldsvaults.spellcraft.scope.CastScope;
import xyz.iwolfking.woldsvaults.spellcraft.scope.ChainScope;
import xyz.iwolfking.woldsvaults.spellcraft.scope.DeferredReach;
import xyz.iwolfking.woldsvaults.spellcraft.scope.ProjectileGlyphTags;

public final class GlyphDamage {
    private GlyphDamage() {
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (ChainScope.active()) {
            return;
        }
        Entity direct = event.getSource().getDirectEntity();
        Entity attacker = event.getSource().getEntity();
        float multiplier = 1.0F;
        String spec = null;
        if (direct instanceof Projectile && direct.getPersistentData().contains(ProjectileGlyphTags.DAMAGE)) {
            multiplier = ProjectileGlyphTags.damage(direct);
            spec = ProjectileGlyphTags.spec(direct);
        } else if (attacker != null) {
            CastScope.Active scope = CastScope.forPlayer(attacker);
            if (scope != null) {
                multiplier = scope.modifiers().damage();
                spec = scope.spec();
            } else {
                Projectile ticking = HitAttribution.tickingProjectileOf(attacker);
                if (ticking != null) {
                    multiplier = ProjectileGlyphTags.damage(ticking);
                    spec = ProjectileGlyphTags.spec(ticking);
                }
            }
        }
        if (spec == null) {
            DeferredReach.Reach reach = DeferredReach.forHit(event.getSource());
            if (reach != null && reach.glyphs().modifiers().damage() != 1.0F
                    && OwnHits.isOwnDeferredHit(reach.glyphs().spec(), event.getSource(), reach.carriers())) {
                event.setAmount(event.getAmount() * reach.glyphs().modifiers().damage());
            }
            return;
        }
        if (multiplier != 1.0F && OwnHits.isOwnHit(spec, event.getSource())) {
            event.setAmount(event.getAmount() * multiplier);
        }
    }
}
