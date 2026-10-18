package xyz.iwolfking.woldsvaults.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.blocks.containers.SuppressorContainer;
import xyz.iwolfking.woldsvaults.blocks.tiles.SuppressorTileEntity;

public class SuppressorBlock extends BaseEntityBlock {

    public SuppressorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof xyz.iwolfking.woldsvaults.blocks.tiles.SuppressorTileEntity suppressor) {
                MenuProvider containerProvider = new SimpleMenuProvider(
                    (id, inventory, p) -> new SuppressorContainer(id, inventory, suppressor, suppressor.getDataAccess()),
                    new TextComponent("Suppressor Options")
                );
                NetworkHooks.openGui((ServerPlayer) player, containerProvider, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SuppressorTileEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}