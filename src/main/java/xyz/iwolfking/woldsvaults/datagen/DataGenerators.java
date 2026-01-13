package xyz.iwolfking.woldsvaults.datagen;

import com.supermartijn642.rechiseled.registration.RechiseledRegistrationImpl;
import com.supermartijn642.rechiseled.registration.data.*;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import vazkii.botania.data.BlockTagProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;

@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        ExistingFileHelper efh = event.getExistingFileHelper();



        if (event.includeClient()) {
            gen.addProvider(new ModLanguageProvider(gen));
            gen.addProvider(new ModBlockStateProvider(gen, efh));
            gen.addProvider(new ModItemModelProvider(gen, efh));
            gen.addProvider(new ModSoundDefinitionsProvider(gen, efh));
        }

        if (event.includeServer()) {
            ModBlockTagProvider blockTags = new ModBlockTagProvider(gen, efh);
            gen.addProvider(new ModRecipeProvider(gen));
            gen.addProvider(new ModBlockLootTableProvider(gen));
            gen.addProvider(new ModCompressiumLootTableProvider(gen));
            gen.addProvider(blockTags);
            gen.addProvider(new ModItemTagProvider(gen, blockTags, efh));
            gen.addProvider(new ModVaultPalettesProvider(gen));
            gen.addProvider(new ModVaultThemesProvider(gen));
            gen.addProvider(new ModVaultTemplatePoolsProvider(gen));
            gen.addProvider(new ModVaultGearTiersProvider(gen));
            gen.addProvider(new ModVaultModifierPoolsProvider(gen));
            gen.addProvider(new ModVaultModifiersProvider(gen));
            gen.addProvider(new ModPlayerTitlesProvider(gen));
            gen.addProvider(new ModVaultInscriptionsProvider(gen));
            gen.addProvider(new ModUniqueGearProvider(gen));
            gen.addProvider(new ModVaultMobsProvider(gen));
            gen.addProvider(new ModSkillDescriptionsProvider(gen));
            gen.addProvider(new ModTooltipsProvider(gen));
            gen.addProvider(new ModTalentsProvider(gen));
            gen.addProvider(new ModAbilitiesProvider(gen));
            gen.addProvider(new ModAbilityStylesProvider(gen));
            gen.addProvider(new ModVaultCatalystPoolsProvider(gen));
            gen.addProvider(new ModAbilityDescriptionsProvider(gen));
            gen.addProvider(new ModTrinketsProvider(gen));
            gen.addProvider(new ModAbilityGroupProvider(gen));
            gen.addProvider(new ModVaultLootTablesProvider(gen));
            gen.addProvider(new ModVaultDiffuserProvider(gen));
            gen.addProvider(new ModVaultRecyclerProvider(gen));
            gen.addProvider(new ModVaultAltarIngredientsProvider(gen));
            gen.addProvider(new ModInscriptionRecipesProvider(gen));
            gen.addProvider(new ModPrebuiltToolsProvider(gen));
            gen.addProvider(new ModVaultPortalBlocksProvider(gen));
            gen.addProvider(new ModChampionsProvider(gen));
            gen.addProvider(new ModEnigmaEggProvider(gen));
            gen.addProvider(new ModExpertisesProvider(gen));
            gen.addProvider(new ModExpertiseStylesProvider(gen));
            gen.addProvider(new ModPrestigePowersProvider(gen));
            gen.addProvider(new ModPrestigePowerStylesProvider(gen));
            gen.addProvider(new ModGearEnchantmentsProvider(gen));
            gen.addProvider(new ModLootInfoProvider(gen));
            gen.addProvider(new ModRoyalePresetsProvider(gen));
            gen.addProvider(new ModSkillGatesProvider(gen));
            gen.addProvider(new ModSkillScrollsProvider(gen));
            gen.addProvider(new ModVaultAltarProvider(gen));
            gen.addProvider(new ModVaultEntitiesProvider(gen));
            gen.addProvider(new ModVaultBlacklistProvider(gen));
            gen.addProvider(new ModVaultItemsProvider(gen));
            gen.addProvider(new ModVaultMapIconsProvider(gen));
            gen.addProvider(new ModModBoxProvider(gen));
            gen.addProvider(new ModBoosterPackProvider(gen));
            gen.addProvider(new ModCardDecksProvider(gen));
            gen.addProvider(new ModDeckCoresProvider(gen));
            gen.addProvider(new ModDeckRecipesProvider(gen));
            gen.addProvider(new ModEtchedVaultLayoutsProvider(gen));
            gen.addProvider(new RechiseledDataProvider(gen));
            gen.addProvider(new ModVaultCrystalProvider(gen));
        }
    }


}
