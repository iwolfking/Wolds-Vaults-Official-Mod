package xyz.iwolfking.woldsvaults.integration.occultism.init;


import iskallia.vault.VaultMod;
import iskallia.vault.config.CompanionRelicsConfig;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.AugmentItem;
import iskallia.vault.item.CompanionRelicItem;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.CrystalDataUtils;
import xyz.iwolfking.woldsvaults.integration.occultism.impl.VaultCrystalRitual;
import xyz.iwolfking.woldsvaults.integration.occultism.lib.DynamicResultRitualRecipe;

import java.util.Random;


public class OccultismRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, WoldsVaults.MOD_ID);

    public static final RegistryObject<RecipeSerializer<?>> AUGMENT_RITUAL =
            SERIALIZERS.register("augment_ritual", () -> new DynamicResultRitualRecipe.Serializer(
                    "theme",
                    OccultismRecipeSerializers.AUGMENT_RITUAL,
                    (resourceLocation, itemStack) -> AugmentItem.create(resourceLocation)
            ));

    public static final RegistryObject<RecipeSerializer<?>> VAULT_CRYSTAL_RITUAL =
            SERIALIZERS.register("vault_crystal_ritual", () -> new DynamicResultRitualRecipe.Serializer(
                    "crystalRitualType",
                    OccultismRecipeSerializers.VAULT_CRYSTAL_RITUAL,
                    VaultCrystalRitual::invokeRitual
            ));

    public static final RegistryObject<RecipeSerializer<?>> COMPANION_RITUAL =
            SERIALIZERS.register("companion_ritual", () -> new DynamicResultRitualRecipe.Serializer(
                    "poolId",
                    OccultismRecipeSerializers.COMPANION_RITUAL,
                    (id, itemStack) -> {
                        Random random = new Random();
                        CompanionRelicsConfig.ResolvedEntry entry = ModConfigs.COMPANION_RELICS.generate(id, 0, ChunkRandom.ofNanoTime()).orElse(null);

                        if(entry == null) {
                            return CompanionRelicItem.create(VaultMod.id("companion_challenge"), 0);
                        }

                        if(random.nextFloat() <= 0.1F) {
                            CompanionRelicsConfig.ResolvedEntry ancientEntry = ModConfigs.COMPANION_RELICS.generate(VaultMod.id("ancient_3"), 0, ChunkRandom.ofNanoTime()).orElse(null);
                            return ModConfigs.COMPANION_RELICS.createRelicStack(ancientEntry, ChunkRandom.ofNanoTime());
                        }

                        return ModConfigs.COMPANION_RELICS.createRelicStack(entry, ChunkRandom.ofNanoTime());
                    }
            ));
}
