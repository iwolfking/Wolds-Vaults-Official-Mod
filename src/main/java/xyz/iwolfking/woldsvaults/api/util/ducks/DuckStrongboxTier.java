package xyz.iwolfking.woldsvaults.api.util.ducks;

/**
 * Carries a strongbox tier (stored 0-5, displayed 1-6) on the loot generator that rolls a strongbox's loot.
 * -1 means "no tier" and leaves every vanilla code path untouched.
 */
public interface DuckStrongboxTier {

    default int getStrongboxTier() {
        return -1;
    }

    default void setStrongboxTier(int tier) {
    }
}
