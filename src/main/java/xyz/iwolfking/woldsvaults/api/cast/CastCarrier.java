package xyz.iwolfking.woldsvaults.api.cast;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Set;

/**
 * What delivers a hit relative to the cast that caused it. CAST and PROJECTILE hits are reached through the cast scope
 * and projectile tags. Every other carrier is deferred, and a spell step's deferred glyphs reach only the hit sources
 * its delivering carrier {@link #reaches}.
 */
public enum CastCarrier {
    CAST,
    PROJECTILE,
    ENTITY,
    BLOCK_ENTITY,
    SCHEDULED,
    DOT,
    MINION,
    TOGGLE,
    EVENT;

    /** The hit sources a deferred carrier delivers, or null for CAST and PROJECTILE, which reach any source. */
    @Nullable
    public Set<CastCarrier> reaches() {
        return switch (this) {
            case CAST, PROJECTILE -> null;
            case ENTITY -> EnumSet.of(ENTITY, MINION);
            default -> EnumSet.of(this);
        };
    }
}
