package xyz.iwolfking.woldsvaults.api.util.ducks;

/**
 * Carries a vault map's god binding and bonus-XP implicit on its crystal; the god is a lowercase name and an empty
 * string means the map has none.
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
