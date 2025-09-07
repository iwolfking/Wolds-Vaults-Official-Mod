package xyz.iwolfking.woldsvaults.mixins.infernalmobs;
import atomicstryker.infernalmobs.client.InfernalMobsClient;
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
@Mixin(value = InfernalMobsClient.class, remap = false)
public class MixinInfernalMobsClient {
    @Redirect(method = "onEntityJoinedWorld", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;debug(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"))
    private static void removeDebugLogSpam2(Logger logger, String message, Object p1, Object p2) {
        logger.trace(message, p1, p2);
    }
}
