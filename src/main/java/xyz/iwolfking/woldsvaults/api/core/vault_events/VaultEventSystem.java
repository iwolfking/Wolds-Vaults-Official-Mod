package xyz.iwolfking.woldsvaults.api.core.vault_events;

import iskallia.vault.core.vault.Vault;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.EventTag;

import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

public class VaultEventSystem {
    private static final HashMap<ResourceLocation, VaultEvent> VAULT_EVENT_REGISTRY = new HashMap<>();

    public static VaultEvent register(ResourceLocation id, VaultEvent event) {
        VAULT_EVENT_REGISTRY.put(id, event);
        return event;
    }

    public static void triggerEvent(ResourceLocation id, BlockPos pos, ServerPlayer player, Vault vault) {
        if(VAULT_EVENT_REGISTRY.containsKey(id)) {
            VAULT_EVENT_REGISTRY.get(id).triggerEvent(pos, player, vault);
        }
        else {
            WoldsVaults.LOGGER.warn("Attempted to trigger event with ID {} but it doesn't exist!", id);
        }
    }

    public static Set<VaultEvent> getEventsByTag(EventTag tag) {
        return VAULT_EVENT_REGISTRY.values().stream().filter(vaultEvent -> vaultEvent.getEventTags().contains(tag)).collect(Collectors.toSet());
    }
}
