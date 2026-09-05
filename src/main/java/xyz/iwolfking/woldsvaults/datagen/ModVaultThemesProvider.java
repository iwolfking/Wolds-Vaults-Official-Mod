package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.config.ThemeAugmentLoreConfig;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.vhapi.api.datagen.gen.AbstractThemeProvider;
import xyz.iwolfking.vhapi.api.util.builder.description.DescriptionDataBuilder;
import xyz.iwolfking.vhapi.api.util.builder.description.JsonDescription;
import xyz.iwolfking.vhapi.api.util.builder.description.ThemeLoreDescriptionBuilder;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModVaultThemesProvider extends AbstractThemeProvider {
    public ModVaultThemesProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    protected void registerThemes() {
        add(WoldsVaults.id("occult"), t -> {
            t.type("classic_vault")
                    .starts(WoldsVaults.id("occult_starts").toString())
                    .rooms(WoldsVaults.id("occult_rooms").toString())
                    .tunnels(WoldsVaults.id("occult_tunnels").toString())
                    .ambientLight(0.15f)
                    .fogColor(0x2B0508)
                    .grassColor(0x3B2023)
                    .foliageColor(0x3B2023)
                    .waterColor(0x4A0000)
                    .waterFogColor(0x1F0000)
                    .themeColor(0x9E0B0F)
                    .particle("minecraft:ambient_entity_effect")
                    .particleProbability(0.002f)
                    .levelEntry("the_vault:default", 50)
                    .themeWeight(5)
                    .themeGroup("Occult")
                    .themeLore("Occult", 0x9E0B0F, themeLoreDescriptionBuilder -> {
                        themeLoreDescriptionBuilder
                                .perk("More Rare Gems", "$name")
                                .horde(4, ThemeLoreDescriptionBuilder.mob("Wild Afrit", 2, 2, 2, "⚔"))
                                .assassin(3, ThemeLoreDescriptionBuilder.mob("Wither Skeletons", 2, 2, 2, "⚔ \uD83C\uDFF9"), ThemeLoreDescriptionBuilder.mob("Piglins", 2, 2, 2, "⚔ \uD83C\uDFF9"))
                                .tank(2, ThemeLoreDescriptionBuilder.mob("Piglin Brutes", 4, 3, 3, "✸ ⚔"))
                                .dweller(2);

                    });
        });

        add(WoldsVaults.id("mystical_forest"), t -> {
            t.type("classic_vault")
                    .starts(WoldsVaults.id("mystical_forest_starts").toString())
                    .rooms(WoldsVaults.id("mystical_forest_rooms").toString())
                    .tunnels(WoldsVaults.id("mystical_forest_tunnels").toString())
                    .ambientLight(0.25f)
                    .fogColor(0x112E27)
                    .grassColor(0x2E8B57)
                    .foliageColor(0x00FF7F)
                    .waterColor(0x13383B)
                    .waterFogColor(0x0A2022)
                    .themeColor(0x50C878)
                    .particle("minecraft:ambient_entity_effect")
                    .particleProbability(0.002f)
                    .levelEntry("the_vault:default", 50)
                    .themeWeight(5)
                    .themeGroup("Arcane")
                    .themeLore("Arcane", 0x9370DB, themeLoreDescriptionBuilder -> { // Medium Purple Lore Title
                        themeLoreDescriptionBuilder
                                .perk("More ", "white")
                                .perk("Wutodie ", "light_purple")
                                .perk(", ", "white")
                                .perk("Benitoite ", "dark_aqua")
                                .perk("and ", "white")
                                .perk("Alexandrite ", "dark_green")
                                .horde(4, ThemeLoreDescriptionBuilder.mob("Wilden Stalker", 2, 2, 2, "⚔"))
                                .assassin(3, ThemeLoreDescriptionBuilder.mob("Wilden Hunter", 2, 2, 2, "⚔ \uD83C\uDFF9"))
                                .tank(2, ThemeLoreDescriptionBuilder.mob("Weald Walkers", 4, 4, 1, "✸ ⚔"))
                                .dweller(1);

                    });
        });

        add(WoldsVaults.id("sculk"), t -> {
            t.type("classic_vault")
                    .starts(WoldsVaults.id("sculk_starts").toString())
                    .rooms(WoldsVaults.id("sculk_rooms").toString())
                    .tunnels(WoldsVaults.id("sculk_tunnels").toString())
                    .ambientLight(0.2f)
                    .fogColor(12358351)
                    .grassColor(8041299)
                    .foliageColor(8041299)
                    .waterColor(3112412)
                    .waterFogColor(3112412)
                    .themeColor(3112412)
                    .particle("minecraft:ambient_entity_effect")
                    .particleProbability(0.002f)
                    .levelEntry("the_vault:default", 30)
                    .levelEntry("the_vault:default", 50)
                    .themeWeight(5)
                    .themeGroup("Void");
        });

        add(WoldsVaults.id("lunar"), t -> {
            t.type("classic_vault")
                    .starts(WoldsVaults.id("astral_starts").toString())
                    .rooms(WoldsVaults.id("astral_rooms").toString())
                    .tunnels(WoldsVaults.id("astral_tunnels").toString())
                    .ambientLight(0.2f)
                    .fogColor(12358351)
                    .grassColor(8041299)
                    .foliageColor(8041299)
                    .waterColor(3112412)
                    .waterFogColor(3112412)
                    .themeColor(6838410)
                    .particle("minecraft:ambient_entity_effect")
                    .particleProbability(0.002f)
                    .levelEntry("the_vault:default", 30)
                    .levelEntry("the_vault:default", 50)
                    .themeWeight(8)
                    .themeGroup("Astral")
                    .themeLore("Astral", 3112412, themeLoreDescriptionBuilder -> {
                        themeLoreDescriptionBuilder
                                .perk("More ", "white")
                                .perk("Wutodie ", "light_purple")
                                .perk("and ", "white")
                                .perk("Player Gems ", "yellow")
                                .horde(3, ThemeLoreDescriptionBuilder.mob("Alien", 2, 2, 2, "⚔"))
                                .assassin(2, ThemeLoreDescriptionBuilder.mob("Astral Stalker", 2, 2, 2, "⚔ \uD83C\uDFF9"), ThemeLoreDescriptionBuilder.mob("Singularity Creeper", 1, 2, 2, "✸"), ThemeLoreDescriptionBuilder.mob("Cosmaw", 3, 1, 4, "✸"))
                                .tank(4, ThemeLoreDescriptionBuilder.mob("Nebula Sentinel", 3, 3, 2, "✸ ⚔"), ThemeLoreDescriptionBuilder.mob("Star Beast", 2, 4, 1, "✸ ⚔"))
                                .dweller(2);

                    });
        });

        add(WoldsVaults.id("red_planet"), t -> {
            t.type("classic_vault")
                    .starts(WoldsVaults.id("astral_red_starts").toString())
                    .rooms(WoldsVaults.id("astral_red_rooms").toString())
                    .tunnels(WoldsVaults.id("astral_red_tunnels").toString())
                    .ambientLight(0.2f)
                    .fogColor(12358351)
                    .grassColor(8041299)
                    .foliageColor(8041299)
                    .waterColor(3112412)
                    .waterFogColor(3112412)
                    .themeColor(8991232)
                    .particle("minecraft:ambient_entity_effect")
                    .particleProbability(0.002f)
                    .levelEntry("the_vault:default", 30)
                    .levelEntry("the_vault:default", 50)
                    .themeWeight(8)
                    .themeGroup("Astral");
        });
    }
}

