package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.integration.jei.lootinfo.LootInfoGroupDefinitionRegistry;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

import java.util.function.Supplier;

@Mixin(value = LootInfoGroupDefinitionRegistry.class, remap = false)
public abstract class MixinLootInfoGroupDefinitionRegistry {
    static {
        register("iskallian_leaves", () -> new ItemStack(ModBlocks.ISKALLIAN_LEAVES_BLOCK));
        register("hellish_sand", () -> new ItemStack(ModBlocks.HELLISH_SAND_BLOCK));
        register("dungeon_pedestal", () -> new ItemStack(ModBlocks.DUNGEON_PEDESTAL_BLOCK));
        register("treasure_pedestal", () -> new ItemStack(iskallia.vault.init.ModBlocks.TREASURE_PEDESTAL));
        register("vendor_pedestal", () -> new ItemStack(iskallia.vault.init.ModBlocks.SHOP_PEDESTAL));
        register("digsite_sand", () -> new ItemStack(iskallia.vault.init.ModBlocks.TREASURE_SAND));
        register("brazier_pillage", () -> new ItemStack(iskallia.vault.init.ModBlocks.MONOLITH));
        register("haunted_brazier_pillage", () -> new ItemStack(iskallia.vault.init.ModBlocks.MONOLITH));
        register("enigma_chest_map", () -> new ItemStack(iskallia.vault.init.ModBlocks.ENIGMA_CHEST));
        register("ornate_chest_map", () -> new ItemStack(iskallia.vault.init.ModBlocks.ORNATE_CHEST));
        register("living_chest_map", () -> new ItemStack(iskallia.vault.init.ModBlocks.LIVING_CHEST));
        register("gilded_chest_map", () -> new ItemStack(iskallia.vault.init.ModBlocks.GILDED_CHEST));
        register("wooden_chest_map", () -> new ItemStack(iskallia.vault.init.ModBlocks.WOODEN_CHEST));
        register("treasure_chest_map", () -> new ItemStack(iskallia.vault.init.ModBlocks.TREASURE_CHEST));
    }

    @Shadow
    protected static void register(String path, Supplier<ItemStack> catalystItemStackSupplier) {
    }
}
