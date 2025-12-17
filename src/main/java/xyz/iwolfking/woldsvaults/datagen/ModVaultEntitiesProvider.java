package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import iskallia.vault.core.world.data.entity.EntityPredicate;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.vhapi.api.datagen.AbstractTooltipProvider;
import xyz.iwolfking.vhapi.api.datagen.AbstractVaultEntitiesProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.init.ModEntities;
import xyz.iwolfking.woldsvaults.init.ModItems;

public class ModVaultEntitiesProvider extends AbstractVaultEntitiesProvider {
    protected ModVaultEntitiesProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        add("wold_mobs", builder -> {
            builder.addDeathEffect(EntityPredicate.of(ModEntities.CRANBERRY_SLIME.getRegistryName().toString(), true).orElse(null), deathEffectBuilder -> {
                deathEffectBuilder.add(0, "", VaultMod.id(""), 100, 3, 5177344, true, 0.5F, ResourceLocation.fromNamespaceAndPath("immersiveengineering", "sticky"), 100, 1);
            });
        });
    }
}
