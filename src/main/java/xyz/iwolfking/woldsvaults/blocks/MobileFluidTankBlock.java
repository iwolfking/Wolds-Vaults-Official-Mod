package xyz.iwolfking.woldsvaults.blocks;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import xyz.iwolfking.woldsvaults.blocks.tiles.MobileFluidTankTileEntity;
import xyz.iwolfking.woldsvaults.items.MobileFluidTankItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * The placed Mobile Extractor Fluid Tank. Buckets fill and empty it by hand, pipes drain it from any face, and it drops
 * as a tank item carrying its glue however it is broken.
 */
public class MobileFluidTankBlock extends Block implements EntityBlock {
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, MobileFluidTankItem.FILL_LEVELS);
    private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);

    public MobileFluidTankBlock() {
        super(BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 6.0F).sound(SoundType.METAL).noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new MobileFluidTankTileEntity(pos, state);
    }

    @Nonnull
    @Override
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void setPlacedBy(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable LivingEntity placer, @Nonnull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof MobileFluidTankTileEntity tank) {
            tank.readFromItem(stack);
        }
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        if (FluidUtil.interactWithFluidHandler(player, hand, level, pos, hit.getDirection())) {
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        if (player.getItemInHand(hand).isEmpty() && !level.isClientSide() && level.getBlockEntity(pos) instanceof MobileFluidTankTileEntity tank) {
            FluidStack fluid = tank.getFluid();
            player.displayClientMessage(new TextComponent(fluid.isEmpty() ? "The tank is empty."
                    : String.format("%s: %,d / %,d mB", fluid.getDisplayName().getString(), fluid.getAmount(), MobileFluidTankItem.CAPACITY))
                    .withStyle(ChatFormatting.LIGHT_PURPLE), true);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public List<ItemStack> getDrops(@Nonnull BlockState state, @Nonnull LootContext.Builder builder) {
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof MobileFluidTankTileEntity tank) {
            return List.of(tank.toItem());
        }
        WoldLogger.error("A Mobile Extractor Fluid Tank broke without its block entity; it dropped empty.");
        return List.of(new ItemStack(this));
    }
}
