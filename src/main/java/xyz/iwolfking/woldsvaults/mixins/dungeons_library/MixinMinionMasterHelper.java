package xyz.iwolfking.woldsvaults.mixins.dungeons_library;

import com.infamous.dungeons_libraries.capabilities.minionmaster.MinionMasterHelper;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "dungeons_libraries")
        }
)
@Mixin(value = MinionMasterHelper.class, remap = false)
public class MixinMinionMasterHelper {
    @Inject(method = "addMinionGoals", at = @At("HEAD"), cancellable = true)
    private static void fixMinionCrash(Mob mobEntity, CallbackInfo ci) {
        ci.cancel();
    }
}
