package xyz.iwolfking.woldsvaults.datagen;

import com.simibubi.create.AllTags;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.integration.arsnouveau.init.ArsRecipeSerializers;
import xyz.iwolfking.woldsvaults.integration.occultism.init.OccultismRecipeSerializers;

public class ModRecipeTagProvider extends TagsProvider<RecipeSerializer<?>> {
    public ModRecipeTagProvider(DataGenerator pGenerator, @Nullable ExistingFileHelper existingFileHelper) {
        super(pGenerator, Registry.RECIPE_SERIALIZER, WoldsVaults.MOD_ID, existingFileHelper);
    }


    @Override
    protected void addTags() {
        tag(AllTags.AllRecipeSerializerTags.AUTOMATION_IGNORE.tag).addOptional(ArsRecipeSerializers.VAULT_CATALYST_INFUSION.getRegistryName());
        tag(AllTags.AllRecipeSerializerTags.AUTOMATION_IGNORE.tag).addOptional(ArsRecipeSerializers.VAULT_GEAR_ENCHANTING_APPARATUS.getRegistryName());
        tag(AllTags.AllRecipeSerializerTags.AUTOMATION_IGNORE.tag).addOptional(OccultismRecipeSerializers.VAULT_CRYSTAL_RITUAL.getId());
        tag(AllTags.AllRecipeSerializerTags.AUTOMATION_IGNORE.tag).addOptional(OccultismRecipeSerializers.AUGMENT_RITUAL.getId());
        tag(AllTags.AllRecipeSerializerTags.AUTOMATION_IGNORE.tag).addOptional(OccultismRecipeSerializers.AUGMENT_POOL_RITUAL.getId());
        tag(AllTags.AllRecipeSerializerTags.AUTOMATION_IGNORE.tag).addOptional(OccultismRecipeSerializers.COMPANION_RITUAL.getId());
    }

    @Override
    public String getName() {
        return "Wold's Recipe Tag Provider";
    }
}
