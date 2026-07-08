package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.bingo.BingoCard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective;

@Mixin(value = BingoCard.class, remap = false)
public class MixinBingoCard {
    // Vanilla rolls a random vault modifier (crate tiers included) from the card's modifier
    // pool for every completed bingo line; in a Hyper vault lines must stay reward-free.
    @Inject(method = "addModifiersToVault", at = @At("HEAD"), cancellable = true)
    private void woldsVaults$skipLineModifiersInHyperVaults(Vault vault, int newlyCompletedBingos, CallbackInfo ci) {
        if (!vault.get(Vault.OBJECTIVES).getAll(HyperVaultObjective.class).isEmpty()) {
            WoldsVaults.LOGGER.info("Suppressed {} bingo-line modifier roll(s) — Hyper lines award nothing.", newlyCompletedBingos);
            ci.cancel();
        }
    }
}
