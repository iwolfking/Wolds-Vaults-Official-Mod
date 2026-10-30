package xyz.iwolfking.woldsvaults.mixins.dungeons_library;

import com.infamous.dungeons_libraries.capabilities.minionmaster.Master;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "dungeons_libraries")
        }
)
@Mixin(value = Master.class, remap = false)
public abstract class MixinMaster {

    @Inject(
        method = "initEntities",
        at = @At("HEAD"),
        cancellable = true
    )
    private void handleNullOrUnloadedWorld(Set<Entity> entities, List<UUID> entityUUIDs, CallbackInfoReturnable<Set<Entity>> cir) {
        if (entities == null && (entityUUIDs == null || entityUUIDs.isEmpty())) {
            cir.setReturnValue(new HashSet<>());
        }
    }

    @Inject(
        method = "initEntities",
        at = @At(
            value = "NEW",
            target = "java/util/HashSet",
            ordinal = 1
        ),
        cancellable = true
    )
    private void preventNullConstructorArgument(Set<Entity> entities, List<UUID> entityUUIDs, CallbackInfoReturnable<Set<Entity>> cir) {
        if (entities == null) {
            cir.setReturnValue(new HashSet<>());
        }
    }
}