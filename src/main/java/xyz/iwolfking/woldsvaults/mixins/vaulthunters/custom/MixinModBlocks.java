package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.block.entity.VaultChestTileEntity;
import iskallia.vault.block.entity.VaultCrateTileEntity;
import iskallia.vault.init.ModBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;
import java.util.Arrays;

import static iskallia.vault.init.ModBlocks.*;

@Mixin(value = ModBlocks.class, remap = false)
public class MixinModBlocks {

    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntityType$Builder;of(Lnet/minecraft/world/level/block/entity/BlockEntityType$BlockEntitySupplier;[Lnet/minecraft/world/level/block/Block;)Lnet/minecraft/world/level/block/entity/BlockEntityType$Builder;", remap = true, ordinal = 9), index = 1)
    private static Block[] injectNewBackpack(Block[] pValidBlocks) {
        ArrayList<Block> pedestalList = new java.util.ArrayList<>(Arrays.stream(pValidBlocks).toList());
        pedestalList.add(xyz.iwolfking.woldsvaults.init.ModBlocks.ETCHING_PEDESTAL);
        return pedestalList.toArray(new Block[]{});
    }

    // Replace VaultChestTileEntity Register with one including Corrupted Chests, can do it with a ModifyArg but im too lazy
    @Shadow
    @Final
    public static BlockEntityType<VaultChestTileEntity> VAULT_CHEST_TILE_ENTITY =
            BlockEntityType.Builder.of(VaultChestTileEntity::new, WOODEN_CHEST, ALTAR_CHEST,
                    GILDED_CHEST, LIVING_CHEST, ORNATE_CHEST, TREASURE_CHEST, HARDENED_CHEST, ENIGMA_CHEST, FLESH_CHEST,
                    WOODEN_CHEST_PLACEABLE, ALTAR_CHEST_PLACEABLE, GILDED_CHEST_PLACEABLE, LIVING_CHEST_PLACEABLE, HARDENED_CHEST_PLACEABLE,
                    ENIGMA_CHEST_PLACEABLE, FLESH_CHEST_PLACEABLE, ORNATE_CHEST_PLACEABLE, TREASURE_CHEST_PLACEABLE, ORNATE_STRONGBOX,
                    GILDED_STRONGBOX, LIVING_STRONGBOX, ORNATE_BARREL, GILDED_BARREL, LIVING_BARREL, WOODEN_BARREL,
                    xyz.iwolfking.woldsvaults.init.ModBlocks.CORRUPTED_CHEST
            ).build(null);

    // Replace VaultCrateTileEntity Register with one including Corrupted Crates, can do it with a ModifyArg but im too lazy
    @Shadow
    @Final
    public static BlockEntityType<VaultCrateTileEntity> VAULT_CRATE_TILE_ENTITY =
            BlockEntityType.Builder.of(VaultCrateTileEntity::new, VAULT_CRATE, VAULT_CRATE_CAKE,
                    VAULT_CRATE_ARENA, VAULT_CRATE_SCAVENGER, VAULT_CRATE_CHAMPION, VAULT_CRATE_BOUNTY,
                    VAULT_CRATE, VAULT_CRATE_MONOLITH, VAULT_CRATE_ELIXIR, VAULT_CRATE_PARADOX,
                    xyz.iwolfking.woldsvaults.init.ModBlocks.VAULT_CRATE_CORRUPTED
            ).build(null);

}
