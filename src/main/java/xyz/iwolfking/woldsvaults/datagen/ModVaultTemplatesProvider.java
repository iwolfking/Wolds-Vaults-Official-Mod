package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.gen.AbstractTemplatesProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;

/**
 * Vault room templates Wold's Vaults adds. Each structure ships in the jar under {@code data/woldsvaults/structures},
 * which VHAPI reads through the server's resource manager when the template's path is a resource location.
 */
public class ModVaultTemplatesProvider extends AbstractTemplatesProvider {
    public ModVaultTemplatesProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    protected void registerTemplates() {
        add("wolds_templates", builder -> builder.addStructureTemplate(VaultMod.id("vault/rooms/special/extraction1"), "Extraction Site",
                WoldsVaults.id("structures/vault/rooms/special/extraction1.nbt")));
    }
}
