package xyz.iwolfking.woldsvaults.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.blocks.tiles.BrewingAltarTileEntity;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.items.AlchemyIngredientItem;
import xyz.iwolfking.woldsvaults.util.VoxelShapeUtils;

@SuppressWarnings("deprecation")
public class BrewingAltar extends Block implements EntityBlock {
    public static final IntegerProperty USES = IntegerProperty.create("used", 0, 10);
    private static final VoxelShape SHAPE;

    public BrewingAltar() {
        super(Properties.of(Material.STONE).sound(SoundType.STONE).strength(-1.0F, 3600000.0F).noDrops().noOcclusion());
        this.registerDefaultState(this.getStateDefinition().any().setValue(USES, 3));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(USES);
    }

    @Override
    public boolean propagatesSkylightDown(@NotNull BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos) {
        return true;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState pState, Level pLevel, @NotNull BlockPos pPos, @NotNull Player pPlayer, @NotNull InteractionHand pHand, @NotNull BlockHitResult pHit) {
        if (pLevel.isClientSide()) return InteractionResult.PASS;
        if (!(pLevel.getBlockEntity(pPos) instanceof BrewingAltarTileEntity tileEntity)) return InteractionResult.PASS;

        ItemStack heldItem = pPlayer.getItemInHand(pHand);

        if (pPlayer.isCrouching() && pHand != InteractionHand.OFF_HAND && (heldItem.isEmpty() || tileEntity.getLastIngredient().sameItem(heldItem))) {
            ItemStack stack = tileEntity.removeLastIngredient();

            if (stack.isEmpty()) {
                return InteractionResult.PASS;
            }

            pPlayer.setItemInHand(pHand, stack);
            pLevel.playSound(null, pPos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 1.0F);
        }


        if (heldItem.getItem() instanceof AlchemyIngredientItem) {
            ItemStack singleItem = heldItem.copy();
            singleItem.setCount(1);
            if (tileEntity.addIngredient(singleItem)) {
                heldItem.shrink(1);

                pLevel.playSound(null, pPos, SoundEvents.CONDUIT_DEACTIVATE, SoundSource.BLOCKS, 0.8F, 1F + (pLevel.getRandom().nextFloat()));
                return InteractionResult.SUCCESS;
            }
        }

        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return ModBlocks.BREWING_ALTAR_TILE_ENTITY_BLOCK_ENTITY_TYPE.create(pPos, pState);
    }

    static {
       SHAPE = VoxelShapeUtils.combine(
               VoxelShapeUtils.box(1, 0, 1, 15, 3, 15), // base
               VoxelShapeUtils.box(0.25, 5, 0.25, 15.75, 8, 15.75),
               VoxelShapeUtils.box(11, 3, 7, 13, 5, 9),
               VoxelShapeUtils.box(7, 3, 11, 9, 5, 13),
               VoxelShapeUtils.box(7, 3, 3, 9, 5, 5),
               VoxelShapeUtils.box(3, 3, 7, 5, 5, 9),
               VoxelShapeUtils.box(5, 3, 5, 11, 5, 11),
               VoxelShapeUtils.box(0.5, 8, 10.5, 5.5, 10, 15.5), // ped
               VoxelShapeUtils.box(5.5, 8, 0.5, 10.5, 10, 5.5), // ped
               VoxelShapeUtils.box(10.5, 8, 10.5, 15.5, 10, 15.5), // ped
               VoxelShapeUtils.box(0, 8.25, 0, 16, 8.25, 16), // main
               VoxelShapeUtils.box(4, 4.25, 16, 12, 8.25, 16),
               VoxelShapeUtils.box(16, 4.25, 4, 16, 8.25, 12),
               VoxelShapeUtils.box(4, 4.25, 0, 12, 8.25, 0),
               VoxelShapeUtils.box(0, 4.25, 4, 0, 8.25, 12)
       );
    }


}
