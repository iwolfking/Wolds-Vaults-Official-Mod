package xyz.iwolfking.woldsvaults.blocks;


import iskallia.vault.init.ModParticles;
import iskallia.vault.util.BlockHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.blocks.tiles.FracturedObeliskTileEntity;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

import java.util.Random;


public class FracturedObelisk extends Block implements EntityBlock {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 32.0, 14.0);
    private static final VoxelShape SHAPE_TOP = SHAPE.move(0.0, -1.0, 0.0);

    public FracturedObelisk() {
        super(Properties.of(Material.STONE).sound(SoundType.METAL).strength(-1.0F, 3600000.0F).noDrops());
        this.registerDefaultState(this.stateDefinition.any().setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HALF);
    }

    // Why do i need to do this, wtf, why do the other double blocks work fine??
    @Override
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        super.onPlace(pState, pLevel, pPos, pOldState, pIsMoving);
        if(pLevel.getBlockState(pPos.above()) != pLevel.getBlockState(pPos).setValue(HALF, DoubleBlockHalf.UPPER) && pState.getValue(HALF) != DoubleBlockHalf.UPPER) {
            pLevel.setBlock(pPos.above(), pLevel.getBlockState(pPos).setValue(HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
        }
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        return InteractionResult.PASS;
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? SHAPE_TOP : SHAPE;
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, BlockState blockState) {
        return blockState.getValue(HALF) == DoubleBlockHalf.LOWER ? ModBlocks.FRACTURED_OBELISK_TILE_ENTITY_BLOCK_ENTITY_TYPE.create(blockPos, blockState) : null;

    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level world, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return BlockHelper.getTicker(type, ModBlocks.FRACTURED_OBELISK_TILE_ENTITY_BLOCK_ENTITY_TYPE, FracturedObeliskTileEntity::tick);
    }

    @Override
    public void animateTick(@NotNull BlockState pState, Level pLevel, BlockPos pPos, @NotNull Random pRandom) {
        if(pState.getValue(HALF) == DoubleBlockHalf.UPPER) return;

        double centerX = pPos.getX() + 0.5;
        double centerY = pPos.getY() + 0.5;
        double centerZ = pPos.getZ() + 0.5;

        double[] radii = {1.0, 1.5, 3.5}; // might add more rings
        int particlesPerTick = 8;

        long gameTime = pLevel.getGameTime();
        double angleOffset = gameTime % 360 * (Math.PI / 180);

        for (double radius : radii) {
            for (int i = 0; i < particlesPerTick; i++) {
                double angle = (2 * Math.PI / particlesPerTick) * i + angleOffset;

                double particleX = centerX + radius * Math.cos(angle);
                double particleZ = centerZ + radius * Math.sin(angle);
                double particleY = centerY;

                ParticleEngine pe = Minecraft.getInstance().particleEngine;
                Particle p = pe.createParticle(ModParticles.UBER_PYLON.get(), particleX, particleY, particleZ, 0, 0, 0);
                p.setColor(1.0F, 0.0F, 0.0F);

            }
        }
    }


    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
        super.onRemove(state, world, pos, newState, isMoving);
        if (!state.is(newState.getBlock())) {
            if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
                BlockState otherState = world.getBlockState(pos.below());
                if (otherState.is(state.getBlock())) {
                    world.removeBlock(pos.below(), isMoving);
                }
            } else {
                BlockState otherState = world.getBlockState(pos.above());
                if (otherState.is(state.getBlock())) {
                    world.removeBlock(pos.above(), isMoving);
                }
            }
        }
    }
}