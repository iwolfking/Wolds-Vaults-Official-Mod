package xyz.iwolfking.woldsvaults.api.core.vault_events;

import iskallia.vault.core.vault.Vault;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.EventTag;
import xyz.iwolfking.woldsvaults.datagen.ModLanguageProvider;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class VaultEventSystem {
    private static final LinkedHashMap<ResourceLocation, VaultEvent> VAULT_EVENT_REGISTRY = new LinkedHashMap<>();

    public static VaultEvent register(ResourceLocation id, VaultEvent event) {
        event.setId(id);
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
        if(id == null || !VAULT_EVENT_REGISTRY.containsKey(id)) {
            return null;
        }
        return VAULT_EVENT_REGISTRY.get(id);
    }

    public static Set<ResourceLocation> getAllEventIds() {
        return VAULT_EVENT_REGISTRY.keySet();
    }

    public static LinkedHashMap<ResourceLocation, VaultEvent> getAllEvents() {
        return VAULT_EVENT_REGISTRY;
    }


    public static Set<VaultEvent> getEventsByTags(Set<EventTag> tags) {
        return VAULT_EVENT_REGISTRY.values().stream().filter(vaultEvent -> !vaultEvent.getEventTags().stream().filter(tags::contains).toList().isEmpty()).collect(Collectors.toSet());
    }

    private static final Random RANDOM = new Random();

    public static VaultEvent getRandomEventByTags(Set<EventTag> tags) {
        Set<VaultEvent> events = VaultEventSystem.getEventsByTags(tags);
        if (events.isEmpty()) return null;

        List<VaultEvent> list = events.stream().toList();
        return list.get(RANDOM.nextInt(list.size()));
    }
}
