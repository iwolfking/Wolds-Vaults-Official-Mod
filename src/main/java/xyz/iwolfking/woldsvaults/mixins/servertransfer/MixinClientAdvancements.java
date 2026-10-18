package xyz.iwolfking.woldsvaults.mixins.servertransfer;

import com.google.common.collect.ImmutableMap;
import de.melanx.jea.AdvancementInfo;
import de.melanx.jea.client.ClientAdvancements;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;

import java.util.Set;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "jea")
    }
)
@Mixin(value = ClientAdvancements.class, remap = false)
public class MixinClientAdvancements {
    @Shadow private static ImmutableMap<ResourceLocation, AdvancementInfo> advancements;

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private static void cancelIfServerRefresh(Set<AdvancementInfo> info, CallbackInfo ci){
        if (WoldsVaultsConfig.CLIENT.serverTransferReloadSkip.get()) {
            if (advancements.size() == info.size()) {
                WoldLogger.debug("JustEnoughAdvancements advancement refresh disabled, skipping update");
                ci.cancel();
            }
        }
    }
}
