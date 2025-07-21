package xyz.iwolfking.woldsvaults.init;

import iskallia.vault.core.world.data.entity.PartialCompoundNbt;
import iskallia.vault.core.world.data.tile.PartialBlockState;
import iskallia.vault.core.world.data.tile.PartialTile;
import iskallia.vault.core.world.template.DynamicTemplate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import xyz.iwolfking.woldsvaults.blocks.FracturedObelisk;

public class ModObjectiveTemplates {
    public static DynamicTemplate CORRUPTED_OBJECTIVE_TEMPLATE;
    public static DynamicTemplate ALCHEMY_OBJECTIVE_TEMPLATE;

    private static DynamicTemplate createDynamicTemplate(BlockPos pos, BlockState state) {
        DynamicTemplate template = new DynamicTemplate();
        template.add(PartialTile.of(PartialBlockState.of(state), PartialCompoundNbt.empty(), pos));
        return template;
    }

    private static DynamicTemplate createDynamicTemplate(BlockPos[] positions, BlockState[] states) {
        DynamicTemplate template = new DynamicTemplate();

        for(int i = 0; i < positions.length; ++i) {
            template.add(PartialTile.of(PartialBlockState.of(states[i]), PartialCompoundNbt.empty(), positions[i]));
        }

        return template;
    }

    static {
        CORRUPTED_OBJECTIVE_TEMPLATE = createDynamicTemplate(new BlockPos[]{new BlockPos(0, 0, 0), new BlockPos(0, 1, 0)}, new BlockState[]{ModBlocks.FRACTURED_OBELISK.defaultBlockState().setValue(FracturedObelisk.HALF, DoubleBlockHalf.LOWER), ModBlocks.FRACTURED_OBELISK.defaultBlockState().setValue(FracturedObelisk.HALF, DoubleBlockHalf.UPPER)});
        ALCHEMY_OBJECTIVE_TEMPLATE = createDynamicTemplate(new BlockPos(0, 0, 0), ModBlocks.BREWING_ALTAR.defaultBlockState());
    }
}
