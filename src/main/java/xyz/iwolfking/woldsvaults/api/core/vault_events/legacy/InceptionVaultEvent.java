package xyz.iwolfking.woldsvaults.api.core.vault_events.legacy;

import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.vault.Vault;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.api.core.vault_events.LegacyVaultEvent;

public class InceptionVaultEvent extends LegacyVaultEvent {

    private final WeightedList<LegacyVaultEvent> events;

    private final boolean shouldEventsBeRandom;

    private final Integer count;
    public InceptionVaultEvent(String eventName, String eventDescription, String primaryColor, WeightedList<LegacyVaultEvent> events, boolean shouldEventsBeRandom, Integer count) {
        super(eventName, eventDescription, primaryColor);
        this.events = events;
        this.shouldEventsBeRandom = shouldEventsBeRandom;
        this.count = count;
    }

    @Override
    public void triggerEvent(BlockPos pos, ServerPlayer player, Vault vault) {
        if(shouldEventsBeRandom) {
            for(int i = 0; i < count; i++) {
                events.getRandom().ifPresent(basicEnchantedEvent -> basicEnchantedEvent.triggerEvent(pos, player, vault));
            }
        }
        else {
            events.forEach((basicEnchantedEvent, aDouble) -> basicEnchantedEvent.triggerEvent(pos, player, vault));
        }

        super.triggerEvent(pos, player, vault);

    }
}
