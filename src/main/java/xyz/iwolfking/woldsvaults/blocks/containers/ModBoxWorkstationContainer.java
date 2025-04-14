package xyz.iwolfking.woldsvaults.blocks.containers;

import iskallia.vault.container.spi.ForgeRecipeContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.blocks.tiles.AugmentCraftingTableTileEntity;
import xyz.iwolfking.woldsvaults.blocks.tiles.ModBoxWorkstationTileEntity;
import xyz.iwolfking.woldsvaults.init.ModContainers;

import java.awt.*;
import java.util.function.Predicate;

public class ModBoxWorkstationContainer extends ForgeRecipeContainer<ModBoxWorkstationTileEntity> {
    public ModBoxWorkstationContainer(int windowId, Level world, BlockPos pos, Inventory playerInventory) {
        super(ModContainers.MOD_BOX_WORKSTATION_CONTAINER, windowId, world, pos, playerInventory);
    }

    protected Class<ModBoxWorkstationTileEntity> getTileClass() {
        return ModBoxWorkstationTileEntity.class;
    }

    public Point getOffset() {
        return new Point(8, 23);
    }

    @Override
    protected @Nullable ResourceLocation getBackgroundTextureForSlot(int i) {
        return null;
    }

    @Override
    protected @Nullable Predicate<ItemStack> getFilterForSlot(int i) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.getTile() != null && this.getTile().stillValid(player);
    }
}
