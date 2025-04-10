package xyz.iwolfking.woldsvaults.modifiers.vault.map.modifiers;

import com.google.gson.annotations.Expose;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.common.TemplateGenerationEvent;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.world.data.entity.PartialCompoundNbt;
import iskallia.vault.core.world.data.tile.PartialBlockState;
import iskallia.vault.core.world.data.tile.PartialTile;
import iskallia.vault.core.world.data.tile.TilePredicate;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkAccess;
import xyz.iwolfking.woldsvaults.modifiers.vault.lib.SettableValueVaultModifier;

import java.util.ArrayList;
import java.util.List;

public class CascadeDecoratorModifierSettable extends SettableValueVaultModifier<CascadeDecoratorModifierSettable.Properties> {
    public CascadeDecoratorModifierSettable(ResourceLocation id, CascadeDecoratorModifierSettable.Properties properties, VaultModifier.Display display) {
        super(id, properties, display);
        this.setDescriptionFormatter((t, p, s) -> {
            return t.formatted((int)Math.abs(p.getValue() * (float)s * 100.0F));
        });
    }

    public void initServer(VirtualWorld world, Vault vault, ModifierContext context) {
        CommonEvents.SURFACE_GENERATION.in(world).register(context.getUUID(), (data) -> {
            this.onGenerate(vault, data.getGenRegion(), data.getChunk(), "generation");
        }, -100);
        CommonEvents.TEMPLATE_GENERATION.in(world).at(TemplateGenerationEvent.Phase.POST).register(context.getUUID(), (data) -> {
            int chunkX = data.getChunkPos().x;
            int chunkZ = data.getChunkPos().z;
            if (data.getWorld().hasChunk(chunkX, chunkZ)) {
                ChunkAccess chunk = data.getWorld().getChunk(chunkX, chunkZ);
                this.onGenerate(vault, data.getWorld(), chunk, "population");
            }

        }, -100);
    }

    public void onGenerate(Vault vault, ServerLevelAccessor world, ChunkAccess access, String phase) {
        List<PartialTile> tiles = new ArrayList();
        List<CompoundTag> pending = new ArrayList();
        access.getBlockEntitiesPos().forEach((pos) -> {
            BlockState state = world.getBlockState(pos);
            BlockEntity entity = world.getBlockEntity(pos);
            CompoundTag raw = access.getBlockEntityNbt(pos);
            if (raw == null) {
                raw = new CompoundTag();
                raw.putInt("x", pos.getX());
                raw.putInt("y", pos.getY());
                raw.putInt("z", pos.getZ());
                access.setBlockEntityNbt(raw);
            }

            if (!raw.getBoolean("cascade_duped")) {
                if (!raw.contains("phase") || raw.getString("phase").equals(phase)) {
                    PartialTile tile = PartialTile.of(PartialBlockState.of(state), PartialCompoundNbt.of(entity), pos);
                    if (((CascadeDecoratorModifierSettable.Properties)this.properties).filter.test(tile)) {
                        tiles.add(tile);
                        pending.add(raw);
                    }

                }
            }
        });
        ChunkRandom random = ChunkRandom.any();

        for(int i = 0; i < tiles.size(); ++i) {
            PartialTile tile = ((PartialTile)tiles.get(i)).copy();
            CompoundTag raw = (CompoundTag)pending.get(i);
            if (raw.contains("cascade_seed", 4)) {
                random.setSeed(raw.getLong("cascade_seed"));
            } else {
                random.setBlockSeed((Long)vault.get(Vault.SEED), tile.getPos(), 237429473L);
            }

            for(float p = ((CascadeDecoratorModifierSettable.Properties)this.properties).getValue(); p > 0.0F && random.nextFloat() < p; --p) {
                BlockPos result = this.getValidPosition(world, tile.getPos(), access.getPos(), random);
                if (result != null) {
                    BlockState current = world.getBlockState(result);
                    if (current.isAir()) {
                        tile.getState().set(BlockStateProperties.WATERLOGGED, false);
                    } else if (current.getBlock() instanceof LiquidBlock) {
                        tile.getState().set(BlockStateProperties.WATERLOGGED, true);
                    }

                    tile.place(world, result, 3);
                    CompoundTag rawResult = access.getBlockEntityNbt(result);
                    if (rawResult == null) {
                        rawResult = new CompoundTag();
                        rawResult.putInt("x", result.getX());
                        rawResult.putInt("y", result.getY());
                        rawResult.putInt("z", result.getZ());
                        access.setBlockEntityNbt(rawResult);
                    }

                    rawResult.putBoolean("cascade_duped", true);
                }
            }

            raw.putLong("cascade_seed", random.getSeed());
            raw.putString("phase", phase);
        }

    }

    public BlockPos getValidPosition(ServerLevelAccessor world, BlockPos origin, ChunkPos chunkPos, RandomSource random) {
        int index = 0;
        BlockPos result = null;

        for(int y = origin.getY() - 3; y <= origin.getY() + 3; ++y) {
            for(int x = origin.getX() - 3; x <= origin.getX() + 3; ++x) {
                for(int z = origin.getZ() - 3; z <= origin.getZ() + 3; ++z) {
                    if (x >> 4 == chunkPos.x && z >> 4 == chunkPos.z) {
                        BlockPos pos = new BlockPos(x, y, z);
                        BlockState state = world.getBlockState(pos);
                        if (world.getBlockState(pos.below()).isFaceSturdy(world, pos, Direction.UP) && (state.isAir() || state.getBlock() instanceof LiquidBlock)) {
                            ++index;
                            if (random.nextInt(index) == 0) {
                                result = pos;
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    public void releaseServer(ModifierContext context) {
        CommonEvents.SURFACE_GENERATION.release(context.getUUID());
    }

    public static class Properties extends SettableValueVaultModifier.Properties {
        @Expose
        private final TilePredicate filter;

        public Properties(TilePredicate filter) {
            this.filter = filter;
        }

        public TilePredicate getFilter() {
            return this.filter;
        }


    }
}
