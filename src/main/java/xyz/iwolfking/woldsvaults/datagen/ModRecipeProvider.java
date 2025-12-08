package xyz.iwolfking.woldsvaults.datagen;

import com.simibubi.create.AllItems;
import iskallia.vault.VaultMod;
import iskallia.vault.tags.ModItemTags;
import me.dinnerbeef.compressium.Compressium;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.data.recipes.NbtOutputResult;
import xyz.iwolfking.vhapi.api.util.ResourceLocUtils;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.init.ModCompressibleBlocks;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.recipes.lib.InfuserRecipeBuilder;
import xyz.iwolfking.woldsvaults.recipes.lib.NbtAwareRecipe;
import xyz.iwolfking.woldsvaults.recipes.lib.UncheckedRecipe;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipeProvider(DataGenerator pGenerator) {
        super(pGenerator);
    }

    @Override
    protected void buildCraftingRecipes(@NotNull Consumer<FinishedRecipe> pFinishedRecipeConsumer) {
//        ShapedRecipeBuilder.shaped(ModItems.XL_BACKPACK)
//                .define('I', ModItems.PRISMATIC_FIBER)
//                .define('L', Registry.ITEM.get(new ResourceLocation("the_vault", "wold_star_chunk")))
//                .define('P', ModItems.CHROMA_CORE)
//                .define('B', Registry.ITEM.get(new ResourceLocation("sophisticatedbackpacks", "netherite_backpack")))
//                .define('S', iskallia.vault.init.ModBlocks.BLACK_CHROMATIC_STEEL_BLOCK)
//                .define('M', iskallia.vault.init.ModItems.OMEGA_POG)
//                .pattern("ILI")
//                .pattern("PBP")
//                .pattern("SMS")
//                .unlockedBy("has_netherite_backpack", has(Registry.ITEM.get(new ResourceLocation("sophisticatedbackpacks", "netherite_backpack"))))
//                .save(pFinishedRecipeConsumer);

        CompoundTag colossus = new CompoundTag();
        colossus.putString("Ability", "Colossus");
        ShapedRecipeBuilder.shaped(iskallia.vault.init.ModItems.RESPEC_FLASK)
                .define('A', iskallia.vault.init.ModItems.VAULT_ESSENCE)
                .define('D', Blocks.GRANITE)
                .define('B', Items.GLASS_BOTTLE)
                .define('C', iskallia.vault.init.ModItems.PERFECT_BENITOITE)
                .pattern("ADA")
                .pattern("ABA")
                .pattern("ACA")
                .unlockedBy("has_perfect_benitoite", has(iskallia.vault.init.ModItems.PERFECT_BENITOITE))
                .save(output -> pFinishedRecipeConsumer.accept(new NbtOutputResult(output, colossus)), VaultMod.id("respec_flask_colossus"));

        CompoundTag expunge = new CompoundTag();
        expunge.putString("Ability", "Expunge");
        ShapedRecipeBuilder.shaped(iskallia.vault.init.ModItems.RESPEC_FLASK)
                .define('A', iskallia.vault.init.ModItems.VAULT_ESSENCE)
                .define('D', Items.BEETROOT)
                .define('B', Items.GLASS_BOTTLE)
                .define('C', iskallia.vault.init.ModItems.PERFECT_BENITOITE)
                .pattern("ADA")
                .pattern("ABA")
                .pattern("ACA")
                .unlockedBy("has_perfect_benitoite", has(iskallia.vault.init.ModItems.PERFECT_BENITOITE))
                .save(output -> pFinishedRecipeConsumer.accept(new NbtOutputResult(output, expunge)), VaultMod.id("respec_flask_expunge"));


        ShapedRecipeBuilder.shaped(ModBlocks.CHROMATIC_STEEL_INFUSER_BLOCK)
                .define('A', iskallia.vault.init.ModItems.CHROMATIC_STEEL_INGOT)
                .define('B', ModBlocks.VAULT_INFUSER_BLOCK)
                .define('C', ModItems.CHROMA_CORE)
                .define('D', iskallia.vault.init.ModBlocks.VAULT_DIAMOND_BLOCK)
                .define('V', iskallia.vault.init.ModItems.ERROR_ITEM) // placeholder
                .pattern("ADA")
                .pattern("VBV")
                .pattern("ACA")
                .unlockedBy("has_infuser", has(ModBlocks.VAULT_INFUSER_BLOCK))
                .save(recipeCosumer -> pFinishedRecipeConsumer.accept(new UncheckedRecipe(recipeCosumer, Map.of(
                        'V', new ResourceLocation("the_vault", "vault_essence_1")
                ))));

        ShapedRecipeBuilder.shaped(ModBlocks.VAULT_INFUSER_BLOCK)
                .define('A', iskallia.vault.init.ModItems.CHROMATIC_IRON_INGOT)
                .define('B', Blocks.FURNACE)
                .define('C', iskallia.vault.init.ModItems.PERFECT_LARIMAR)
                .define('D', iskallia.vault.init.ModItems.VAULT_DIAMOND)
                .define('V', iskallia.vault.init.ModItems.VAULT_ESSENCE)
                .pattern("ADA")
                .pattern("VBV")
                .pattern("ACA")
                .unlockedBy("has_furnace", has(Blocks.FURNACE))
                .save(pFinishedRecipeConsumer);

        ShapedRecipeBuilder.shaped(ModItems.NULLITE_CRYSTAL)
                .define('N', ModItems.NULLITE_FRAGMENT)
                .define('E', ModItems.RUINED_ESSENCE)
                .pattern(" N ")
                .pattern("NEN")
                .pattern(" N ")
                .unlockedBy("has_nullite", has(ModItems.NULLITE_FRAGMENT))
                .save(pFinishedRecipeConsumer);


        ShapedRecipeBuilder.shaped(ModItems.CHROMA_CORE)
                .define('L', iskallia.vault.init.ModItems.PERFECT_LARIMAR)
                .define('G', Registry.ITEM.get(new ResourceLocation("the_vault", "chromatic_gold_nugget")))
                .define('B', iskallia.vault.init.ModItems.PERFECT_BENITOITE)
                .define('A', iskallia.vault.init.ModItems.PERFECT_ALEXANDRITE)
                .define('W', iskallia.vault.init.ModItems.PERFECT_WUTODIE)
                .define('P', iskallia.vault.init.ModItems.PERFECT_PAINITE)
                .pattern("LGB")
                .pattern("GAG")
                .pattern("WGP")
                .unlockedBy("has_perfect_benitoite", has(iskallia.vault.init.ModItems.PERFECT_BENITOITE))
                .save(pFinishedRecipeConsumer);

        ShapedRecipeBuilder.shaped(ModBlocks.AUGMENT_CRAFTING_TABLE)
                .define('N', Registry.ITEM.get(new ResourceLocation("the_vault", "pog_prism")))
                .define('I', iskallia.vault.init.ModItems.VAULT_INGOT)
                .define('L', Blocks.LECTERN)
                .define('C', iskallia.vault.init.ModItems.AUGMENT)
                .pattern("NCN")
                .pattern("ILI")
                .pattern("III")
                .unlockedBy("has_augment", has(iskallia.vault.init.ModItems.AUGMENT))
                .save(pFinishedRecipeConsumer);


        ShapedRecipeBuilder.shaped(ModBlocks.MOD_BOX_WORKSTATION)
                .define('N', Registry.ITEM.get(new ResourceLocation("the_vault", "pog_prism")))
                .define('I', iskallia.vault.init.ModItems.VAULT_INGOT)
                .define('L', Blocks.CRAFTING_TABLE) // placeholder
                .define('C', iskallia.vault.init.ModItems.MOD_BOX)
                .pattern("NCN")
                .pattern("ILI")
                .pattern("III")
                .unlockedBy("has_modbox", has(iskallia.vault.init.ModItems.MOD_BOX))
                .save(recipeCosumer -> pFinishedRecipeConsumer.accept(new UncheckedRecipe(recipeCosumer, Map.of(
                        'L', new ResourceLocation("craftingstation", "crafting_station")
                ))));

        ShapedRecipeBuilder.shaped(ModBlocks.WEAVING_STATION)
                .define('N', iskallia.vault.init.ModItems.SILVER_SCRAP)
                .define('I', iskallia.vault.init.ModItems.DRIFTWOOD)
                .define('L', Blocks.FLETCHING_TABLE)
                .define('C', iskallia.vault.init.ModItems.VAULT_DIAMOND)
                .pattern("NCN")
                .pattern("ILI")
                .pattern("III")
                .unlockedBy("has_vault_diamond", has(iskallia.vault.init.ModItems.VAULT_DIAMOND))
                .save(pFinishedRecipeConsumer);

        ShapedRecipeBuilder.shaped(ModItems.FILTER_NECKLACE)
                .define('F', Registry.ITEM.get(new ResourceLocation("create", "attribute_filter")))
                .define('P', ModItems.POG_PRISM)
                .define('I', iskallia.vault.init.ModBlocks.MAGIC_SILK_BLOCK)
                .pattern(" II")
                .pattern(" PI")
                .pattern("F  ")
                .unlockedBy("has_attribute_filter", has(Registry.ITEM.get(new ResourceLocation("create", "attribute_filter"))))
                .save(pFinishedRecipeConsumer);

        CompoundTag basicVanilla = new CompoundTag();
        basicVanilla.putString("id", "woldsvaults:basic_vanilla");
        ShapedRecipeBuilder.shaped(ModItems.TRINKET_POUCH)
                .define('E', iskallia.vault.init.ModItems.VAULT_ESSENCE)
                .define('L', Items.LEATHER)
                .define('S', iskallia.vault.init.ModItems.MAGIC_SILK)
                .define('V', iskallia.vault.init.ModItems.VAULT_DIAMOND)
                .pattern("SES")
                .pattern("SLS")
                .pattern("LVL")
                .unlockedBy("has_magic_silk", has(iskallia.vault.init.ModItems.MAGIC_SILK))
                .save(output -> pFinishedRecipeConsumer.accept(new NbtOutputResult(output, basicVanilla)), WoldsVaults.id("trinket_pouch_basic_vanilla")); // thanks vazkii

        CompoundTag basicAltR = new CompoundTag();
        basicAltR.putString("id", "woldsvaults:basic_alt_r");
        ShapedRecipeBuilder.shaped(ModItems.TRINKET_POUCH)
                .define('E', iskallia.vault.init.ModItems.LARIMAR_GEM)
                .define('L', Items.LEATHER)
                .define('S', iskallia.vault.init.ModItems.MAGIC_SILK)
                .define('V', iskallia.vault.init.ModItems.VAULT_DIAMOND)
                .pattern("SES")
                .pattern("SLS")
                .pattern("LVL")
                .unlockedBy("has_magic_silk", has(iskallia.vault.init.ModItems.MAGIC_SILK))
                .save(output -> pFinishedRecipeConsumer.accept(new NbtOutputResult(output, basicAltR)), WoldsVaults.id("trinket_pouch_basic_alt_r")); // thanks vazkii

        CompoundTag basicAltG = new CompoundTag();
        basicAltG.putString("id", "woldsvaults:basic_alt_g");
        ShapedRecipeBuilder.shaped(ModItems.TRINKET_POUCH)
                .define('E', iskallia.vault.init.ModItems.BENITOITE_GEM)
                .define('L', Items.LEATHER)
                .define('S', iskallia.vault.init.ModItems.MAGIC_SILK)
                .define('V', iskallia.vault.init.ModItems.VAULT_DIAMOND)
                .pattern("SES")
                .pattern("SLS")
                .pattern("LVL")
                .unlockedBy("has_magic_silk", has(iskallia.vault.init.ModItems.MAGIC_SILK))
                .save(output -> pFinishedRecipeConsumer.accept(new NbtOutputResult(output, basicAltG)), WoldsVaults.id("trinket_pouch_basic_alt_g")); // thanks vazkii


        ShapedRecipeBuilder.shaped(ModItems.IDONA_DAGGER)
                .pattern("M#M")
                .pattern("EAE")
                .pattern("MSM")
                .define('#', iskallia.vault.init.ModItems.VAULT_ROCK)
                .define('E', iskallia.vault.init.ModItems.WUTODIC_MASS)
                .define('A', iskallia.vault.init.ModItems.AUGMENT)
                .define('S', iskallia.vault.init.ModItems.GOD_TOKEN)
                .define('M', iskallia.vault.init.ModItems.MEMORY_SHARD)
                .unlockedBy("has_memory_shard", has(iskallia.vault.init.ModItems.MEMORY_SHARD))
                .save(recipeConsumer -> pFinishedRecipeConsumer.accept(new NbtAwareRecipe(recipeConsumer, Map.of(
                        'A', new NbtAwareRecipe.IngredientWithNBT("the_vault:augment", "{\"theme\":\"the_vault:classic_vault_idona_normal\"}"),
                        'S', new NbtAwareRecipe.IngredientWithNBT("the_vault:god_token", "{\"type\":\"idona\"}")
                ))));

        ShapedRecipeBuilder.shaped(ModItems.TOME_OF_TENOS)
                .pattern("M#M")
                .pattern("EAE")
                .pattern("MSM")
                .define('#', iskallia.vault.init.ModItems.VAULT_ROCK)
                .define('E', iskallia.vault.init.ModItems.WUTODIC_MASS)
                .define('A', iskallia.vault.init.ModItems.AUGMENT)
                .define('S', iskallia.vault.init.ModItems.GOD_TOKEN)
                .define('M', iskallia.vault.init.ModItems.MEMORY_SHARD)
                .unlockedBy("has_memory_shard", has(iskallia.vault.init.ModItems.MEMORY_SHARD))
                .save(recipeConsumer -> pFinishedRecipeConsumer.accept(new NbtAwareRecipe(recipeConsumer, Map.of(
                        'A', new NbtAwareRecipe.IngredientWithNBT("the_vault:augment", "{\"theme\":\"the_vault:classic_vault_tenos_normal\"}"),
                        'S', new NbtAwareRecipe.IngredientWithNBT("the_vault:god_token", "{\"type\":\"tenos\"}")
                ))));

        ShapedRecipeBuilder.shaped(ModItems.VELARA_APPLE)
                .pattern("M#M")
                .pattern("EAE")
                .pattern("MSM")
                .define('#', iskallia.vault.init.ModItems.VAULT_ROCK)
                .define('E', iskallia.vault.init.ModItems.WUTODIC_MASS)
                .define('A', iskallia.vault.init.ModItems.AUGMENT)
                .define('S', iskallia.vault.init.ModItems.GOD_TOKEN)
                .define('M', iskallia.vault.init.ModItems.MEMORY_SHARD)
                .unlockedBy("has_memory_shard", has(iskallia.vault.init.ModItems.MEMORY_SHARD))
                .save(recipeConsumer -> pFinishedRecipeConsumer.accept(new NbtAwareRecipe(recipeConsumer, Map.of(
                        'A', new NbtAwareRecipe.IngredientWithNBT("the_vault:augment", "{\"theme\":\"the_vault:classic_vault_velara_normal\"}"),
                        'S', new NbtAwareRecipe.IngredientWithNBT("the_vault:god_token", "{\"type\":\"velara\"}")
                ))));

        ShapedRecipeBuilder.shaped(ModItems.WENDARR_GEM)
                .pattern("M#M")
                .pattern("EAE")
                .pattern("MSM")
                .define('#', iskallia.vault.init.ModItems.VAULT_ROCK)
                .define('E', iskallia.vault.init.ModItems.WUTODIC_MASS)
                .define('A', iskallia.vault.init.ModItems.AUGMENT)
                .define('S', iskallia.vault.init.ModItems.GOD_TOKEN)
                .define('M', iskallia.vault.init.ModItems.MEMORY_SHARD)
                .unlockedBy("has_memory_shard", has(iskallia.vault.init.ModItems.MEMORY_SHARD))
                .save(recipeConsumer -> pFinishedRecipeConsumer.accept(new NbtAwareRecipe(recipeConsumer, Map.of(
                        'A', new NbtAwareRecipe.IngredientWithNBT("the_vault:augment", "{\"theme\":\"the_vault:classic_vault_wendarr_normal\"}"),
                        'S', new NbtAwareRecipe.IngredientWithNBT("the_vault:god_token", "{\"type\":\"wendarr\"}")
                ))));


        ShapelessRecipeBuilder.shapeless(ModItems.PRISMATIC_FIBER, 9)
                .requires(ModBlocks.PRISMATIC_FIBER_BLOCK)
                .unlockedBy("has_prismatic_fiber_block", has(ModBlocks.PRISMATIC_FIBER_BLOCK))
                .save(pFinishedRecipeConsumer);

        ShapelessRecipeBuilder.shapeless(ModBlocks.PRISMATIC_FIBER_BLOCK)
                .requires(ModItems.PRISMATIC_FIBER, 9)
                .unlockedBy("has_prismatic_fiber", has(ModItems.PRISMATIC_FIBER))
                .save(pFinishedRecipeConsumer);

        ShapelessRecipeBuilder.shapeless(ModItems.VAULT_DIAMOND_NUGGET, 9)
                .requires(iskallia.vault.init.ModItems.VAULT_DIAMOND)
                .unlockedBy("has_vault_diamond", has(iskallia.vault.init.ModItems.VAULT_DIAMOND))
                .save(pFinishedRecipeConsumer);

        ShapelessRecipeBuilder.shapeless(iskallia.vault.init.ModItems.VAULT_DIAMOND)
                .requires(ModItems.VAULT_DIAMOND_NUGGET, 9)
                .unlockedBy("has_vault_diamond_nugget", has(ModItems.VAULT_DIAMOND_NUGGET))
                .save(pFinishedRecipeConsumer);

        new InfuserRecipeBuilder(
                iskallia.vault.init.ModItems.GRAPES,
                iskallia.vault.init.ModItems.VAULT_APPLE,
                iskallia.vault.init.ModItems.BITTER_LEMON,
                40,
                9
        ).unlockedBy("has_grapes", has(iskallia.vault.init.ModItems.GRAPES)).save(pFinishedRecipeConsumer);

        new InfuserRecipeBuilder(
                iskallia.vault.init.ModItems.ECHO_GEM,
                ModItems.POGOMINIUM_INGOT,
                iskallia.vault.init.ModItems.ECHOING_INGOT,
                160,
                16
        ).unlockedBy("has_echo", has(iskallia.vault.init.ModItems.ECHO_GEM)).save(pFinishedRecipeConsumer);

        new InfuserRecipeBuilder(
                iskallia.vault.init.ModItems.SWEET_KIWI,
                iskallia.vault.init.ModItems.VAULT_APPLE,
                iskallia.vault.init.ModItems.GRAPES,
                40,
                9
        ).unlockedBy("has_kiwi", has(iskallia.vault.init.ModItems.SWEET_KIWI)).save(pFinishedRecipeConsumer);

        new InfuserRecipeBuilder(
                iskallia.vault.init.ModItems.VAULT_ESSENCE,
                iskallia.vault.init.ModItems.DRIFTWOOD,
                ModItems.INFUSED_DRIFTWOOD,
                80,
                4
        ).unlockedBy("has_essence", has(iskallia.vault.init.ModItems.VAULT_ESSENCE)).save(pFinishedRecipeConsumer);

        new InfuserRecipeBuilder(
                iskallia.vault.init.ModItems.POG,
                iskallia.vault.init.ModItems.ETERNAL_SOUL,
                iskallia.vault.init.ModItems.INFUSED_ETERNAL_SOUL,
                120,
                16
        ).unlockedBy("has_pog", has(iskallia.vault.init.ModItems.POG)).save(pFinishedRecipeConsumer);

        new InfuserRecipeBuilder(
                iskallia.vault.init.ModItems.BITTER_LEMON,
                iskallia.vault.init.ModItems.VAULT_APPLE,
                iskallia.vault.init.ModItems.MANGO,
                40,
                9
        ).unlockedBy("has_lemon", has(iskallia.vault.init.ModItems.BITTER_LEMON)).save(pFinishedRecipeConsumer);

        new InfuserRecipeBuilder(
                iskallia.vault.init.ModItems.POG,
                iskallia.vault.init.ModItems.BLACK_CHROMATIC_STEEL_INGOT,
                ModItems.POGOMINIUM_INGOT,
                80,
                16
        ).unlockedBy("has_pog", has(iskallia.vault.init.ModItems.POG)).save(pFinishedRecipeConsumer);

        new InfuserRecipeBuilder(
                iskallia.vault.init.ModItems.POG,
                iskallia.vault.init.ModBlocks.MAGIC_SILK_BLOCK,
                ModItems.PRISMATIC_FIBER,
                40,
                9
        ).unlockedBy("has_pog", has(iskallia.vault.init.ModItems.POG)).save(pFinishedRecipeConsumer);

        new InfuserRecipeBuilder(
                iskallia.vault.init.ModItems.VAULT_ESSENCE,
                Items.APPLE,
                iskallia.vault.init.ModItems.VAULT_APPLE,
                40,
                9
        ).unlockedBy("has_essence", has(iskallia.vault.init.ModItems.VAULT_ESSENCE)).save(pFinishedRecipeConsumer);

        companionRerollRecipe("companion_temporalizer", pFinishedRecipeConsumer);

        gemBlockRecipe("block_gem_wutodie", ModBlocks.WUTODIE, iskallia.vault.init.ModItems.WUTODIE_GEM, pFinishedRecipeConsumer);

        ShapedRecipeBuilder.shaped(ModItems.EXPERTISE_ORB_ITEM)
                .define('X', ModItems.ARCANE_SHARD)
                .define('O', iskallia.vault.init.ModItems.SKILL_ORB_FRAME)
                .pattern("XXX")
                .pattern("XOX")
                .pattern("XXX")
                .unlockedBy("has_orb_frame", has(iskallia.vault.init.ModItems.SKILL_ORB_FRAME))
                .save(pFinishedRecipeConsumer);

        ShapedRecipeBuilder.shaped(ModItems.REPAIR_AUGMENTER)
                .define('X', ModItems.ARCANE_SHARD)
                .define('O', iskallia.vault.init.ModItems.SKILL_ORB_FRAME)
                .define('T', iskallia.vault.init.ModItems.BLACK_CHROMATIC_STEEL_INGOT)
                .pattern("XTX")
                .pattern("TOT")
                .pattern("XTX")
                .unlockedBy("has_resilient_focus", has(iskallia.vault.init.ModItems.RESILIENT_FOCUS))
                .save(pFinishedRecipeConsumer);

        ShapedRecipeBuilder.shaped(ModItems.VAULT_ROCK_CANDY)
                .define('X', iskallia.vault.init.ModItems.VAULT_ROCK)
                .define('O', iskallia.vault.init.ModBlocks.VAULT_SWEETS_ITEM)
                .define('T', Items.SUGAR)
                .pattern("XTX")
                .pattern("XOX")
                .pattern("XTX")
                .unlockedBy("has_sweets", has(iskallia.vault.init.ModBlocks.VAULT_SWEETS_ITEM))
                .save(pFinishedRecipeConsumer);

        ShapedRecipeBuilder.shaped(ModItems.MERCY_ORB)
                .define('X', iskallia.vault.init.ModItems.PAINITE_GEM)
                .define('O', iskallia.vault.init.ModItems.GORGINITE_CLUSTER)
                .define('T', Blocks.PINK_WOOL)
                .pattern("XTX")
                .pattern("TOT")
                .pattern("XTX")
                .unlockedBy("has_gorginite_cluster", has(iskallia.vault.init.ModItems.GORGINITE_CLUSTER))
                .save(pFinishedRecipeConsumer);

        ShapedRecipeBuilder.shaped(ModItems.CRYSTAL_SEAL_UNHINGED)
                .define('X', iskallia.vault.init.ModItems.DREAMSTONE)
                .define('O', iskallia.vault.init.ModItems.CRYSTAL_SEAL_HUNTER)
                .define('G', iskallia.vault.init.ModBlocks.PACKED_VAULT_MEAT_BLOCK)
                .define('B', iskallia.vault.init.ModItems.EXTRAORDINARY_PAINITE)
                .pattern("XGX")
                .pattern("XOX")
                .pattern("XBX")
                .unlockedBy("has_scav_seal", has(iskallia.vault.init.ModItems.CRYSTAL_SEAL_HUNTER))
                .save(pFinishedRecipeConsumer);

        ShapedRecipeBuilder.shaped(ModItems.CRYSTAL_SEAL_SPIRITS)
                .define('X', iskallia.vault.init.ModItems.ETERNAL_SOUL)
                .define('O', iskallia.vault.init.ModItems.CRYSTAL_SEAL_SCOUT)
                .define('G', Blocks.SOUL_SAND)
                .define('B', iskallia.vault.init.ModItems.EXTRAORDINARY_WUTODIE)
                .pattern("XGX")
                .pattern("XOX")
                .pattern("XBX")
                .unlockedBy("has_brazier_seal", has(iskallia.vault.init.ModItems.CRYSTAL_SEAL_SCOUT))
                .save(pFinishedRecipeConsumer);

        ShapedRecipeBuilder.shaped(iskallia.vault.init.ModItems.CRYSTAL_SEAL_ARCHITECT)
                .define('X', iskallia.vault.init.ModItems.WUTODIC_SILVER_INGOT)
                .define('O', iskallia.vault.init.ModItems.CRYSTAL_SEAL_EMPTY)
                .define('G', Blocks.SCAFFOLDING)
                .define('B', iskallia.vault.init.ModItems.PERFECT_LARIMAR)
                .define('L', iskallia.vault.init.ModItems.VAULT_ESSENCE)
                .pattern("LGL")
                .pattern("XOX")
                .pattern("LBL")
                .unlockedBy("has_blank_seal", has(iskallia.vault.init.ModItems.CRYSTAL_SEAL_EMPTY))
                .save(pFinishedRecipeConsumer);

        ShapedRecipeBuilder.shaped(ModItems.CRYSTAL_SEAL_ENCHANTER)
                .define('X', ModBlocks.VAULT_ESSENCE_BLOCK)
                .define('O', iskallia.vault.init.ModItems.CRYSTAL_SEAL_SAGE)
                .define('G', Blocks.ENCHANTING_TABLE)
                .define('B', iskallia.vault.init.ModItems.EXTRAORDINARY_ALEXANDRITE)
                .pattern("XGX")
                .pattern("XOX")
                .pattern("XBX")
                .unlockedBy("has_elixir_seal", has(iskallia.vault.init.ModItems.CRYSTAL_SEAL_SAGE))
                .save(pFinishedRecipeConsumer);

        ShapedRecipeBuilder.shaped(ModItems.CRYSTAL_SEAL_TITAN)
                .define('X', ModBlocks.VAULT_ESSENCE_BLOCK)
                .define('O', iskallia.vault.init.ModItems.CRYSTAL_SEAL_EXECUTIONER)
                .define('G', Items.DIAMOND_SWORD)
                .define('B', iskallia.vault.init.ModBlocks.VAULT_DIAMOND_BLOCK)
                .pattern("XGX")
                .pattern("XOX")
                .pattern("XBX")
                .unlockedBy("has_executioner_seal", has(iskallia.vault.init.ModItems.CRYSTAL_SEAL_EXECUTIONER))
                .save(pFinishedRecipeConsumer);

        ShapedRecipeBuilder.shaped(ModItems.INSCRIPTION_BOX)
                .define('X', iskallia.vault.init.ModItems.WUTODIC_MASS)
                .define('R', iskallia.vault.init.ModItems.POG)
                .define('G', iskallia.vault.init.ModItems.INSCRIPTION)
                .pattern("GXG")
                .pattern("XRX")
                .pattern("GXG")
                .unlockedBy("has_inscription", has(iskallia.vault.init.ModItems.INSCRIPTION))
                .save(pFinishedRecipeConsumer);

        ShapedRecipeBuilder.shaped(ModItems.GEM_BOX)
                .define('G', ModItems.SMASHED_VAULT_GEM)
                .define('T', iskallia.vault.init.ModItems.CARBON_NUGGET)
                .define('X', iskallia.vault.init.ModItems.VAULT_DIAMOND)
                .define('R', ModBlocks.VAULT_ESSENCE_BLOCK)
                .pattern("GTG")
                .pattern("XRX")
                .pattern("GTG")
                .unlockedBy("has_smashed_vault_gem", has(ModItems.SMASHED_VAULT_GEM))
                .save(pFinishedRecipeConsumer);

        ShapelessRecipeBuilder.shapeless(ModItems.GEM_BOX, 1)
                .requires(ModItems.SMASHED_VAULT_GEM_CLUSTER, 1)
                .requires(iskallia.vault.init.ModItems.VAULT_DIAMOND, 2)
                .requires(ModBlocks.VAULT_ESSENCE_BLOCK, 1)
                .requires(iskallia.vault.init.ModItems.CARBON_NUGGET, 2)
                .unlockedBy("smashed_vault_gem_cluster", has(ModItems.SMASHED_VAULT_GEM_CLUSTER))
                .save(pFinishedRecipeConsumer, WoldsVaults.id("gem_box_shortcut"));

        ShapedRecipeBuilder.shaped(ModItems.POG_PRISM)
                .define('A', iskallia.vault.init.ModItems.PERFECT_LARIMAR)
                .define('B', iskallia.vault.init.ModItems.PERFECT_BENITOITE)
                .define('C', iskallia.vault.init.ModItems.PERFECT_ALEXANDRITE)
                .define('X', iskallia.vault.init.ModItems.POG)
                .define('R', iskallia.vault.init.ModItems.ECHO_GEM)
                .define('D', iskallia.vault.init.ModItems.PERFECT_PAINITE)
                .define('E', iskallia.vault.init.ModItems.PERFECT_WUTODIE)
                .define('F', iskallia.vault.init.ModItems.PERFECT_BLACK_OPAL)
                .pattern("ABC")
                .pattern("XRX")
                .pattern("DEF")
                .unlockedBy("has_pog", has(iskallia.vault.init.ModItems.POG))
                .save(pFinishedRecipeConsumer);

        ShapedRecipeBuilder.shaped(ModItems.EXTRAORDINARY_POG_PRISM)
                .define('A', iskallia.vault.init.ModItems.ECHO_POG)
                .define('B', iskallia.vault.init.ModItems.EXTRAORDINARY_BENITOITE)
                .define('C', iskallia.vault.init.ModItems.EXTRAORDINARY_ALEXANDRITE)
                .define('X', ModItems.POG_PRISM)
                .define('R', iskallia.vault.init.ModItems.EXTRAORDINARY_ECHO_GEM)
                .define('D', iskallia.vault.init.ModItems.EXTRAORDINARY_PAINITE)
                .define('E', iskallia.vault.init.ModItems.EXTRAORDINARY_WUTODIE)
                .define('F', iskallia.vault.init.ModItems.PERFECT_BLACK_OPAL)
                .pattern("ABC")
                .pattern("XRX")
                .pattern("DEF")
                .unlockedBy("has_pog_prism", has(ModItems.EXTRAORDINARY_POG_PRISM))
                .save(pFinishedRecipeConsumer);

        ShapedRecipeBuilder.shaped(iskallia.vault.init.ModItems.SKILL_ORB_FRAME)
                .define('S', iskallia.vault.init.ModItems.SUBLIME_VAULT_SUBSTANCE)
                .define('X', iskallia.vault.init.ModItems.EXTRAORDINARY_BLACK_OPAL)
                .define('E', iskallia.vault.init.ModItems.SUBLIME_VAULT_ELIXIR)
                .define('V', iskallia.vault.init.ModItems.SUBLIME_VAULT_VISION)
                .define('M', iskallia.vault.init.ModItems.MEMORY_CRYSTAL)
                .define('R', iskallia.vault.init.ModItems.OMEGA_POG)
                .pattern("SXE")
                .pattern("XRX")
                .pattern("VXM")
                .unlockedBy("has_omega_pog", has(iskallia.vault.init.ModItems.OMEGA_POG))
                .save(pFinishedRecipeConsumer);

        ShapelessRecipeBuilder.shapeless(ModItems.UNINFUSED_TERRASTEEL_INGOT, 1)
                .requires(vazkii.botania.common.item.ModItems.manaSteel)
                .requires(vazkii.botania.common.item.ModItems.manaPearl)
                .requires(vazkii.botania.common.item.ModItems.manaDiamond)
                .requires(iskallia.vault.init.ModItems.VAULT_ESSENCE)
                .requires(iskallia.vault.init.ModItems.VAULT_DIAMOND)
                .requires(iskallia.vault.init.ModItems.CHROMATIC_STEEL_INGOT)
                .unlockedBy("manasteel", has(vazkii.botania.common.item.ModItems.manaSteel))
                .save(pFinishedRecipeConsumer);

        ShapedRecipeBuilder.shaped(ModBlocks.VAULT_SALVAGER_BLOCK)
                .define('X', iskallia.vault.init.ModBlocks.VAULT_RECYCLER)
                .define('G', iskallia.vault.init.ModItems.BLACK_CHROMATIC_STEEL_INGOT)
                .define('B', iskallia.vault.init.ModItems.ECHO_POG)
                .pattern("   ")
                .pattern("X X")
                .pattern("GBG")
                .unlockedBy("has_recycler", has(iskallia.vault.init.ModBlocks.VAULT_RECYCLER))
                .save(pFinishedRecipeConsumer);

        ShapedRecipeBuilder.shaped(ModBlocks.DECO_LODESTONE_BLOCK)
                .define('X', iskallia.vault.init.ModItems.WUTODIE_GEM)
                .define('G', Blocks.AMETHYST_BLOCK)
                .define('R', ModItems.VAULT_DECO_SCROLL)
                .pattern("GXG")
                .pattern("XRX")
                .pattern("GXG")
                .unlockedBy("has_deco_scroll", has(ModItems.VAULT_DECO_SCROLL))
                .save(pFinishedRecipeConsumer);

        ShapedRecipeBuilder.shaped(ModBlocks.DECO_OBELISK_BLOCK)
                .define('X', iskallia.vault.init.ModItems.PAINITE_GEM)
                .define('G', iskallia.vault.init.ModBlocks.LIVING_ROCK_BLOCK_COBBLE)
                .define('R', ModItems.VAULT_DECO_SCROLL)
                .pattern("GXG")
                .pattern("GRG")
                .pattern("GXG")
                .unlockedBy("has_deco_scroll", has(ModItems.VAULT_DECO_SCROLL))
                .save(pFinishedRecipeConsumer);

        ShapedRecipeBuilder.shaped(ModBlocks.DECO_SCAVENGER_ALTAR_BLOCK)
                .define('X', iskallia.vault.init.ModItems.ALEXANDRITE_GEM)
                .define('G', Blocks.SMOOTH_STONE)
                .define('R', ModItems.VAULT_DECO_SCROLL)
                .pattern("GXG")
                .pattern(" R ")
                .pattern("GXG")
                .unlockedBy("has_deco_scroll", has(ModItems.VAULT_DECO_SCROLL))
                .save(pFinishedRecipeConsumer);

        ShapedRecipeBuilder.shaped(ModBlocks.DECO_MONOLITH_BLOCK)
                .define('X', iskallia.vault.init.ModItems.ETERNAL_SOUL)
                .define('G', iskallia.vault.init.ModBlocks.ORNATE_BLOCK)
                .define('R', ModItems.VAULT_DECO_SCROLL)
                .define('C', Blocks.CAMPFIRE)
                .pattern(" C ")
                .pattern("GRG")
                .pattern("GXG")
                .unlockedBy("has_deco_scroll", has(ModItems.VAULT_DECO_SCROLL))
                .save(pFinishedRecipeConsumer);

        ShapedRecipeBuilder.shaped(ModBlocks.SURVIVAL_MOB_BARRIER, 16)
                .define('X', iskallia.vault.init.ModItems.PAINITE_GEM)
                .define('Y', Blocks.RED_STAINED_GLASS)
                .define('G', Items.BONE)
                .pattern("XYX")
                .pattern("YGY")
                .pattern("XYX")
                .unlockedBy("has_painite", has(iskallia.vault.init.ModItems.PAINITE_GEM))
                .save(pFinishedRecipeConsumer);

        ShapelessRecipeBuilder.shapeless(ModItems.SMASHED_VAULT_GEM_CLUSTER, 1)
                .requires(ModItems.SMASHED_VAULT_GEM, 4)
                .unlockedBy("smashed_vault_gem", has(ModItems.SMASHED_VAULT_GEM))
                .save(pFinishedRecipeConsumer);

        ShapelessRecipeBuilder.shapeless(ModItems.SMASHED_VAULT_GEM, 4)
                .requires(ModItems.SMASHED_VAULT_GEM_CLUSTER, 1)
                .unlockedBy("smashed_vault_gem_cluster", has(ModItems.SMASHED_VAULT_GEM_CLUSTER))
                .save(pFinishedRecipeConsumer);

        ShapelessRecipeBuilder.shapeless(ModItems.CHROMATIC_GOLD_INGOT, 1)
                .requires(Items.GOLD_INGOT, 2)
                .requires(ModBlocks.VAULT_ESSENCE_BLOCK, 1)
                .requires(iskallia.vault.init.ModItems.MAGIC_SILK, 1)
                .unlockedBy("gold_ingot", has(Items.GOLD_INGOT))
                .save(pFinishedRecipeConsumer);

        ShapelessRecipeBuilder.shapeless(ModItems.CHROMATIC_GOLD_NUGGET, 9)
                .requires(ModItems.CHROMATIC_GOLD_INGOT, 1)
                .unlockedBy("chromatic_gold_ingot", has(ModItems.CHROMATIC_GOLD_INGOT))
                .save(pFinishedRecipeConsumer);

        ShapelessRecipeBuilder.shapeless(ModItems.CHROMATIC_GOLD_INGOT, 1)
                .requires(ModItems.CHROMATIC_GOLD_NUGGET, 9)
                .unlockedBy("chromatic_gold_nugget", has(ModItems.CHROMATIC_GOLD_NUGGET))
                .save(pFinishedRecipeConsumer, WoldsVaults.id("chromatic_gold_nugget_to_ingot"));

        ShapelessRecipeBuilder.shapeless(ModBlocks.CHROMATIC_GOLD_BLOCK, 1)
                .requires(ModItems.CHROMATIC_GOLD_INGOT, 9)
                .unlockedBy("chromatic_gold_ingot", has(ModItems.CHROMATIC_GOLD_INGOT))
                .save(pFinishedRecipeConsumer);

        ShapelessRecipeBuilder.shapeless(ModItems.CHROMATIC_GOLD_INGOT, 9)
                .requires(ModBlocks.CHROMATIC_GOLD_BLOCK, 1)
                .unlockedBy("chromatic_gold_block", has(ModBlocks.CHROMATIC_GOLD_BLOCK))
                .save(pFinishedRecipeConsumer, WoldsVaults.id("chromatic_gold_block_to_ingot"));

        ShapelessRecipeBuilder.shapeless(iskallia.vault.init.ModItems.VAULT_INGOT, 1)
                .requires(iskallia.vault.init.ModItems.CHROMATIC_IRON_INGOT, 1)
                .requires(iskallia.vault.init.ModItems.CHROMATIC_STEEL_INGOT, 1)
                .requires(ModItems.CHROMATIC_GOLD_INGOT, 1)
                .requires(ModItems.SMASHED_VAULT_GEM_CLUSTER, 1)
                .unlockedBy("chromatic_gold_ingot", has(ModItems.CHROMATIC_GOLD_INGOT))
                .save(pFinishedRecipeConsumer);

        ShapelessRecipeBuilder.shapeless(ModItems.ARCANE_SHARD, 1)
                .requires(ModItems.ARCANE_ESSENCE, 9)
                .unlockedBy("arcane_essence", has(ModItems.ARCANE_ESSENCE))
                .save(pFinishedRecipeConsumer);

        ShapelessRecipeBuilder.shapeless(ModItems.ARCANE_ESSENCE, 9)
                .requires(ModItems.ARCANE_SHARD, 1)
                .unlockedBy("arcane_shard", has(ModItems.ARCANE_SHARD))
                .save(pFinishedRecipeConsumer);

        ShapedRecipeBuilder.shaped(ModItems.CRYSTAL_REINFORCEMENT)
                .define('Y', ModItems.CHROMA_CORE)
                .define('X', iskallia.vault.init.ModItems.OMEGA_POG)
                .define('B', iskallia.vault.init.ModBlocks.BLACK_CHROMATIC_STEEL_BLOCK)
                .define('O', ModItems.WOLD_STAR_CHUNK)
                .define('S', ModItems.CHUNK_OF_POWER)
                .pattern("YXY")
                .pattern("BOB")
                .pattern("YSY")
                .unlockedBy("has_wold_star_chunk", has(ModItems.WOLD_STAR_CHUNK))
                .save(pFinishedRecipeConsumer);

        ShapedRecipeBuilder.shaped(ModItems.RESONATING_REINFORCEMENT)
                .define('Y', iskallia.vault.init.ModBlocks.CHROMATIC_STEEL_BLOCK)
                .define('X', ModItems.POG_PRISM)
                .define('O', iskallia.vault.init.ModItems.HARDENED_WUTODIC_MASS)
                .define('S', iskallia.vault.init.ModItems.BLACK_CHROMATIC_STEEL_INGOT)
                .pattern("SXS")
                .pattern("YOY")
                .pattern("SXS")
                .unlockedBy("has_pog_prism", has(ModItems.POG_PRISM))
                .save(pFinishedRecipeConsumer);

        ShapedRecipeBuilder.shaped(ModItems.UBER_CHAOS_CATALYST)
                .define('C', iskallia.vault.init.ModItems.VAULT_CATALYST_CHAOS)
                .define('O', ModItems.HEART_OF_CHAOS)
                .pattern("CCC")
                .pattern("COC")
                .pattern("CCC")
                .unlockedBy("has_heart_of_chaos", has(ModItems.HEART_OF_CHAOS))
                .save(pFinishedRecipeConsumer);

        ShapedRecipeBuilder.shaped(ModItems.WOLD_STAR)
                .define('C', ModItems.WOLD_STAR_CHUNK)
                .define('O', iskallia.vault.init.ModItems.OMEGA_POG)
                .pattern("CCC")
                .pattern("COC")
                .pattern("CCC")
                .unlockedBy("has_omega_pog", has(iskallia.vault.init.ModItems.OMEGA_POG))
                .save(pFinishedRecipeConsumer);



        ShapedRecipeBuilder.shaped(iskallia.vault.init.ModItems.MEMORY_POWDER)
                .define('C', iskallia.vault.init.ModItems.KNOWLEDGE_STAR_ESSENCE)
                .define('O', iskallia.vault.init.ModItems.PERFECT_BENITOITE)
                .pattern("CCC")
                .pattern("COC")
                .pattern("CCC")
                .unlockedBy("has_perfect_beni", has(iskallia.vault.init.ModItems.PERFECT_BENITOITE))
                .save(pFinishedRecipeConsumer);

        ShapedRecipeBuilder.shaped(iskallia.vault.init.ModItems.MEMORY_SHARD)
                .define('C', iskallia.vault.init.ModItems.MEMORY_POWDER)
                .pattern("CCC")
                .pattern("CCC")
                .pattern("CCC")
                .unlockedBy("has_memory_powder", has(iskallia.vault.init.ModItems.MEMORY_POWDER))
                .save(pFinishedRecipeConsumer);

        ShapedRecipeBuilder.shaped(iskallia.vault.init.ModItems.RED_VAULT_ESSENCE)
                .define('C', iskallia.vault.init.ModItems.VAULT_ESSENCE)
                .define('O', iskallia.vault.init.ModItems.PERFECT_PAINITE)
                .pattern("CCC")
                .pattern("COC")
                .pattern("CCC")
                .unlockedBy("has_perfect_painite", has(iskallia.vault.init.ModItems.PERFECT_PAINITE))
                .save(pFinishedRecipeConsumer);

        ShapedRecipeBuilder.shaped(iskallia.vault.init.ModItems.SUBLIME_VAULT_SUBSTANCE)
                .define('C', iskallia.vault.init.ModItems.EXTRAORDINARY_PAINITE)
                .define('A', iskallia.vault.init.ModItems.EXTRAORDINARY_ALEXANDRITE)
                .define('D', ModBlocks.VAULT_ESSENCE_BLOCK)
                .define('O', iskallia.vault.init.ModBlocks.PACKED_VAULT_MEAT_BLOCK)
                .pattern("CAC")
                .pattern("DOD")
                .pattern("CAC")
                .unlockedBy("has_packed_vault_meat", has(iskallia.vault.init.ModBlocks.PACKED_VAULT_MEAT_BLOCK))
                .save(pFinishedRecipeConsumer);

        ShapedRecipeBuilder.shaped(iskallia.vault.init.ModItems.SUBLIME_VAULT_VISION)
                .define('C', iskallia.vault.init.ModItems.MYSTICAL_POWDER)
                .define('D', iskallia.vault.init.ModItems.DREAMSTONE)
                .define('O', ModItems.POG_PRISM)
                .pattern("CDC")
                .pattern("DOD")
                .pattern("CDC")
                .unlockedBy("has_dreamstone", has(iskallia.vault.init.ModItems.DREAMSTONE))
                .save(pFinishedRecipeConsumer);

        ShapedRecipeBuilder.shaped(iskallia.vault.init.ModItems.SUBLIME_VAULT_ELIXIR)
                .define('C', iskallia.vault.init.ModBlocks.VAULT_DIAMOND_BLOCK)
                .define('D', iskallia.vault.init.ModItems.HARDENED_WUTODIC_MASS)
                .define('O', Items.GLASS_BOTTLE)
                .pattern("CDC")
                .pattern("DOD")
                .pattern("CDC")
                .unlockedBy("has_hardened_wutodic_mass", has(iskallia.vault.init.ModItems.HARDENED_WUTODIC_MASS))
                .save(pFinishedRecipeConsumer);

        ShapedRecipeBuilder.shaped(ModItems.HEART_OF_CHAOS)
                .define('B', iskallia.vault.init.ModItems.BLACK_CHROMATIC_STEEL_INGOT)
                .define('M', iskallia.vault.init.ModItems.MEMORY_CRYSTAL)
                .define('E', iskallia.vault.init.ModItems.SUBLIME_VAULT_ELIXIR)
                .define('X', iskallia.vault.init.ModItems.SUBLIME_VAULT_SUBSTANCE)
                .define('U', ModItems.POG_PRISM)
                .pattern("BMB")
                .pattern("EXE")
                .pattern("BUB")
                .unlockedBy("has_sublime_elixir", has(iskallia.vault.init.ModItems.SUBLIME_VAULT_ELIXIR))
                .save(pFinishedRecipeConsumer);

        List<String> REAGENT_TYPES = List.of("ashium", "bomignite", "gorginite", "iskallium", "petzanite", "sparkletine", "tubium", "upaline", "xenium");

        REAGENT_TYPES.forEach(type -> {
            ShapelessRecipeBuilder.shapeless(ForgeRegistries.ITEMS.getValue(VaultMod.id("gem_" + type)), 2)
                    .requires(ForgeRegistries.ITEMS.getValue(WoldsVaults.id("gem_reagent_" + type)), 1)
                    .requires(Ingredient.of(ModItemTags.GEMS), 1)
                    .unlockedBy("has_gem_reagent_" + type, has(ForgeRegistries.ITEMS.getValue(WoldsVaults.id("gem_reagent_" + type))))
                    .save(pFinishedRecipeConsumer);

            ShapelessRecipeBuilder.shapeless(ForgeRegistries.ITEMS.getValue(WoldsVaults.id("gem_reagent_" + type)), 1)
                    .requires(ForgeRegistries.ITEMS.getValue(VaultMod.id("gem_" + type)), 1)
                    .requires(iskallia.vault.init.ModItems.WILD_FOCUS, 1)
                    .requires(iskallia.vault.init.ModItems.VAULT_DIAMOND, 1)
                    .requires(ModItems.SMASHED_VAULT_GEM_CLUSTER, 1)
                    .unlockedBy("has_gem_" + type, has(ForgeRegistries.ITEMS.getValue(VaultMod.id("gem_" + type))))
                    .save(pFinishedRecipeConsumer);
        });

        compactingRecipe(ModBlocks.SILVER_SCRAP_BLOCK, iskallia.vault.init.ModItems.SILVER_SCRAP, pFinishedRecipeConsumer);
        compactingRecipe(ModBlocks.VAULT_ESSENCE_BLOCK, iskallia.vault.init.ModItems.VAULT_ESSENCE, pFinishedRecipeConsumer);
        compactingRecipe(ModBlocks.CARBON_BLOCK, iskallia.vault.init.ModItems.CARBON, pFinishedRecipeConsumer);
        compactingRecipe(ModBlocks.VAULT_INGOT_BLOCK, iskallia.vault.init.ModItems.VAULT_INGOT, pFinishedRecipeConsumer);
        compactingRecipe(ModBlocks.VAULT_PLATING_BLOCK, iskallia.vault.init.ModItems.VAULT_PLATING, pFinishedRecipeConsumer);
        compactingRecipe(ModBlocks.OMEGA_POG_BLOCK, iskallia.vault.init.ModItems.OMEGA_POG, pFinishedRecipeConsumer);
        compactingRecipe(ModBlocks.ECHO_POG_BLOCK, iskallia.vault.init.ModItems.ECHO_POG, pFinishedRecipeConsumer);
        compactingRecipe(ModBlocks.POG_BLOCK, iskallia.vault.init.ModItems.POG, pFinishedRecipeConsumer);

        for(DyeColor color : DyeColor.values()) {
            unobtanium(ModItems.COLORED_UNOBTANIUMS.get(color), ModBlocks.COLORED_UNOBTANIUMS.get(color), pFinishedRecipeConsumer);
        }
        unobtanium(ModItems.RAINBOW_UNOBTANIUM, ModBlocks.RAINBOW_UNOBTANIUM, pFinishedRecipeConsumer);

        ModCompressibleBlocks.getRegisteredBlocks().forEach((k, v) -> {
            var name = k.name().toLowerCase();

            Block baseBlock = ForgeRegistries.BLOCKS.getValue(k.baseResourceLocation());
            ShapelessRecipeBuilder // Uses Minecraft Blocks Here
                    .shapeless(baseBlock, 9)
                    .requires(v.get(0).get())
                    .unlockedBy("has_compressed_" + name + "_x1", has(v.get(0).get()))
                    .save(pFinishedRecipeConsumer, Compressium.MODID + ":" + name + "_" + 1 + "_uncraft");
            ShapedRecipeBuilder
                    .shaped(v.get(0).get())
                    .define('#', baseBlock)
                    .pattern("###").pattern("###").pattern("###")
                    .unlockedBy("has_" + name, has(baseBlock))
                    .save(pFinishedRecipeConsumer);

            for (int i = 0; i < v.size() - 1; i ++) {
                int index = i + 1;
                ShapelessRecipeBuilder
                        .shapeless(v.get(index - 1).get(), 9)
                        .requires(v.get(index).get())
                        .unlockedBy("has_compressed_" + name + "_x" + (index + 1), has(v.get(index).get()))
                        .save(pFinishedRecipeConsumer, Compressium.MODID + ":" + name + "_" + (index + 1) + "_uncraft");
                ShapedRecipeBuilder
                        .shaped(v.get(index).get())
                        .define('#', v.get(index - 1).get())
                        .pattern("###").pattern("###").pattern("###")
                        .unlockedBy("has_compressed_" + name + "_x" + index, has(v.get(index - 1).get()))
                        .save(pFinishedRecipeConsumer);
            };
        });



    }

    private void companionRerollRecipe(String recipeId, Consumer<FinishedRecipe> finishedRecipe) {
        ShapedRecipeBuilder.shaped(ModItems.COMPANION_REROLLER)
                .define('T', iskallia.vault.init.ModItems.TEMPORAL_SHARD)
                .define('R', iskallia.vault.init.ModItems.ECHO_POG)
                .define('C', ModItems.POG_PRISM)
                .define('D', iskallia.vault.init.ModBlocks.VAULT_DIAMOND_BLOCK)
                .pattern("DTD")
                .pattern("CRC")
                .pattern("DTD")
                .unlockedBy("has_temporal_shard", has(iskallia.vault.init.ModItems.TEMPORAL_SHARD))
                .save(finishedRecipe, WoldsVaults.id(recipeId));
    }

    private void gemBlockRecipe(String recipeId, Block gemBlock, Item gemItem, Consumer<FinishedRecipe> finishedRecipe) {
        ShapedRecipeBuilder.shaped(gemBlock)
                .define('X', gemItem)
                .define('O', iskallia.vault.init.ModBlocks.CHROMATIC_IRON_BLOCK)
                .pattern(" X ")
                .pattern("XOX")
                .pattern(" X ")
                .unlockedBy("has_" + gemItem.getRegistryName().getPath(), has(gemItem))
                .save(finishedRecipe, WoldsVaults.id(recipeId));
    }

    private void compactingRecipe(Block result, Item input, Consumer<FinishedRecipe> finishedRecipe) {
        ShapelessRecipeBuilder.shapeless(result, 1)
                .requires(input, 9)
                .unlockedBy("has_" + input.getRegistryName().getPath(), has(input))
                .save(finishedRecipe, WoldsVaults.id(result.getRegistryName().getPath()));

        ShapelessRecipeBuilder.shapeless(input, 9)
                .requires(result, 1)
                .unlockedBy("has_" + result.getRegistryName().getPath(), has(result))
                .save(finishedRecipe, WoldsVaults.id(result.getRegistryName().getPath() + "_to_" + input.getRegistryName().getPath()));
    }

    private void unobtanium(Item itemForm, Block blockForm, Consumer<FinishedRecipe> finishedRecipeConsumer) {
        ShapelessRecipeBuilder.shapeless(blockForm, 1)
                .requires(itemForm, 4)
                .unlockedBy("has_" + itemForm.getRegistryName().getPath(), has(itemForm))
                .save(finishedRecipeConsumer, WoldsVaults.id(blockForm.getRegistryName().getPath()));

        ShapelessRecipeBuilder.shapeless(itemForm, 5)
                .requires(blockForm, 1)
                .unlockedBy("has_" + blockForm.getRegistryName().getPath(), has(blockForm))
                .save(finishedRecipeConsumer, WoldsVaults.id(itemForm.getRegistryName().getPath()));


        if(blockForm != ModBlocks.RAINBOW_UNOBTANIUM) {
            ShapelessRecipeBuilder.shapeless(blockForm, 8)
                    .requires(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("architects_palette", "unobtanium_block")), 8)
                    .requires(ForgeRegistries.ITEMS.getValue(ResourceLocUtils.replace(ResourceLocUtils.swapNamespace(itemForm.getRegistryName(), "minecraft"), "unobtanium", "dye")), 1)
                    .unlockedBy("has_unobtanium", has(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("architects_palette", "unobtanium_block"))))
                    .save(finishedRecipeConsumer, WoldsVaults.id(blockForm.getRegistryName().getPath() + "_from_dye"));
        }
        else {
            ShapelessRecipeBuilder.shapeless(blockForm, 1)
                    .requires(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("architects_palette", "unobtanium_block")), 1)
                    .requires(Items.GREEN_DYE)
                    .requires(Items.RED_DYE)
                    .requires(Items.BLUE_DYE)
                    .requires(Items.PURPLE_DYE)
                    .requires(Items.YELLOW_DYE)
                    .requires(Items.ORANGE_DYE)
                    .requires(Items.CYAN_DYE)
                    .requires(Items.WHITE_DYE)
                    .unlockedBy("has_unobtanium", has(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("architects_palette", "unobtanium_block"))))
                    .save(finishedRecipeConsumer, WoldsVaults.id(blockForm.getRegistryName().getPath() + "_from_dye"));
        }

    }
}
