package xyz.iwolfking.woldsvaults.datagen;

import com.simibubi.create.AllItems;
import iskallia.vault.VaultMod;
import me.dinnerbeef.compressium.Compressium;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.data.recipes.NbtOutputResult;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.init.ModCompressibleBlocks;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.recipes.lib.InfuserRecipeBuilder;
import xyz.iwolfking.woldsvaults.recipes.lib.NbtAwareRecipe.IngredientWithNBT;
import xyz.iwolfking.woldsvaults.recipes.lib.NbtAwareRecipe;
import xyz.iwolfking.woldsvaults.recipes.lib.UncheckedRecipe;

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
                .save(output -> pFinishedRecipeConsumer.accept(new NbtOutputResult(output, colossus))); // thanks vazkii


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

        //TODO: Crystal Seal Alchemy recipe

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
}
