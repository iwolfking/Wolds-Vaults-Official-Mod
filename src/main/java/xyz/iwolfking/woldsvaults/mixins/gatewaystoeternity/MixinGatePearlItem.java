package xyz.iwolfking.woldsvaults.mixins.gatewaystoeternity;

import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shadows.gateways.item.GatePearlItem;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "gateways")
    }
)
@Mixin(value = GatePearlItem.class)
public abstract class MixinGatePearlItem extends Item {

    public MixinGatePearlItem(Properties pProperties) {
        super(pProperties);
    }

    /**
     * @author iwolfking
     * @reason Remove being able to use Gate Pearls anywhere.
     */
    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    public void useOn(UseOnContext ctx, CallbackInfoReturnable<InteractionResult> cir)  {
        if(!WoldsVaultsConfig.SERVER.enableNormalGatewayPearls.get()) {
            cir.setReturnValue(super.useOn(ctx));
        }
    }
}
