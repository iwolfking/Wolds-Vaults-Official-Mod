package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.VaultMod;
import iskallia.vault.config.Config;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.vhapi.api.datagen.lib.BasicListBuilder;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ThemePaletteRegistryConfig extends Config {

    @Expose
    public Map<ResourceLocation, ThemePaletteMapEntry> THEME_TO_PALETTE_MAP = new HashMap<>();

    @Expose
    public List<String> EXCLUDED_ROOM_POOLS = new ArrayList<>();

    @Override
    public String getName() {
        return "theme_palette_registry";
    }

    @Override
    protected void reset() {
        EXCLUDED_ROOM_POOLS.add("common");
        EXCLUDED_ROOM_POOLS.add("resource");
        EXCLUDED_ROOM_POOLS.add("raw");
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_plastic"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_plastic"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_plastic_platime"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_plastic_playtime"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("skaia_vault_prismatic_umbra"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_prismatic_umbra"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("skaia_vault_prismatic_lumen"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_prismatic_lumen"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("skaia_vault_autumn_cherry_grove"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_autumn_cherry_grove"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("vault_lost_depths"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_lost_depths"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_trial"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_plastic"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_trial"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_plastic"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_ice"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_ice"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_ice_cave"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_ice_cave"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_cave"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_cave"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_lush_cave"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_lush_cave"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_gloom"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_gloom"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_living_cave"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_living_cave"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_nether_crimson"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_nether_crimson"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_nether_warped"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_nether_warped"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_desert"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_desert"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_sandy_cave"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_sandy_cave"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_beach"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_beach"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_tropical"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_tropical"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_dark_cavern"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_dark_cavern"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_mesa"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_mesa"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_deep_mesa"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_deep_mesa"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_void"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_void"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_factory_void"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_factory_void"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_cave_deepslate"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_deepslate_cave"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_cave_geode"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_cave_geode"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_cave_wasteland"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_cave_wasteland"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_end_void"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_end_void"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_nether_blackstone"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_nether_blackstone"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_nether_soul"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_nether_soul"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_sweet"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_sweet"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_festive"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_festive"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_festive"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_gingerbread"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_mushroom"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_mushroom"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_flooded"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_flooded"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_fairy_ring"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_fairy_ring"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_poison"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_poison"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_easter_egg"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_easter_egg"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_easter_pastel"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_easter_pastel"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_white"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_white"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_shipwreck"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_shipwreck"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_haunted"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_haunted"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_sea_floor"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_sea_floor"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_undersea"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_undersea"));
            });
            themePaletteMapEntry.affectsInscriptionRooms = true;
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_blood_moon"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_blood_moon"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_season_spring"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_season_spring"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_season_summer"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_season_summer"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_season_fall"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_season_fall"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_season_winter"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_season_winter"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_thanksgiving"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_thanksgiving"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_barnyard"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("classic_vault_barnyard"));
            });
            themePaletteMapEntry.addPostProcessors(paletteEntries -> {
                paletteEntries.add(VaultMod.id("generic/wooden_chest_placeholder_living"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_flesh"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_flesh"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_aurora_cave"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_aurora_cave"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_wendarr_normal"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_wendarr_normal"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_idona_normal"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_idona_normal"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_tenos_normal"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_tenos_normal"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_velara_normal"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_velara_normal"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_blood"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_blood"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("classic_vault_blood_ash"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("universal_blood_ash"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(WoldsVaults.id("if_factory"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(WoldsVaults.id("universal_if"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(WoldsVaults.id("mekanism_factory"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(WoldsVaults.id("universal_mekanism"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(WoldsVaults.id("pnc_factory"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(WoldsVaults.id("pnc_factory"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(WoldsVaults.id("thermal_factory"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(WoldsVaults.id("universal_thermal"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(WoldsVaults.id("ie_factory"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(WoldsVaults.id("universal_immersiveengineering"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(WoldsVaults.id("create_factory"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(WoldsVaults.id("create_factory"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(WoldsVaults.id("botanic_temple"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(WoldsVaults.id("universal_botania"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(WoldsVaults.id("occult"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(WoldsVaults.id("universal_occult"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(WoldsVaults.id("mystical_forest"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(WoldsVaults.id("universal_mystical_forest"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(WoldsVaults.id("sculk"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(WoldsVaults.id("universal_sculk"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(WoldsVaults.id("lunar"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(WoldsVaults.id("universal_astral"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(WoldsVaults.id("red_planet"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(WoldsVaults.id("universal_astral_red"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("map_beach_t0"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("map/universal_beach"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("map_desert_t0"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("map/universal_desert"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("map_cave_t0"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("map/universal_cave"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("map_void_t0"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("map/universal_void"));
            });
        }));
        THEME_TO_PALETTE_MAP.put(VaultMod.id("map_nether_t0"), ThemePaletteMapEntry.create(themePaletteMapEntry -> {
            themePaletteMapEntry.addPalettes(paletteEntries -> {
                paletteEntries.add(VaultMod.id("map/universal_nether_blackstone"));
            });
        }));
    }

    public static class ThemePaletteMapEntry {
        @Expose
        public final List<ResourceLocation> FULL_PALETTE_ENTRIES = new ArrayList<>();

        @Expose
        public final List<ResourceLocation> POST_PROCESSING_PALETTES = new ArrayList<>();

        @Expose
        public boolean affectsInscriptionRooms = false;

        public void addPalettes(Consumer<BasicListBuilder<ResourceLocation>> paletteEntriesConsumer) {
            BasicListBuilder<ResourceLocation> palettesBuilder = new BasicListBuilder<>();
            paletteEntriesConsumer.accept(palettesBuilder);
            FULL_PALETTE_ENTRIES.addAll(palettesBuilder.build());
        }

        public void addPostProcessors(Consumer<BasicListBuilder<ResourceLocation>> postProcessorEntriesConsumer) {
            BasicListBuilder<ResourceLocation> palettesBuilder = new BasicListBuilder<>();
            postProcessorEntriesConsumer.accept(palettesBuilder);
            POST_PROCESSING_PALETTES.addAll(palettesBuilder.build());
        }

        public static ThemePaletteMapEntry create(Consumer<ThemePaletteMapEntry> themePaletteMapEntryConsumer) {
            ThemePaletteMapEntry themePaletteMapEntry = new ThemePaletteMapEntry();
            themePaletteMapEntryConsumer.accept(themePaletteMapEntry);
            return themePaletteMapEntry;
        }
      }
}
