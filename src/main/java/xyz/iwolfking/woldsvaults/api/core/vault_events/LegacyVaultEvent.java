package xyz.iwolfking.woldsvaults.api.core.vault_events;

import com.google.gson.annotations.Expose;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.player.Listener;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import xyz.iwolfking.woldsvaults.objectives.data.EnchantedEventsRegistry;

import java.util.Random;

public abstract class LegacyVaultEvent {
    @Expose
    private final String eventName;

    @Expose
    private final String eventDescription;

    @Expose
    private final String color;

    @Expose
    public boolean isOmega;

    @Expose
    public boolean isPositive;

    private final TextColor primaryColor;

    private int cascadingValue = 85;

    private boolean cascadingRandomly = false;

    private boolean shouldCascade = true;

    private boolean shouldSendMessage = true;



    protected LegacyVaultEvent(String eventName, String eventDescription, String primaryColor) {
        this.eventDescription = eventDescription;
        this.eventName = eventName;
        this.color = primaryColor;
        this.primaryColor = TextColor.parseColor(color);
    }

    public void triggerEvent(BlockPos pos, ServerPlayer player, Vault vault) {
        sendEventMessages(vault, player);
        targetOthers(vault, player);
    }

    public void sendEventMessages(Vault vault, ServerPlayer originator) {
        MutableComponent eventMessage = new TextComponent("");

        eventMessage.append(originator.getDisplayName());
        eventMessage.append(new TextComponent(" encountered a ").withStyle(ChatFormatting.GRAY));
        eventMessage.append(getEventMessage());

        for(Listener listener: vault.get(Vault.LISTENERS).getAll()) {
            listener.getPlayer().ifPresent((other) -> {
                originator.level.playSound(null, other.getX(), other.getY(), other.getZ(), SoundEvents.NOTE_BLOCK_BELL, SoundSource.PLAYERS, 0.9F, 1.2F);
                other.displayClientMessage(eventMessage, false);
            });
        }

    }

    public void targetOthers(Vault vault, ServerPlayer originator) {
        Random random = new Random();
        for( Listener listener: vault.get(Vault.LISTENERS).getAll()) {
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
                    if(!cascadingRandomly) {
                        triggerEvent(other.getOnPos(), other, vault);
                    }
                    else {
                        EnchantedEventsRegistry.getEvents().getRandom().get().triggerEvent(other.getOnPos(), other, vault);
                    }

                });
            }
            else {
                if(random.nextInt(0, 100) >= 75 && cascadingValue > 85) {
                    cascadingValue--;
                }
            }
        }
    }


    public Component getEventMessage() {
        return new TextComponent(eventName + " Event!").withStyle(Style.EMPTY.withColor(primaryColor)).withStyle(getHoverDescription());
    }

    public Component getCascadingEventMessage(ServerPlayer originator) {
        MutableComponent cascadeMessage = new TextComponent("");
        cascadeMessage.append(originator.getDisplayName());
        cascadeMessage.append("'s ").withStyle(originator.getDisplayName().getStyle());
        cascadeMessage.append(eventName).withStyle(Style.EMPTY.withColor(primaryColor)).withStyle(getHoverDescription());
        cascadeMessage.append(" event has cascaded onto you!").withStyle(ChatFormatting.GRAY);
        return cascadeMessage;
    }

    public String getEventName() {
        return this.eventName;
    }

    public TextColor getPrimaryColor() {
        return primaryColor;
    }

    public Component getEventDescriptor() {
        return new TextComponent(eventDescription);
    }

    public Style getHoverDescription() {
        return Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, getEventDescriptor()));
    }


}
