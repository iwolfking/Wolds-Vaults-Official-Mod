package xyz.iwolfking.woldsvaults.api.util.ducks;

/**
 * Carries a vault map's god binding and bonus-XP implicit on the crystal it was imprinted onto, the
 * same way {@link DuckMapTier} carries the map tier. The god is stored by its lowercase name and an
 * empty string means "not a god map", which leaves every vanilla code path untouched.
 */
public interface DuckMapGod {

    default String getMapGod() {
        return "";
    }

    default void setMapGod(String god) {
    }

    default int getMapBonusXp() {
        return 0;
    }

    default void setMapBonusXp(int bonusPercent) {
    }
}
