package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.world.generator.GridGenerator;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.loadingzone.PocketTemplates;
import xyz.iwolfking.woldsvaults.loadingzone.PocketTerritory;

@Mixin(value = GridGenerator.class, remap = false)
public class MixinGridGenerator {
    @Inject(method = "generate(Liskallia/vault/core/vault/Vault;Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/level/ChunkPos;)V", at = @At("HEAD"), cancellable = true)
    private void woldsvaults$generatePockets(Vault vault, ServerLevelAccessor world, ChunkPos chunkPos, CallbackInfo ci) {
        if (PocketTerritory.inTerritory(chunkPos.getMinBlockX())) {
            PocketTemplates.generate(vault, world, chunkPos);
            ci.cancel();
        }
    }
}
