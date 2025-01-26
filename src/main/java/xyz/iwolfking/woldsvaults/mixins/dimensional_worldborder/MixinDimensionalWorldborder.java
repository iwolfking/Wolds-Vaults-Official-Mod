package xyz.iwolfking.woldsvaults.mixins.dimensional_worldborder;

import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import uk.me.joeclack.dimensionalworldborder.DimensionalWorldBorder;
@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "dimensionalworldborder")
        }
)
@Mixin(value = DimensionalWorldBorder.class,remap = false)
public class MixinDimensionalWorldborder {
    @Shadow @Final private static Logger LOGGER;

    @Redirect(method = "onWorldSave", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;)V"))
    public void onWorldSave(Logger instance, String s) {

    }
}
