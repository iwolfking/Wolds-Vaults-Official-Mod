package xyz.iwolfking.woldsvaults.api.core.layout;

import iskallia.vault.item.crystal.layout.CrystalLayout;
import xyz.iwolfking.woldsvaults.api.core.layout.lib.LayoutDefinition;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class LayoutDefinitionRegistry {

    private static final Map<String, LayoutDefinition> DEFINITIONS = new LinkedHashMap<>();

    public static void register(LayoutDefinition def) {
        DEFINITIONS.put(def.id(), def);
    }

    public static Optional<LayoutDefinition> get(String id) {
        return Optional.ofNullable(DEFINITIONS.get(id));
    }

    public static Map<String, LayoutDefinition> getDefinitions() {
        return DEFINITIONS;
    }

    public static Optional<LayoutDefinition> getForLayout(CrystalLayout layout) {
        return DEFINITIONS.values().stream()
                .filter(def -> def.supports(layout))
                .findFirst();
    }

}
