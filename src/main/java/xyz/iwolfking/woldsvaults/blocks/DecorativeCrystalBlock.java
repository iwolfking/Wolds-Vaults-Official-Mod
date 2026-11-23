package xyz.iwolfking.woldsvaults.blocks;

import com.mojang.math.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import xyz.iwolfking.woldsvaults.api.util.ColorUtil;
import xyz.iwolfking.woldsvaults.client.simon.DecorativeCrystalScreen;
import xyz.iwolfking.woldsvaults.entities.simon.SimonCrystalEntity;

import java.util.Locale;
import java.util.Random;

/**
 * A decorative Simon's Set crystal, built from plain tilted boxes like the real crystals but as an ordinary block with no
 * tile entity. Everything about it lives in its blockstate: the side it grows out of, set when it's placed, and its shape,
 * variant, stage, colour and whether it's Prismatic, which right click opens a screen to change. Placing one picks a variant
 * from its position, so a group of them comes out mixed. A Prismatic one cycles through the hues with an animated texture.
 * The real crystals' halo, aura, facets and light pillars are drawn every frame, which a plain block can't do, so a
 * decorative one sheds sparkles instead. Like the real ones it has no collision and gives off light.
 */
public class DecorativeCrystalBlock extends Block {
    public enum Shape implements StringRepresentable {
        ORIGINAL("Original"), CATHEDRAL("Cathedral"), PHANTOM("Phantom");

        public final String label;

        Shape(String label) {
            this.label = label;
        }

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    public static final int VARIANTS = 6;

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final EnumProperty<Shape> SHAPE = EnumProperty.create("shape", Shape.class);
    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, VARIANTS - 1);
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", SimonCrystalEntity.SEED, SimonCrystalEntity.GEODE);
    public static final EnumProperty<SimonCrystalEntity.Color> COLOR = EnumProperty.create("color", SimonCrystalEntity.Color.class);
    public static final BooleanProperty PRISMATIC = BooleanProperty.create("prismatic");

    public static final double REACH_SQR = 64.0;
    private static final double[][] SIZES = {{5.6, 4.8}, {8.8, 9.6}, {14.4, 19.2}, {20.8, 32.0}};
    private static final VoxelShape[][] SHAPES = new VoxelShape[SIZES.length][Direction.values().length];

    static {
        for (int stage = 0; stage < SIZES.length; stage++) {
            for (Direction out : Direction.values()) {
                SHAPES[stage][out.ordinal()] = shape(SIZES[stage][0] / 2.0, SIZES[stage][1], out);
            }
        }
    }

    public DecorativeCrystalBlock() {
        super(BlockBehaviour.Properties.of(Material.AMETHYST).strength(0.5F).sound(SoundType.AMETHYST_CLUSTER).noCollission()
                .lightLevel(state -> state.getValue(STAGE) == SimonCrystalEntity.GEODE ? 13 : 9));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP).setValue(SHAPE, Shape.ORIGINAL).setValue(VARIANT, 0)
                .setValue(STAGE, SimonCrystalEntity.SHARD).setValue(COLOR, SimonCrystalEntity.Color.RED).setValue(PRISMATIC, false));
    }

    private static VoxelShape shape(double half, double length, Direction out) {
        double low = 8.0 - half;
        double high = 8.0 + half;
        return switch (out) {
            case UP -> box(low, 0.0, low, high, length, high);
            case DOWN -> box(low, 16.0 - length, low, high, 16.0, high);
            case NORTH -> box(low, low, 16.0 - length, high, high, 16.0);
            case SOUTH -> box(low, low, 0.0, high, high, length);
            case WEST -> box(16.0 - length, low, low, 16.0, high, high);
            case EAST -> box(0.0, low, low, length, high, high);
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, SHAPE, VARIANT, STAGE, COLOR, PRISMATIC);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace()).setValue(VARIANT, Math.floorMod(Mth.getSeed(context.getClickedPos()), VARIANTS));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(STAGE)][state.getValue(FACING).ordinal()];
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.mayBuild()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> DecorativeCrystalScreen.open(pos, state));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Validates and applies a player's choices from the crystal screen. */
    public static void configure(ServerPlayer player, BlockPos pos, int shape, int variant, int stage, int color, boolean prismatic) {
        Level level = player.level;
        if (player.distanceToSqr(Vec3.atCenterOf(pos)) > REACH_SQR || !player.mayBuild() || !level.mayInteract(player, pos)) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof DecorativeCrystalBlock) {
            SimonCrystalEntity.Color[] colors = SimonCrystalEntity.Color.values();
            Shape[] shapes = Shape.values();
            level.setBlock(pos, state.setValue(SHAPE, shapes[Mth.clamp(shape, 0, shapes.length - 1)]).setValue(VARIANT, Mth.clamp(variant, 0, VARIANTS - 1))
                    .setValue(STAGE, Mth.clamp(stage, SimonCrystalEntity.SEED, SimonCrystalEntity.GEODE))
                    .setValue(COLOR, colors[Mth.clamp(color, 0, colors.length - 1)]).setValue(PRISMATIC, prismatic), Block.UPDATE_ALL);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, Random random) {
        boolean prismatic = state.getValue(PRISMATIC);
        if (!prismatic && state.getValue(STAGE) != SimonCrystalEntity.GEODE) {
            return;
        }
        AABB box = state.getShape(level, pos).bounds().move(pos);
        Vec3 c = box.getCenter();
        for (int i = prismatic ? 3 : 1; i > 0; i--) {
            Vector3f color = prismatic ? ColorUtil.vector(Mth.hsvToRgb(random.nextFloat(), 0.7F, 1.0F)) : state.getValue(COLOR).vector();
            level.addParticle(new DustParticleOptions(color, 1.0F), c.x + (random.nextDouble() - 0.5) * box.getXsize() * 1.6,
                    c.y + (random.nextDouble() - 0.5) * box.getYsize() * 1.6, c.z + (random.nextDouble() - 0.5) * box.getZsize() * 1.6, 0.0, 0.0, 0.0);
        }
    }
}
