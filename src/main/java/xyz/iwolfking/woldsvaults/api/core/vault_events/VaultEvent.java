package xyz.iwolfking.woldsvaults.api.core.vault_events;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.player.Listener;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.EventTag;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.VaultEventTask;
import xyz.iwolfking.woldsvaults.objectives.data.EnchantedEventsRegistry;

import java.util.*;
import java.util.function.Supplier;

public class VaultEvent {
    private ResourceLocation id;
    private final EventDisplayType eventDisplayType;
    private final Set<EventTag> eventTags;
    public final TextComponent eventMessage;
    public final String eventName;
    public final TextComponent eventDescription;
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

    public VaultEvent(ResourceLocation id, String eventName, TextComponent eventMessage, TextColor nameColor, TextComponent eventDescription, EventDisplayType eventDisplayType, Set<EventTag> eventTags, List<VaultEventTask> eventTasks) {
        this.id = id;
        this.eventDisplayType = eventDisplayType;
        this.eventTags = eventTags;
        this.eventMessage = eventMessage;
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.nameColor = nameColor;
        this.eventTasks = eventTasks;
    }

    public void triggerEvent(Supplier<BlockPos> pos, ServerPlayer player, Vault vault, boolean shouldCascade, EventDisplayType displayOverride) {
        if(displayOverride != null) {
            sendEventMessages(vault, player, displayOverride);
        }
        else {
            sendEventMessages(vault, player);
        }


        if(shouldCascade) {
            cascadeEvent(vault, player);
        }

        DelayedSequenceHandler.startSequence(eventTasks, pos, player, vault);
    }

    public void triggerEvent(Supplier<BlockPos> pos, ServerPlayer player, Vault vault) {
        triggerEvent(pos, player, vault, false, null);
    }

    public void triggerEvent(Vault vault) {
        Collection<Listener> listeners = vault.get(Vault.LISTENERS).getAll();

        listeners.stream().findFirst().flatMap(Listener::getPlayer).ifPresent(player -> {
            triggerEvent(player::getOnPos, player, vault);
        });
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

                    EnchantedEventsRegistry.getEvents().getRandom().get().triggerEvent(other::getOnPos, other, vault);


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
        return new TranslatableComponent("vault_event.woldsvaults.legacy_event_message", target.getDisplayName(), getEventName()).withStyle(getHoverDescription());
    }

    public Component getCascadingEventMessage(ServerPlayer originator) {
        return new TranslatableComponent("vault_event.woldsvaults.event_cascade", originator.getDisplayName().copy().append("'s").withStyle(originator.getDisplayName().getStyle()), getEventName()).withStyle(getHoverDescription());
    }

    public void sendEventMessages(Vault vault, ServerPlayer originator, EventDisplayType type) {
        switch (type) {
            case NONE -> {
                return;
            }
            case ACTION_BAR -> handleActionBarMessage(originator);
            case CHAT_MESSAGE_TARGET, CHAT_MESSAGE_ALL -> handleChatMessage(originator, vault, type);
            default -> handleLegacyEventMessage(originator, vault);
        }
    }

    public void sendEventMessages(Vault vault, ServerPlayer originator) {
        sendEventMessages(vault, originator, eventDisplayType);
    }

    public void handleActionBarMessage(ServerPlayer player) {
        player.displayClientMessage(eventMessage, true);
    }

    public void handleChatMessage(ServerPlayer target, Vault vault, EventDisplayType type) {
        if(type.equals(EventDisplayType.CHAT_MESSAGE_TARGET)) {
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

    public Component getEventMessage() {
        if(id != null) {
            return new TranslatableComponent("vault_event." + id.getNamespace() + "." + id.getPath() + "_message");
        }
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
        if(this.id != null) {
            return new TranslatableComponent("vault_event." + id.getNamespace() + "." + id.getPath() + "_description");
        }

        return eventDescription;
    }

    public Style getHoverDescription() {
        return Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, getEventDescriptor()));
    }

    public Component getEventName() {
        if(this.id != null) {
            return new TranslatableComponent("vault_event." + id.getNamespace() + "." + id.getPath()).withStyle(Style.EMPTY.withColor(nameColor));
        }

        return new TextComponent(eventName).withStyle(Style.EMPTY.withColor(nameColor));
    }

    public Set<EventTag> getEventTags() {
        return eventTags;
    }

    public void setId(ResourceLocation id) {
        this.id = id;
    }

    public ResourceLocation getId() {
        return id;
    }

    public static class Builder {
        private EventDisplayType eventDisplayType = EventDisplayType.NONE;
        private final Set<EventTag> eventTags = new HashSet<>();
        private TextComponent eventMessage;
        private TextColor nameColor = TextColor.fromLegacyFormat(ChatFormatting.WHITE);
        private final List<VaultEventTask> eventTasks = new ArrayList<>();

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
            if(eventMessage == null) {
                eventMessage = new TextComponent("");
            }
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
