package xyz.iwolfking.woldsvaults.integration.occultism.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

public class OccultismEntityTagHelper {
    public static ResourceLocation convertSacrificeTagToEntityId(ResourceLocation typeTagKey) {
        String path = typeTagKey.getPath();
        if (path.startsWith("sacrifices/")) {
            path = path.substring("sacrifices/".length());
        }

        ResourceLocation entityId = path.contains(":")
                ? ResourceLocation.parse(path)
                : ResourceLocation.fromNamespaceAndPath("minecraft", path);

        EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(entityId);

        if(entityType != null) {
            return entityType.getRegistryName();
        }

        return typeTagKey;
    }

    public static ResourceLocation convertSacrificeTagToEntityId(TagKey<EntityType<?>> typeTagKey) {
        return convertSacrificeTagToEntityId(typeTagKey.location());
    }
}
