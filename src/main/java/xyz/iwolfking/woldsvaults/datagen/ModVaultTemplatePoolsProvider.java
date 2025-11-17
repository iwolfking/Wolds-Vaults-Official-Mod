package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.lib.core.datagen.gen.AbstractTemplatePoolProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModVaultTemplatePoolsProvider extends AbstractTemplatePoolProvider {
    public ModVaultTemplatePoolsProvider(DataGenerator generator, String modid) {
        super(generator, modid);
    }

    @Override
    protected void registerPools() {
        createStandardPoolsForTheme("eclipse", WoldsVaults.id("universal_eclipse"), VaultMod.id("generic/ore_placeholder_void"));
    }
}
