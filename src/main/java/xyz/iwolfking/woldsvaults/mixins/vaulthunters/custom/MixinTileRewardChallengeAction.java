package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.challenge.action.TileRewardChallengeAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = TileRewardChallengeAction.class, remap = false)
public class MixinTileRewardChallengeAction {

    @ModifyArg(
        method = "onActivate",
        at = @At(
            value = "INVOKE",
            target = "Liskallia/vault/core/world/processor/ProcessorContext;<init>(Liskallia/vault/core/vault/Vault;Liskallia/vault/core/random/RandomSource;)V"
        ),
        index = 0
    )
    private Vault injectVaultContext(Vault nullVault, @Local(name = "theVault") Vault theVault) {
        return theVault;
    }
}