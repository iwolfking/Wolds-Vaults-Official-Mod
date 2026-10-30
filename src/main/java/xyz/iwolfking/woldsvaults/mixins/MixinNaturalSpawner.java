package xyz.iwolfking.woldsvaults.mixins;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NaturalSpawner.class)
public class MixinNaturalSpawner {

    @Inject(
        method = "spawnForChunk(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/NaturalSpawner$SpawnState;ZZZ)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void throttleChunkSpawning(
        ServerLevel level, 
        LevelChunk chunk, 
        NaturalSpawner.SpawnState spawnState, 
        boolean spawnFriendlies, 
        boolean spawnEnemies, 
        boolean spawnPersistent, 
        CallbackInfo ci
    ) {
        if (level.getGameTime() % 20L != 0L) {
            ci.cancel();
        }
    }
}