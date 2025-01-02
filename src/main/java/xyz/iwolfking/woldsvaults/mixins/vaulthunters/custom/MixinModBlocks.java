package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.block.entity.VaultChestTileEntity;
import iskallia.vault.init.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import static iskallia.vault.init.ModBlocks.*;

@Mixin(value = ModBlocks.class, remap = false)
public class MixinModBlocks {

    // Replace VaultChestTileEntity Register with one including Corrupted Chests, can do it with a ModifyArg but im too lazy
    @Shadow
    @Final
    public static BlockEntityType<VaultChestTileEntity> VAULT_CHEST_TILE_ENTITY =
            BlockEntityType.Builder.of(VaultChestTileEntity::new, WOODEN_CHEST, ALTAR_CHEST,
                    GILDED_CHEST, LIVING_CHEST, ORNATE_CHEST, TREASURE_CHEST, HARDENED_CHEST, ENIGMA_CHEST, FLESH_CHEST,
                    WOODEN_CHEST_PLACEABLE, ALTAR_CHEST_PLACEABLE, GILDED_CHEST_PLACEABLE, LIVING_CHEST_PLACEABLE, HARDENED_CHEST_PLACEABLE, ENIGMA_CHEST_PLACEABLE,
                    FLESH_CHEST_PLACEABLE, ORNATE_CHEST_PLACEABLE, TREASURE_CHEST_PLACEABLE, ORNATE_STRONGBOX, GILDED_STRONGBOX, LIVING_STRONGBOX,
                    ORNATE_BARREL, GILDED_BARREL, LIVING_BARREL, WOODEN_BARREL, xyz.iwolfking.woldsvaults.init.ModBlocks.CORRUPTED_CHEST
            ).build(null);
}