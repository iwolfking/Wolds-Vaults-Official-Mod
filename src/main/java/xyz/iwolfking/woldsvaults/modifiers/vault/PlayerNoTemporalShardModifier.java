package xyz.iwolfking.woldsvaults.modifiers.vault;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.resources.ResourceLocation;

public class PlayerNoTemporalShardModifier extends VaultModifier<PlayerNoTemporalShardModifier.Properties> {
    public PlayerNoTemporalShardModifier(ResourceLocation id, Properties properties, Display display) {
        super(id, properties, display);
    }

    public static class Properties {
    }

    @Override
    public void initServer(VirtualWorld world, Vault vault, ModifierContext context) {
        CommonEvents.VAULT_MODIFIER_ITEM_USE.register(context.getUUID(), data -> {
            data.setCancelled(true);
        });
    }
}
