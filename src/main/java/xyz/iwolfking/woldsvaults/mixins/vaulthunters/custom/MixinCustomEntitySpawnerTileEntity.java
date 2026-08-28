package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.block.entity.CustomEntitySpawnerTileEntity;
import iskallia.vault.core.world.storage.IZonedWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.CustomEntitySpawnerAccessor;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Stops an orphaned custom entity spawner from filling the log.
 *
 * <p>A spawner whose {@code spawnerGroupName} matches nothing in {@code custom_entity_spawner.json}
 * never resolves a group. The base mod warns and then removes the block, but a vault world refuses
 * unbypassed block edits, so the removal silently fails and the spawner warns again every tick -
 * close to two thousand lines in one session, none of which name the group that is missing. The
 * warning is now logged once per spawner, with the group name and position, and the removal is
 * retried with the zone bypass so the spawner actually stops ticking.
 */
@Mixin(value = CustomEntitySpawnerTileEntity.class, remap = false)
public abstract class MixinCustomEntitySpawnerTileEntity {
    @Unique
    private static final Set<Object> WOLDSVAULTS$WARNED = Collections.synchronizedSet(
            Collections.newSetFromMap(new WeakHashMap<>()));

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;)V"), remap = false)
    private static void woldsvaults$warnOnceAndClear(Logger logger, String message, Operation<Void> original,
                                                     Level level, BlockPos blockPos, CustomEntitySpawnerTileEntity te) {
        if (WOLDSVAULTS$WARNED.add(te)) {
            WoldsVaults.LOGGER.warn(
                    "Custom Entity Spawner at {} wants spawn group '{}', which is not in custom_entity_spawner.json; "
                            + "it can never spawn, so it is being removed and this warning is silenced for it.",
                    blockPos, ((CustomEntitySpawnerAccessor) te).getSpawnerGroupName());
        }
        IZonedWorld.runWithBypass(level, true, () -> level.removeBlock(blockPos, false));
    }
}
