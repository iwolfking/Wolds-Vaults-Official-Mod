package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.stat.DiscoveredRoomStat;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.loadingzone.PocketTerritory;

import java.util.Map;

@Mixin(value = DiscoveredRoomStat.class, remap = false)
public class MixinDiscoveredRoomStat {
    // the layout still has rooms on paper out in pocket territory; standing there mustn't discover them
    @Inject(method = "onTick", at = @At("HEAD"), cancellable = true)
    private void woldsvaults$skipPockets(ServerPlayer player, Vault vault, CallbackInfoReturnable<Map.Entry<BlockPos, ResourceLocation>> cir) {
        if (PocketTerritory.inPocket(player)) {
            cir.setReturnValue(null);
        }
    }
}
