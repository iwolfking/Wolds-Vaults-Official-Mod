package xyz.iwolfking.woldsvaults.modifiers.vault.map.modifiers;

import com.google.gson.annotations.Expose;
import iskallia.vault.block.PlaceholderBlock;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.modifiers.vault.lib.SettableValueVaultModifier;

public class VaultLootableWeightModifierSettable extends SettableValueVaultModifier<VaultLootableWeightModifierSettable.Properties> {
    public VaultLootableWeightModifierSettable(ResourceLocation id, VaultLootableWeightModifierSettable.Properties properties, VaultModifier.Display display) {
        super(id, properties, display);
        this.setDescriptionFormatter((t, p, s) -> {
            return t.formatted((int)Math.abs(p.getValue() * (double)s * 100.0));
        });
    }

    public void initServer(VirtualWorld world, Vault vault, ModifierContext context) {
        CommonEvents.PLACEHOLDER_GENERATION.register(context.getUUID(), (data) -> {
            if (data.getVault() == vault) {
                if (data.getParent().target == ((VaultLootableWeightModifierSettable.Properties)this.properties).type) {
                    data.setProbability(data.getProbability() + ((VaultLootableWeightModifierSettable.Properties)this.properties).getValue(context) * data.getBaseProbability());
                }

            }
        });
    }

    public static class Properties extends SettableValueVaultModifier.Properties{
        @Expose
        private final PlaceholderBlock.Type type;

        public Properties(PlaceholderBlock.Type type) {
            this.type = type;
        }

        public PlaceholderBlock.Type getType() {
            return this.type;
        }

    }
}
