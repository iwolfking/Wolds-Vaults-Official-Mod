package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
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
        //These themes files are in the Wold's Vaults pack files, TODO move them, but this is to create the additional files like theme lore for theme
        add(VaultMod.id("classic_vault_barnyard"), t -> {
            t.type("classic_vault")
                    .starts(VaultMod.id("vault/starts/barnyard_starts").toString())
                    .rooms(VaultMod.id("vault/rooms/barnyard_rooms").toString())
                    .tunnels(VaultMod.id("vault/tunnels/barnyard_tunnel").toString())
                    .ambientLight(0.35f)
                    .fogColor(15718399)
                    .grassColor(7979098)
                    .foliageColor(7979098)
                    .waterColor(4240091)
                    .waterFogColor(4240091)
                    .themeColor(0xE07A5F)
                    .particle("minecraft:composter")
                    .particleProbability(0.001F)
                    .atmosphere(WoldsVaults.id("classic_vault_barnyard"))
                    .minLevel(50)
                    .mobPalette(VaultMod.id("generic/spawners/barnyard_mobs"))
                    .themeGroup("Barnyard")
                    .themeLore("Barnyard", 0x535846, themeLoreDescriptionBuilder -> {
                        themeLoreDescriptionBuilder
                                .perk("Wooden Chests rarely become Living chests", "$name")
                                .horde(4, ThemeLoreDescriptionBuilder.mob("Aggressive Chickens", 2, 2, 2, "⚔"))
                                .assassin(3, ThemeLoreDescriptionBuilder.mob("Aggressive Pigs", 2, 2, 2, "⚔ \uD83C\uDFF9"))
                                .tank(2, ThemeLoreDescriptionBuilder.mob("Aggressive Cows", 4, 3, 3, "✸ ⚔"))
                                .dweller(2);


                    });
        });

        add(VaultMod.id("classic_vault_dark_cavern"), t -> {
            t.type("classic_vault")
                    .starts(VaultMod.id("vault/starts/dark_cavern_starts").toString())
                    .rooms(VaultMod.id("vault/rooms/dark_cavern_rooms").toString())
                    .tunnels(VaultMod.id("vault/tunnels/dark_cavern_tunnels").toString())
                    .ambientLight(0.1F)
                    .fogColor(9409174)
                    .grassColor(10471858)
                    .foliageColor(10471858)
                    .waterColor(4159204)
                    .waterFogColor(329011)
                    .themeColor(0xE07A5F)
                    .particle("minecraft:ash")
                    .particleProbability(0.002F)
                    .atmosphere(WoldsVaults.id("classic_vault_dark_cavern"))
                    .minLevel(50)
                    .mobPalette(VaultMod.id("generic/spawners/dark_cavern_mobs"))
                    .themeGroup("Grimdark")
                    .themeLore("Grimdark", 0x535846, themeLoreDescriptionBuilder -> {
                        themeLoreDescriptionBuilder
                                .perk("Special Resource Rooms, More Black Opal and Painite", "$name")
                                .horde(4, ThemeLoreDescriptionBuilder.mob("Dark Horrors", 2, 2, 2, "⚔"))
                                .assassin(3, ThemeLoreDescriptionBuilder.mob("Dark Horrors", 2, 2, 2, "⚔ \uD83C\uDFF9"))
                                .tank(2, ThemeLoreDescriptionBuilder.mob("Dark Horrors", 4, 3, 3, "✸ ⚔"))
                                .dweller(2);
                    });
        });

        add(VaultMod.id("classic_vault_thanksgiving"), t -> {
            t.type("classic_vault")
                    .starts(VaultMod.id("vault/starts/thanksgiving_starts").toString())
                    .rooms(VaultMod.id("vault/rooms/thanksgiving_rooms").toString())
                    .tunnels(VaultMod.id("vault/tunnels/thanksgiving_tunnels").toString())
                    .ambientLight(0.2F)
                    .fogColor(13088143)
                    .grassColor(8817962)
                    .foliageColor(8817962)
                    .waterColor(7904389)
                    .waterFogColor(7904389)
                    .themeColor(0xE07A5F)
                    .particle("minecraft:happy_villager")
                    .particleProbability(0.001F)
                    .atmosphere(WoldsVaults.id("classic_vault_thanksgiving"))
                    .minLevel(50)
                    .seasonal(true)
                    .mobPalette(VaultMod.id("generic/spawners/thanksgiving_mobs"))
                    .themeGroup("Harvest")
                    .themeLore("Harvest", 0x535846, themeLoreDescriptionBuilder -> {
                        themeLoreDescriptionBuilder
                                .perk("Be thankful!", "$name")
                                .horde(4, ThemeLoreDescriptionBuilder.mob("Aggressive Turkey", 2, 2, 2, "⚔"))
                                .assassin(3, ThemeLoreDescriptionBuilder.mob("Haturkin", 2, 2, 2, "⚔ \uD83C\uDFF9"))
                                .tank(2, ThemeLoreDescriptionBuilder.mob("Zombie Villager", 4, 3, 3, "✸ ⚔"))
                                .dweller(2);
                    });
        });

        add(VaultMod.id("classic_vault_undersea"), t -> {
            t.type("classic_vault")
                    .starts(VaultMod.id("vault/starts/undersea_starts").toString())
                    .rooms(VaultMod.id("vault/rooms/undersea_rooms").toString())
                    .tunnels(VaultMod.id("vault/tunnels/undersea_tunnels").toString())
                    .ambientLight(0.2F)
                    .fogColor(11796461)
                    .grassColor(2987600)
                    .foliageColor(2987600)
                    .waterColor(7599581)
                    .waterFogColor(7599581)
                    .themeColor(0xE07A5F)
                    .particle("minecraft:dripping_water")
                    .particleProbability(0.004F)
                    .atmosphere(VaultMod.id("classic_vault_undersea"))
                    .minLevel(50)
                    .mobPalette(VaultMod.id("generic/spawners/underwater_mobs"))
                    .themeGroup("Underwater")
                    .themeLore("Underwater", 0x535846, themeLoreDescriptionBuilder -> {
                        themeLoreDescriptionBuilder
                                .perk("The vault is flooded!", "$name")
                                .horde(4, ThemeLoreDescriptionBuilder.mob("Drowned", 2, 2, 2, "⚔"))
                                .assassin(3, ThemeLoreDescriptionBuilder.mob("Shark", 2, 2, 2, "⚔ \uD83C\uDFF9"))
                                .tank(2, ThemeLoreDescriptionBuilder.mob("Orca", 4, 3, 3, "✸ ⚔"))
                                .dweller(2);
                    });
        });

        //Just need one god theme defined for the lore to get generated
        add(VaultMod.id("classic_vault_idona_normal"), t -> {
            t.type("classic_vault")
                    .starts(VaultMod.id("vault/starts/idona_starts_normal").toString())
                    .rooms(VaultMod.id("vault/rooms/idona_rooms").toString())
                    .tunnels(VaultMod.id("vault/tunnels/idona_tunnels_normal").toString())
                    .ambientLight(0.3F)
                    .fogColor(16777215)
                    .grassColor(8497458)
                    .foliageColor(8497458)
                    .waterColor(4159204)
                    .waterFogColor(4159204)
                    .themeColor(0xE07A5F)
                    .particle("minecraft:ash")
                    .particleProbability(0.002F)
                    .atmosphere(VaultMod.id("classic_vault_idona_normal"))
                    .minLevel(50)
                    .mobPalette(VaultMod.id("generic/spawners/nether_mobs"))
                    .themeGroup("Gods")
                    .themeLore("Gods", 0x535846, themeLoreDescriptionBuilder -> {
                        themeLoreDescriptionBuilder
                                .perk("God Altars correspond to the God", "$name")
                                .horde(4, ThemeLoreDescriptionBuilder.mob("???", 2, 2, 2, "⚔"))
                                .assassin(3, ThemeLoreDescriptionBuilder.mob("???", 2, 2, 2, "⚔ \uD83C\uDFF9"))
                                .tank(2, ThemeLoreDescriptionBuilder.mob("???", 4, 3, 3, "✸ ⚔"))
                                .dweller(2);
                    });
        });

        //Themes fully configured from this datagen provider
        add(WoldsVaults.id("if_factory"), t -> {
            t.type("classic_vault")
                    .starts(WoldsVaults.id("if_factory_starts").toString())
                    .rooms(WoldsVaults.id("if_factory_rooms").toString())
                    .tunnels(WoldsVaults.id("if_factory_tunnels").toString())
                    .ambientLight(0.35f)
                    .fogColor(0x2B2625)
                    .grassColor(0x4A5043)
                    .foliageColor(0x3B4036)
                    .waterColor(0x7F5A3C)
                    .waterFogColor(0x3D2B1D)
                    .themeColor(0xE07A5F)
                    .particle("minecraft:smoke")
                    .particleProbability(0.015F)
                    .levelEntry("the_vault:default", 50)
                    .themeWeight(5)
                    .atmosphere(WoldsVaults.id("if_factory"))
                    .minLevel(50)
                    .mobPalette(VaultMod.id("generic/spawners/cave_mobs"))
                    .themeGroup("Industrial")
                    .themeLore("Industrial", 0x535846, themeLoreDescriptionBuilder -> {
                        themeLoreDescriptionBuilder
                                .perk("Wooden Chests rarely become Ornate chests", "$name")
                                .horde(4, ThemeLoreDescriptionBuilder.mob("???", 2, 2, 2, "⚔"))
                                .assassin(3, ThemeLoreDescriptionBuilder.mob("???", 2, 2, 2, "⚔ \uD83C\uDFF9"))
                                .tank(2, ThemeLoreDescriptionBuilder.mob("???", 4, 3, 3, "✸ ⚔"))
                                .dweller(2);


                    });
        });

        add(WoldsVaults.id("mekanism_factory"), t -> {
            t.type("classic_vault")
                    .starts(WoldsVaults.id("mekanism_factory_starts").toString())
                    .rooms(WoldsVaults.id("mekanism_factory_rooms").toString())
                    .tunnels(WoldsVaults.id("mekanism_factory_tunnels").toString())
                    .ambientLight(0.35f)
                    .fogColor(0x2B2625)
                    .grassColor(0x4A5043)
                    .foliageColor(0x3B4036)
                    .waterColor(0x7F5A3C)
                    .waterFogColor(0x3D2B1D)
                    .themeColor(0xE07A5F)
                    .particle("minecraft:smoke")
                    .particleProbability(0.015F)
                    .atmosphere(WoldsVaults.id("mekanism_factory"))
                    .minLevel(50)
                    .mobPalette(VaultMod.id("generic/spawners/cave_mobs"))
                    .levelEntry("the_vault:default", 50)
                    .themeWeight(5)
                    .themeGroup("Industrial");
        });


        add(WoldsVaults.id("pnc_factory"), t -> {
            t.type("classic_vault")
                    .starts(WoldsVaults.id("pnc_factory_starts").toString())
                    .rooms(WoldsVaults.id("pnc_factory_rooms").toString())
                    .tunnels(WoldsVaults.id("pnc_factory_tunnels").toString())
                    .ambientLight(0.35f)
                    .fogColor(0x2B2625)
                    .grassColor(0x4A5043)
                    .foliageColor(0x3B4036)
                    .waterColor(0x7F5A3C)
                    .waterFogColor(0x3D2B1D)
                    .themeColor(0xE07A5F)
                    .particle("minecraft:smoke")
                    .atmosphere(WoldsVaults.id("pnc_factory"))
                    .minLevel(50)
                    .mobPalette(VaultMod.id("generic/spawners/cave_mobs"))
                    .particleProbability(0.015F)
                    .levelEntry("the_vault:default", 50)
                    .themeWeight(5)
                    .themeGroup("Plastic");
        });

        add(WoldsVaults.id("thermal_factory"), t -> {
            t.type("classic_vault")
                    .starts(WoldsVaults.id("thermal_factory_starts").toString())
                    .rooms(WoldsVaults.id("thermal_factory_rooms").toString())
                    .tunnels(WoldsVaults.id("thermal_factory_tunnels").toString())
                    .ambientLight(0.35f)
                    .fogColor(0x2B2625)
                    .grassColor(0x4A5043)
                    .foliageColor(0x3B4036)
                    .waterColor(0x7F5A3C)
                    .waterFogColor(0x3D2B1D)
                    .themeColor(0xE07A5F)
                    .particle("minecraft:smoke")
                    .atmosphere(WoldsVaults.id("thermal_factory"))
                    .minLevel(50)
                    .mobPalette(WoldsVaults.id("generic/spawners/thermal_theme_mobs"))
                    .particleProbability(0.015F)
                    .levelEntry("the_vault:default", 50)
                    .themeWeight(5)
                    .themeGroup("Industrial");
        });

        add(WoldsVaults.id("ie_factory"), t -> {
            t.type("classic_vault")
                    .starts(WoldsVaults.id("ie_factory_starts").toString())
                    .rooms(WoldsVaults.id("ie_factory_rooms").toString())
                    .tunnels(WoldsVaults.id("ie_factory_tunnels").toString())
                    .ambientLight(0.35f)
                    .fogColor(0x2B2625)
                    .grassColor(0x4A5043)
                    .foliageColor(0x3B4036)
                    .waterColor(0x7F5A3C)
                    .waterFogColor(0x3D2B1D)
                    .themeColor(0xE07A5F)
                    .particle("minecraft:smoke")
                    .atmosphere(WoldsVaults.id("ie_factory"))
                    .minLevel(50)
                    .mobPalette(VaultMod.id("generic/spawners/cave_mobs"))
                    .particleProbability(0.015F)
                    .levelEntry("the_vault:default", 50)
                    .themeWeight(5)
                    .themeGroup("Industrial");
        });

        add(WoldsVaults.id("create_factory"), t -> {
            t.type("classic_vault")
                    .starts(WoldsVaults.id("create_factory_starts").toString())
                    .rooms(WoldsVaults.id("create_factory_rooms").toString())
                    .tunnels(WoldsVaults.id("create_factory_tunnels").toString())
                    .ambientLight(0.35f)
                    .fogColor(0x2B2625)
                    .grassColor(0x4A5043)
                    .foliageColor(0x3B4036)
                    .waterColor(0x7F5A3C)
                    .waterFogColor(0x3D2B1D)
                    .themeColor(0xE07A5F)
                    .particle("minecraft:smoke")
                    .atmosphere(WoldsVaults.id("create_factory"))
                    .minLevel(50)
                    .mobPalette(VaultMod.id("generic/spawners/cave_mobs"))
                    .particleProbability(0.015F)
                    .levelEntry("the_vault:default", 50)
                    .themeWeight(5)
                    .themeGroup("Industrial");
        });

        add(WoldsVaults.id("botanic_temple"), t -> {
            t.type("classic_vault")
                    .starts(WoldsVaults.id("botanic_temple_starts").toString())
                    .rooms(WoldsVaults.id("botanic_temple_rooms").toString())
                    .tunnels(WoldsVaults.id("botanic_temple_tunnels").toString())
                    .ambientLight(0.25f)
                    .fogColor(0x0F2E1B)
                    .grassColor(0x1EA857)
                    .foliageColor(0x19944C)
                    .waterColor(0x00A896)
                    .waterFogColor(0x004D43)
                    .themeColor(0x10B981)
                    .particle("minecraft:end_rod")
                    .atmosphere(VaultMod.id("classic_vault_temple"))
                    .minLevel(50)
                    .mobPalette(VaultMod.id("generic/spawners/lush_cave_mobs"))
                    .particleProbability(0.005f)
                    .levelEntry("the_vault:default", 50)
                    .themeWeight(5)
                    .themeGroup("Glimmergrove");
        });

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
                    .atmosphere(WoldsVaults.id("occult"))
                    .minLevel(50)
                    .mobPalette(WoldsVaults.id("generic/spawners/occult_mobs"))
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
                    .atmosphere(WoldsVaults.id("mystical_forest"))
                    .minLevel(50)
                    .mobPalette(WoldsVaults.id("generic/spawners/ars_mobs"))
                    .themeLore("Arcane", 0x9370DB, themeLoreDescriptionBuilder -> {
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
                    .atmosphere(VaultMod.id("classic_vault_void"))
                    .minLevel(50)
                    .mobPalette(VaultMod.id("generic/spawners/void_mobs"))
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
                    .atmosphere(WoldsVaults.id("lunar"))
                    .minLevel(50)
                    .mobPalette(WoldsVaults.id("generic/spawners/astral_mobs"))
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
                    .atmosphere(WoldsVaults.id("red_planet"))
                    .minLevel(50)
                    .mobPalette(WoldsVaults.id("generic/spawners/astral_mobs"))
                    .themeGroup("Astral");
        });
    }
}

