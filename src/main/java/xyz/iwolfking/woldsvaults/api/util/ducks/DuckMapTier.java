package xyz.iwolfking.woldsvaults.api.util.ducks;

/**
 * Carries a vault map tier (stored 0-5, display 1-6) on the crystal data it is imprinted on at
 * anvil craft. -1 means "no tier".
 */
public interface DuckMapTier {

    default int getMapTier() {
        return -1;
    }

    default void setMapTier(int tier) {
    }
}
