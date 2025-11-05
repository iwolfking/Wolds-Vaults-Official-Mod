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
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

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

    public static void addTimedModifier(Vault vault, ResourceLocation modifierId, int count, int duration, Player player) {
        VaultModifierRegistry.getOpt(modifierId).ifPresent((modifier) -> {
            ((Modifiers) vault.get(Vault.MODIFIERS)).addModifier(modifier, count, true, ChunkRandom.ofNanoTime(), (context) -> {
                if (duration > 0) {
                    context.set(ModifierContext.TICKS_LEFT, duration);
                }
            });
            Component text;
            MutableComponent modifierMessage = (new TextComponent("")).append(player.getDisplayName()).append((new TextComponent(" added ")).withStyle(ChatFormatting.GRAY)).append(modifier.getChatDisplayNameComponent(count)).append((new TextComponent(" for ")).withStyle(ChatFormatting.GRAY));
            text = modifierMessage.append(new TextComponent(duration / 20 + " seconds")).append((new TextComponent(".")).withStyle(ChatFormatting.GRAY));
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
