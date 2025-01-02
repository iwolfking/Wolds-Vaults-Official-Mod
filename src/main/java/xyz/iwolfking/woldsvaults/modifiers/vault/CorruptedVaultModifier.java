package xyz.iwolfking.woldsvaults.modifiers.vault;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import net.minecraft.resources.ResourceLocation;

public class CorruptedVaultModifier extends VaultModifier<CorruptedVaultModifier.Properties> {

    public CorruptedVaultModifier(ResourceLocation id, CorruptedVaultModifier.Properties properties, Display display) {
        super(id, properties, display);
    }

    // dummy for now.
    public static class Properties {
        private final boolean active;
        public Properties(boolean isActive) {
            this.active = isActive;
        }

    }
}