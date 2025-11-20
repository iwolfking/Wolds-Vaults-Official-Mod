package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
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
            gen.addProvider(new ModRecipeProvider(gen));
            gen.addProvider(new ModVaultPalettesProvider(gen, WoldsVaults.MOD_ID));
            gen.addProvider(new ModVaultThemesProvider(gen, WoldsVaults.MOD_ID));
            gen.addProvider(new ModVaultTemplatePoolsProvider(gen, WoldsVaults.MOD_ID));
            gen.addProvider(new ModVaultGearTiersProvider(gen));
            gen.addProvider(new ModVaultModifierPoolsProvider(gen));
            gen.addProvider(new ModVaultModifiersProvider(gen));
            gen.addProvider(new ModPlayerTitlesProvider(gen));
            gen.addProvider(new ModVaultInscriptionsProvider(gen));
            gen.addProvider(new ModUniqueGearProvider(gen));
        }
    }
}
