package xyz.iwolfking.woldsvaults.blocks;

import iskallia.vault.init.ModSounds;
import iskallia.vault.util.BlockHelper;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.blocks.tiles.DecoLodestoneTileEntity;
import xyz.iwolfking.woldsvaults.api.data.DecoLodestoneStrings;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

import javax.annotation.Nonnull;

public class DecoLodestoneBlock extends Block implements EntityBlock {

    public DecoLodestoneBlock() {
        super(Properties.of(Material.STONE).sound(SoundType.METAL).strength(2.0F, 3600000.0F));
        this.registerDefaultState(this.stateDefinition.any());
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return ModBlocks.DECO_LODESTONE_TILE_ENTITY_BLOCK_ENTITY_TYPE.create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level world, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return BlockHelper.getTicker(type, ModBlocks.DECO_LODESTONE_TILE_ENTITY_BLOCK_ENTITY_TYPE, DecoLodestoneTileEntity::tick);
    }


    @NotNull
    @Override
    public InteractionResult use(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if(world.getBlockEntity(pos) instanceof DecoLodestoneTileEntity le) {
            if (le.isConsumed()) {
                if (!world.isClientSide) {
                    le.setConsumed(false);
                }

                return InteractionResult.SUCCESS;
            }
            else if(ServerVaults.get(world).isEmpty()) {
                if(player.isShiftKeyDown()) {
                    if(!world.isClientSide) {
                        world.playSound(null, pos, ModSounds.VAULT_PORTAL_OPEN, SoundSource.BLOCKS, 0.75F, 0.5F);
                        if(!DecoLodestoneStrings.decoLodestoneMessages.isEmpty()) {
                            player.displayClientMessage(DecoLodestoneStrings.getMessage(), false);
                        }
                    }
                    return InteractionResult.SUCCESS;
                }
                if(!world.isClientSide) {
                    le.setConsumed(true);
                    world.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 2.0F);
                }

                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }


    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 18.0, 14.0);


    @Nonnull
    @Override
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
        return SHAPE;
    }

    @Nonnull
    @Override
    public RenderShape getRenderShape(@Nonnull BlockState state) {
        return RenderShape.MODEL;
    }




}
