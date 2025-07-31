package xyz.iwolfking.woldsvaults.mixins.ftbteams;

import com.mojang.blaze3d.platform.InputConstants;
import dev.ftb.mods.ftbteams.client.FTBTeamsClient;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FTBTeamsClient.class, remap = false)
public class MixinFTBTeamsClient {
    @Shadow public static KeyMapping openTeamsKey;

    @Inject(method = "registerKeys", at = @At("HEAD"), cancellable = true)
    private void registerKeys(CallbackInfo ci) {
        if(Minecraft.getInstance() == null) {
            ci.cancel();
        }
    }
}
