package xyz.iwolfking.woldsvaults.api.core.vault_events.impl.tasks;

import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.player.Listener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import xyz.iwolfking.woldsvaults.api.core.vault_events.VaultEvent;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.VaultEventTask;

import java.util.function.Supplier;

public class MessageTask implements VaultEventTask {

    private final VaultEvent.EventDisplayType displayType;
    private final TextComponent message;

    public MessageTask(VaultEvent.EventDisplayType displayType, TextComponent message) {
        this.displayType = displayType;
        this.message = message;
    }


    @Override
    public void performTask(Supplier<BlockPos> pos, ServerPlayer player, Vault vault) {
        sendEventMessages(vault, player);
    }
    public void sendEventMessages(Vault vault, ServerPlayer originator) {
        switch (displayType) {
            case NONE -> {
                return;
            }
            case ACTION_BAR -> handleActionBarMessage(originator);
            case CHAT_MESSAGE_TARGET -> handleChatMessage(originator, vault, false);
            case CHAT_MESSAGE_ALL -> handleChatMessage(originator, vault, true);
            default -> handleLegacyEventMessage(originator, vault);
        }
    }

    public void handleActionBarMessage(ServerPlayer player) {
        player.displayClientMessage(message, true);
    }

    public void handleChatMessage(ServerPlayer target, Vault vault, boolean shouldSendAll) {
        if(this.displayType.equals(VaultEvent.EventDisplayType.CHAT_MESSAGE_TARGET)) {
            target.displayClientMessage(message, false);
            return;
        }

        for(Listener listener: vault.get(Vault.LISTENERS).getAll()) {
            listener.getPlayer().ifPresent((other) -> {
                target.level.playSound(null, other.getX(), other.getY(), other.getZ(), SoundEvents.NOTE_BLOCK_BELL, SoundSource.PLAYERS, 0.9F, 1.2F);
                other.displayClientMessage(message, false);
            });
        }
    }

    public void handleLegacyEventMessage(ServerPlayer target, Vault vault) {
        return;
    }

}
