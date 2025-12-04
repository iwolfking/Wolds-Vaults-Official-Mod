package xyz.iwolfking.woldsvaults.datagen;

import com.alrex.parcool.api.Effects;
import iskallia.vault.VaultMod;
import iskallia.vault.gear.trinket.effects.AttributeTrinket;
import iskallia.vault.gear.trinket.effects.PotionEffectTrinket;
import iskallia.vault.init.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.effect.MobEffects;
import xyz.iwolfking.vhapi.api.datagen.AbstractTrinketProvider;
import xyz.iwolfking.vhapi.api.datagen.AbstractVaultAltarIngredientsProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.lib.trinket.MultiAttributeTrinket;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;

import java.util.List;

public class ModVaultAltarIngredientsProvider extends AbstractVaultAltarIngredientsProvider {
    protected ModVaultAltarIngredientsProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {

        add("additional_ingredients", builder -> {
//            builder.addIngredients(List.of(0, 10, 20, 50, 75, 90), "resource", altarIngredientsBuilder -> {
//                altarIngredientsBuilder.add(itemStacks -> {
//                    itemStacks.add(ModItems.POG.getDefaultInstance());
//                }, 1, 3, 1.0, 1000);
//            });
        });

    }
}
