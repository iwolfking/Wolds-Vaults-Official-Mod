package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import iskallia.vault.config.VaultAltarConfig;
import iskallia.vault.config.VaultModifiersConfig;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.core.vault.modifier.VaultModifierStack;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.world.data.item.ItemPredicate;
import iskallia.vault.core.world.roll.IntRoll;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.VaultCrystalItem;
import iskallia.vault.item.crystal.modifiers.CrystalModifiers;
import iskallia.vault.item.crystal.modifiers.DefaultCrystalModifiers;
import iskallia.vault.item.crystal.objective.CrystalObjective;
import iskallia.vault.item.crystal.objective.PoolCrystalObjective;
import iskallia.vault.item.crystal.objective.RaidCrystalObjective;
import iskallia.vault.item.crystal.properties.CapacityCrystalProperties;
import iskallia.vault.item.crystal.theme.CrystalTheme;
import iskallia.vault.item.crystal.theme.PoolCrystalTheme;
import iskallia.vault.item.crystal.theme.ValueCrystalTheme;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import xyz.iwolfking.vhapi.api.datagen.AbstractTooltipProvider;
import xyz.iwolfking.vhapi.api.datagen.AbstractVaultAltarProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.objectives.ZealotCrystalObjective;

public class ModVaultAltarProvider extends AbstractVaultAltarProvider {
    protected ModVaultAltarProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {

        add("wolds_altar_interfaces", builder -> {
            ModConfigs.VAULT_MODIFIERS = new VaultModifiersConfig().readConfig();
            builder
                    .add(new VaultAltarConfig.Interface(ItemPredicate.of(ModItems.GREEDY_VAULT_ROCK.getRegistryName().toString(), true).orElse(null), VaultCrystalItem.create(crystalData -> {
                        CapacityCrystalProperties properties = new CapacityCrystalProperties();
                        properties.setVolume(0);
                        properties.setUnmodifiable(false);

                        CrystalModifiers modifiers = new DefaultCrystalModifiers();
                        modifiers.add(new VaultModifierStack(VaultModifierRegistry.get(VaultMod.id("greedy")), 1));

                        crystalData.setProperties(properties);
                        crystalData.setModifiers(modifiers);
                    })))
                    .add(zealotCrystal(VaultGod.IDONA, ItemPredicate.of(ModItems.IDONA_DAGGER.getRegistryName().toString(), true).orElse(null)))
                    .add(zealotCrystal(VaultGod.TENOS, ItemPredicate.of(ModItems.TOME_OF_TENOS.getRegistryName().toString(), true).orElse(null)))
                    .add(zealotCrystal(VaultGod.VELARA, ItemPredicate.of(ModItems.VELARA_APPLE.getRegistryName().toString(), true).orElse(null)))
                    .add(zealotCrystal(VaultGod.WENDARR, ItemPredicate.of(ModItems.WENDARR_GEM.getRegistryName().toString(), true).orElse(null)))
                    .add(new VaultAltarConfig.Interface(ItemPredicate.of(ModItems.UBER_CHAOS_CATALYST.getRegistryName().toString(), true).orElse(null), VaultCrystalItem.create(crystalData -> {
                        CrystalModifiers modifiers = new DefaultCrystalModifiers();
                        modifiers.add(new VaultModifierStack(VaultModifierRegistry.get(VaultMod.id("unhinged")), 1));

                        CapacityCrystalProperties properties = new CapacityCrystalProperties();
                        properties.setVolume(0);
                        properties.setUnmodifiable(true);

                        crystalData.setObjective(new PoolCrystalObjective(VaultMod.id("unhinged_default")));
                        crystalData.setTheme(new ValueCrystalTheme(VaultMod.id("classic_vault_chaos")));
                        crystalData.setModifiers(modifiers);
                        crystalData.setProperties(properties);
                    })))
                    .add(new VaultAltarConfig.Interface(ItemPredicate.of(ModItems.CRYSTAL_SEAL_RAID_ROCK_INFINITE_HARD.getRegistryName().toString(), true).orElse(null), VaultCrystalItem.create(crystalData -> {
                        CrystalModifiers modifiers = new DefaultCrystalModifiers();
                        modifiers.add(new VaultModifierStack(VaultModifierRegistry.get(VaultMod.id("raid")), 1));

                        CapacityCrystalProperties properties = new CapacityCrystalProperties();
                        properties.setVolume(0);
                        properties.setUnmodifiable(true);

                        crystalData.setObjective(new RaidCrystalObjective());
                        crystalData.setTheme(new PoolCrystalTheme(VaultMod.id("infinite_raid_hard")));
                        crystalData.setModifiers(modifiers);
                        crystalData.setProperties(properties);
                    })));
        });
    }

    public VaultAltarConfig.Interface zealotCrystal(VaultGod god, ItemPredicate zealotItem) {
         return new VaultAltarConfig.Interface(zealotItem, VaultCrystalItem.create(crystalData -> {
            CapacityCrystalProperties properties = new CapacityCrystalProperties();
            properties.setVolume(50);
            properties.setUnmodifiable(false);

            CrystalModifiers modifiers = new DefaultCrystalModifiers();
            modifiers.add(new VaultModifierStack(VaultModifierRegistry.get(VaultMod.id(god.getName().toLowerCase() + "_hunter")), 1));

            CrystalTheme theme = new ValueCrystalTheme(VaultMod.id("classic_vault_" + god.getName().toLowerCase() + "_normal"));

            CrystalObjective objective = new ZealotCrystalObjective(IntRoll.ofUniform(4, 6));

            crystalData.setProperties(properties);
            crystalData.setModifiers(modifiers);
            crystalData.setTheme(theme);
            crystalData.setObjective(objective);
        }));
    }
}
