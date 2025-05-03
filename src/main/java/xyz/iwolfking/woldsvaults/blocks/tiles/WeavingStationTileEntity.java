package xyz.iwolfking.woldsvaults.blocks.tiles;

import iskallia.vault.block.entity.base.ForgeRecipeTileEntity;
import iskallia.vault.config.recipe.ForgeRecipeType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.blocks.containers.ModBoxWorkstationContainer;
import xyz.iwolfking.woldsvaults.blocks.containers.WeavingStationContainer;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

public class WeavingStationTileEntity extends ForgeRecipeTileEntity implements MenuProvider {

    public WeavingStationTileEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModBlocks.WEAVING_STATION_TILE_ENTITY_BLOCK_ENTITY_TYPE, pWorldPosition, pBlockState, 6, ForgeRecipeType.valueOf("WEAVING"));
    }

    @Override
    public Component getDisplayName() {
        return new TextComponent("Weaving Station");
    }

    @Override
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return new WeavingStationContainer(i, this.getLevel(), this.getBlockPos(), inventory);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new WeavingStationContainer(i, this.getLevel(), this.getBlockPos(), inventory);
    }
}
