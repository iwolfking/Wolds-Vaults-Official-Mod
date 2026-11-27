package xyz.iwolfking.woldsvaults.mixins.vaulthunters.multicell;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.WorldManager;
import iskallia.vault.core.vault.objective.ArchitectObjective;
import iskallia.vault.core.world.generator.GridGenerator;
import iskallia.vault.core.world.generator.VaultGenerator;
import iskallia.vault.core.world.generator.layout.VaultGridLayout;
import iskallia.vault.core.world.storage.VirtualWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.core.layout.multicell.MultiCellPresets;

@Mixin(value = ArchitectObjective.class, remap = false)
public abstract class MixinArchitectObjectiveMultiCell {

    /** Room inscriptions that name a multi-cell pool become multi-cell rooms grown from the cell the preset picked. */
    @Inject(method = "initServer", at = @At("RETURN"))
    private void woldsvaults$applyMultiCellInscriptions(VirtualWorld world, Vault vault, CallbackInfo ci) {
        VaultGenerator generator = vault.get(Vault.WORLD).get(WorldManager.GENERATOR);
        if (generator instanceof GridGenerator grid && grid.get(GridGenerator.LAYOUT) instanceof VaultGridLayout layout) {
            ArchitectObjective self = (ArchitectObjective) (Object) this;
            if (self.has(ArchitectObjective.PRESET)) {
                MultiCellPresets.apply(vault, layout, self.get(ArchitectObjective.PRESET), "room inscription");
            }
        }
    }
}
