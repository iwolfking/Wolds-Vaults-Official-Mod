package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.difficulty_lock;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.data.key.FieldKey;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.util.RegionPos;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.CakeObjective;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.world.generator.GridGenerator;
import iskallia.vault.core.world.generator.layout.GridLayout;
import iskallia.vault.core.world.generator.layout.VaultGridLayout;
import iskallia.vault.core.world.processor.ProcessorContext;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.core.world.template.JigsawTemplate;
import iskallia.vault.core.world.template.PlacementSettings;
import iskallia.vault.core.world.template.StructureTemplate;
import iskallia.vault.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.util.ObjectiveHelper;

import java.util.Random;

@Mixin(value = CakeObjective.class, remap = false)
public abstract class MixinCakeObjective extends Objective {
    @Shadow @Final public static FieldKey<BlockPos> CAKE_POS;

    @Inject(method = "initServer", at = @At("HEAD"))
    private void addNormalizedToCakeVaults(VirtualWorld world, Vault vault, CallbackInfo ci) {
        ObjectiveHelper.handleAddingNormalizedToVault(vault, world.getLevel());
    }

    @WrapOperation(method = "onCakeEaten", at = @At(value = "INVOKE", target = "Liskallia/vault/core/vault/objective/CakeObjective;discoverCakeRoom(Liskallia/vault/core/world/storage/VirtualWorld;Liskallia/vault/core/vault/Vault;Liskallia/vault/core/world/generator/GridGenerator;Liskallia/vault/core/world/generator/layout/VaultGridLayout;Liskallia/vault/core/util/RegionPos;)V"))
    private void genOnTopIfVillage(CakeObjective instance, VirtualWorld world, Vault vault, GridGenerator generator, VaultGridLayout layout, RegionPos region, Operation<Void> original, @Local(name = "gen")
    GridGenerator gen, @Local(name = "neighbor") RegionPos neighbor) {

        ChunkRandom chunkRandom = ChunkRandom.any();
        chunkRandom.setRegionSeed(vault.get(Vault.SEED), region.getX(), region.getZ(), 1234567890L);
        PlacementSettings settings = (new PlacementSettings(new ProcessorContext(vault, ChunkRandom.ofNanoTime()))).setFlags(272);
        var template = ((GridLayout)gen.getLayout()).getAt(vault, RegionPos.ofBlockPos(neighbor.toBlockPos(), 1, 1),  chunkRandom, settings);
        if (template instanceof JigsawTemplate jigsaw) {
            if (jigsaw.getRoot() instanceof StructureTemplate structure) {
                var path = structure.getPath();
                if (path != null && path.contains("village")) {
                    if (tryToGenCakeAbove(world, region, ChunkRandom.ofNanoTime(), 25)){
                        return;
                    }
                }
            }
        }
        original.call(instance, world, vault, generator, layout, region);
    }

    /**
     * @return true if it managed to place a cake, false otherwise
     */
    @Unique
    private boolean tryToGenCakeAbove(VirtualWorld world, RegionPos region, RandomSource random, int minPos){
        int minX = region.getX() * region.getSizeX();
        int minZ = region.getZ() * region.getSizeZ();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for(int i = 0; i < 5000; ++i) {
            int x = minX + random.nextInt(region.getSizeX());
            int z = minZ + random.nextInt(region.getSizeZ());
            int y = random.nextInt(minPos, 64);
            pos.set(x, y, z);
            if (world.getBlockState(pos).isAir() && world.getBlockState(pos.above()).isAir() && world.getBlockState(pos.below()).isFaceSturdy(world, pos, Direction.UP)) {
                world.setBlock(pos, ModBlocks.CAKE.defaultBlockState(), 3);
                this.set(CAKE_POS, pos.immutable());
                return true;
            }
        }

        return false;
    }
}
