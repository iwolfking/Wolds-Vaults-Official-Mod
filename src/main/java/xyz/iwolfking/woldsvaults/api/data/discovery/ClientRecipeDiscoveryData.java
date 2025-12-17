package xyz.iwolfking.woldsvaults.api.data.discovery;

import iskallia.vault.util.function.ObservableSupplier;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public class ClientRecipeDiscoveryData {
    private static final Set<ResourceLocation> discoveredRecipes = new HashSet<>();

    public static Set<ResourceLocation> getDiscoveredRecipes() {
        return new HashSet<>(discoveredRecipes);
    }

    public static ObservableSupplier<Set<ResourceLocation>> getObserverThemes() {
        return ObservableSupplier.of(
                ClientRecipeDiscoveryData::getDiscoveredRecipes, (modelSet, newModelSet) -> modelSet.size() == newModelSet.size()
        );
    }

    public static void receiveMessage(Set<ResourceLocation> recipes) {
        discoveredRecipes.clear();
        discoveredRecipes.addAll(recipes);
    }
}
