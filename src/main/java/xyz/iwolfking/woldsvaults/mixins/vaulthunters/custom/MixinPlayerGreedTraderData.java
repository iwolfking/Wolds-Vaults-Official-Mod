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

    /**
     * Reprices the shop reroll in greedy tickets instead of reputation.
     *
     * <p>Base charges {@code 3 + resetCount} reputation, which the addon used to cap at 36.
     * Reputation is a finite rank-up currency under the greed rework, so the reroll moves onto
     * greedy tickets at 2, then one more every second reroll. The count this reads is already
     * cleared by the black market's daily tick (see {@code MixinPlayerBlackMarketData}), so the
     * price ladder restarts once a day. The cap is gone with the old formula: at one step per two
     * rerolls a single day would have to see 68 rerolls to reach the old ceiling.</p>
     *
     * <p>Every consumer of this number moves with it: the trader container's {@code resetCost},
     * the restock button's tooltip, and the greed screen's reroll-cost readout.</p>
     */
    @Inject(method = "getResetCost", at = @At("HEAD"), cancellable = true)
    private void chargeRerollInGreedyTickets(UUID playerUuid, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(GreedShopHelper.rerollTicketCost(this.getResetCount(playerUuid)));
    }

    /**
     * Adds the two shop offer types the greed rework's tables need and base cannot express.
     *
     * <p>{@code random_etching} narrows to the etchings that gate on no rank at all, and the new
     * {@code powerful_etching} type takes the rank-gated ones - base draws uniformly from both at
     * once, so the sheet's two separately weighted, separately priced etching rows had no way to
     * exist. {@code xp_burger} is intercepted only to hand the roller the greed tier, which base
     * never passes down, so the Greedy Meal can carry the sheet's {@code 1.15^rank} scaling.</p>
     *
     * <p>All three branches take over the return value completely, including a null result, so a
     * failed roll drops the slot exactly as base would rather than falling through to an
     * unfiltered etching.</p>
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
