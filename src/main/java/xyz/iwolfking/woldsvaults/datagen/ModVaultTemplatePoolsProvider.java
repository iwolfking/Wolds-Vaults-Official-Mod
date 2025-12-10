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
        createStandardPoolsForTheme("eclipse", WoldsVaults.id("universal_eclipse"), VaultMod.id("generic/ore_placeholder_void"));
//        addPool(templatePoolBuilder -> templatePoolBuilder.id(VaultMod.id("vault/rooms/omega_rooms_merge"))
//                .addReference(1, VaultMod.id("vault/rooms/challenge/dragon")));
    }
}
