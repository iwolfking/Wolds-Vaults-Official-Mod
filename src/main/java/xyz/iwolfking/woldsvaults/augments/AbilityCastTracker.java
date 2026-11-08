package xyz.iwolfking.woldsvaults.augments;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.skill.ability.effect.spi.core.Ability;

import java.util.function.BooleanSupplier;

/**
 * Tells whether a specific ability instance actually cast during a call. VH fires {@code ABILITY_CAST} only after
 * a successful cast, while {@code Ability.onKeyUp} returns true even when the cast itself failed. Server thread only.
 */
public final class AbilityCastTracker {
    private static final Object LISTENER_OWNER = new Object();
    private static Ability watched;
    private static boolean watchedCast;

    private AbilityCastTracker() {
    }

    /** Registers the permanent ABILITY_CAST listener; called once from common setup. */
    public static void register() {
        CommonEvents.ABILITY_CAST.register(LISTENER_OWNER, data -> {
            if (data.getAbility() == watched) {
                watchedCast = true;
            }
        });
    }

    /** Runs the action while watching the ability: the action's own result, and whether the ability cast. */
    public static Outcome watch(Ability ability, BooleanSupplier action) {
        Ability outerWatched = watched;
        boolean outerCast = watchedCast;
        watched = ability;
        watchedCast = false;
        try {
            boolean accepted = action.getAsBoolean();
            return new Outcome(accepted, watchedCast);
        } finally {
            watched = outerWatched;
            watchedCast = outerCast;
        }
    }

    /** Marks a cast whose ABILITY_CAST event was suppressed (Shotgun copies) as cast. */
    public static void note(Ability ability) {
        if (ability == watched) {
            watchedCast = true;
        }
    }

    public record Outcome(boolean accepted, boolean cast) {
    }
}
