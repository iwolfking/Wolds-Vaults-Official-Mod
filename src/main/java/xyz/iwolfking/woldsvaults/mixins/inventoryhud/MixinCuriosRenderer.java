package xyz.iwolfking.woldsvaults.mixins.inventoryhud;

import dlovin.inventoryhud.gui.renderers.CuriosRenderer;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import top.theillusivec4.curios.api.type.util.ISlotHelper;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "inventoryhud")
    }
)
@Mixin(value = CuriosRenderer.class, remap = false)
public class MixinCuriosRenderer {

    @Redirect(method = "setupTrinkets", at = @At(value = "INVOKE", target = "Ltop/theillusivec4/curios/api/CuriosApi;getSlotHelper()Ltop/theillusivec4/curios/api/type/util/ISlotHelper;", ordinal = 0))
    private ISlotHelper getSlotHelper() {
        // prevent access of incomplete server only data in SP, the tryToSetupTrinketsMP method works just fine in SP
        return null;
    }
}
