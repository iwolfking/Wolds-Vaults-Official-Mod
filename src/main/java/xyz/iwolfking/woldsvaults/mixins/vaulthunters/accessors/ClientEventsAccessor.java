package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.core.event.ClientEvents;
import iskallia.vault.core.event.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = ClientEvents.class, remap = false)
public interface ClientEventsAccessor {

    @Accessor
    static List<Event<?, ?>> getREGISTRY() {
        throw new UnsupportedOperationException();
    }

}
