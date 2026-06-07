package xyz.iwolfking.woldsvaults.mixins.dungeons_library;

import com.infamous.dungeons_libraries.capabilities.minionmaster.MinionMasterHelper;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MinionMasterHelper.class, remap = false)
public class MixinMinionMasterHelper {
    @Inject(method = "addMinionGoals", at = @At("HEAD"), cancellable = true)
    private static void fixMinionCrash(Mob mobEntity, CallbackInfo ci) {
        ci.cancel();
    }
}
