package xyz.iwolfking.woldsvaults.mixins.cast;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.iwolfking.woldsvaults.api.cast.CastCarrier;
import xyz.iwolfking.woldsvaults.api.cast.CastContext;
import xyz.iwolfking.woldsvaults.spellcraft.scope.PlacedBlockGlyphs;

/** Runs the tick of a block entity a cast placed under that cast's context. */
@Mixin(targets = "net.minecraft.world.level.chunk.LevelChunk$BoundTickingBlockEntity")
public class MixinBoundTickingBlockEntityCastTag {

    @Shadow
    @Final
    private BlockEntity blockEntity;

    @WrapMethod(method = "tick")
    private void woldsvaults$tickUnderCast(Operation<Void> original) {
        CastContext.runTask(PlacedBlockGlyphs.tagOrRestore(this.blockEntity), CastCarrier.BLOCK_ENTITY, () -> {
            original.call();
        });
    }
}
