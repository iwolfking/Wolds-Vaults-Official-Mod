package xyz.iwolfking.woldsvaults.gods;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The single {@link GodNodeAttributeSource} implementation, aggregating one attribute provider
 * per god tree. Tree modules call {@link #register(VaultGod, Provider)} from their setup; the
 * aggregator installs itself into {@link GodNodeAttributeSource} on the first registration.
 *
 * <p>Node ids are namespaced by tree (e.g. {@code idona_hard_hitter}); providers receive the
 * full minor-transfer id set and must ignore ids that are not theirs.
 */
public final class GodTreeAttributeProviders implements GodNodeAttributeSource {
    private static final GodTreeAttributeProviders INSTANCE = new GodTreeAttributeProviders();
    private static final Map<VaultGod, Provider> PROVIDERS = new EnumMap<>(VaultGod.class);
    private static boolean installed = false;

    public interface Provider {
        List<VaultGearAttributeInstance<?>> getGearAttributes(ServerPlayer player, Scope scope);

        List<VaultGearAttributeInstance<?>> getMinorTransferAttributes(ServerPlayer player, Collection<String> nodeIds);
    }

    private GodTreeAttributeProviders() {
    }

    public static synchronized void register(VaultGod god, Provider provider) {
        PROVIDERS.put(god, provider);
        if (!installed) {
            installed = true;
            GodNodeAttributeSource.register(INSTANCE);
        }
    }

    @Override
    public List<VaultGearAttributeInstance<?>> getGearAttributes(ServerPlayer player, VaultGod god, Scope scope) {
        Provider provider = PROVIDERS.get(god);
        return provider == null ? List.of() : provider.getGearAttributes(player, scope);
    }

    @Override
    public List<VaultGearAttributeInstance<?>> getMinorTransferAttributes(ServerPlayer player, Collection<String> nodeIds) {
        List<VaultGearAttributeInstance<?>> result = new ArrayList<>();
        for (Provider provider : PROVIDERS.values()) {
            result.addAll(provider.getMinorTransferAttributes(player, nodeIds));
        }
        return result;
    }
}
