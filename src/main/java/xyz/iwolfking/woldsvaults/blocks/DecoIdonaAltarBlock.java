package xyz.iwolfking.woldsvaults.blocks;

import iskallia.vault.block.base.FacedBlock;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.init.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.blocks.tiles.DecoIdonaAltarTileEntity;

import javax.annotation.Nonnull;
import java.util.Random;

public class DecoIdonaAltarBlock extends FacedBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final VaultGod GOD = VaultGod.IDONA;
    public static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);

    public DecoIdonaAltarBlock() {
        super(Properties.of(Material.STONE).strength(2.0F, 3600000.0F).noOcclusion());
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
    }

    @Nonnull
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
        return SHAPE;
    }

    public ParticleOptions getFlameParticle(BlockState state) {
        SimpleParticleType var10000 = (SimpleParticleType)ModParticles.RED_FLAME.get();
        return var10000;
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return super.getStateForPlacement(context);
    }

    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> items) {
        items.add(DecoIdonaAltarBlockItem.fromType(GOD));


    }

    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        ItemStack itemStack = super.getCloneItemStack(state, target, level, pos, player);
        itemStack.getOrCreateTag().putString("god", GOD.getSerializedName());
        return itemStack;
    }

    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, Level world, BlockPos pos, Random rand) {
        Direction facing = stateIn.getValue(FACING);
        Direction rightDirection = facing.getClockWise();
        Direction leftDirection = rightDirection.getOpposite();
        BlockEntity tileEntity = world.getBlockEntity(pos);
        for(int i = 0; i < 2; ++i) {
            this.addFlameParticle(world, pos, 8.0 - (double)rightDirection.getStepX() * 5.5 - (double)facing.getStepX() * 4.5, 6.5, 8.0 - (double)rightDirection.getStepZ() * 5.5 - (double)facing.getStepZ() * 4.5);
            this.addFlameParticle(world, pos, 8.0 - (double)rightDirection.getStepX() * 6.5 - (double)facing.getStepX() * 2.5, 6.0, 8.0 - (double)rightDirection.getStepZ() * 6.5 - (double)facing.getStepZ() * 2.5);
            this.addFlameParticle(world, pos, 8.0 - (double)rightDirection.getStepX() * 6.5 - (double)facing.getStepX() * 6.5, 6.0, 8.0 - (double)rightDirection.getStepZ() * 6.5 - (double)facing.getStepZ() * 6.5);
            this.addFlameParticle(world, pos, 8.0 - (double)leftDirection.getStepX() * 5.5 - (double)facing.getStepX() * 4.5, 6.5, 8.0 - (double)leftDirection.getStepZ() * 5.5 - (double)facing.getStepZ() * 4.5);
            this.addFlameParticle(world, pos, 8.0 - (double)leftDirection.getStepX() * 6.5 - (double)facing.getStepX() * 2.5, 6.0, 8.0 - (double)leftDirection.getStepZ() * 6.5 - (double)facing.getStepZ() * 2.5);
            this.addFlameParticle(world, pos, 8.0 - (double)leftDirection.getStepX() * 6.5 - (double)facing.getStepX() * 6.5, 6.0, 8.0 - (double)leftDirection.getStepZ() * 6.5 - (double)facing.getStepZ() * 6.5);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void addFlameParticle(Level world, BlockPos pos, double xOffset, double yOffset, double zOffset) {
        double x = (double)pos.getX() + xOffset / 16.0;
        double y = (double)pos.getY() + yOffset / 16.0;
        double z = (double)pos.getZ() + zOffset / 16.0;
        world.addParticle(this.getFlameParticle(world.getBlockState(pos)), x, y, z, 0.0, 0.0, 0.0);
    }

    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DecoIdonaAltarTileEntity(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return null;
    }
}