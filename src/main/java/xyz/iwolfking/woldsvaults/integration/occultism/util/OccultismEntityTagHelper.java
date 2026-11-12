package xyz.iwolfking.woldsvaults.integration.occultism.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.vhapi.api.util.ResourceLocUtils;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

public class OccultismEntityTagHelper {
    public static ResourceLocation convertSacrificeTagToEntityId(ResourceLocation typeTagKey) {

        ResourceLocation strippedKey = ResourceLocUtils.removePrefixFromId("sacrifices/", typeTagKey);

        EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(strippedKey);

        if(entityType != null) {
            return entityType.getRegistryName();
        }

        return typeTagKey;
    }

    public static ResourceLocation convertSacrificeTagToEntityId(TagKey<EntityType<?>> typeTagKey) {
        return convertSacrificeTagToEntityId(typeTagKey.location());
    }
}
