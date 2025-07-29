package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.difficulty_lock;

import iskallia.vault.VaultMod;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.vault.objective.CakeObjective;
import iskallia.vault.core.world.storage.VirtualWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.helper.NormalizedHelper;
import xyz.iwolfking.woldsvaults.util.VaultModifierUtils;

@Mixin(value = CakeObjective.class, remap = false)
public class MixinCakeObjective {
    @Inject(method = "initServer", at = @At("HEAD"))
    private void addNormalizedToCakeVaults(VirtualWorld world, Vault vault, CallbackInfo ci) {
        NormalizedHelper.handleAddingNormalizedToVault(vault, world.getLevel());
    }
}
