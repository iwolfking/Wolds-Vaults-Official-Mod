package xyz.iwolfking.woldsvaults.blocks.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.events.vaultevents.WoldCommonEvents;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

import java.util.ArrayList;
import java.util.List;


public class FracturedObeliskTileEntity extends BlockEntity {
    private float percentObfuscated = 0F;
    private boolean initialCompletion = false;



    public FracturedObeliskTileEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.FRACTURED_OBELISK_TILE_ENTITY_BLOCK_ENTITY_TYPE, pos, state);
    }

    public void sendUpdates() {
        if(this.level == null) return;

        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
        this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
        this.setChanged();
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null && level != null) {
            handleUpdateTag(tag);
            sendUpdates();
        }
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);

        percentObfuscated = nbt.getFloat("PercentObfuscated");
        initialCompletion = nbt.getBoolean("InitialCompletion");
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag nbt) {
        super.saveAdditional(nbt);

        nbt.putFloat("PercentObfuscated", percentObfuscated);
        nbt.putBoolean("InitialCompletion", initialCompletion);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FracturedObeliskTileEntity tile) {
        WoldCommonEvents.FRACTURED_OBELISK_UPDATE.invoke(level, state, pos, tile);
    }

    public float getPercentObfuscated() {
        return percentObfuscated;
    }

    public boolean getInitialCompletion() {
        return initialCompletion;
    }

    public void setPercentObfuscated(float percentObfuscated) {
        this.percentObfuscated = percentObfuscated;
    }

    public void setInitialCompletion(boolean initialCompletion) {
        this.initialCompletion = initialCompletion;
    }
}