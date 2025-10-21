package xyz.iwolfking.woldsvaults.blocks;

import iskallia.vault.block.MobBarrier;
import iskallia.vault.entity.entity.EternalEntity;
import iskallia.vault.entity.entity.PetEntity;
import iskallia.vault.item.CompanionItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

public class SurvivalMobBarrier extends Block implements EntityBlock {

    public SurvivalMobBarrier() {
        super(Properties.copy(Blocks.GLASS).strength(4.0F, 3600000.0F).noOcclusion().isSuffocating((state, blockGetter, pos) -> false).isViewBlocking((state, blockGetter, pos) -> false));
    }

    @Override
    public float getShadeBrightness(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState pState, BlockGetter pReader, BlockPos pPos) {
        return true;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        if (pContext instanceof EntityCollisionContext entityCollisionContext) {
            Entity entity = entityCollisionContext.getEntity();
            if (entity instanceof Player || entity instanceof EternalEntity || entity instanceof PetEntity) {
                return Shapes.empty();
            }
        }

        return super.getCollisionShape(pState, pLevel, pPos, pContext);
    }

    @Override
    public boolean skipRendering(BlockState pState, BlockState pAdjacentBlockState, Direction pSide) {
        return pAdjacentBlockState.is(this) || super.skipRendering(pState, pAdjacentBlockState, pSide);
    }

    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModBlocks.SURVIVAL_MOB_BARRIER_TILE_ENTITY_BLOCK_ENTITY_TYPE.create(pos, state);
    }
}
