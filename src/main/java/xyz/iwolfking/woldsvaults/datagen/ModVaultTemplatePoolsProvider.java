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
        createStandardPoolsForTheme("astral", WoldsVaults.id("universal_astral"), WoldsVaults.id("generic/ore_placeholder_astral"));
        createStandardPoolsForTheme("astral_red", WoldsVaults.id("universal_astral_red"), WoldsVaults.id("generic/ore_placeholder_astral"));
    }
}
