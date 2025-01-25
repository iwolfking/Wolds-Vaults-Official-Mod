package xyz.iwolfking.woldsvaults.modifiers.vault.map.modifiers;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.resources.ResourceLocation;

public class GreedyVaultModifier extends VaultModifier<GreedyVaultModifier.Properties> {
    public GreedyVaultModifier(ResourceLocation id, Properties properties, Display display) {
        super(id, properties, display);
    }

    public void initServer(VirtualWorld world, Vault vault, ModifierContext context) {
    }

    public static class Properties {

        public Properties() {

        }

    }
}
