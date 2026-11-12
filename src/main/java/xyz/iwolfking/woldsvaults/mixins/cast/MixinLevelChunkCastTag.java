package xyz.iwolfking.woldsvaults.mixins.cast;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.cast.CastContext;

/** Tags a block entity placed during a cast (a totem) with that cast. */
@Mixin(LevelChunk.class)
public class MixinLevelChunkCastTag {

    @Inject(method = "setBlockEntity", at = @At("HEAD"))
    private void woldsvaults$tagPlacedBlockEntity(BlockEntity blockEntity, CallbackInfo ci) {
        CastContext.tagIfCasting(blockEntity);
    }
}
