package xyz.iwolfking.woldsvaults.modifiers.vault.map.modifiers;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.modifiers.vault.lib.SettableValueVaultModifier;

public class SoulShardChanceModifierSettable extends SettableValueVaultModifier<SettableValueVaultModifier.Properties> {
    public SoulShardChanceModifierSettable(ResourceLocation id, SettableValueVaultModifier.Properties properties, VaultModifier.Display display) {
        super(id, properties, display);
    }

    public void initServer(VirtualWorld world, Vault vault, ModifierContext context) {
        CommonEvents.SOUL_SHARD_CHANCE.register(context.getUUID(), (data) -> {
            if (data.getKiller().level == world) {
                if (!context.hasTarget() || context.getTarget().equals(data.getKiller().getUUID())) {
                    data.setChance(data.getChance() + ((SettableValueVaultModifier.Properties)this.properties).getValue());
                }
            }
        });
    }
}
