package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.gen.AbstractTemplatePoolProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModVaultTemplatePoolsProvider extends AbstractTemplatePoolProvider {
    public ModVaultTemplatePoolsProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    protected void registerPools() {
        createStandardPoolsForTheme("sculk", WoldsVaults.id("universal_sculk"), VaultMod.id("generic/ore_placeholder_void"));
        createStandardPoolsForTheme("mystical_forest", WoldsVaults.id("universal_mystical_forest"), WoldsVaults.id("generic/ore_placeholder_magic"));
        createStandardPoolsForTheme("occult", WoldsVaults.id("universal_occult"), VaultMod.id("generic/ore_placeholder_nether"));
        createStandardPoolsForTheme("botanic_temple", WoldsVaults.id("universal_botania"), VaultMod.id("generic/ore_placeholder"));
        createStandardPoolsForTheme("create_factory", WoldsVaults.id("universal_create"), VaultMod.id("generic/ore_placeholder"));
        createStandardPoolsForTheme("ie_factory", WoldsVaults.id("universal_immersiveengineering"), VaultMod.id("generic/ore_placeholder"));
        createStandardPoolsForTheme("thermal_factory", WoldsVaults.id("universal_thermal"), VaultMod.id("generic/ore_placeholder"));
        createStandardPoolsForTheme("pnc_factory", WoldsVaults.id("universal_pnc"), VaultMod.id("generic/ore_placeholder"));
        createStandardPoolsForTheme("mekanism_factory", WoldsVaults.id("universal_mekanism"), VaultMod.id("generic/ore_placeholder"));
        createStandardPoolsForTheme("if_factory", WoldsVaults.id("universal_if"), VaultMod.id("generic/ore_placeholder"));
        createStandardPoolsForTheme("astral", WoldsVaults.id("universal_astral"), WoldsVaults.id("generic/ore_placeholder_astral"));
        createStandardPoolsForTheme("astral_red", WoldsVaults.id("universal_astral_red"), WoldsVaults.id("generic/ore_placeholder_astral"));
    }
}
