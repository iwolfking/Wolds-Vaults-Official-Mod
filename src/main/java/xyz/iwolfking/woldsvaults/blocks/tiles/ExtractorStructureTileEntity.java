package xyz.iwolfking.woldsvaults.blocks.tiles;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

import javax.annotation.Nullable;

public class ExtractorStructureTileEntity extends BlockEntity {
    private BlockPos controllerPos;

    public ExtractorStructureTileEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.EXTRACTOR_STRUCTURE_TILE_ENTITY_TYPE, pos, state);
    }

    @Nullable
    public BlockPos getControllerPos() {
        return this.controllerPos;
    }

    public void setControllerPos(BlockPos controllerPos) {
        this.controllerPos = controllerPos;
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.controllerPos = tag.contains("Controller") ? NbtUtils.readBlockPos(tag.getCompound("Controller")) : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.controllerPos != null) {
            tag.put("Controller", NbtUtils.writeBlockPos(this.controllerPos));
        }
    }

    /**
     * Every cell of the Extraction Forge forwards pipe capabilities to the controller, so the forge can be fed from
     * any face. Assembler cells forward too, but the assembler itself exposes nothing.
     */
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (this.level != null && this.controllerPos != null && !this.isRemoved()
                && this.level.getBlockEntity(this.controllerPos) instanceof ExtractorForgeTileEntity forge) {
            return forge.getCapability(cap, side);
        }
        return super.getCapability(cap, side);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
