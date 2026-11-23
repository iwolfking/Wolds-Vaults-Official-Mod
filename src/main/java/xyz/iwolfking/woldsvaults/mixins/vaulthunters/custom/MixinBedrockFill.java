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
import xyz.iwolfking.woldsvaults.api.core.layout.multicell.TallRooms;
import xyz.iwolfking.woldsvaults.api.data.WoldConstants;
import xyz.iwolfking.woldsvaults.loadingzone.PocketTerritory;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = BedrockFill.class, remap = false)
public class MixinBedrockFill {
    /** Pockets bring their own walls, so no bedrock is filled in pocket territory. */
    @Inject(method = "blocksToFill", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$leavePocketsOpen(BlockPos offset, Vault vault, CallbackInfoReturnable<List<Pair<BlockPos, BlockState>>> cir) {
        if (PocketTerritory.inTerritory(offset.getX())) {
            cir.setReturnValue(new ArrayList<>());
        }
    }

    /** Leaves the bedrock band above the room box empty where a tall multi-cell room rises through it. */
    @Inject(method = "blocksToFill", at = @At("RETURN"))
    private static void woldsvaults$openTallRooms(BlockPos offset, Vault vault, CallbackInfoReturnable<List<Pair<BlockPos, BlockState>>> cir) {
        if (offset.getY() + 15 <= WoldConstants.VAULT_ROOM_BOX_TOP) {
            return;
        }
        cir.getReturnValue().removeIf(entry -> TallRooms.opensBedrock(vault, entry.getFirst()));
    }
}
