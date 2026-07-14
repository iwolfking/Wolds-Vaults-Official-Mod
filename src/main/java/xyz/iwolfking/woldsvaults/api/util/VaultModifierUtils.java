package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.VaultMod;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.vault.Modifiers;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.player.Listeners;
import iskallia.vault.init.ModConfigs;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.modifiers.vault.lib.SettableValueVaultModifier;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class VaultModifierUtils {
    public static void sendModifierAddedMessage(ServerPlayer player, VaultModifier<?> modifier, Integer stackSize) {
        player.displayClientMessage(new TranslatableComponent("util.woldsvaults.vault_modifier_added", modifier.getChatDisplayNameComponent(stackSize)), false);
    }

    public static void addModifier(Vault vault, ResourceLocation modifier, int count) {
        VaultModifier<?> vaultModifier = VaultModifierRegistry.get(modifier);
        if(vaultModifier != null) {
            vault.get(Vault.MODIFIERS).addModifier(vaultModifier, count, true, ChunkRandom.ofNanoTime());
        }
        else {
            WoldsVaults.LOGGER.error("Vault modifier {} is not registered — addModifier({}) did nothing! Check the modifier defs.", modifier, count);
        }
    }

    public static void addTimedModifier(Vault vault, ResourceLocation modifierId, int count, int duration, Player player) {
        VaultModifierRegistry.getOpt(modifierId).ifPresent((modifier) -> {
            ((Modifiers) vault.get(Vault.MODIFIERS)).addModifier(modifier, count, true, ChunkRandom.ofNanoTime(), (context) -> {
                if (duration > 0) {
                    context.set(ModifierContext.TICKS_LEFT, duration);
                }
            });
            Component text = new TranslatableComponent("util.woldsvaults.timed_modifier_added", player.getDisplayName(), modifier.getChatDisplayNameComponent(count), duration / 20);
            for (Listener listener : ((Listeners) vault.get(Vault.LISTENERS)).getAll()) {
                listener.getPlayer().ifPresent((other) -> {
                    player.getLevel().playSound((Player) null, other.getX(), other.getY(), other.getZ(), SoundEvents.NOTE_BLOCK_BELL, SoundSource.PLAYERS, 0.9F, 1.2F);
                    other.displayClientMessage(text, false);
                });
            }
        });
    }

    public static void addModifierFromPool(Vault vault, ResourceLocation modifierPool) {
        List<VaultModifier<?>> modifiers = ModConfigs.VAULT_MODIFIER_POOLS.getRandom(modifierPool, 0, JavaRandom.ofNanoTime());

        if(!modifiers.isEmpty()) {
            Iterator<VaultModifier<?>> modIter = modifiers.iterator();

            VaultModifier<?> modifier = VaultModifierRegistry.get(VaultMod.id("empty"));

            while(modIter.hasNext()) {
                VaultModifier<?> mod = modIter.next();
                modifier = mod;
                (vault.get(Vault.MODIFIERS)).addModifier(mod, 1, true, ChunkRandom.ofNanoTime());
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

    public static List<VaultModifier<?>> getModifiersFromPool(ResourceLocation modifierPool, int level) {
        return ModConfigs.VAULT_MODIFIER_POOLS.getRandom(modifierPool, level, JavaRandom.ofNanoTime());
    }

    public static <T extends VaultModifier<?>> List<T> getModifiersOfType(Vault vault, Class<T> type) {
        return vault.get(Vault.MODIFIERS)
                .getModifiers()
                .stream()
                .filter(type::isInstance)
                .map(type::cast)
                .toList();
    }

    public static <T extends VaultModifier<?>> Optional<T> getModifierOfType(Vault vault, Class<T> type) {
        return vault.get(Vault.MODIFIERS)
                .getModifiers()
                .stream()
                .filter(type::isInstance)
                .map(type::cast)
                .findFirst();
    }

    public static <T extends SettableValueVaultModifier<?>> void setModifierValueOfType(Vault vault, Class<T> type, Float value) {
        Optional<T> modifierOpt = getModifierOfType(vault, type);
        modifierOpt.ifPresent(modifier -> modifier.properties().setValue(value));
    }

    public static <T extends SettableValueVaultModifier<?>> float getModifierValueOfType(Vault vault, Class<T> type) {
        Optional<T> modifierOpt = getModifierOfType(vault, type);
        return modifierOpt.map(t -> t.properties().getValue()).orElse(0F);
    }

    public static <T extends SettableValueVaultModifier<?>> void incrementModifierValueOfType(Vault vault, Class<T> type, ResourceLocation id, Float value) {
        Optional<T> modifierOpt = getModifierOfType(vault, type);
        modifierOpt.ifPresentOrElse(modifier -> modifier.properties().setValue(modifier.properties().getValue() + value), () -> {
            Optional<VaultModifier<?>> modOpt = VaultModifierRegistry.getOpt(id);
            if(modOpt.isPresent()) {
                VaultModifier<?> mod = modOpt.get();
                if(mod instanceof SettableValueVaultModifier<?> settableValueVaultModifier) {
                    settableValueVaultModifier.properties().setValue(value);
                    vault.get(Vault.MODIFIERS).addModifier(settableValueVaultModifier, 1, true, ChunkRandom.ofNanoTime());
                }
            }
            else {
                WoldsVaults.LOGGER.error("Settable modifier " + id.toString() + " is missing, please make sure your vault_modifiers.json config is correct!!!");
            }
        });
    }

    public static boolean hasCountOfModifiers(Vault vault, ResourceLocation modifierId, int count) {
        return getCountOfModifiers(vault, modifierId) >= count;
    }

    public static long getCountOfModifiers(Vault vault, ResourceLocation modifierId) {
        List<VaultModifier<?>> modifiers = vault.get(Vault.MODIFIERS).getModifiers();
        return modifiers.stream().filter(modifier -> modifier.getId().equals(modifierId)).count();
    }
}
