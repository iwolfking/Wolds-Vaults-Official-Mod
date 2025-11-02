package xyz.iwolfking.woldsvaults.api.core.vault_events.impl;

import com.google.gson.annotations.Expose;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.vault.Vault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.BasicVaultEvent;

public class MessagePlayerVaultEvent extends BasicVaultEvent {
    @Expose
    private final WeightedList<Component> messages;
    protected MessagePlayerVaultEvent(String eventName, String eventDescription, String primaryColor, WeightedList<Component> messages) {
        super(eventName, eventDescription, primaryColor);
        this.messages = messages;
    }

    @Override
    public void triggerEvent(BlockPos pos, ServerPlayer player, Vault vault) {
        player.displayClientMessage(messages.getRandom().get(), false);
        super.triggerEvent(pos, player, vault);
    }
}
