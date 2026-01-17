package xyz.iwolfking.woldsvaults.init;

import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModTags {
    public static final TagKey<Item> PLAYER_GEMS = TagKey.create(Registry.ITEM_REGISTRY, WoldsVaults.id("player_gems"));
    public static final TagKey<Item> ALCHEMY_INGREDIENT = TagKey.create(Registry.ITEM_REGISTRY, WoldsVaults.id("alchemy_ingredient"));
    public static final TagKey<Item> ALCHEMY_CATALYST = TagKey.create(Registry.ITEM_REGISTRY, WoldsVaults.id("alchemy_catalyst"));

}
