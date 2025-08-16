package xyz.iwolfking.woldsvaults.util;

import iskallia.vault.VaultMod;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.init.ModConfigs;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Iterator;
import java.util.List;

public class VaultModifierUtils {
    private static final TextComponent MODIFIER_ADDED_SUFFIX = new TextComponent(" was added to the vault.");
    public static void sendModifierAddedMessage(ServerPlayer player, VaultModifier<?> modifier, Integer stackSize) {
        player.displayClientMessage(modifier.getChatDisplayNameComponent(stackSize).copy().append(MODIFIER_ADDED_SUFFIX), false);
    }

    public static void addModifier(Vault vault, ResourceLocation modifier, int count) {
        VaultModifier<?> vaultModifier = VaultModifierRegistry.get(modifier);
        if(vaultModifier != null) {
            vault.get(Vault.MODIFIERS).addModifier(vaultModifier, count, true, ChunkRandom.any());
        }
    }

    public static void addModifierFromPool(Vault vault, ResourceLocation modifierPool) {
        List<VaultModifier<?>> modifiers = ModConfigs.VAULT_MODIFIER_POOLS.getRandom(modifierPool, 0, JavaRandom.ofNanoTime());

        if(!modifiers.isEmpty()) {
            Iterator<VaultModifier<?>> modIter = modifiers.iterator();

            VaultModifier<?> modifier = VaultModifierRegistry.get(VaultMod.id("empty"));

            while(modIter.hasNext()) {
                VaultModifier<?> mod = modIter.next();
                modifier = mod;
                (vault.get(Vault.MODIFIERS)).addModifier(mod, 1, true, ChunkRandom.any());
            }

            if(modifier.getId().equals(VaultMod.id("empty"))) {
                return;
            }

            for (Listener listener : vault.get(Vault.LISTENERS).getAll()) {
                listener.getPlayer().ifPresent(other -> {
                    sendModifierAddedMessage(other, modifiers.get(0), 1);
                });
            }
        }
    }

    public static boolean hasCountOfModifiers(Vault vault, ResourceLocation modifierId, int count) {
        List<VaultModifier<?>> modifiers = vault.get(Vault.MODIFIERS).getModifiers();
        return modifiers.stream().filter(modifier -> modifier.getId().equals(modifierId)).count() >= count;
    }
}
