package xyz.iwolfking.woldsvaults.spellcraft.hit;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xyz.iwolfking.woldsvaults.api.cast.CastCarrier;
import xyz.iwolfking.woldsvaults.spellcraft.scope.CastScope;
import xyz.iwolfking.woldsvaults.spellcraft.scope.DeferredReach;
import xyz.iwolfking.woldsvaults.spellcraft.scope.ProjectileGlyphTags;
import xyz.iwolfking.woldsvaults.spellcraft.scope.ShotgunShot;

import javax.annotation.Nullable;
import java.util.BitSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Supplier;

/**
 * Shotgun volleys and hurt immunity. Every cast of a step with Shotgun is one volley, the native cast member 0 and each
 * copy one more member, and a member's hit on an enemy another member of the volley hit within {@link #WINDOW_TICKS}
 * ticks ignores hurt immunity, while a member's own repeated hits keep it. Server thread only.
 */
public final class ShotgunHits {
    private static final int WINDOW_TICKS = 20;
    private static final Map<LivingEntity, Volley> HITS = new WeakHashMap<>();
    private static long nextGroup;
    private static Member explicit;

    private ShotgunHits() {
    }

    private record Member(String spec, ShotgunShot shot, @Nullable Set<CastCarrier> carriers) {
        Member(String spec, ShotgunShot shot) {
            this(spec, shot, null);
        }
    }

    private static final class Volley {
        final long group;
        final BitSet members = new BitSet();
        long lastTick;

        Volley(long group) {
            this.group = group;
        }
    }

    public static long newGroup() {
        return ++nextGroup;
    }

    /** Runs hits dealt outside any cast scope (the Size Up rail sweep) as {@code spec}'s hits by {@code shot}. */
    public static <T> T runAs(String spec, @Nullable ShotgunShot shot, Supplier<T> action) {
        Member outer = explicit;
        explicit = shot == null ? null : new Member(spec, shot);
        try {
            return action.get();
        } finally {
            explicit = outer;
        }
    }

    /** Whether {@code shot}'s own member already hit {@code target} within the window. */
    public static boolean memberHit(LivingEntity target, ShotgunShot shot) {
        Volley volley = HITS.get(target);
        return volley != null && volley.group == shot.group() && target.level.getGameTime() - volley.lastTick <= WINDOW_TICKS
                && volley.members.get(shot.member());
    }

    /** Whether a member of {@code shot}'s volley other than {@code shot}'s own hit {@code target} within the window. */
    public static boolean volleyHit(LivingEntity target, ShotgunShot shot) {
        Volley volley = HITS.get(target);
        if (volley == null || volley.group != shot.group() || target.level.getGameTime() - volley.lastTick > WINDOW_TICKS) {
            return false;
        }
        BitSet others = (BitSet) volley.members.clone();
        others.clear(shot.member());
        return !others.isEmpty();
    }

    @Nullable
    private static ShotgunShot of(ServerPlayer player, DamageSource source) {
        Member member = memberOf(player, source);
        if (member == null || member.shot() == null) {
            return null;
        }
        boolean own = member.carriers() == null ? OwnHits.matchesOwnHit(member.spec(), source)
                : OwnHits.isOwnDeferredHit(member.spec(), source, member.carriers());
        return own ? member.shot() : null;
    }

    @Nullable
    private static Member memberOf(ServerPlayer player, DamageSource source) {
        if (explicit != null) {
            return explicit;
        }
        Entity direct = source.getDirectEntity();
        if (direct instanceof Projectile && ProjectileGlyphTags.shot(direct) != null) {
            return new Member(ProjectileGlyphTags.spec(direct), ProjectileGlyphTags.shot(direct));
        }
        CastScope.Active scope = CastScope.forPlayer(player);
        if (scope != null) {
            return new Member(scope.spec(), scope.shot());
        }
        Projectile ticking = HitAttribution.tickingProjectileOf(player);
        if (ticking != null && ProjectileGlyphTags.shot(ticking) != null) {
            return new Member(ProjectileGlyphTags.spec(ticking), ProjectileGlyphTags.shot(ticking));
        }
        DeferredReach.Reach reach = DeferredReach.forHit(source);
        if (reach != null && reach.glyphs().shot() != null) {
            return new Member(reach.glyphs().spec(), reach.glyphs().shot(), reach.carriers());
        }
        return null;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity target = event.getEntityLiving();
        if (target.level.isClientSide() || !(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ShotgunShot shot = of(player, event.getSource());
        if (shot == null) {
            return;
        }
        if (memberHit(target, shot) || !volleyHit(target, shot)) {
            return;
        }
        target.invulnerableTime = 0;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntityLiving();
        if (target.level.isClientSide() || !(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ShotgunShot shot = of(player, event.getSource());
        if (shot == null) {
            return;
        }
        Volley volley = HITS.get(target);
        if (volley == null || volley.group != shot.group()) {
            volley = new Volley(shot.group());
            HITS.put(target, volley);
        }
        volley.members.set(shot.member());
        volley.lastTick = target.level.getGameTime();
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        HITS.clear();
    }
}
