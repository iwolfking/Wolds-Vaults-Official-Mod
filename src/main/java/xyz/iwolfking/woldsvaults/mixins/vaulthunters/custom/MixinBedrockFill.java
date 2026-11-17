package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.world.generator.BedrockFill;
import iskallia.vault.core.world.template.tasks.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.loadingzone.PocketTerritory;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = BedrockFill.class, remap = false)
public class MixinBedrockFill {
    // the vault walls everything below y 64 in with bedrock except its own room boxes; a pocket brings its own walls
    @Inject(method = "blocksToFill", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$leavePocketsOpen(BlockPos offset, Vault vault, CallbackInfoReturnable<List<Pair<BlockPos, BlockState>>> cir) {
        if (PocketTerritory.inTerritory(offset.getX())) {
            cir.setReturnValue(new ArrayList<>());
        }
    }
}
