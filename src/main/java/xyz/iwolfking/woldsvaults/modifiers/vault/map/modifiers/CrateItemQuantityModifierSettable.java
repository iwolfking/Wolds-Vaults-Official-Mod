package xyz.iwolfking.woldsvaults.modifiers.vault.map.modifiers;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.vault.objective.AwardCrateObjective;
import iskallia.vault.core.vault.objective.Objectives;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.modifiers.vault.lib.SettableValueVaultModifier;

public class CrateItemQuantityModifierSettable extends SettableValueVaultModifier<SettableValueVaultModifier.Properties> {
    public CrateItemQuantityModifierSettable(ResourceLocation id, SettableValueVaultModifier.Properties properties, VaultModifier.Display display) {
        super(id, properties, display);
        this.setDescriptionFormatter((t, p, s) -> {
            return t.formatted((int)Math.abs(p.getValue() * (float)s * 100.0F));
        });
    }

    public void onVaultAdd(VirtualWorld world, Vault vault, ModifierContext context) {
        ((Objectives)vault.get(Vault.OBJECTIVES)).forEach(AwardCrateObjective.class, (objective) -> {
            objective.set(AwardCrateObjective.ITEM_QUANTITY, (Float)objective.getOr(AwardCrateObjective.ITEM_QUANTITY, 0.0F) + ((SettableValueVaultModifier.Properties)this.properties).getValue());
            return false;
        });
    }
}
