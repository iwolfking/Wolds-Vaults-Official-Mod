package xyz.iwolfking.woldsvaults.mixins.scannable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import li.cil.scannable.client.scanning.ScanResultProviderBlock;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "scannable")
        }
)
@Mixin(value = ScanResultProviderBlock.class)
public class MixinScanResultProviderBlock {
    @WrapOperation(method = "computeScanResults", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkAccess;getSections()[Lnet/minecraft/world/level/chunk/LevelChunkSection;"))
    private LevelChunkSection[] fixIndexOutOfRangeCrash(ChunkAccess instance, Operation<LevelChunkSection[]> original, @Local(ordinal = 2) int chunkSectionIndex){
        LevelChunkSection[] result = original.call(instance);
        if (chunkSectionIndex >= result.length){
            // original code continues in looping when it retrieves null from the array
            return new LevelChunkSection[chunkSectionIndex + 1];
        }
        return result;
    }
}
