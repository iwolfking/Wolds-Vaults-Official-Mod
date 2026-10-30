package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.integration.occultism.OccultismTagRegistry;

import javax.annotation.Nullable;
import java.util.Map;

public class ModEntityTagProvider extends EntityTypeTagsProvider {

    public ModEntityTagProvider(DataGenerator generator, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator, WoldsVaults.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        for (Map.Entry<TagKey<EntityType<?>>, EntityType<?>> entry : OccultismTagRegistry.AUTO_ENTITY_TAGS.entrySet()) {
            this.tag(entry.getKey()).add(entry.getValue());
        }
    }
}