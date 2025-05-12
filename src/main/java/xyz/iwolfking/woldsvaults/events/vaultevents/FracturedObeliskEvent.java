package xyz.iwolfking.woldsvaults.events.vaultevents;

import iskallia.vault.core.event.Event;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import xyz.iwolfking.woldsvaults.blocks.tiles.FracturedObeliskTileEntity;

public class FracturedObeliskEvent extends Event<FracturedObeliskEvent, FracturedObeliskEvent.Data> {
    public FracturedObeliskEvent() {
    }

    protected FracturedObeliskEvent(FracturedObeliskEvent parent) {
        super(parent);
    }

    public FracturedObeliskEvent createChild() {
        return new FracturedObeliskEvent(this);
    }

    public FracturedObeliskEvent.Data invoke(Level world, BlockState state, BlockPos pos, FracturedObeliskTileEntity entity) {
        return this.invoke(new FracturedObeliskEvent.Data(world, state, pos, entity));
    }

    public static class Data {
        private final Level world;
        private final BlockState state;
        private final BlockPos pos;
        private final FracturedObeliskTileEntity entity;

        public Data(Level world, BlockState state, BlockPos pos, FracturedObeliskTileEntity entity) {
            this.world = world;
            this.state = state;
            this.pos = pos;
            this.entity = entity;
        }

        public Level getWorld() {
            return this.world;
        }

        public BlockState getState() {
            return this.state;
        }

        public BlockPos getPos() {
            return this.pos;
        }

        public FracturedObeliskTileEntity getEntity() {
            return this.entity;
        }
    }
}