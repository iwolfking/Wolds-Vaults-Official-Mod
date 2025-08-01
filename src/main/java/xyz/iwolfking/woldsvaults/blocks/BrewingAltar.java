package xyz.iwolfking.woldsvaults.blocks;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.init.ModParticles;
import iskallia.vault.util.BlockHelper;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.blocks.tiles.BrewingAltarTileEntity;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.items.alchemy.AlchemyIngredientItem;
import xyz.iwolfking.woldsvaults.items.alchemy.CatalystItem;
import xyz.iwolfking.woldsvaults.network.message.BrewingAltarParticleMessage;
import xyz.iwolfking.woldsvaults.util.VoxelShapeUtils;

import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("deprecation")
public class BrewingAltar extends Block implements EntityBlock {
    private static final VoxelShape SHAPE;
    public static final IntegerProperty USES = IntegerProperty.create("uses", 0, 5);


    public BrewingAltar() {
        super(Properties.of(Material.STONE).sound(SoundType.STONE).strength(-1.0F, 3600000.0F).noDrops().noOcclusion());
        this.registerDefaultState(this.defaultBlockState().setValue(USES, 3));
    }

    @Override
    public boolean propagatesSkylightDown(@NotNull BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(USES);
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState pState, Level pLevel, @NotNull BlockPos pPos, @NotNull Player pPlayer, @NotNull InteractionHand pHand, @NotNull BlockHitResult pHit) {
        if (pLevel.isClientSide()) return InteractionResult.PASS;
        if (!(pLevel.getBlockEntity(pPos) instanceof BrewingAltarTileEntity tileEntity)) return InteractionResult.PASS;
        if (pState.getValue(USES) == 0) return InteractionResult.PASS;

        ItemStack heldItem = pPlayer.getItemInHand(pHand);

        if (pPlayer.isCrouching() && pHand != InteractionHand.OFF_HAND && !tileEntity.isBrewing()) {
            ItemStack catalyst = tileEntity.getCatalyst();
            boolean holdingCatalyst = !heldItem.isEmpty() && catalyst.sameItem(heldItem);

            // Handle catalyst removal
            if (catalyst.getItem() != ModItems.INGREDIENT_TEMPLATE && (heldItem.isEmpty() || holdingCatalyst)) {
                ItemStack removedCatalyst = tileEntity.removeCatalyst();

                if (removedCatalyst.isEmpty()) {
                    return InteractionResult.PASS;
                }

                if (removedCatalyst.sameItem(heldItem) && ItemStack.tagMatches(removedCatalyst, heldItem)) {
                    int total = removedCatalyst.getCount() + heldItem.getCount();
                    if (total > 64) {
                        int overflow = total - 64;
                        removedCatalyst.setCount(64);
                        ItemStack overflowStack = heldItem.copy();
                        overflowStack.setCount(overflow);

                        if (!pPlayer.getInventory().add(overflowStack)) {
                            pPlayer.drop(overflowStack, false);
                        }
                    } else {
                        removedCatalyst.grow(heldItem.getCount());
                    }
                }

                pPlayer.setItemInHand(pHand, removedCatalyst);
                pLevel.playSound(null, pPos, SoundEvents.BUCKET_FILL_LAVA, SoundSource.BLOCKS, 1.0F, 0.5F);
                return InteractionResult.SUCCESS;
            }

            // Handle ingredient removal
            ItemStack lastIngredient = tileEntity.getLastIngredient();
            boolean holdingIngredient = !heldItem.isEmpty() && lastIngredient.sameItem(heldItem);

            if (!lastIngredient.isEmpty() && (heldItem.isEmpty() || holdingIngredient)) {
                ItemStack removedIngredient = tileEntity.removeLastIngredient();

                if (removedIngredient.isEmpty()) {
                    return InteractionResult.PASS;
                }

                if (removedIngredient.sameItem(heldItem) && ItemStack.tagMatches(removedIngredient, heldItem)) {
                    int total = removedIngredient.getCount() + heldItem.getCount();
                    if (total > 64) {
                        int overflow = total - 64;
                        removedIngredient.setCount(64);
                        ItemStack overflowStack = heldItem.copy();
                        overflowStack.setCount(overflow);

                        if (!pPlayer.getInventory().add(overflowStack)) {
                            pPlayer.drop(overflowStack, false);
                        }
                    } else {
                        removedIngredient.grow(heldItem.getCount());
                    }
                }

                pPlayer.setItemInHand(pHand, removedIngredient);
                pLevel.playSound(null, pPos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 1.0F);
                return InteractionResult.SUCCESS;
            }
        }

        AtomicReference<Boolean> isMatchingVault = new AtomicReference<>();
        ServerVaults.get(pLevel).ifPresent(vault -> {
            if (heldItem.getTag() == null) {
                isMatchingVault.set(false);
                return;
            }

            isMatchingVault.set(heldItem.getTag().getString("VaultId").equals(vault.get(Vault.ID).toString()));
        });
        
        if (heldItem.getItem() instanceof AlchemyIngredientItem ingredientItem && isMatchingVault.get()) {
            ItemStack singleItem = heldItem.copy();
            singleItem.setCount(1);
            if (tileEntity.addIngredient(singleItem)) {
                heldItem.shrink(1);

                pLevel.playSound(null, pPos, SoundEvents.CONDUIT_DEACTIVATE, SoundSource.BLOCKS, 0.8F, 1F + (pLevel.getRandom().nextFloat()));
                ModNetwork.sendToLevel(new BrewingAltarParticleMessage(
                                new Vec3(pPos.getX(), pPos.getY() + 0.5, pPos.getZ()),
                                pPlayer,
                                ingredientItem.getType().getStyle().getColor().getValue()
                        ),
                        pLevel
                );
                return InteractionResult.SUCCESS;
            }
        } else if (heldItem.getItem() instanceof CatalystItem && isMatchingVault.get()) {
            ItemStack singleItem = heldItem.copy();
            singleItem.setCount(1);

            if (tileEntity.addCatalyst(singleItem)) {
                heldItem.shrink(1);

                pLevel.playSound(null, pPos, SoundEvents.CONDUIT_ACTIVATE, SoundSource.BLOCKS, 0.8F, 1F + (pLevel.getRandom().nextFloat()));
                return InteractionResult.SUCCESS;
            }
        }

        if (tileEntity.isFilled() && pHand != InteractionHand.OFF_HAND && !tileEntity.isBrewing()) {
            tileEntity.setBrewing(true);


        }

        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return ModBlocks.BREWING_ALTAR_TILE_ENTITY_BLOCK_ENTITY_TYPE.create(pPos, pState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
        return BlockHelper.getTicker(
                pBlockEntityType,
                ModBlocks.BREWING_ALTAR_TILE_ENTITY_BLOCK_ENTITY_TYPE,
                (pLevel1, pPos, pState1, pBlockEntity) ->
                pBlockEntity.tick(pLevel1, pPos, pState1)
                );
    }

    @Override
    public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Random random) {
        for (int i = 0; i < 2; i++) {
            double y = pos.getY() + 0.01; // near the bottom of the block
            double vx = 0;
            double vy = 0.03 + random.nextDouble() * 0.05;
            double vz = 0;

            // Pick one of the 4 edges randomly
            double x = 0;
            double z = 0;

            switch (random.nextInt(4)) {
                case 0 -> { // North edge (z = 0)
                    x = pos.getX() + random.nextDouble();
                    z = pos.getZ();
                }
                case 1 -> { // South edge (z = 1)
                    x = pos.getX() + random.nextDouble();
                    z = pos.getZ() + 1.0;
                }
                case 2 -> { // West edge (x = 0)
                    x = pos.getX();
                    z = pos.getZ() + random.nextDouble();
                }
                case 3 -> { // East edge (x = 1)
                    x = pos.getX() + 1.0;
                    z = pos.getZ() + random.nextDouble();
                }
            }

            int uses = state.getValue(USES);
            float useRatio = Mth.clamp((float) uses / 5, 0f, 1f);

            Particle particle = Minecraft.getInstance().particleEngine.createParticle(ModParticles.BONK.get(), x, y, z, vx, vy, vz);

            if (uses == 0) {
                particle.setColor(0, 0, 0); // float values [0, 1]
                return;
            }

            int r = (int) (255 * (1 - useRatio));
            int g = (int) (255 * useRatio);
            int b = 0;



            if (particle != null) {
                particle.setColor(r / 255f, g / 255f, b / 255f); // float values [0, 1]
            }
        }
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
