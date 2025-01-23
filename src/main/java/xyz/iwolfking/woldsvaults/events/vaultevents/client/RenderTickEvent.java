package xyz.iwolfking.woldsvaults.events.vaultevents.client;

import iskallia.vault.core.event.ForgeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;

import java.util.function.Consumer;

public class RenderTickEvent extends ForgeEvent<RenderTickEvent, TickEvent.RenderTickEvent> {

    public RenderTickEvent() {
    }

    protected RenderTickEvent(RenderTickEvent parent) {
        super(parent);
    }

    @Override
    public RenderTickEvent createChild() {
        return new RenderTickEvent(this);
    }

    @Override
    protected void initialize() {
        for(EventPriority priority : EventPriority.values()) {
            MinecraftForge.EVENT_BUS.addListener(priority, true, (Consumer<TickEvent.RenderTickEvent>) event -> {this.invoke(event);});
        }
    }


}
