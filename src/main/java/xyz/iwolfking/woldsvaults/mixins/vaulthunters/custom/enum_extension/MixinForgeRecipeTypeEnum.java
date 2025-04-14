package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.enum_extension;

import iskallia.vault.config.recipe.ForgeRecipeType;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;
import xyz.iwolfking.woldsvaults.config.recipes.augment.AugmentForgeRecipe;
import xyz.iwolfking.woldsvaults.config.recipes.mod_box.ModBoxForgeRecipe;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@Mixin(value = ForgeRecipeType.class, remap = false)
public class MixinForgeRecipeTypeEnum {
    @Shadow
    @Final
    @Mutable
    @SuppressWarnings("target")
    private static ForgeRecipeType[] $VALUES;

    private static final ForgeRecipeType AUGMENT = enumExpansion$addVariant("AUGMENT", AugmentForgeRecipe::new, () -> {
        return ModConfigs.AUGMENT_RECIPES;
    });

    private static final ForgeRecipeType MOD_BOX = enumExpansion$addVariant("MOD_BOX", ModBoxForgeRecipe::new, () -> {
        return ModConfigs.MOD_BOX_RECIPES_CONFIG;
    });


    @Invoker("<init>")
    public static ForgeRecipeType enumExpansion$invokeInit(String internalName, int internalId, BiFunction recipeClassCtor, Supplier configSupplier) {
        throw new AssertionError();
    }

    @Unique
    private static ForgeRecipeType enumExpansion$addVariant(String internalName, BiFunction recipeClassCtor, Supplier configSupplier) {
        ArrayList<ForgeRecipeType> variants = new ArrayList<ForgeRecipeType>(Arrays.asList(MixinForgeRecipeTypeEnum.$VALUES));
        ForgeRecipeType type = enumExpansion$invokeInit(internalName, variants.get(variants.size() - 1).ordinal() + 1, recipeClassCtor, configSupplier);
        variants.add(type);
        MixinForgeRecipeTypeEnum.$VALUES = variants.toArray(new ForgeRecipeType[0]);
        return type;
    }
}
