package xyz.iwolfking.woldsvaults.events.vaultevents;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.Event;

public class WoldCommonEvents {
    public static final FracturedObeliskEvent FRACTURED_OBELISK_UPDATE = register(new FracturedObeliskEvent());


    private static <T extends Event<?, ?>> T register(T event) {
        CommonEvents.REGISTRY.add(event);
        return event;
    }
}