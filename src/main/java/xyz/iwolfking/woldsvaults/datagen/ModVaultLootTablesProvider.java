package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.init.ModItems;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractTooltipProvider;
import xyz.iwolfking.vhapi.api.datagen.gen.AbstractLootTableProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModVaultLootTablesProvider extends AbstractLootTableProvider {
    protected ModVaultLootTablesProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    private static final List<VaultGearItem> gearItems = new ArrayList<VaultGearItem>(Arrays.asList(ModItems.CHESTPLATE, ModItems.BOOTS, ModItems.LEGGINGS, ModItems.HELMET, ModItems.SWORD, ModItems.AXE, ModItems.FOCUS, ModItems.WAND, ModItems.MAGNET, ModItems.SHIELD, xyz.iwolfking.woldsvaults.init.ModItems.BATTLESTAFF, xyz.iwolfking.woldsvaults.init.ModItems.LOOT_SACK, xyz.iwolfking.woldsvaults.init.ModItems.RANG, xyz.iwolfking.woldsvaults.init.ModItems.PLUSHIE, xyz.iwolfking.woldsvaults.init.ModItems.TRIDENT));

    @Override
    public void registerLootTables() {
        add(VaultMod.id("survival_gear_cache"), lootBuilder -> {
            lootBuilder.entry(entryBuilder -> {
                entryBuilder.rolls(1, 1)
                        .pool(1, poolBuilder -> {
                            gearItems.forEach(item -> poolBuilder.itemNbt(6, item.getItem().getRegistryName().toString(), 1, 1, nbt -> {
                                nbt.put("the_vault:gear_roll_type", "Rare+");
                            }));
                            gearItems.forEach(item -> poolBuilder.itemNbt(2, item.getItem().getRegistryName().toString(), 1, 1, nbt -> {
                                nbt.put("the_vault:gear_roll_type", "Rare+");
                                nbt.put("the_vault:is_legendary", "true");
                            }));
                            gearItems.forEach(item -> poolBuilder.itemNbt(1, item.getItem().getRegistryName().toString(), 1, 1, nbt -> {
                                nbt.put("the_vault:gear_roll_type_pool", "Chaotic");
                            }));
                            poolBuilder.itemNbt(6, ModItems.MAGNET.getRegistryName().toString(), 1, 1, nbt -> {
                                nbt.put("the_vault:gear_unique_pool", "woldsvaults:aural_magnet");
                                nbt.put("the_vault:gear_roll_type", "Unique");
                            });
                            poolBuilder.itemNbt(6, ModItems.SWORD.getRegistryName().toString(), 1, 1, nbt -> {
                                nbt.put("the_vault:gear_unique_pool", "woldsvaults:lava_chicken_sword");
                                nbt.put("the_vault:gear_roll_type", "Unique");
                            });
                            poolBuilder.itemNbt(6, xyz.iwolfking.woldsvaults.init.ModItems.TRIDENT.getRegistryName().toString(), 1, 1, nbt -> {
                                nbt.put("the_vault:gear_unique_pool", "woldsvaults:fork_of_the_glutton");
                                nbt.put("the_vault:gear_roll_type", "Unique");
                            });
                        });
            });
        });
    }
}
