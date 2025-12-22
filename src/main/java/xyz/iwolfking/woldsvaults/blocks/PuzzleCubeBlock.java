package xyz.iwolfking.woldsvaults.blocks;

import iskallia.vault.block.base.LootableBlock;
import iskallia.vault.util.BlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;
import xyz.iwolfking.woldsvaults.blocks.tiles.IskallianLeavesTileEntity;
import xyz.iwolfking.woldsvaults.blocks.tiles.PuzzleCubeTileEntity;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PuzzleCubeBlock extends LootableBlock {
    public static final EnumProperty<CubeColor> COLOR = EnumProperty.create("color", PuzzleCubeBlock.CubeColor.class);

    public PuzzleCubeBlock() {
        super(Properties.copy(Blocks.BLACK_CONCRETE).strength(1.8F, 3600000.0F));
        this.registerDefaultState(this.stateDefinition.any().setValue(COLOR, CubeColor.BLUE));
    }

    @Nullable
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new PuzzleCubeTileEntity(pos, state);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(COLOR);
    }

    @Nullable
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        PuzzleCubeBlock.CubeColor color = PuzzleCubeBlock.CubeColor.values()[context.getLevel().random.nextInt(PuzzleCubeBlock.CubeColor.values().length)];
        return (BlockState)this.defaultBlockState().setValue(COLOR, color);
    }

    public enum CubeColor implements StringRepresentable {
        BLUE("blue"),
        GREEN("green"),
        RED("red"),
        YELLOW("yellow");

        private final String name;

        CubeColor(String name) {
            this.name = name;
        }

        @NotNull
        public String getSerializedName() {
            return this.name;
        }
    }
}
