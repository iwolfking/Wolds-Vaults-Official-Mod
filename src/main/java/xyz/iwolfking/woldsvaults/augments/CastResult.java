package xyz.iwolfking.woldsvaults.augments;

import javax.annotation.Nullable;

/**
 * Outcome of casting one ability step of a recipe. Every value except SUCCESS, STARTED, STOPPED, SKIPPED and FAILED
 * tells the player why the step did nothing; ON_COOLDOWN and FAILED (usually too little mana) only play the cooldown sound.
 */
public enum CastResult {
    SUCCESS(null),
    STARTED(null),
    STOPPED(null),
    NOT_LEARNED("%s is not learned"),
    ON_COOLDOWN("%s is on cooldown"),
    BLOCKED("%s can't be cast right now"),
    FAILED(null),
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
