package xyz.iwolfking.woldsvaults.init;

import iskallia.vault.VaultMod;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModTags {
    public static final TagKey<Item> PLAYER_GEMS = TagKey.create(Registry.ITEM_REGISTRY, WoldsVaults.id("player_gems"));
    public static final TagKey<Item> ALCHEMY_INGREDIENT = TagKey.create(Registry.ITEM_REGISTRY, WoldsVaults.id("alchemy_ingredient"));
    public static final TagKey<Item> ALCHEMY_CATALYST = TagKey.create(Registry.ITEM_REGISTRY, WoldsVaults.id("alchemy_catalyst"));
    public static final TagKey<Item> BLOOD = TagKey.create(Registry.ITEM_REGISTRY, WoldsVaults.id("blood"));
    public static final TagKey<Item> STACKABLE_SEALS = TagKey.create(Registry.ITEM_REGISTRY, WoldsVaults.id("stackable_seals"));
    public static final TagKey<Block> MINEABLE_WITH_SWORD = TagKey.create(Registry.BLOCK_REGISTRY, ResourceLocation.fromNamespaceAndPath("fabric", "mineable/sword"));
    public static final TagKey<Block> CHIPPED_COBWEB = TagKey.create(Registry.BLOCK_REGISTRY, ResourceLocation.fromNamespaceAndPath("chipped", "cobweb"));
}
