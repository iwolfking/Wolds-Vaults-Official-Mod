package xyz.iwolfking.woldsvaults.api.core.vault_events.impl.tasks;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.player.Listener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.VaultEventTask;

import java.util.Iterator;
import java.util.Random;
import java.util.function.Supplier;

public class PlayerSwapTask implements VaultEventTask {
    @Override
    public void performTask(Supplier<BlockPos> pos, ServerPlayer player, Vault vault) {
        Random random = new Random();

        Iterator<Listener> playerIterator = vault.get(Vault.LISTENERS).getAll().iterator();

        while(playerIterator.hasNext()) {
            Listener listener = (Listener) playerIterator.next();
            ServerPlayer target = listener.getPlayer().get();
            if(target.equals(player)) {
                continue;
            }
            else {
                if(playerIterator.hasNext()) {
                    if(random.nextBoolean()) {
                        teleSwap(pos.get(), player, target);
                    }
                }
                else {
                    teleSwap(pos.get(), player, target);
                }
            }
        }
    }

    private void teleSwap(BlockPos pos, ServerPlayer player, ServerPlayer target) {
        player.teleportTo(target.getX(), target.getY(), target.getZ());
        sendSwapMessage(player, target);
        target.teleportTo(pos.getX(), pos.getY(), pos.getZ());
        sendSwapMessage(target, player);
    }

    private void sendSwapMessage(ServerPlayer player, ServerPlayer target) {
        player.displayClientMessage(new TextComponent("You have been swapped with ").append(target.getDisplayName()), false);
    }
}
