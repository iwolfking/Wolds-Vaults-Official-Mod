package xyz.iwolfking.woldsvaults.api.core.vault_events.impl;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.player.Listener;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.objectives.data.EnchantedEventsRegistry;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.BasicVaultEvent;

import java.util.Iterator;
import java.util.Random;

public class PlayerSwapVaultEvent extends BasicVaultEvent {
    public PlayerSwapVaultEvent(String eventName, String eventDescription, String primaryColor) {
        super(eventName, eventDescription, primaryColor);
    }

    @Override
    public void triggerEvent(BlockPos pos, ServerPlayer player, Vault vault) {
        Random random = new Random();
        Iterator playerIterator = vault.get(Vault.LISTENERS).getAll().iterator();
        boolean fired = false;

        while(playerIterator.hasNext()) {
            Listener listener = (Listener) playerIterator.next();
            ServerPlayer target = listener.getPlayer().get();
            if(target.equals(player)) {
                continue;
            }
            else {
                if(playerIterator.hasNext()) {
                    if(random.nextBoolean()) {
                        fired = true;
                       teleSwap(pos, player, target);
                    }
                }
                else {
                    fired = true;
                    teleSwap(pos, player, target);
                }
            }
        }
        if(fired) {
            super.triggerEvent(pos, player, vault);
        }
        else {
            EnchantedEventsRegistry.getEvents().getRandom().ifPresent(basicEnchantedEvent -> {
                basicEnchantedEvent.triggerEvent(pos, player, vault);
            });
        }

    }

    private void teleSwap(BlockPos pos, ServerPlayer player, ServerPlayer target) {
        player.teleportTo(target.getX(), target.getY(), target.getZ());
        target.teleportTo(pos.getX(), pos.getY(), pos.getZ());
    }
}
