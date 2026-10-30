package xyz.iwolfking.woldsvaults.mixins.lightmanscurrency;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import io.github.lightman314.lightmanscurrency.proxy.ClientProxy;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "lightmanscurrency")
        }
)
@Mixin(value = ClientProxy.class, remap = false)
public class MixinClientProxy {
    // why tf would I want to load all items and deduplicate them when I join??
    // that's stupidly expensive for such a rarely used feature
    // players buying items in lc traders can wait for a minute in the gui instead
    @WrapWithCondition(method = "onPlayerLogin", at = @At(value = "INVOKE", target = "Lio/github/lightman314/lightmanscurrency/client/gui/widget/ItemEditWidget;ConfirmItemListLoaded()V"))
    private boolean dontIterateOverAllItemsOnPlayerLogin(){
        return false;
    }
}
