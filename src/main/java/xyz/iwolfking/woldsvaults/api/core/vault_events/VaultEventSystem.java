package xyz.iwolfking.woldsvaults.api.core.vault_events;

import iskallia.vault.core.vault.Vault;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.EventTag;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class VaultEventSystem {
    private static final HashMap<ResourceLocation, VaultEvent> VAULT_EVENT_REGISTRY = new HashMap<>();

    public static VaultEvent register(ResourceLocation id, VaultEvent event) {
        VAULT_EVENT_REGISTRY.put(id, event);
        return event;
    }

    public static void triggerEvent(ResourceLocation id, Supplier<BlockPos>  pos, ServerPlayer player, Vault vault) {
        if(VAULT_EVENT_REGISTRY.containsKey(id)) {
            VAULT_EVENT_REGISTRY.get(id).triggerEvent(pos, player, vault);
        }
        else {
            WoldsVaults.LOGGER.warn("Attempted to trigger event with ID {} but it doesn't exist!", id);
        }
    }


    public static VaultEvent getEventById(ResourceLocation id) {
        return VAULT_EVENT_REGISTRY.get(id);
    }

    public static Set<ResourceLocation> getAllEventIds() {
        return VAULT_EVENT_REGISTRY.keySet();
    }


    public static Set<VaultEvent> getEventsByTags(Set<EventTag> tags) {
        return VAULT_EVENT_REGISTRY.values().stream().filter(vaultEvent -> !vaultEvent.getEventTags().stream().filter(tags::contains).toList().isEmpty()).collect(Collectors.toSet());
    }
}
