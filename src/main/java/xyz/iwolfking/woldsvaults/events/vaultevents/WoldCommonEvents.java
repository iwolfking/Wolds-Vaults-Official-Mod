package xyz.iwolfking.woldsvaults.events.vaultevents;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.Event;

public class WoldCommonEvents {
    public static final FracturedObeliskEvent FRACTURED_OBELISK_UPDATE = register(new FracturedObeliskEvent());
    public static final BrewingAltarBrewEvent BREWING_ALTAR_BREW_EVENT = register(new BrewingAltarBrewEvent());


    private static <T extends Event<?, ?>> T register(T event) {
        CommonEvents.REGISTRY.add(event);
        return event;
    }
}