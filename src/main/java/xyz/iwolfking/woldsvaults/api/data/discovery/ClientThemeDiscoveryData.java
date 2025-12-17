package xyz.iwolfking.woldsvaults.api.data.discovery;

import iskallia.vault.util.function.ObservableSupplier;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public class ClientThemeDiscoveryData {
    private static final Set<ResourceLocation> discoveredThemes = new HashSet<>();

    public static Set<ResourceLocation> getDiscoveredThemes() {
        return new HashSet<>(discoveredThemes);
    }

    public static ObservableSupplier<Set<ResourceLocation>> getObserverThemes() {
        return ObservableSupplier.of(
                ClientThemeDiscoveryData::getDiscoveredThemes, (modelSet, newModelSet) -> modelSet.size() == newModelSet.size()
        );
    }

    public static void receiveMessage(Set<ResourceLocation> themes) {
        discoveredThemes.clear();
        discoveredThemes.addAll(themes);
    }
}
