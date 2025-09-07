package xyz.iwolfking.woldsvaults.mixins.infernalmobs;

import atomicstryker.infernalmobs.common.network.MobModsPacket;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.woldsvaults.compat.infernalmobs.DevEnvTester;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "infernalmobs"),
    }
)

@Mixin(value = MobModsPacket.class, remap = false)
public class MixinMobModsPacket {
    @Redirect(method = "handle", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;debug(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"))
    private void removeDebugLogSpam2(Logger logger, String message, Object p1, Object p2) {
        logger.trace(message, p1, p2);
    }
    @Redirect(method = "handle", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;debug(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V"))
    private void removeDebugLogSpam3(Logger logger, String message, Object p1, Object p2, Object p3) {
        logger.trace(message, p1, p2, p3);
    }
}
