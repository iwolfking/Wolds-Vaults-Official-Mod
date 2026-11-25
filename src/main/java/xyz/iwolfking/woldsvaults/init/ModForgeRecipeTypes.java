package xyz.iwolfking.woldsvaults.init;

import iskallia.vault.config.recipe.ForgeRecipeType;
import xyz.iwolfking.vhapi.api.extensions.general.ForgeRecipeTypeFactory;
import xyz.iwolfking.woldsvaults.config.recipes.augment.AugmentForgeRecipe;
import xyz.iwolfking.woldsvaults.config.recipes.mod_box.ModBoxForgeRecipe;
import xyz.iwolfking.woldsvaults.config.recipes.weaving.WeavingForgeRecipe;

public class ModForgeRecipeTypes {
    public static final ForgeRecipeType AUGMENT = ForgeRecipeTypeFactory.INSTANCE.create(
            "AUGMENT",
            AugmentForgeRecipe::new,
            () -> ModConfigs.AUGMENT_RECIPES
    );

    public static final ForgeRecipeType MOD_BOX = ForgeRecipeTypeFactory.INSTANCE.create(
            "MOD_BOX",
            ModBoxForgeRecipe::new,
            () -> ModConfigs.MOD_BOX_RECIPES_CONFIG
    );

    public static final ForgeRecipeType WEAVING = ForgeRecipeTypeFactory.INSTANCE.create(
            "WEAVING",
            WeavingForgeRecipe::new,
            () -> ModConfigs.WEAVING_RECIPES_CONFIG
    );

    public static void init() {
    }
}