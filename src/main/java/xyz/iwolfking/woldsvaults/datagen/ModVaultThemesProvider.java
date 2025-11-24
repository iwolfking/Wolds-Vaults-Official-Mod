package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.config.ThemeAugmentLoreConfig;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.vhapi.api.lib.core.datagen.genResearchExclusionConfig.AbstractThemeProvider;
import xyz.iwolfking.vhapi.api.util.builder.description.DescriptionDataBuilder;
import xyz.iwolfking.vhapi.api.util.builder.description.JsonDescription;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModVaultThemesProvider extends AbstractThemeProvider {
    public ModVaultThemesProvider(DataGenerator generator, String modid) {
        super(generator, modid);
    }

    @Override
    protected void registerThemes() {
        add(new ResourceLocation("woldsvaults", "eclipse"), t -> {
            t.type("classic_vault")
                    .starts(WoldsVaults.id("eclipse_starts").toString())
                    .rooms(WoldsVaults.id("eclipse_rooms").toString())
                    .tunnels(WoldsVaults.id("eclipse_tunnels").toString())

                    .ambientLight(0.2f)
                    .fogColor(12358351)
                    .grassColor(8041299)
                    .foliageColor(8041299)
                    .waterColor(3112412)
                    .waterFogColor(3112412)
                    .themeColor(3112412)
                    .particle("minecraft:ambient_entity_effect")
                    .particleProbability(0.002f)
                    .levelEntry("the_vault:default", 0)
                    .levelEntry("the_vault:default", 30)
                    .themeWeight(20)
                    .themeGroup("Astral")
                    .themeLore(new ThemeAugmentLoreConfig.AugmentLore("Astral", new DescriptionDataBuilder()
                            .description(JsonDescription.perkDescription("This is a test description.\n"))
                                    .description(JsonDescription.mobsDescription("Hordes: ", new JsonDescription.MobEntry("Enderman", 1, 2, 1), new JsonDescription.MobEntry("Test", 3, 2, 3)))
                                    .description(JsonDescription.mobsDescription("Assassins: ", new JsonDescription.MobEntry("Enderman", 1, 2, 1), new JsonDescription.MobEntry("Test", 3, 2, 3)))
                                    .description(JsonDescription.mobsDescription("Tanks: ", new JsonDescription.MobEntry("Enderman", 1, 2, 1), new JsonDescription.MobEntry("Test", 3, 2, 3)))
                                    .description(JsonDescription.dwellersDescription(3))
                            .build(),
                            3));
        });
    }
}
