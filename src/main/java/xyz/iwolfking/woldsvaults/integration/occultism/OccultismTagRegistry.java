package xyz.iwolfking.woldsvaults.integration.occultism;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.HashMap;
import java.util.Map;

public class OccultismTagRegistry {

    public static final Map<TagKey<EntityType<?>>, EntityType<?>> AUTO_ENTITY_TAGS = new HashMap<>();

    public static TagKey<EntityType<?>> getOrCreateEntityTag(EntityType<?> entityType) {
        ResourceLocation id = ForgeRegistries.ENTITIES.getKey(entityType);
        
        TagKey<EntityType<?>> tagKey = TagKey.create(
                Registry.ENTITY_TYPE_REGISTRY,
                ResourceLocation.fromNamespaceAndPath(WoldsVaults.MOD_ID, "sacrifices/" + id.getPath())
        );

        AUTO_ENTITY_TAGS.put(tagKey, entityType);
        return tagKey;
    }

    public static TagKey<EntityType<?>> getOrCreateEntityTag(ResourceLocation entityId) {
        EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(entityId);

        TagKey<EntityType<?>> tagKey = TagKey.create(
                Registry.ENTITY_TYPE_REGISTRY,
                ResourceLocation.fromNamespaceAndPath(WoldsVaults.MOD_ID, "sacrifices/" + entityId.getPath())
        );

        AUTO_ENTITY_TAGS.put(tagKey, entityType);
        return tagKey;

    }
}