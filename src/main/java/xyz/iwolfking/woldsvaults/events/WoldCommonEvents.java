package xyz.iwolfking.woldsvaults.events;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.Event;
import xyz.iwolfking.woldsvaults.events.event.CorruptedMonolithEvent;

public class WoldCommonEvents {
    public static final CorruptedMonolithEvent CORRUPTED_MONOLITH_UPDATE = register(new CorruptedMonolithEvent());


    private static <T extends Event<?, ?>> T register(T event) {
        CommonEvents.REGISTRY.add(event);
        return event;
    }
}
