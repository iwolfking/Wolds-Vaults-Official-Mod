package xyz.iwolfking.woldsvaults.mixins.ftbquests;

import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "ftbquests"),
        }
)
@Mixin(value = FTBQuestsClient.class, remap = false)
public class MixinFTBQuestsClient {
    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void cancelWhenClientIsNull(CallbackInfo ci) {
        if(Minecraft.getInstance() == null) {
            ci.cancel();
        }
    }
}
