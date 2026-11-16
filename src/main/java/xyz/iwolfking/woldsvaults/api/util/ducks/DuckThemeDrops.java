package xyz.iwolfking.woldsvaults.api.util.ducks;

import xyz.iwolfking.woldsvaults.loot.themedrops.ThemeDropInjections;

import javax.annotation.Nullable;

/**
 * Carries the theme drops resolved for one chest on the tiered loot generator that rolls its loot. Null means none
 * and leaves every vanilla code path untouched.
 */
public interface DuckThemeDrops {

    @Nullable
    default ThemeDropInjections getThemeDrops() {
        return null;
    }

    default void setThemeDrops(@Nullable ThemeDropInjections themeDrops) {
    }
}
