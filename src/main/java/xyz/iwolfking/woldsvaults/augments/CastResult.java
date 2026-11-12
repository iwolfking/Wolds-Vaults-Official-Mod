package xyz.iwolfking.woldsvaults.augments;

import javax.annotation.Nullable;

/**
 * Outcome of casting one ability step of a recipe. Every value except SUCCESS, STARTED and STOPPED tells the
 * player why the step did nothing.
 */
public enum CastResult {
    SUCCESS(null),
    STARTED(null),
    STOPPED(null),
    NOT_LEARNED("%s is not learned"),
    ON_COOLDOWN("%s is on cooldown"),
    BLOCKED("%s can't be cast right now"),
    FAILED("%s failed to cast"),
    UNSUPPORTED("%s can't be cast this way"),
    DISABLED("%s is disabled for augments"),
    SKIPPED(null);

    private final String message;

    CastResult(@Nullable String message) {
        this.message = message;
    }

    /** Action-bar text taking the ability name, or null when the result needs no message. */
    @Nullable
    public String message() {
        return this.message;
    }

    /** Whether the native cast went through, so later steps that depend on it may run. */
    public boolean went() {
        return this == SUCCESS || this == STARTED || this == STOPPED;
    }
}
