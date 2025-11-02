package xyz.iwolfking.woldsvaults.api.core.vault_events.impl;

import iskallia.vault.core.vault.Vault;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.objectives.data.EnchantedEventsRegistry;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.BasicVaultEvent;

import java.util.Random;

public class LaCucarachaSpecialVaultEvent extends BasicVaultEvent {
    private final float eventOdds;

    private static final Component COCKROACH_BLESSING_MESSAGE = new TextComponent("The dancing cockroaches are fond towards you and bless each player with a random positive event!").withStyle(ChatFormatting.GOLD);
    private static final Component COCKROACH_CURSE_MESSAGE = new TextComponent("The dancing cockroaches hate your guts and curse everyone in the vault with a random negative event!").withStyle(ChatFormatting.RED);
    public LaCucarachaSpecialVaultEvent(String eventName, String eventDescription, String primaryColor, float eventOdds) {
        super(eventName, eventDescription, primaryColor);
        this.eventOdds = eventOdds;
    }

    @Override
    public void triggerEvent(BlockPos pos, ServerPlayer player, Vault vault) {
        Random random = new Random();

        //Check if any event should occur
        if(random.nextFloat(0.0F, 1.0F) >= eventOdds) {
            //Check if it should be a blessing or a curse
            if(random.nextBoolean()) {
                vault.get(Vault.LISTENERS).getAll().forEach(listener -> {
                    player.displayClientMessage(COCKROACH_BLESSING_MESSAGE, false);
                    EnchantedEventsRegistry.getPositiveEvents().getRandom().get().triggerEvent(listener.getPlayer().get().getOnPos(), listener.getPlayer().get(), vault);
                });
            }
            else {
                vault.get(Vault.LISTENERS).getAll().forEach(listener -> {
                    player.displayClientMessage(COCKROACH_CURSE_MESSAGE, false);
                    EnchantedEventsRegistry.getNegativeEvents().getRandom().get().triggerEvent(listener.getPlayer().get().getOnPos(), listener.getPlayer().get(), vault);
                });
            }
            super.triggerEvent(pos, player, vault);
        }


    }
}
