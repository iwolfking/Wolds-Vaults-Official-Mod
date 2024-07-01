package xyz.iwolfking.woldsvaults.mixins.dimensional_worldborder;

import net.minecraftforge.event.world.WorldEvent;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.me.joeclack.dimensionalworldborder.DimensionalWorldBorder;

@Mixin(value = DimensionalWorldBorder.class,remap = false)
public class MixinDimensionalWorldborder {
    @Shadow @Final private static Logger LOGGER;

    @Inject(method = "onWorldSave", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;)V"), cancellable = true)
    public void onWorldSave(WorldEvent.Save event, CallbackInfo ci) {
        ci.cancel();
    }
}