package xyz.iwolfking.woldsvaults.mixins.cast;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import xyz.iwolfking.woldsvaults.api.cast.CastCarrier;
import xyz.iwolfking.woldsvaults.api.cast.CastContext;

/** Runs the tick of an entity a cast created under that cast's context. */
@Mixin(ServerLevel.class)
public class MixinServerLevelCastTag {

    @WrapMethod(method = "tickNonPassenger")
    private void woldsvaults$tickUnderCast(Entity entity, Operation<Void> original) {
        CastContext.runTask(CastContext.tagOf(entity), CastCarrier.ENTITY, () -> {
            original.call(entity);
        });
    }
}
