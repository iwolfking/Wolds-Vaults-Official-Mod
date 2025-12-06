package xyz.iwolfking.woldsvaults.events;

import iskallia.vault.VaultMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModItems;

@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MissingMappingsEvents {
    @SubscribeEvent
    public static void onMissingMappings(RegistryEvent.MissingMappings<Block> event) {

        for (RegistryEvent.MissingMappings.Mapping<Block> mapping : event.getAllMappings()) {

            String path = mapping.key.getPath();

//            if (path.equals("old_block_name")) {
//                mapping.remap(YourModBlocks.NEW_BLOCK);
//            }
//
//            // Example remapping vault_stone_1 → vault_stone
//            if (path.matches("vault_stone_\\d+")) {
//                mapping.remap(YourModBlocks.VAULT_STONE);
//            }
        }
    }

    @SubscribeEvent
    public static void onMissingMappingsItem(RegistryEvent.MissingMappings<Item> event) {

        for (RegistryEvent.MissingMappings.Mapping<Item> mapping : event.getAllMappings()) {
            if (mapping.key.equals(VaultMod.id("chromatic_gold_ingot"))) {
                mapping.remap(ModItems.CHROMATIC_GOLD_INGOT);
            }
            else if (mapping.key.equals(VaultMod.id("chromatic_gold_nugget"))) {
                mapping.remap(ModItems.CHROMATIC_GOLD_NUGGET);
            }
            else if (mapping.key.equals(VaultMod.id("crystal_seal_monolith"))) {
                mapping.remap(iskallia.vault.init.ModItems.CRYSTAL_SEAL_SCOUT);
            }
            else if (mapping.key.equals(VaultMod.id("crystal_seal_unhinged"))) {
                mapping.remap(ModItems.CRYSTAL_SEAL_UNHINGED);
            }
            else if (mapping.key.equals(VaultMod.id("crystal_seal_spirits"))) {
                mapping.remap(ModItems.CRYSTAL_SEAL_SPIRITS);
            }
            else if (mapping.key.equals(VaultMod.id("crystal_seal_enchanter"))) {
                mapping.remap(ModItems.CRYSTAL_SEAL_ENCHANTER);
            }
            else if (mapping.key.equals(VaultMod.id("crystal_seal_titan"))) {
                mapping.remap(ModItems.CRYSTAL_SEAL_TITAN);
            }
            else if (mapping.key.equals(VaultMod.id("crystal_seal_doomsayer"))) {
                mapping.remap(ModItems.CRYSTAL_SEAL_DOOMSAYER);
            }
            else if (mapping.key.equals(VaultMod.id("crystal_seal_doomsayer"))) {
                mapping.remap(ModItems.CRYSTAL_SEAL_DOOMSAYER);
            }
            else if (mapping.key.equals(VaultMod.id("gem_box"))) {
                mapping.remap(ModItems.GEM_BOX);
            }
            else if (mapping.key.equals(VaultMod.id("supply_box"))) {
                mapping.remap(ModItems.SUPPLY_BOX);
            }
            else if (mapping.key.equals(VaultMod.id("augment_box"))) {
                mapping.remap(ModItems.AUGMENT_BOX);
            }
            else if (mapping.key.equals(VaultMod.id("altar_recatalyzer"))) {
                mapping.remap(ModItems.ALTAR_DECATALYZER);
            }
            else if (mapping.key.equals(VaultMod.id("wold_star_chunk"))) {
                mapping.remap(ModItems.WOLD_STAR_CHUNK);
            }
            else if (mapping.key.equals(VaultMod.id("wold_star"))) {
                mapping.remap(ModItems.WOLD_STAR);
            }
            else if (mapping.key.equals(VaultMod.id("pog_prism"))) {
                mapping.remap(ModItems.POG_PRISM);
            }
            else if (mapping.key.equals(VaultMod.id("gem_reagent_ashium"))) {
                mapping.remap(ModItems.GEM_REAGENT_ASHIUM);
            }
            else if (mapping.key.equals(VaultMod.id("gem_reagent_bomignite"))) {
                mapping.remap(ModItems.GEM_REAGENT_BOMIGNITE);
            }
            else if (mapping.key.equals(VaultMod.id("gem_reagent_gorginite"))) {
                mapping.remap(ModItems.GEM_REAGENT_GORGINITE);
            }
            else if (mapping.key.equals(VaultMod.id("gem_reagent_petzanite"))) {
                mapping.remap(ModItems.GEM_REAGENT_PETEZANITE);
            }
            else if (mapping.key.equals(VaultMod.id("gem_reagent_iskallium"))) {
                mapping.remap(ModItems.GEM_REAGENT_ISKALLIUM);
            }
            else if (mapping.key.equals(VaultMod.id("gem_reagent_tubium"))) {
                mapping.remap(ModItems.GEM_REAGENT_TUBIUM);
            }
            else if (mapping.key.equals(VaultMod.id("gem_reagent_upaline"))) {
                mapping.remap(ModItems.GEM_REAGENT_GORGINITE);
            }
            else if (mapping.key.equals(VaultMod.id("gem_reagent_xenium"))) {
                mapping.remap(ModItems.GEM_REAGENT_BOMIGNITE);
            }
            else if (mapping.key.equals(VaultMod.id("gem_reagent_sparkletine"))) {
                mapping.remap(ModItems.GEM_REAGENT_PETEZANITE);
            }
            else if (mapping.key.equals(VaultMod.id("smashed_vault_gem"))) {
                mapping.remap(ModItems.SMASHED_VAULT_GEM);
            }
            else if (mapping.key.equals(VaultMod.id("smashed_vault_gem_cluster"))) {
                mapping.remap(ModItems.SMASHED_VAULT_GEM_CLUSTER);
            }
            else if (mapping.key.equals(new ResourceLocation("uninfused_terrasteel_ingot"))) {
                mapping.remap(ModItems.UNINFUSED_TERRASTEEL_INGOT);
            }
        }
    }
}
