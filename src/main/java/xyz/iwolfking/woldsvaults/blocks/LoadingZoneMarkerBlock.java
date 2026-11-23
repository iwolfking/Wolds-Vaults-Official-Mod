package xyz.iwolfking.woldsvaults.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import xyz.iwolfking.woldsvaults.blocks.tiles.LoadingZoneMarkerTileEntity;
import xyz.iwolfking.woldsvaults.client.loadingzone.LoadingZoneScreen;
import xyz.iwolfking.woldsvaults.loadingzone.LoadingZoneShape;
import xyz.iwolfking.woldsvaults.loadingzone.Pockets;

import java.util.Locale;

public abstract class LoadingZoneMarkerBlock extends Block implements EntityBlock {
    public static final int MAX_LINK_LENGTH = 32;
    public static final int MAX_HEIGHT = 32;
    public static final int MAX_ORDER = 64;
    public static final int MAX_POCKET_LENGTH = 128;
    private static final double REACH_SQR = 64.0;
    private static final VoxelShape SELECTION = Block.box(4.0, 4.0, 4.0, 12.0, 12.0, 12.0);

    protected LoadingZoneMarkerBlock() {
        super(Properties.of(Material.BARRIER).strength(-1.0F, 3600000.0F).noDrops().noOcclusion().noCollission());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LoadingZoneMarkerTileEntity(pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return context instanceof EntityCollisionContext entityContext && entityContext.getEntity() instanceof Player player && player.isCreative() ? SELECTION : Shapes.empty();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.isCreative()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide && level.getBlockEntity(pos) instanceof LoadingZoneMarkerTileEntity marker) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> LoadingZoneScreen.open(marker));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Validates and applies a builder's choices from the marker screen. */
    public static void configure(ServerPlayer player, BlockPos pos, String link, int height, int order, int rotation, LoadingZoneShape.Leads leads, String pocket) {
        Level level = player.level;
        if (!player.isCreative() || player.distanceToSqr(Vec3.atCenterOf(pos)) > REACH_SQR || !(level.getBlockEntity(pos) instanceof LoadingZoneMarkerTileEntity marker)) {
            return;
        }
        String trimmed = link.trim();
        String pocketId = pocket.trim().toLowerCase(Locale.ROOT);
        marker.configure(trimmed.substring(0, Math.min(trimmed.length(), MAX_LINK_LENGTH)), Mth.clamp(height, 1, MAX_HEIGHT), Mth.clamp(order, 0, MAX_ORDER),
                leads, pocketId.substring(0, Math.min(pocketId.length(), MAX_POCKET_LENGTH)));
        BlockState state = level.getBlockState(pos);
        if (state.hasProperty(LoadingZoneArrivalBlock.ROTATION)) {
            level.setBlock(pos, state.setValue(LoadingZoneArrivalBlock.ROTATION, rotation & 15), Block.UPDATE_ALL);
        }
        Pockets.arrivalChanged(marker);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())) {
            Pockets.arrivalRemoved(level, pos);
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }
}
