package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.integration.jei.lootinfo.LootInfoGroupDefinitionRegistry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.init.ModItems;
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
        register("tombstone", () -> new ItemStack(ModBlocks.GRAVEYARD_LOOT_BLOCK));
        register("god_altar_velara", () -> new ItemStack(ModItems.VELARA_APPLE));
        register("god_altar_tenos", () -> new ItemStack(ModItems.TOME_OF_TENOS));
        register("god_altar_idona", () -> new ItemStack(ModItems.IDONA_DAGGER));
        register("god_altar_wendarr", () -> new ItemStack(ModItems.WENDARR_GEM));
        register("dungeon_mobs", () -> new ItemStack(Blocks.ZOMBIE_HEAD));
        register("dungeon_boss", () -> new ItemStack(Blocks.WITHER_SKELETON_SKULL));
        register("cardboard_box", () -> new ItemStack(iskallia.vault.init.ModBlocks.CARDBOARD_BOX));
        register("cardboard_box_dull", () -> new ItemStack(iskallia.vault.init.ModBlocks.CARDBOARD_BOX));
        register("wooden_dungeon_barrel", () -> new ItemStack(iskallia.vault.init.ModBlocks.WOODEN_BARREL));
        register("time_trial_reward_crate", () -> new ItemStack(ModBlocks.getCrateFor("time_trial_reward")));
    }

    @Shadow
    protected static void register(String path, Supplier<ItemStack> catalystItemStackSupplier) {
    }
}
