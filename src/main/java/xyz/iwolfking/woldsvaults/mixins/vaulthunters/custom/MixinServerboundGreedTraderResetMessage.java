package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.container.GreedTraderContainer;
import iskallia.vault.network.message.ServerboundGreedTraderResetMessage;
import iskallia.vault.world.data.PlayerGreedTraderData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.GreedShopHelper;

import java.util.function.Supplier;

@Mixin(value = ServerboundGreedTraderResetMessage.class, remap = false)
public class MixinServerboundGreedTraderResetMessage {
    /**
     * Pays for the shop reroll with greedy tickets instead of reputation, taken from the inventory the
     * way greed coins are, containers included. An unaffordable reroll is refused and logged at debug.
     */
    @Inject(method = "handle", at = @At("HEAD"), cancellable = true)
    private static void payRerollWithGreedyTickets(ServerboundGreedTraderResetMessage message,
                                                   Supplier<NetworkEvent.Context> contextSupplier,
                                                   CallbackInfo ci) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender == null || !(sender.containerMenu instanceof GreedTraderContainer)) {
                return;
            }
            PlayerGreedTraderData traderData = PlayerGreedTraderData.get(sender.getLevel());
            int resetCost = traderData.getResetCost(sender.getUUID());
            if (!GreedShopHelper.consumeGreedyTickets(sender, resetCost)) {
                WoldsVaults.LOGGER.debug("Refused greed shop reroll for {}: {} greedy tickets required, {} held",
                        sender.getGameProfile().getName(), resetCost, GreedShopHelper.countGreedyTickets(sender));
                traderData.syncToClient(sender);
                return;
            }
            traderData.resetShop(sender);
        });
        context.setPacketHandled(true);
        ci.cancel();
    }
}
