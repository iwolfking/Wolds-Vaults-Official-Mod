package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.vault.challenge.temple.TempleChallengeManager;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import iskallia.vault.core.world.storage.BlockCuboid;
import iskallia.vault.core.world.storage.IZonedWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TempleChallengeManager.class, remap = false)
public class MixinTempleChallengeManager {
    @Shadow
    private BlockCuboid zone;

    @Unique
    private final List<BlockPos> wolds$gatePositions = new ArrayList<>();

    @Inject(method = "activateChallenge", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;playSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"), remap = true)
    private void wolds$closeTempleGates(ServerLevel world, CallbackInfo ci) {
        wolds$closeGates(world);
    }

    @Inject(method = "completeChallenge", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;playSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"), remap = true)
    private void wolds$openTempleGates(ServerLevel world, CallbackInfo ci) {
        wolds$openGates(world);
    }

    @Unique
    private void wolds$closeGates(ServerLevel world) {
        if (this.zone != null && this.wolds$gatePositions.isEmpty()) {
            BlockCuboid room = this.zone.offset(((TempleChallengeManager)(Object)this).getPos());
            int top = Math.min(room.getMaxY(), ((TempleChallengeManager)(Object)this).getPos().getY() + 17);
            Set<BlockPos> openings = new LinkedHashSet<>();

            for (int y = room.getMinY(); y <= top; y++) {
                for (int x = room.getMinX(); x <= room.getMaxX(); x++) {
                    wolds$addIfOpen(world, new BlockPos(x, y, room.getMinZ()), openings);
                    wolds$addIfOpen(world, new BlockPos(x, y, room.getMaxZ()), openings);
                }
                for (int z = room.getMinZ(); z <= room.getMaxZ(); z++) {
                    wolds$addIfOpen(world, new BlockPos(room.getMinX(), y, z), openings);
                    wolds$addIfOpen(world, new BlockPos(room.getMaxX(), y, z), openings);
                }
            }

            if (!openings.isEmpty() && openings.size() <= 80) {
                wolds$moveIntoRoom(world, room, openings);
                IZonedWorld.runWithBypass(world, true, () -> {
                    for (BlockPos gate : openings) {
                        world.setBlock(gate, Blocks.BARRIER.defaultBlockState(), 3);
                    }
                    for (BlockPos gate : openings) {
                        world.setBlock(gate, Block.updateFromNeighbourShapes(world.getBlockState(gate), world, gate), 3);
                    }
                });
                this.wolds$gatePositions.addAll(openings);
            }
        }
    }

    @Unique
    private void wolds$moveIntoRoom(ServerLevel world, BlockCuboid room, Set<BlockPos> openings) {
        double centerX = (room.getMinX() + room.getMaxX() + 1) / 2.0;
        double centerZ = (room.getMinZ() + room.getMaxZ() + 1) / 2.0;
        for (BlockPos gate : openings) {
            for (ServerPlayer player : world.getEntitiesOfClass(ServerPlayer.class, new AABB(gate))) {
                double dx = centerX - player.getX();
                double dz = centerZ - player.getZ();
                double length = Math.sqrt(dx * dx + dz * dz);
                if (length >= 0.001) {
                    player.setPos(player.getX() + dx / length * 1.5, player.getY(), player.getZ() + dz / length * 1.5);
                }
            }
        }
    }

    @Unique
    private void wolds$addIfOpen(ServerLevel world, BlockPos pos, Set<BlockPos> openings) {
        if (world.getBlockState(pos).isAir()) {
            openings.add(pos);
        }
    }

    @Unique
    private void wolds$openGates(ServerLevel world) {
        if (!this.wolds$gatePositions.isEmpty()) {
            IZonedWorld.runWithBypass(world, true, () -> {
                for (BlockPos gate : this.wolds$gatePositions) {
                    if (world.getBlockState(gate).is(Blocks.BARRIER)) {
                        world.setBlock(gate, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            });
            this.wolds$gatePositions.clear();
        }
    }
}