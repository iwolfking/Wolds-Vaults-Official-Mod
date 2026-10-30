package xyz.iwolfking.woldsvaults.api.util.ducks;

/**
 * Carries a vault map tier (stored 0-5, display 1-6) on objects that don't natively know it:
 * the crystal data it is imprinted on at anvil craft, and the strongbox loot generator that
 * scales by it. -1 means "no tier" and leaves every vanilla code path untouched.
 */
public interface DuckMapTier {

    default int getMapTier() {
        return -1;
    }

    default void setMapTier(int tier) {
    }
}
