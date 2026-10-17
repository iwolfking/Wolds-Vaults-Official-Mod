package xyz.iwolfking.woldsvaults.blocks.containers;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.blocks.tiles.SuppressorTileEntity;
import xyz.iwolfking.woldsvaults.init.ModContainers;

public class SuppressorContainer extends AbstractContainerMenu {

    private final ContainerData data;
    public final SuppressorTileEntity tileEntity;
    private final BlockPos blockPos;

    public SuppressorContainer(int windowId, Inventory inv, FriendlyByteBuf friendlyByteBuf) {
        this(windowId, inv, getTileEntityFromBuffer(inv, friendlyByteBuf), 
             new SimpleContainerData(SuppressorTileEntity.SuppressorType.values().length * 2));
    }

    private static SuppressorTileEntity getTileEntityFromBuffer(Inventory inv, FriendlyByteBuf buffer) {
        try {
            BlockPos pos = buffer.readBlockPos();
            if (inv.player.level != null && inv.player.level.getBlockEntity(pos) instanceof SuppressorTileEntity suppressor) {
                return suppressor;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public SuppressorContainer(int windowId, Inventory inv, BlockPos pos) {
        this(windowId, inv, inv.player.level.getBlockEntity(pos) instanceof SuppressorTileEntity suppressor ? suppressor : null, 
             new SimpleContainerData(SuppressorTileEntity.SuppressorType.values().length * 2));
    }

    public SuppressorContainer(int windowId, Inventory inv, SuppressorTileEntity tileEntity, ContainerData data) {
        super(ModContainers.SUPPRESSOR_CONTAINER, windowId);
        checkContainerDataCount(data, SuppressorTileEntity.SuppressorType.values().length * 2);
        this.tileEntity = tileEntity;
        this.data = data;
        this.blockPos = tileEntity != null ? tileEntity.getBlockPos() : BlockPos.ZERO;
        
        if (tileEntity != null) {
            ContainerData sourceData = tileEntity.getDataAccess();
            for (int i = 0; i < data.getCount(); i++) {
                data.set(i, sourceData.get(i));
            }
        }
        
        this.addDataSlots(data);
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    @Override
    public boolean stillValid(Player player) {
        return tileEntity != null && !tileEntity.isRemoved() 
            && player.distanceToSqr(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    public boolean isEnabled(SuppressorTileEntity.SuppressorType type) {
        return data.get(type.ordinal() * 2) != 0;
    }

    public int getRange(SuppressorTileEntity.SuppressorType type) {
        return data.get(type.ordinal() * 2 + 1);
    }
}