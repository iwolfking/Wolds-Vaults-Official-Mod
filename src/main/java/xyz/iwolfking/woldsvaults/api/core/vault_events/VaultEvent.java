package xyz.iwolfking.woldsvaults.api.core.vault_events;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.player.Listener;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.EventTag;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.VaultEventTask;
import xyz.iwolfking.woldsvaults.objectives.data.EnchantedEventsRegistry;

import java.util.*;

public class VaultEvent {

    private final EventDisplayType eventDisplayType;
    private final Set<EventTag> eventTags;
    private final TextComponent eventMessage;
    private final String eventName;
    private final TextComponent eventDescription;
    private final TextColor nameColor;
    private final List<VaultEventTask> eventTasks;

    private int cascadingValue = 85;

    public VaultEvent(String eventName, TextComponent eventMessage, TextColor nameColor, TextComponent eventDescription, EventDisplayType eventDisplayType, Set<EventTag> eventTags, List<VaultEventTask> eventTasks) {
        this.eventDisplayType = eventDisplayType;
        this.eventTags = eventTags;
        this.eventMessage = eventMessage;
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.nameColor = nameColor;
        this.eventTasks = eventTasks;
    }

    public void triggerEvent(BlockPos pos, ServerPlayer player, Vault vault) {
        sendEventMessages(vault, player);

        if(eventTags.contains(EventTag.CASCADE)) {
            cascadeEvent(vault, player);
        }

        for (VaultEventTask eventTask : eventTasks) {
            eventTask.performTask(pos, player, vault);
        }
    }

    public void cascadeEvent(Vault vault, ServerPlayer originator) {
        Random random = new Random();
        for(Listener listener: vault.get(Vault.LISTENERS).getAll()) {
            if((random.nextInt(0, 100) >= cascadingValue)) {
                listener.getPlayer().ifPresent(other -> {
                    if(originator.equals(other)) {
                        if(!((random.nextInt(100 - 1) + 1) >= cascadingValue)) {
                            return;
                        }
                    }
                    other.level.playSound(null, other.getX(), other.getY(), other.getZ(), SoundEvents.GUARDIAN_ATTACK, SoundSource.PLAYERS, 0.9F, 1.2F);
                    other.displayClientMessage(getCascadingEventMessage(originator), false);
                    cascadingValue = cascadingValue+3;

                    EnchantedEventsRegistry.getEvents().getRandom().get().triggerEvent(other.getOnPos(), other, vault);


                });
            }
            else {
                if(random.nextInt(0, 100) >= 75 && cascadingValue > 85) {
                    cascadingValue--;
                }
            }
        }
    }

    public Component getLegacyEventMessage(ServerPlayer target) {
        MutableComponent eventMessage = new TextComponent("");
        eventMessage.append(target.getDisplayName());
        eventMessage.append(new TextComponent(" encountered a ").withStyle(ChatFormatting.GRAY));
        eventMessage.append(new TextComponent(eventName + " Event!").withStyle(Style.EMPTY.withColor(nameColor)).withStyle(getHoverDescription()));
        return eventMessage;
    }

    public Component getCascadingEventMessage(ServerPlayer originator) {
        MutableComponent cascadeMessage = new TextComponent("");
        cascadeMessage.append(originator.getDisplayName());
        cascadeMessage.append("'s ").withStyle(originator.getDisplayName().getStyle());
        cascadeMessage.append(eventName).withStyle(Style.EMPTY.withColor(nameColor)).withStyle(getHoverDescription());
        cascadeMessage.append(" event has cascaded onto you!").withStyle(ChatFormatting.GRAY);
        return cascadeMessage;
    }

    public void sendEventMessages(Vault vault, ServerPlayer originator) {
        switch (eventDisplayType) {
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
        player.displayClientMessage(eventMessage, true);
    }

    public void handleChatMessage(ServerPlayer target, Vault vault, boolean shouldSendAll) {
        if(this.eventDisplayType.equals(EventDisplayType.CHAT_MESSAGE_TARGET)) {
            target.displayClientMessage(eventMessage, false);
            return;
        }

        for(Listener listener: vault.get(Vault.LISTENERS).getAll()) {
            listener.getPlayer().ifPresent((other) -> {
                target.level.playSound(null, other.getX(), other.getY(), other.getZ(), SoundEvents.NOTE_BLOCK_BELL, SoundSource.PLAYERS, 0.9F, 1.2F);
                other.displayClientMessage(eventMessage, false);
            });
        }
    }

    public TextComponent getEventMessage() {
        return eventMessage;
    }

    public void handleLegacyEventMessage(ServerPlayer target, Vault vault) {
        if(this.eventDisplayType.equals(EventDisplayType.CHAT_MESSAGE_TARGET)) {
            target.displayClientMessage(getLegacyEventMessage(target), false);
            return;
        }

        for(Listener listener: vault.get(Vault.LISTENERS).getAll()) {
            listener.getPlayer().ifPresent((other) -> {
                target.level.playSound(null, other.getX(), other.getY(), other.getZ(), SoundEvents.NOTE_BLOCK_BELL, SoundSource.PLAYERS, 0.9F, 1.2F);
                other.displayClientMessage(getLegacyEventMessage(target), false);
            });
        }
    }

    public Component getEventDescriptor() {
        return eventDescription;
    }

    public Style getHoverDescription() {
        return Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, getEventDescriptor()));
    }

    public static class Builder {
        private EventDisplayType eventDisplayType = EventDisplayType.NONE;
        private Set<EventTag> eventTags = new HashSet<>();
        private TextComponent eventMessage;
        private TextColor nameColor = TextColor.fromLegacyFormat(ChatFormatting.WHITE);
        private List<VaultEventTask> eventTasks = new ArrayList<>();

        public Builder displayType(EventDisplayType type) {
            this.eventDisplayType = type;
            return this;
        }

        public Builder tag(EventTag tag) {
            this.eventTags.add(tag);
            return this;
        }

        public Builder message(TextComponent messsage) {
            this.eventMessage = messsage;
            return this;
        }

        public Builder color(TextColor color) {
            this.nameColor = color;
            return this;
        }

        public Builder task(VaultEventTask task) {
            this.eventTasks.add(task);
            return this;
        }

        public VaultEvent build(String eventName, TextComponent eventDescription) {
            return new VaultEvent(eventName, eventMessage, nameColor, eventDescription, eventDisplayType, eventTags, eventTasks);
        }
    }

    public enum EventDisplayType {
        NONE,
        CHAT_MESSAGE_TARGET,
        CHAT_MESSAGE_ALL,
        ACTION_BAR,
        LEGACY
    }
}
