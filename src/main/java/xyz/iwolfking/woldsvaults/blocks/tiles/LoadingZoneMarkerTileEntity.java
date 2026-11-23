package xyz.iwolfking.woldsvaults.blocks.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import xyz.iwolfking.woldsvaults.blocks.LoadingZoneArrivalBlock;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.loadingzone.LoadingZoneShape;
import xyz.iwolfking.woldsvaults.loadingzone.LoadingZones;
import xyz.iwolfking.woldsvaults.loadingzone.Pockets;

public class LoadingZoneMarkerTileEntity extends BlockEntity {
    public static final int DEFAULT_HEIGHT = 3;

    private String link = "";
    private int height = DEFAULT_HEIGHT;
    private int order;
    private LoadingZoneShape.Leads leads = LoadingZoneShape.Leads.ARRIVAL;
    private String pocket = "";

    public LoadingZoneMarkerTileEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.LOADING_ZONE_MARKER_TILE_ENTITY, pos, state);
    }

    public String getLink() {
        return this.link;
    }

    public int getHeight() {
        return this.height;
    }

    public int getOrder() {
        return this.order;
    }

    public LoadingZoneShape.Leads getLeads() {
        return this.leads;
    }

    public String getPocket() {
        return this.pocket;
    }

    public boolean isArrival() {
        return getBlockState().getBlock() instanceof LoadingZoneArrivalBlock;
    }

    public void configure(String link, int height, int order, LoadingZoneShape.Leads leads, String pocket) {
        this.link = link;
        this.height = height;
        this.order = order;
        this.leads = leads;
        this.pocket = pocket;
        setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            LoadingZones.changed(this.level);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        LoadingZones.add(this);
        Pockets.arrivalChanged(this);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (this.level != null) {
            LoadingZones.remove(this);
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (this.level != null) {
            LoadingZones.remove(this);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.link = tag.getString("Link");
        this.height = tag.contains("Height") ? tag.getInt("Height") : DEFAULT_HEIGHT;
        this.order = tag.getInt("Order");
        this.leads = LoadingZoneShape.Leads.byId(tag.getInt("Leads"));
        this.pocket = tag.getString("Pocket");
        if (this.level != null) {
            LoadingZones.changed(this.level);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("Link", this.link);
        tag.putInt("Height", this.height);
        tag.putInt("Order", this.order);
        tag.putInt("Leads", this.leads.ordinal());
        tag.putString("Pocket", this.pocket);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
