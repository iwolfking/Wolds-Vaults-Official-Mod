package xyz.iwolfking.woldsvaults.integration.vhapi.modules.recipes;

import xyz.iwolfking.woldsvaults.integration.vhapi.lib.IVHAPIModule;

public class VHAPIAugmentRecipes implements IVHAPIModule {
    public void init() {
        IVHAPIModule.registerVHAPIFile("recipes/augments.json", "vault_recipes/augment/wold_augment_recipes");
    }
}
