package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.vhapi.api.datagen.AbstractTooltipProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.init.ModItems;

public class ModTooltipsProvider extends AbstractTooltipProvider {
    protected ModTooltipsProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {

        add("builtin_overrides", builder -> {
            builder
                    .addTooltipEntry(iskallia.vault.init.ModBlocks.VOID_CRUCIBLE.getRegistryName(), "Use to whitelist items you don't want to void with <light_purple>Void + Pickup<gray> mode on Vault Magnets.")
                    .addTooltipEntry(iskallia.vault.init.ModItems.RELIC_BOOSTER_PACK.getRegistryName(), "Legacy Item");
        });

        add("supplementaries_cage", builder -> {
            builder
                    .addTooltipEntry(ResourceLocation.fromNamespaceAndPath("supplementaries", "mob_cage"), "Can capture any mob below 10% health.");
        });

        add("wolds_items", builder -> {
            builder
                    .addTooltipEntry(ModBlocks.MOD_BOX_WORKSTATION.getRegistryName(), "Used to create Targeted Mod Boxes that will only give items from a particular mod.")
                    .addTooltipEntry(ModItems.SOUL_ICHOR.getRegistryName(), "Used in the Black Market to reroll the offers.")
                    .addTooltipEntry(ModItems.AUGMENT_PIECE.getRegistryName(), "Used in the Augment Assembly Pedestal to craft Augments, can be obtained by recycling Augments.")
                    .addTooltipEntry(ModItems.MERCY_ORB.getRegistryName(), "Combine in anvil with a weapon to enchant it with Curse of Mercy, preventing it from killing mobs.")
                    .addTooltipEntry(ModItems.TOME_OF_TENOS.getRegistryName(), "Place on Vault Altar to create a Tenos Zealot Vault.")
                    .addTooltipEntry(ModItems.WENDARR_GEM.getRegistryName(), "Place on Vault Altar to create a Wendarr Zealot Vault.")
                    .addTooltipEntry(ModItems.VELARA_APPLE.getRegistryName(), "Place on Vault Altar to create a Velara Zealot Vault.")
                    .addTooltipEntry(ModItems.IDONA_DAGGER.getRegistryName(), "Place on Vault Altar to create a Idona Zealot Vault.")
                    .addTooltipEntry(ModItems.ECCENTRIC_FOCUS.getRegistryName(), "Use with Vault Gear in the Artisan Station to add an unusual modifier.")
                    .addTooltipEntry(ModItems.SUSPENSION_FOCUS.getRegistryName(), "Use with Vault Gear in the Artisan Station to freeze a Legendary, Greater, Corrupted, or Unusual modifier.")
                    .addTooltipEntry(ModItems.BLAZING_FOCUS.getRegistryName(), "Use with Vault Gear in the Artisan Station to unfreeze all frozen modifiers.")
                    .addTooltipEntry(ModItems.STYLISH_FOCUS.getRegistryName(), "Use with Vault Tool in an anvil to add Stylish, causing it to rotate through all tool types visually.")
                    .addTooltipEntry(ModItems.CRYSTAL_REINFORCEMENT.getRegistryName(), "Use with Vault Tool in an anvil to add 10 capacity.")
                    .addTooltipEntry(ModItems.RESONATING_REINFORCEMENT.getRegistryName(), "Use with Vault Tool in an anvil to add 10 capacity. (can be used a limited number of times)")
                    .addTooltipEntry(ModItems.REPAIR_AUGMENTER.getRegistryName(), "Use with Vault Gear in an anvil to add a repair slot.")
                    .addTooltipEntry(ModItems.WANING_AUGMENTER.getRegistryName(), "Use with Vault Gear in an anvil to add a suffix slot. (Legacy)")
                    .addTooltipEntry(ModItems.WAXING_AUGMENTER.getRegistryName(), "Use with Vault Gear in an anvil to add a prefix slot. (Legacy)")
                    .addTooltipEntry(ModItems.LAYOUT_MANIPULATOR.getRegistryName(), "Use in the Crystal Workbench to set the Vault's Layout")
                    .addTooltipEntry(ModItems.CRYSTAL_SEAL_DOOMSAYER.getRegistryName(), "Use in the Crystal Workbench to set the objective to 'Ballistic Bingo'")
                    .addTooltipEntry(ModItems.CRYSTAL_SEAL_WARRIOR.getRegistryName(), "Use in the Crystal Workbench to set the objective to 'Slay the Guardians'")
                    .addTooltipEntry(ModItems.CRYSTAL_SEAL_UNHINGED.getRegistryName(), "Use in the Crystal Workbench to set the objective to 'Unhinged Scavenger Hunt'")
                    .addTooltipEntry(ModItems.CRYSTAL_SEAL_TITAN.getRegistryName(), "Use in the Crystal Workbench to set the objective to 'Slay the Brutal Bosses'")
                    .addTooltipEntry(ModItems.CRYSTAL_SEAL_ENCHANTER.getRegistryName(), "Use in the Crystal Workbench to set the objective to 'Enchanted Elixir'")
                    .addTooltipEntry(ModItems.CRYSTAL_SEAL_SPIRITS.getRegistryName(), "Use in the Crystal Workbench to set the objective to 'Haunted Braziers'")
                    .addTooltipEntry(ModItems.CRYSTAL_SEAL_CORRUPT.getRegistryName(), "Use in the Crystal Workbench to set the objective to 'Corrupted'")
                    .addTooltipEntry(ModItems.CRYSTAL_SEAL_SURVIVOR.getRegistryName(), "Use in the Crystal Workbench to set the objective to 'Survival'")
                    .addTooltipEntry(ModItems.CRYSTAL_SEAL_UNHINGED_SCAVINGO.getRegistryName(), "Use in the Crystal Workbench to set the objective to 'Unhinged Collector'")
                    .addTooltipEntry(ModItems.VENDOOR_CAPSTONE.getRegistryName(), "Use in the Crystal Workbench as a capstone, making the crystal unmodifiable and adding the 'Vendoor Hunter' modifier.")
                    .addTooltipEntry(ModItems.ENCHANTED_CAPSTONE.getRegistryName(), "Use in the Crystal Workbench as a capstone, making the crystal unmodifiable and adding the 'Enchanted' modifier.")
                    .addTooltipEntry(ModItems.FRENZY_CAPSTONE.getRegistryName(), "Use in the Crystal Workbench as a capstone, making the crystal unmodifiable and adding the 'Frenzy' curse.")
                    .addTooltipEntry(ModItems.PROSPEROUS_CAPSTONE.getRegistryName(), "Use in the Crystal Workbench as a capstone, making the crystal unmodifiable and adding the 'Prosperous' modifier, guaranteeing an artifact")
                    .addTooltipEntry(ModItems.ALL_SEEING_EYE_CAPSTONE.getRegistryName(), "Use in the Crystal Workbench as a capstone, making the crystal unmodifiable and adding the 'All Seeing Eye' modifier.")
                    .addTooltipEntry(ModItems.EXPERTISE_ORB_ITEM.getRegistryName(), "Requires Vault Level 100 to use, adds 1 Expertise point unless you are maxed.")
                    .addTooltipEntry(ModItems.ALTAR_DECATALYZER.getRegistryName(), "Sneak and right-click on your Vault Altar to reroll the recipe.")
                    .addTooltipEntry(ModBlocks.DECO_OBELISK_BLOCK.getRegistryName(), "Decorative version of the Obelisk. Right-click the base to summon lightning!")
                    .addTooltipEntry(ModBlocks.DECO_LODESTONE_BLOCK.getRegistryName(), "Decorative version of the Lodestone. Sneak and right-click to get a message!")
                    .addTooltipEntry(ModBlocks.DECO_MONOLITH_BLOCK.getRegistryName(), "Decorative version of the Brazier. Right-click to cycle through display options.")
                    .addTooltipEntry(ModBlocks.DECO_SCAVENGER_ALTAR_BLOCK.getRegistryName(), "Decorative version of the Scavenger Altar.")
                    .addTooltipEntry(ModItems.VAULT_DECO_SCROLL.getRegistryName(), "Used to create decorative versions of the Vault Objective blocks.")
                    .addTooltipEntry(ModItems.POG_PRISM.getRegistryName(), "Ingredient for mid-tier crafting recipes.")
                    .addTooltipEntry(ModItems.EXTRAORDINARY_POG_PRISM.getRegistryName(), "Ingredient for high-end crafting recipes.")
                    .addTooltipEntry(ModItems.INSCRIPTION_BOX.getRegistryName(), "Use to create a random Inscription, including some special ones.")
                    .addTooltipEntry(ModItems.OMEGA_BOX.getRegistryName(), "Use to create a random valuable item.")
                    .addTooltipEntry(ModItems.WOLD_STAR_CHUNK.getRegistryName(), "An extremely rare item used in crafting a Wold Star.")
                    .addTooltipEntry(ModItems.WOLD_STAR.getRegistryName(), "Used in crafting Creative items.")
                    .addTooltipEntry(ModItems.SMASHED_VAULT_GEM.getRegistryName(), "Obtained when a vault ore would normally drop nothing.")
                    .addTooltipEntry(ModItems.GREED_TREE_RESET_ITEM.getRegistryName(), "Use to reset your Greed Tree unlocks!")
                    .addTooltipEntry(ModBlocks.OWNED_CRAFTING_TABLE_BLOCK.getRegistryName(), "Use with <aqua>Integrated Dynamics's<yellow> Crafting Interface<white> when you want to utilize research locked recipes!")
                    .addTooltipEntry(ModBlocks.TRINKET_FUSION_BLOCK.getRegistryName(), "Combines <light_purple>Trinkets into <gold>Fusion Trinkets<gray> using the power of <light_purple>Prismatic Glue")
                    .addTooltipEntry(ModItems.CONCEALED_CHAOS.getRegistryName(), "Use in the <aqua>Crystal Workbench<gray> to add random modifiers to your <yellow>Vault Crystal<gray>, has a chance to backfire! Consumes 50 capacity.");
        });
    }
}
