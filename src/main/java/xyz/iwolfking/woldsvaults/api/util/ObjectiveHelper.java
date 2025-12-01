package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.VaultMod;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import xyz.iwolfking.woldsvaults.init.ModGameRules;
import xyz.iwolfking.woldsvaults.modifiers.vault.DifficultyLockModifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ObjectiveHelper {
    public static void handleAddingNormalizedToVault(Vault vault, ServerLevel level) {
        if(!level.getGameRules().getBoolean(ModGameRules.NORMALIZED_ENABLED)) {
            return;
        }

        boolean hasGeneratedModifiers = false;

        for(VaultModifier<?> modifier : vault.get(Vault.MODIFIERS).getModifiers()) {
            if(modifier.getId().equals(VaultMod.id("normalized")) || modifier instanceof DifficultyLockModifier) {
                hasGeneratedModifiers = true;
                break;
            }
        }

        if(!hasGeneratedModifiers) {
            VaultModifierUtils.addModifier(vault, VaultMod.id("normalized"), 1);
        }
    }

    public static void addInitModifiersToVault(Vault vault, Consumer<List<ResourceLocation>> modifiersConsumer) {
        boolean hasGeneratedModifiers = false;
        List<ResourceLocation> modifiers = new ArrayList<>();
        modifiersConsumer.accept(modifiers);

        for(VaultModifier<?> modifier : vault.get(Vault.MODIFIERS).getModifiers()) {
            if(modifiers.contains(modifier.getId())) {
                hasGeneratedModifiers = true;
                break;
            }
        }

        if(!hasGeneratedModifiers) {
            modifiers.forEach(id -> {
                VaultModifierUtils.addModifier(vault, id, 1);
            });
        }
    }

    public static void addInitModifiersMapToVault(Vault vault, Consumer<Map<ResourceLocation, Integer>> modifiersConsumer) {
        boolean hasGeneratedModifiers = false;
        Map<ResourceLocation, Integer> modifiers = new HashMap<>();
        modifiersConsumer.accept(modifiers);

        for(VaultModifier<?> modifier : vault.get(Vault.MODIFIERS).getModifiers()) {
            if(modifiers.containsKey(modifier.getId())) {
                hasGeneratedModifiers = true;
                break;
            }
        }

        if(!hasGeneratedModifiers) {
            modifiers.forEach((id, amount) -> {
                VaultModifierUtils.addModifier(vault, id, amount);
            });
        }
    }
}
