package xyz.iwolfking.woldsvaults.blocks;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.blocks.containers.FloatingTextEditScreen;
import xyz.iwolfking.woldsvaults.blocks.tiles.ConfigurableFloatingTextTileEntity;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.packets.OpenFloatingTextScreenPacket;

public class ConfigurableFloatingTextBlock extends Block implements EntityBlock {

    public ConfigurableFloatingTextBlock() {
        super(Properties.of(Material.BARRIER).strength(4.0F, 3.6E8F).noDrops().noOcclusion().noCollission());
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ConfigurableFloatingTextTileEntity(pos, state);
    }

    @Override
    public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        if (!world.isClientSide) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof ConfigurableFloatingTextTileEntity tile) {
                ItemStack stack = new ItemStack(this);

                CompoundTag nbt = tile.saveToItem();
                stack.setTag(nbt);

                Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }

        super.playerWillDestroy(world, pos, state, player);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);

        if (!world.isClientSide) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof ConfigurableFloatingTextTileEntity tile && stack.hasTag() && stack.getTag() != null) {
                tile.loadFromItem(stack.getTag());
            }
        }
    }


    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            ModNetwork.CHANNEL.send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new OpenFloatingTextScreenPacket(pos)
            );
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @OnlyIn(Dist.CLIENT)
    private void openConfigurableFloatingTextEditScreen(ConfigurableFloatingTextTileEntity tile) {
        Minecraft.getInstance().setScreen(
            new FloatingTextEditScreen(tile)
        );
    }

    // stuff from BarrierBlock
    @Override
    public boolean propagatesSkylightDown(BlockState pState, BlockGetter pReader, BlockPos pPos) {
        return true;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public float getShadeBrightness(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return 1.0F;
    }

}
