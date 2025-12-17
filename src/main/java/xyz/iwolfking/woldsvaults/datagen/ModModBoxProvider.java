package xyz.iwolfking.woldsvaults.datagen;

import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.tool.conveyor.IConveyorType;
import blusunrize.immersiveengineering.common.blocks.wooden.TreatedWoodStyles;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.register.IEItems;
import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.item.StorageUpgradeItem;
import com.buuz135.industrial.module.*;
import com.progwml6.ironchest.common.block.IronChestsBlocks;
import com.progwml6.ironchest.common.item.IronChestsItems;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.tom.storagemod.StorageMod;
import ironfurnaces.IronFurnaces;
import ironfurnaces.init.Registration;
import irongenerators.init.IrongeneratorsModBlocks;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.craftingmanager.CraftingManagerModule;
import mcjty.rftoolsstorage.modules.modularstorage.ModularStorageModule;
import mcjty.rftoolsstorage.modules.scanner.StorageScannerModule;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integratedtunnels.part.PartTypes;
import shadows.hostilenetworks.Hostile;
import xyz.iwolfking.vhapi.api.datagen.boxes.AbstractModBoxProvider;
import xyz.iwolfking.vhapi.api.loaders.box.MappedWeightedProductEntryConfigLoader;
import xyz.iwolfking.vhapi.api.loaders.box.lib.MappedWeightedProductEntryConfig;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModModBoxProvider extends AbstractModBoxProvider {
    protected ModModBoxProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        add("immersive_engineering", builder -> {
            builder.addModBox("Immersive Engineering", productEntryListBuilder -> {
               productEntryListBuilder.add(IEBlocks.MetalDecoration.ENGINEERING_LIGHT.asItem(), 1, 4, null, 25);
               productEntryListBuilder.add(IEBlocks.MetalDecoration.ENGINEERING_RS.asItem(), 1, 4, null, 20);
               productEntryListBuilder.add(IEBlocks.MetalDecoration.ENGINEERING_HEAVY.asItem(), 1, 4, null, 10);
               productEntryListBuilder.add(IEBlocks.MetalDevices.FLUID_PUMP.asItem(), 1, 1, null, 10);
               productEntryListBuilder.add(IEBlocks.Metals.STORAGE.get(EnumMetals.STEEL).asItem(), 1, 3, null, 20);
               productEntryListBuilder.add(IEBlocks.MetalDevices.FLUID_PIPE.asItem(), 8, 16, null, 15);
               productEntryListBuilder.add(IEBlocks.MetalDevices.CAPACITOR_LV.asItem(), 1, 1, null, 20);
               productEntryListBuilder.add(IEBlocks.MetalDevices.CAPACITOR_MV.asItem(), 1, 1, null, 10);
               productEntryListBuilder.add(IEBlocks.MetalDevices.CAPACITOR_HV.asItem(), 1, 1, null, 5);
               productEntryListBuilder.add(IEBlocks.MetalDevices.TESLA_COIL.asItem(), 1, 1, null, 5);
               productEntryListBuilder.add(IEBlocks.MetalDevices.CLOCHE.asItem(), 1, 1, null, 5);
               productEntryListBuilder.add(IEBlocks.WoodenDevices.WOODEN_BARREL.asItem(), 2, 2, null, 20);
               productEntryListBuilder.add(IEBlocks.WoodenDecoration.TREATED_WOOD.get(TreatedWoodStyles.HORIZONTAL).asItem(), 16, 32, null, 25);
            });
        });

        add("industrial_foregoing", builder -> {
            builder.addModBox("Industrial Foregoing", productEntryListBuilder -> {
                productEntryListBuilder.add(ModuleCore.PINK_SLIME_ITEM.get(), 2, 8, null, 50);
                productEntryListBuilder.add(ModuleCore.PITY.get().asItem(), 1, 2, null, 40);
                productEntryListBuilder.add(ModuleCore.SIMPLE.get().asItem(), 1, 1, null, 30);
                productEntryListBuilder.add(ModuleCore.ADVANCED.get().asItem(), 1, 1, null, 10);
                productEntryListBuilder.add(ModuleCore.SUPREME.get().asItem(), 1, 1, null, 2);
                productEntryListBuilder.add(ModuleTransportStorage.CONVEYOR.getKey().get().asItem(), 8, 16, null, 16);
                productEntryListBuilder.add(ModuleTransportStorage.BLACK_HOLE_TANK_COMMON.getKey().get().asItem(), 1, 1, null, 10);
                productEntryListBuilder.add(ModuleTransportStorage.BLACK_HOLE_UNIT_COMMON.getKey().get().asItem(), 1, 1, null, 10);
                productEntryListBuilder.add(ModuleTransportStorage.BLACK_HOLE_CONTROLLER.getKey().get().asItem(), 1, 1, null, 1);
                productEntryListBuilder.add(ModuleResourceProduction.MARINE_FISHER.getKey().get().asItem(), 1, 1, null, 6);
                productEntryListBuilder.add(ModuleResourceProduction.FLUID_COLLECTOR.getKey().get().asItem(), 1, 1, null, 10);
                productEntryListBuilder.add(ModuleResourceProduction.WASHING_FACTORY.getKey().get().asItem(), 1, 1, null, 6);
                productEntryListBuilder.add(ModuleResourceProduction.MATERIAL_STONEWORK_FACTORY.getKey().get().asItem(), 1, 1, null, 4);
                productEntryListBuilder.add(ModuleResourceProduction.MECHANICAL_DIRT.getKey().get().asItem(), 1, 1, null, 10);
                productEntryListBuilder.add(ModuleResourceProduction.BLOCK_BREAKER.getKey().get().asItem(), 1, 1, null, 10);
                productEntryListBuilder.add(ModuleResourceProduction.BLOCK_PLACER.getKey().get().asItem(), 1, 1, null, 10);
                productEntryListBuilder.add(ModuleResourceProduction.FLUID_PLACER.getKey().get().asItem(), 1, 1, null, 10);
                productEntryListBuilder.add(ModuleResourceProduction.LASER_DRILL.getKey().get().asItem(), 1, 1, null, 4);
                productEntryListBuilder.add(ModuleTransportStorage.BLACK_HOLE_TANK_ADVANCED.getKey().get().asItem(), 1, 1, null, 6);
                productEntryListBuilder.add(ModuleTransportStorage.BLACK_HOLE_UNIT_ADVANCED.getKey().get().asItem(), 1, 1, null, 6);
                productEntryListBuilder.add(ModuleTransportStorage.BLACK_HOLE_TANK_SUPREME.getKey().get().asItem(), 1, 1, null, 2);
                productEntryListBuilder.add(ModuleTransportStorage.BLACK_HOLE_UNIT_SUPREME.getKey().get().asItem(), 1, 1, null, 2);
                productEntryListBuilder.add(ModuleCore.ETHER.getBucketFluid().get(), 1, 1, null, 10);
            });
        });

        add("integrated_dynamics", builder -> {
            builder.addModBox("Integrated Dynamics", productEntryListBuilder -> {
                productEntryListBuilder.add(RegistryEntries.ITEM_VARIABLE, 32, 64, null, 50);
                productEntryListBuilder.add(RegistryEntries.ITEM_CABLE, 6, 12, null, 50);
                productEntryListBuilder.add(RegistryEntries.BLOCK_VARIABLE_STORE.asItem(), 1, 1, null, 10);
                productEntryListBuilder.add(PartTypes.INTERFACE_ITEM.getItem(), 2, 2, null, 10);
                productEntryListBuilder.add(PartTypes.EXPORTER_ITEM.getItem(), 2, 2, null, 10);
                productEntryListBuilder.add(PartTypes.IMPORTER_ITEM.getItem(), 2, 2, null, 10);
                productEntryListBuilder.add(PartTypes.INTERFACE_ENERGY.getItem(), 2, 2, null, 10);
                productEntryListBuilder.add(PartTypes.EXPORTER_ENERGY.getItem(), 2, 2, null, 10);
                productEntryListBuilder.add(PartTypes.IMPORTER_ENERGY.getItem(), 2, 2, null, 10);
                productEntryListBuilder.add(PartTypes.INTERFACE_FLUID.getItem(), 2, 2, null, 10);
                productEntryListBuilder.add(PartTypes.EXPORTER_FLUID.getItem(), 2, 2, null, 10);
                productEntryListBuilder.add(PartTypes.IMPORTER_FLUID.getItem(), 2, 2, null, 10);
                productEntryListBuilder.add(PartTypes.PLAYER_SIMULATOR.getItem(), 2, 2, null, 2);
                productEntryListBuilder.add(PartTypes.EXPORTER_WORLD_BLOCK.getItem(), 1, 1, null, 4);
                productEntryListBuilder.add(PartTypes.EXPORTER_WORLD_ENERGY.getItem(), 1, 1, null, 4);
                productEntryListBuilder.add(PartTypes.EXPORTER_WORLD_ITEM.getItem(), 1, 1, null, 4);
                productEntryListBuilder.add(PartTypes.EXPORTER_FLUID.getItem(), 1, 1, null, 4);
                productEntryListBuilder.add(PartTypes.IMPORTER_WORLD_ENERGY.getItem(), 1, 1, null, 4);
                productEntryListBuilder.add(PartTypes.IMPORTER_WORLD_FLUID.getItem(), 1, 1, null, 4);
                productEntryListBuilder.add(PartTypes.IMPORTER_WORLD_BLOCK.getItem(), 1, 1, null, 4);
                productEntryListBuilder.add(PartTypes.IMPORTER_WORLD_ITEM.getItem(), 1, 1, null, 4);
                productEntryListBuilder.add(org.cyclops.integrateddynamics.core.part.PartTypes.INVENTORY_READER.getItem(), 1, 1, null, 4);
                productEntryListBuilder.add(org.cyclops.integrateddynamics.core.part.PartTypes.BLOCK_READER.getItem(), 1, 1, null, 4);
                productEntryListBuilder.add(org.cyclops.integrateddynamics.core.part.PartTypes.EXTRADIMENSIONAL_READER.getItem(), 1, 1, null, 2);
                productEntryListBuilder.add(org.cyclops.integrateddynamics.core.part.PartTypes.WORLD_READER.getItem(), 1, 1, null, 2);
                productEntryListBuilder.add(org.cyclops.integrateddynamics.core.part.PartTypes.AUDIO_WRITER.getItem(), 1, 1, null, 5);
                productEntryListBuilder.add(org.cyclops.integrateddynamics.core.part.PartTypes.EFFECT_WRITER.getItem(), 1, 1, null, 5);
                productEntryListBuilder.add(org.cyclops.integrateddynamics.core.part.PartTypes.NETWORK_READER.getItem(), 1, 1, null, 5);
                productEntryListBuilder.add(org.cyclops.integrateddynamics.core.part.PartTypes.REDSTONE_READER.getItem(), 1, 1, null, 5);
                productEntryListBuilder.add(org.cyclops.integrateddynamics.core.part.PartTypes.REDSTONE_WRITER.getItem(), 1, 1, null, 5);
                productEntryListBuilder.add(org.cyclops.integrateddynamics.core.part.PartTypes.CONNECTOR_MONO.getItem(), 2, 2, null, 15);
                productEntryListBuilder.add(org.cyclops.integrateddynamics.core.part.PartTypes.CONNECTOR_OMNI.getItem(), 2, 2, null, 5);
                productEntryListBuilder.add(org.cyclops.integratedterminals.RegistryEntries.ITEM_ENERGY_BATTERY, 1, 1, null, 10);
                productEntryListBuilder.add(org.cyclops.integratedterminals.RegistryEntries.ITEM_PART_TERMINAL_STORAGE, 1, 1, null, 1);
                productEntryListBuilder.add(RegistryEntries.BLOCK_CRYSTALIZED_MENRIL_BLOCK.asItem(), 16, 16, null, 30);
                productEntryListBuilder.add(RegistryEntries.BLOCK_CRYSTALIZED_CHORUS_BLOCK.asItem(), 4, 4, null, 15);
            });
        });

        add("iron_mods", builder -> {
            builder.addModBox("Oops, All Iron Mods", productEntryListBuilder -> {
                productEntryListBuilder.add(IronChestsBlocks.COPPER_CHEST.get().asItem(), 1, 1, null, 75);
                productEntryListBuilder.add(IronChestsBlocks.IRON_CHEST.get().asItem(), 1, 1, null, 50);
                productEntryListBuilder.add(IronChestsBlocks.GOLD_CHEST.get().asItem(), 1, 1, null, 25);
                productEntryListBuilder.add(IronChestsBlocks.DIAMOND_CHEST.get().asItem(), 1, 1, null, 10);
                productEntryListBuilder.add(IronChestsBlocks.CRYSTAL_CHEST.get().asItem(), 1, 1, null, 10);
                productEntryListBuilder.add(IronChestsBlocks.OBSIDIAN_CHEST.get().asItem(), 1, 1, null, 5);
                productEntryListBuilder.add(Registration.IRON_FURNACE.get().asItem(), 1, 1, null, 25);
                productEntryListBuilder.add(Registration.GOLD_FURNACE.get().asItem(), 1, 1, null, 15);
                productEntryListBuilder.add(Registration.DIAMOND_FURNACE.get().asItem(), 1, 1, null, 5);
                productEntryListBuilder.add(Registration.OBSIDIAN_FURNACE.get().asItem(), 1, 1, null, 1);
                productEntryListBuilder.add(IrongeneratorsModBlocks.GOLD_GENERATOR.get().asItem(), 1, 1, null, 25);
                productEntryListBuilder.add(IrongeneratorsModBlocks.DIAMOND_GENERATOR.get().asItem(), 1, 1, null, 10);
                productEntryListBuilder.add(IrongeneratorsModBlocks.OBSIDIAN_GENERATOR.get().asItem(), 1, 1, null, 5);
                productEntryListBuilder.add(IrongeneratorsModBlocks.IRON_GENERATOR.get().asItem(), 1, 1, null, 35);
            });
        });

        add("pneumaticcraft", builder -> {
            builder.addModBox("PneumaticCraft", productEntryListBuilder -> {
                productEntryListBuilder.add(ModItems.AMADRON_TABLET.get(), 1, 1, null, 1);
                productEntryListBuilder.add(ModItems.TRANSISTOR.get(), 2, 2, null, 15);
                productEntryListBuilder.add(ModItems.CAPACITOR.get(), 2, 2, null, 15);
                productEntryListBuilder.add(ModItems.UPGRADE_MATRIX.get(), 2, 2, null, 30);
                productEntryListBuilder.add(ModItems.SPAWNER_CORE_SHELL.get(), 1, 1, null, 10);
                productEntryListBuilder.add(ModItems.PRINTED_CIRCUIT_BOARD.get(), 1, 1, null, 5);
                productEntryListBuilder.add(ModBlocks.REINFORCED_PRESSURE_TUBE.get().asItem(), 4, 4, null, 10);
                productEntryListBuilder.add(ModBlocks.AIR_COMPRESSOR.get().asItem(), 1, 1, null, 10);
                productEntryListBuilder.add(ModBlocks.ADVANCED_AIR_COMPRESSOR.get().asItem(), 1, 1, null, 5);
                productEntryListBuilder.add(ModBlocks.ADVANCED_LIQUID_COMPRESSOR.get().asItem(), 1, 1, null, 5);
                productEntryListBuilder.add(ModBlocks.CHARGING_STATION.get().asItem(), 1, 1, null, 5);
                productEntryListBuilder.add(ModBlocks.EMPTY_SPAWNER.get().asItem(), 1, 1, null, 2);
                productEntryListBuilder.add(ModBlocks.VORTEX_TUBE.get().asItem(), 4, 4, null, 10);
                productEntryListBuilder.add(ModBlocks.ADVANCED_PRESSURE_TUBE.get().asItem(), 4, 4, null, 5);
                productEntryListBuilder.add(ModBlocks.HEAT_SINK.get().asItem(), 4, 4, null, 15);
                productEntryListBuilder.add(ModBlocks.HEAT_PIPE.get().asItem(), 1, 1, null, 10);
            });
        });

        add("sophisticated_storage", builder -> {
            builder.addModBox("Sophisticated Storage", productEntryListBuilder -> {
                productEntryListBuilder.add(net.p3pp3rf1y.sophisticatedstorage.init.ModItems.BASIC_TO_IRON_TIER_UPGRADE.get(), 1, 1, null, 30);
                productEntryListBuilder.add(net.p3pp3rf1y.sophisticatedstorage.init.ModItems.BASIC_TO_GOLD_TIER_UPGRADE.get(), 1, 1, null, 20);
                productEntryListBuilder.add(net.p3pp3rf1y.sophisticatedstorage.init.ModItems.BASIC_TO_DIAMOND_TIER_UPGRADE.get(), 1, 1, null, 10);
                productEntryListBuilder.add(net.p3pp3rf1y.sophisticatedstorage.init.ModItems.BASIC_TO_NETHERITE_TIER_UPGRADE.get(), 1, 1, null, 5);
                productEntryListBuilder.add(net.p3pp3rf1y.sophisticatedstorage.init.ModItems.STACK_UPGRADE_TIER_2.get(), 2, 2, null, 60);
                productEntryListBuilder.add(net.p3pp3rf1y.sophisticatedstorage.init.ModItems.STACK_UPGRADE_TIER_3.get(), 1, 2, null, 20);
                productEntryListBuilder.add(net.p3pp3rf1y.sophisticatedstorage.init.ModItems.STACK_UPGRADE_TIER_4.get(), 1, 2, null, 5);
                productEntryListBuilder.add(net.p3pp3rf1y.sophisticatedstorage.init.ModItems.VOID_UPGRADE.get(), 1, 1, null, 30);
                productEntryListBuilder.add(net.p3pp3rf1y.sophisticatedstorage.init.ModItems.COMPRESSION_UPGRADE.get(), 1, 1, null, 30);
                productEntryListBuilder.add(net.p3pp3rf1y.sophisticatedstorage.init.ModItems.HOPPER_UPGRADE.get(), 1, 1, null, 30);
                productEntryListBuilder.add(net.p3pp3rf1y.sophisticatedstorage.init.ModItems.FEEDING_UPGRADE.get(), 1, 1, null, 10);
                productEntryListBuilder.add(net.p3pp3rf1y.sophisticatedstorage.init.ModItems.CRAFTING_UPGRADE.get(), 1, 1, null, 10);
                productEntryListBuilder.add(net.p3pp3rf1y.sophisticatedstorage.init.ModItems.JUKEBOX_UPGRADE.get(), 1, 1, null, 5);
                productEntryListBuilder.add(net.p3pp3rf1y.sophisticatedstorage.init.ModItems.AUTO_SMELTING_UPGRADE.get(), 1, 1, null, 5);
                productEntryListBuilder.add(net.p3pp3rf1y.sophisticatedstorage.init.ModItems.MAGNET_UPGRADE.get(), 1, 1, null, 15);
                productEntryListBuilder.add(net.p3pp3rf1y.sophisticatedstorage.init.ModItems.ADVANCED_MAGNET_UPGRADE.get(), 1, 1, null, 15);
                productEntryListBuilder.add(net.p3pp3rf1y.sophisticatedstorage.init.ModItems.ADVANCED_HOPPER_UPGRADE.get(), 1, 1, null, 15);
                productEntryListBuilder.add(net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks.CONTROLLER.get().asItem(), 1, 1, null, 5);
                productEntryListBuilder.add(net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks.STORAGE_LINK.get().asItem(), 1, 1, null, 10);
            });
        });

        add("toms_simple_storage", builder -> {
            builder.addModBox("Toms Simple Storage", productEntryListBuilder -> {
                productEntryListBuilder.add(StorageMod.connector.asItem(), 1, 1, null, 40);
                productEntryListBuilder.add(StorageMod.openCrate.asItem(), 1, 1, null, 20);
                productEntryListBuilder.add(StorageMod.invCableConnector.asItem(), 1, 1, null, 20);
                productEntryListBuilder.add(StorageMod.levelEmitter.asItem(), 1, 1, null, 20);
                productEntryListBuilder.add(StorageMod.inventoryTrim.asItem(), 8, 8, null, 30);
                productEntryListBuilder.add(StorageMod.invCable.asItem(), 8, 8, null, 50);
                productEntryListBuilder.add(StorageMod.invHopperBasic.asItem(), 1, 1, null, 30);
                productEntryListBuilder.add(StorageMod.wirelessTerminal.asItem(), 1, 1, null, 3);
                productEntryListBuilder.add(StorageMod.advWirelessTerminal.asItem(), 1, 1, null, 1);
                productEntryListBuilder.add(StorageMod.terminal.asItem(), 1, 1, null, 20);
                productEntryListBuilder.add(StorageMod.craftingTerminal.asItem(), 1, 1, null, 5);
            });
        });

        add("rftools_storage", builder -> {
            builder.addModBox("RFTools Storage", productEntryListBuilder -> {
                productEntryListBuilder.add(StorageScannerModule.STORAGE_SCANNER_ITEM.get(), 1, 1, null, 10);
                productEntryListBuilder.add(ModularStorageModule.MODULAR_STORAGE_ITEM.get(), 1, 1, null, 50);
                productEntryListBuilder.add(ModularStorageModule.STORAGE_MODULE0.get(), 1, 1, null, 200);
                productEntryListBuilder.add(ModularStorageModule.STORAGE_MODULE1.get(), 1, 1, null, 100);
                productEntryListBuilder.add(ModularStorageModule.STORAGE_MODULE3.get(), 1, 1, null, 25);
            });
        });

        add("functional_storage", builder -> {
            builder.addModBox("Functional Storage", productEntryListBuilder -> {
                productEntryListBuilder.add(FunctionalStorage.COMPACTING_DRAWER.getKey().get().asItem(), 1, 1, null, 10);
                productEntryListBuilder.add(FunctionalStorage.FLUID_DRAWER_1.getKey().get().asItem(), 1, 1, null, 10);
                productEntryListBuilder.add(FunctionalStorage.DRAWER_CONTROLLER.getKey().get().asItem(), 1, 1, null, 1);
                productEntryListBuilder.add(FunctionalStorage.CONTROLLER_EXTENSION.getKey().get().asItem(), 1, 1, null, 5);
                productEntryListBuilder.add(FunctionalStorage.COLLECTOR_UPGRADE.get(), 1, 1, null, 5);
                productEntryListBuilder.add(FunctionalStorage.PULLING_UPGRADE.get(), 1, 1, null, 5);
                productEntryListBuilder.add(FunctionalStorage.VOID_UPGRADE.get(), 1, 1, null, 5);
                productEntryListBuilder.add(FunctionalStorage.ENDER_DRAWER.getKey().get().asItem(), 1, 1, null, 4);
                productEntryListBuilder.add(FunctionalStorage.STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.COPPER).get(), 1, 1, null, 40);
                productEntryListBuilder.add(FunctionalStorage.STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.GOLD).get(), 1, 1, null, 20);
                productEntryListBuilder.add(FunctionalStorage.STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.DIAMOND).get(), 1, 1, null, 10);
                productEntryListBuilder.add(FunctionalStorage.STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.NETHERITE).get(), 1, 1, null, 5);
                productEntryListBuilder.add(FunctionalStorage.FRAMED_COMPACTING_DRAWER.getKey().get().asItem(), 1, 1, null, 10);
            });
        });

        add("hostile_neural_networks", builder -> {
            builder.addModBox("Hostile Neural Networks", productEntryListBuilder -> {
                productEntryListBuilder.add(Hostile.Items.EMPTY_PREDICTION, 32, 64, null, 120);
                productEntryListBuilder.add(Hostile.Items.BLANK_DATA_MODEL, 1, 1, null, 60);
                productEntryListBuilder.add(Hostile.Blocks.SIM_CHAMBER.asItem(), 1, 1, null, 5);
                productEntryListBuilder.add(Hostile.Blocks.LOOT_FABRICATOR.asItem(), 1, 1, null, 5);
            });
        });

        add("create", builder -> {
            builder.addModBox("Create", productEntryListBuilder -> {
                productEntryListBuilder.add(AllBlocks.ROTATION_SPEED_CONTROLLER.get().asItem(), 1, 1, null, 5);
                productEntryListBuilder.add(AllItems.POTATO_CANNON.get().asItem(), 1, 1, null, 3);
                productEntryListBuilder.add(AllItems.WAND_OF_SYMMETRY.get().asItem(), 1, 1, null, 2);
            });
        });

        add("mekanism", builder -> {
            builder.addModBox("Mekanism", productEntryListBuilder -> {
                productEntryListBuilder.add(MekanismBlocks.ULTIMATE_ENERGY_CUBE.asItem(), 1, 1, null, 3);
                productEntryListBuilder.add(MekanismBlocks.STEEL_BLOCK.asItem(), 4, 8, null, 15);
                productEntryListBuilder.add(MekanismItems.FLAMETHROWER.get(), 1, 1, null, 2);
                productEntryListBuilder.add(MekanismItems.ULTIMATE_TIER_INSTALLER.get(), 1, 1, null, 2);
                productEntryListBuilder.add(MekanismItems.ATOMIC_ALLOY.get(), 2, 8, null, 5);
                productEntryListBuilder.add(MekanismItems.REINFORCED_ALLOY.get(), 4, 12, null, 20);
                productEntryListBuilder.add(MekanismItems.INFUSED_ALLOY.get(), 16, 16, null, 35);
                productEntryListBuilder.add(MekanismItems.FREE_RUNNERS.get(), 1, 1, null, 2);
            });
        });

    }

}
