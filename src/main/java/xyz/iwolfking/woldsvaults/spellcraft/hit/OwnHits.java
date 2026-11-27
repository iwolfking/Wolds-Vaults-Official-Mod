package xyz.iwolfking.woldsvaults.spellcraft.hit;

import iskallia.vault.event.ActiveFlags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import xyz.iwolfking.woldsvaults.api.cast.CastCarrier;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.events.WoldActiveFlags;
import xyz.iwolfking.woldsvaults.spellcraft.hit.HitSources.Kind;
import xyz.iwolfking.woldsvaults.spellcraft.hit.HitSources.Source;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Which hurt events are an ability's own hits, so damage glyphs multiply those and nothing else (no on-hit procs,
 * lucky-hit extras or etching side hits). Each spec lists its hit sources as VH deals them ({@link HitSources}). A hit
 * matches when its damage source id, direct entity, VH flags, WV flags, carrier and nesting all agree.
 */
public final class OwnHits {
    private static final Set<ActiveFlags> LEAKY_FLAGS = EnumSet.of(ActiveFlags.IS_THORNS_REFLECTING);
    private static final Set<WoldActiveFlags> PROC_WV_FLAGS = EnumSet.of(WoldActiveFlags.IS_REAVING_ATTACKING,
            WoldActiveFlags.IS_ECHOING_ATTACKING, WoldActiveFlags.IS_PROC_FANG_ATTACKING, WoldActiveFlags.IS_DERIVED_ATTACKING,
            WoldActiveFlags.IS_LIGHTNING_FIEND_ATTACKING, WoldActiveFlags.IS_SCEPTER_CAST_ATTACKING, WoldActiveFlags.IS_TRUE_DAMAGE);

    private static final Set<String> UNKNOWN_WARNED = ConcurrentHashMap.newKeySet();
    private static final Set<String> SKIP_NOTED = ConcurrentHashMap.newKeySet();
    private static final ThreadLocal<int[]> HURT_DEPTH = ThreadLocal.withInitial(() -> new int[1]);

    private OwnHits() {
    }

    /**
     * Whether this hurt is one of {@code spec}'s own hits. A null spec (a Bundled Spell orb's step) has none, and a
     * spec the table lacks gets no glyph damage and logs an error once.
     */
    public static boolean isOwnHit(@Nullable String spec, DamageSource source) {
        if (matchesOwnHit(spec, source)) {
            return true;
        }
        if (spec != null && HitSources.has(spec)) {
            String key = spec + "|" + signature(source);
            if (SKIP_NOTED.add(key)) {
                WoldLogger.info("Spell hits: a {} hit inside {}'s cast is not one of its own hits; damage glyphs skip it.", signature(source), spec);
            }
        }
        return false;
    }

    /**
     * Whether this hurt, dealt later through a deferred carrier, is one of {@code spec}'s own hits: like {@link
     * #isOwnHit} but only sources {@code carriers} delivers count. Procs that share an own hit's signature but run
     * through another carrier (Chain Lightning's delayed strike is scheduled, its own hit is a projectile's) stay out.
     */
    public static boolean isOwnDeferredHit(@Nullable String spec, DamageSource source, Set<CastCarrier> carriers) {
        return matchesOwnHit(spec, source, carriers);
    }

    /** Whether {@code spec} has any hit source one of {@code carriers} delivers (so area reads there are its own). */
    public static boolean hasSourceVia(@Nullable String spec, Set<CastCarrier> carriers) {
        List<Source> sources = spec == null ? null : HitSources.sources(spec);
        if (sources == null) {
            return false;
        }
        for (Source candidate : sources) {
            if (carriers.contains(candidate.carrier())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether this hurt is a DIRECT own hit of {@code spec} (one target the ability or its projectile struck), counting
     * only sources {@code carriers} delivers when given. Chaining starts from these hits only.
     */
    public static boolean isOwnDirectHit(@Nullable String spec, DamageSource source, @Nullable Set<CastCarrier> carriers) {
        List<Source> sources = spec == null ? null : HitSources.sources(spec);
        if (sources == null) {
            return false;
        }
        for (Source candidate : sources) {
            if (candidate.kind() == Kind.DIRECT && (carriers == null || carriers.contains(candidate.carrier())) && matches(candidate, source)) {
                return true;
            }
        }
        return false;
    }

    /** {@link #isOwnHit} without the log of skipped hits. */
    static boolean matchesOwnHit(@Nullable String spec, DamageSource source) {
        return matchesOwnHit(spec, source, null);
    }

    private static boolean matchesOwnHit(@Nullable String spec, DamageSource source, @Nullable Set<CastCarrier> carriers) {
        if (spec == null) {
            return false;
        }
        List<Source> sources = HitSources.sources(spec);
        if (sources == null) {
            if (UNKNOWN_WARNED.add(spec)) {
                WoldLogger.error("Spell hits: ability {} has no hit-source entry; damage glyphs skip its hits until it is added to HitSources.", spec);
            }
            return false;
        }
        for (Source candidate : sources) {
            if ((carriers == null || carriers.contains(candidate.carrier())) && matches(candidate, source)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matches(Source candidate, DamageSource source) {
        if (!candidate.nested() && HURT_DEPTH.get()[0] > 1) {
            return false;
        }
        if (candidate.msgId() != null && !candidate.msgId().equals(source.getMsgId())) {
            return false;
        }
        Entity direct = source.getDirectEntity();
        if (candidate.direct() == null ? direct != source.getEntity() : !candidate.direct().isInstance(direct)) {
            return false;
        }
        for (ActiveFlags flag : ActiveFlags.values()) {
            boolean wanted = candidate.flags().contains(flag);
            if (flag.isSet() != wanted && !(flag.isSet() && LEAKY_FLAGS.contains(flag))) {
                return false;
            }
        }
        for (WoldActiveFlags flag : WoldActiveFlags.values()) {
            if (candidate.wvFlags().contains(flag) ? !flag.isSet() : flag.isSet() && PROC_WV_FLAGS.contains(flag)) {
                return false;
            }
        }
        return true;
    }

    private static String signature(DamageSource source) {
        StringBuilder flags = new StringBuilder();
        for (ActiveFlags flag : ActiveFlags.values()) {
            if (flag.isSet()) {
                flags.append(flags.length() == 0 ? "" : ",").append(flag.name());
            }
        }
        for (WoldActiveFlags flag : WoldActiveFlags.values()) {
            if (flag.isSet()) {
                flags.append(flags.length() == 0 ? "" : ",").append(flag.name());
            }
        }
        Entity direct = source.getDirectEntity();
        return source.getMsgId() + "/" + (direct == null ? "none" : direct.getClass().getSimpleName()) + "/{" + flags + "}/depth " + HURT_DEPTH.get()[0];
    }

    /**
     * Called around every {@code LivingEntity.hurt}: hurts nested inside another are procs unless a source says
     * otherwise.
     */
    public static void enterHurt() {
        HURT_DEPTH.get()[0]++;
    }

    public static void exitHurt() {
        int[] depth = HURT_DEPTH.get();
        depth[0] = Math.max(0, depth[0] - 1);
    }
}
