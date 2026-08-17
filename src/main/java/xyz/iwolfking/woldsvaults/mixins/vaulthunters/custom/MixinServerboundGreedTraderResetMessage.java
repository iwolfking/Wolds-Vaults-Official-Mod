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
     * Pays for the shop reroll with greedy tickets instead of reputation.
     *
     * <p>Base reads the price off {@code PlayerGreedTraderData.getResetCost}, checks it against the
     * player's greed reputation and deducts from the greed tree. That price is now a ticket count,
     * so the whole handler is replaced rather than patched: reputation is left alone and the
     * tickets come out of the inventory the same way greed coins do, containers included. The
     * reroll counter itself is still base's - {@code resetShop} bumps it and syncs the client -
     * and the black market's daily tick still clears it.</p>
     *
     * <p>Refusing on an unaffordable reroll is logged at debug: the restock button is already
     * disabled client-side when the player cannot pay, so reaching this branch means the client's
     * ticket count and the server's disagree.</p>
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
