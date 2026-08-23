package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.config.greed.GreedTraderConfig;
import iskallia.vault.world.data.PlayerGreedTraderData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.api.util.GreedShopHelper;

import java.util.Random;
import java.util.UUID;

@Mixin(value = PlayerGreedTraderData.class, remap = false)
public abstract class MixinPlayerGreedTraderData {
    @Shadow
    public abstract int getResetCount(UUID playerUuid);

    /** Reprices the shop reroll in greedy tickets: 2, then one more every second reroll, uncapped. */
    @Inject(method = "getResetCost", at = @At("HEAD"), cancellable = true)
    private void chargeRerollInGreedyTickets(UUID playerUuid, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(GreedShopHelper.rerollTicketCost(this.getResetCount(playerUuid)));
    }

    /**
     * Narrows {@code random_etching} to ungated etchings, adds {@code powerful_etching} for the gated
     * ones, and passes the greed tier to {@code xp_burger}. All three take over the return value.
     */
    @Inject(method = "rollSingleOffer", at = @At("HEAD"), cancellable = true)
    private void rollReworkedOfferTypes(GreedTraderConfig.TradeEntry entry,
                                        int greedTier,
                                        GreedTraderConfig config,
                                        Random random,
                                        int playerVaultLevel,
                                        CallbackInfoReturnable<PlayerGreedTraderData.TradeOffer> cir) {
        switch (entry.getType()) {
            case "random_etching" ->
                    cir.setReturnValue(GreedShopHelper.rollEtching(entry, greedTier, config, random, false));
            case "powerful_etching" ->
                    cir.setReturnValue(GreedShopHelper.rollEtching(entry, greedTier, config, random, true));
            case "xp_burger" ->
                    cir.setReturnValue(GreedShopHelper.rollXpBurger(entry, greedTier, config, random));
            default -> {
            }
        }
    }
}
