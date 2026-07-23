package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.vault.modifier.VaultModifierStack;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.data.InscriptionData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class CrystalDataUtils {
    public static void addModifier(ResourceLocation modifierId, CrystalData data) {
        addModifier(modifierId, data, 1);
    }

    public static void addModifier(ResourceLocation modifierId, CrystalData data, int count) {
        VaultModifier<?> modifier = VaultModifierRegistry.get(modifierId);
        if(modifier == null) {
            return;
        }

        data.addModifierByCrafting(new VaultModifierStack(modifier, count), true, false);
    }

    public static void addModifierFromPool(ResourceLocation modifierId, CrystalData data, int level) {
        addModifierFromPool(modifierId, data, level, 1);
    }


    public static void addModifierFromPool(ResourceLocation modifierPoolId, CrystalData data, int level, int count) {
        Map<VaultModifier<?>, Integer> modifierCounts = new HashMap<>();

        for (int i = 0; i < count; i++) {
            VaultModifier<?> modifier = ModConfigs.VAULT_MODIFIER_POOLS.getRandomModifier(
                    modifierPoolId,
                    level,
                    ChunkRandom.ofNanoTime()
            );

            if (modifier != null) {
                modifierCounts.put(modifier, modifierCounts.getOrDefault(modifier, 0) + 1);
            }
        }

        for (Map.Entry<VaultModifier<?>, Integer> entry : modifierCounts.entrySet()) {
            data.getModifiers().add(new VaultModifierStack(entry.getKey(), entry.getValue()));
        }
    }

    public static boolean hasCountOfModifiers(ResourceLocation modifierId, CrystalData data, int count) {
        AtomicInteger modifierCount = new AtomicInteger(0);
        data.getModifiers().getList().forEach(vaultModifierStack -> {
            if(vaultModifierStack.getModifierId().equals(modifierId)) {
                modifierCount.addAndGet(vaultModifierStack.getSize());
            }
        });

        return modifierCount.get() >= count;
    }

    public static void addInscriptionFromPool(ResourceLocation poolId, CrystalData data, ItemStack crystal, int level) {
        InscriptionData inscriptionData =  ModConfigs.INSCRIPTION.generate(poolId, level, JavaRandom.ofNanoTime()).orElse(null);
        if(inscriptionData == null) {
            return;
        }

        inscriptionData.apply(null, crystal, data);
    }
}
