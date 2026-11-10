package xyz.iwolfking.woldsvaults.augments.cast;

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
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.augments.effect.AugmentTags;
import xyz.iwolfking.woldsvaults.augments.effect.SpellHits;
import xyz.iwolfking.woldsvaults.augments.effect.TickingEntity;

import javax.annotation.Nullable;
import java.util.BitSet;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

/**
 * Shotgun volleys and hurt immunity. Every cast of a step with Shotgun is one volley: the native cast is member 0 and
 * each copy one more member. When a member hits an enemy that another member of the same volley hit within the last
 * {@link #WINDOW_TICKS} ticks, its hit ignores the enemy's hurt immunity, so every copy can land on the same enemy on
 * the same tick. A member's own repeated hits on one enemy keep normal hurt immunity. Server thread only.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class ShotgunHits {
    private static final int WINDOW_TICKS = 20;
    private static final Map<LivingEntity, Volley> HITS = new WeakHashMap<>();
    private static long nextGroup;
    private static Member explicit;

    private ShotgunHits() {
    }

    /** One member of a Shotgun volley: {@code member} 0 is the native cast, 1 and up the copies. */
    public record Shot(long group, int member) {
    }

    private record Member(String spec, Shot shot) {
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
    public static <T> T runAs(String spec, @Nullable Shot shot, Supplier<T> action) {
        Member outer = explicit;
        explicit = shot == null ? null : new Member(spec, shot);
        try {
            return action.get();
        } finally {
            explicit = outer;
        }
    }

    /** Whether {@code shot}'s own member already hit {@code target} within the window. */
    public static boolean memberHit(LivingEntity target, Shot shot) {
        Volley volley = HITS.get(target);
        return volley != null && volley.group == shot.group() && target.level.getGameTime() - volley.lastTick <= WINDOW_TICKS
                && volley.members.get(shot.member());
    }

    /** The volley member an own hit of a Shotgun cast belongs to; null for any other hit, procs included. */
    /** Whether a member of {@code shot}'s volley other than {@code shot}'s own hit {@code target} within the window. */
    public static boolean volleyHit(LivingEntity target, Shot shot) {
        Volley volley = HITS.get(target);
        if (volley == null || volley.group != shot.group() || target.level.getGameTime() - volley.lastTick > WINDOW_TICKS) {
            return false;
        }
        BitSet others = (BitSet) volley.members.clone();
        others.clear(shot.member());
        return !others.isEmpty();
    }

    @Nullable
    private static Shot of(ServerPlayer player, DamageSource source) {
        Member member = memberOf(player, source);
        return member != null && member.shot() != null && SpellHits.matchesOwnHit(member.spec(), source) ? member.shot() : null;
    }

    @Nullable
    private static Member memberOf(ServerPlayer player, DamageSource source) {
        if (explicit != null) {
            return explicit;
        }
        Entity direct = source.getDirectEntity();
        if (direct instanceof Projectile && AugmentTags.shot(direct) != null) {
            return new Member(AugmentTags.spec(direct), AugmentTags.shot(direct));
        }
        CastScope.Active scope = CastScope.current().filter(active -> active.player() == player).orElse(null);
        if (scope != null) {
            return new Member(scope.spec(), scope.shot());
        }
        Entity ticking = TickingEntity.current();
        if (ticking instanceof Projectile projectile && projectile.getOwner() == player && AugmentTags.shot(projectile) != null) {
            return new Member(AugmentTags.spec(projectile), AugmentTags.shot(projectile));
        }
        return null;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity target = event.getEntityLiving();
        if (target.level.isClientSide() || !(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        Shot shot = of(player, event.getSource());
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
        Shot shot = of(player, event.getSource());
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
