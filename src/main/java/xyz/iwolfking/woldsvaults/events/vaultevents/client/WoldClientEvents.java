package xyz.iwolfking.woldsvaults.events.vaultevents.client;

import iskallia.vault.core.event.Event;
import iskallia.vault.core.event.client.*;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ClientEventsAccessor;

public class WoldClientEvents {


    public static final RenderTickEvent RENDER_TICK_EVENT = register(new RenderTickEvent());




    private static <T extends Event<?, ?>> T register(T event) {
        ClientEventsAccessor.getREGISTRY().add(event);
        return event;
    }
}
