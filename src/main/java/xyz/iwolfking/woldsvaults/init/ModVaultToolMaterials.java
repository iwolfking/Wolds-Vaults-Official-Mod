package xyz.iwolfking.woldsvaults.init;

import iskallia.vault.item.tool.ToolMaterial;
import xyz.iwolfking.vhapi.api.extensions.general.VaultToolMaterialFactory;

public class ModVaultToolMaterials {
    public static ToolMaterial NULLITE;

    public static void init() {
        NULLITE = VaultToolMaterialFactory.INSTANCE.create("NULLITE", "nullite", 100, 80, 9.0F, 4096, 9);
    }
}
