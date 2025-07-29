package xyz.iwolfking.woldsvaults.mixins.angelrings;

import dev.denismasterherobrine.angelring.compat.curios.AbstractRingCurio;
import iskallia.vault.core.vault.ClientVaults;
import iskallia.vault.world.data.ServerVaults;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.SlotContext;
import xyz.iwolfking.woldsvaults.init.ModGameRules;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "angelring")
        }
)
@Mixin(value = AbstractRingCurio.class, remap = false)
public abstract class MixinAbstractRingCurio {

    @Inject(method = "curioTick", at = @At("HEAD"), cancellable = true)
    public void curioTick(SlotContext slotContext, CallbackInfo ci) {
        if(!slotContext.entity().getLevel().getGameRules().getBoolean(ModGameRules.ALLOW_FLIGHT_IN_VAULTS) && (ClientVaults.getActive().isPresent() || ServerVaults.get(slotContext.entity().getLevel()).isPresent())) {
            ci.cancel();
        }
    }
}
