package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.world.data.PlayerBlackMarketData;
import iskallia.vault.world.data.PlayerGreedTraderData;
import iskallia.vault.world.data.PlayerGreedTreeData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PlayerBlackMarketData.BlackMarket.class, remap = false)
public class MixinPlayerBlackMarketData {
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Liskallia/vault/world/data/PlayerBlackMarketData$BlackMarket;resetTrades(Ljava/util/UUID;)V", shift = At.Shift.BEFORE))
    private void resetGreedShop(MinecraftServer server, ServerPlayer player, CallbackInfo ci) {
        if(PlayerGreedTreeData.get(player.getLevel()).getGreedTier(player) > 0) {
            PlayerGreedTraderData.get(player.getLevel()).rerollOffers(player);
            PlayerGreedTraderData.get(player.getLevel()).resetPenaltyOnTierUp(player.getUUID());
        }
    }
}
