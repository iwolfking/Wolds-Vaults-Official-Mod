package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModEntities;
import xyz.iwolfking.woldsvaults.integration.occultism.OccultismTagRegistry;

import javax.annotation.Nullable;
import java.util.Map;

public class ModEntityTagProvider extends EntityTypeTagsProvider {
    public static final TagKey<EntityType<?>> HAUNTED_BRAZIERS_MOB = TagKey.create(
            Registry.ENTITY_TYPE_REGISTRY,
            WoldsVaults.id("haunted_braziers_mob")
    );

    public ModEntityTagProvider(DataGenerator generator, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator, WoldsVaults.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        for (Map.Entry<TagKey<EntityType<?>>, EntityType<?>> entry : OccultismTagRegistry.AUTO_ENTITY_TAGS.entrySet()) {
            this.tag(entry.getKey()).add(entry.getValue());
        }

        this.tag(HAUNTED_BRAZIERS_MOB)
                .add(iskallia.vault.init.ModEntities.VAULT_WRAITH_WHITE)
                .add(iskallia.vault.init.ModEntities.VAULT_WRAITH_YELLOW)
                .add(ModEntities.GREEN_GHOST)
                .add(ModEntities.YELLOW_GHOST)
                .add(ModEntities.RED_GHOST)
                .add(ModEntities.BLUE_GHOST)
                .add(ModEntities.BLACK_GHOST)
                .add(ModEntities.BROWN_GHOST)
                .add(ModEntities.DARK_RED_GHOST)
                .add(ModEntities.DARK_GRAY_GHOST)
                .add(ModEntities.DARK_BLUE_GHOST)
                .add(ModEntities.PURPLE_GHOST);
    }
}