package xyz.iwolfking.woldsvaults.abilities;

import java.util.List;

public final class WoldsAbilityGroups {
    public static final String LIFE_BLOCK = "Life_Block";

    /**
     * Ability groups this mod adds through datagen. They join the ability tree after client setup,
     * so the_vault never creates their quickfire keybinds; MixinModKeybinds registers them instead.
     */
    public static final List<String> DATAGEN_GROUPS = List.of(LIFE_BLOCK);

    private WoldsAbilityGroups() {
    }
}
