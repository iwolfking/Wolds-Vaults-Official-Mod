package xyz.iwolfking.woldsvaults.events.vaultevents;

import iskallia.vault.core.event.Event;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import xyz.iwolfking.woldsvaults.blocks.tiles.BrewingAltarTileEntity;

import java.util.List;

public class BrewingAltarBrewEvent extends Event<BrewingAltarBrewEvent, BrewingAltarBrewEvent.Data> {
    public BrewingAltarBrewEvent() {
    }

    protected BrewingAltarBrewEvent(BrewingAltarBrewEvent parent) {
        super(parent);
    }

    public BrewingAltarBrewEvent createChild() {
        return new BrewingAltarBrewEvent(this);
    }

    public BrewingAltarBrewEvent.Data invoke(Level world, BlockState state, BlockPos pos, BrewingAltarTileEntity entity, List<ItemStack> ingredients) {
        return this.invoke(new BrewingAltarBrewEvent.Data(world, state, pos, entity, ingredients));
    }

    public static class Data {
        private final Level world;
        private final BlockState state;
        private final BlockPos pos;
        private final BrewingAltarTileEntity entity;
        private final List<ItemStack> ingredients;

        public Data(Level world, BlockState state, BlockPos pos, BrewingAltarTileEntity entity, List<ItemStack> ingredients) {
            this.world = world;
            this.state = state;
            this.pos = pos;
            this.entity = entity;
            this.ingredients = ingredients;
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

        public BrewingAltarTileEntity getEntity() {
            return this.entity;
        }

        public List<ItemStack> getIngredients() {
            return ingredients;
        }
    }
}