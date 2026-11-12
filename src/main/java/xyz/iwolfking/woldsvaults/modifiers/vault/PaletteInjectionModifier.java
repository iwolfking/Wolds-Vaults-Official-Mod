package xyz.iwolfking.woldsvaults.modifiers.vault;

import com.google.gson.annotations.Expose;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PaletteInjectionModifier extends VaultModifier<PaletteInjectionModifier.Properties> {

    public PaletteInjectionModifier(ResourceLocation id, PaletteInjectionModifier.Properties properties, VaultModifier.Display display) {
        super(id, properties, display);
    }

    public static class Properties {
        @Expose
        private final List<String> pregenPalettes;

        @Expose
        private final List<String> postgenPalettes;

        public Properties(List<String> pregenPalettes, List<String> postgenPalettes) {
            this.pregenPalettes = pregenPalettes;
            this.postgenPalettes = postgenPalettes;
        }

        public Set<ResourceLocation> getPregenPalettes() {
            if (pregenPalettes == null) {
                return Collections.emptySet();
            }
            return pregenPalettes.stream()
                    .map((ResourceLocation::parse))
                    .collect(Collectors.toSet());
        }

        public Set<ResourceLocation> getPostgenPalettes() {
            if (postgenPalettes == null) {
                return Collections.emptySet();
            }
            return postgenPalettes.stream()
                    .map(ResourceLocation::parse)
                    .collect(Collectors.toSet());
        }
    }
}