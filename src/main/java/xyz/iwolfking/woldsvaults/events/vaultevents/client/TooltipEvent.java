package xyz.iwolfking.woldsvaults.events.vaultevents.client;

import iskallia.vault.core.event.ForgeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;

import java.util.function.Consumer;

public class TooltipEvent extends ForgeEvent<TooltipEvent, ItemTooltipEvent> {

    public TooltipEvent() {
    }

    protected TooltipEvent(TooltipEvent parent) {
        super(parent);
    }

    @Override
    public TooltipEvent createChild() {
        return new TooltipEvent(this);
    }

    @Override
    protected void initialize() {
        for(EventPriority priority : EventPriority.values()) {
            MinecraftForge.EVENT_BUS.addListener(priority, true, (Consumer<ItemTooltipEvent>) event -> {this.invoke(event);});
        }
    }


}