package xyz.iwolfking.woldsvaults.modifiers.vault.map.modifiers;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.modifiers.vault.lib.SettableValueVaultModifier;

public class ChampionChanceModifierSettable extends SettableValueVaultModifier<SettableValueVaultModifier.Properties> {
    public ChampionChanceModifierSettable(ResourceLocation id, SettableValueVaultModifier.Properties properties, VaultModifier.Display display) {
        super(id, properties, display);
    }

    public void initServer(VirtualWorld world, Vault vault, ModifierContext context) {
        CommonEvents.CHAMPION_PROMOTE.register(context.getUUID(), (data) -> {
            if (data.getEntity().level == world) {
                data.setProbability(data.getProbability() + (double)((SettableValueVaultModifier.Properties)this.properties).getValue());
            }
        });
    }
}
