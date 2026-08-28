package xyz.iwolfking.woldsvaults.mixins.lightmanscurrency;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.entity.merchant.villager.listings.EnchantedItemForCoinsTrade;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "lightmanscurrency")
        }
)
@Mixin(value = EnchantedItemForCoinsTrade.class, remap = false)
public class MixinEnchantedItemForCoinsTrade {
    @WrapOperation(method = "getOffer", at = @At(value = "INVOKE", target = "Lio/github/lightman314/lightmanscurrency/LightmansCurrency;LogInfo(Ljava/lang/String;)V", remap = false), remap = true)
    private void logDebug(String message, Operation<Void> original){
        LightmansCurrency.LogDebug(message);
    }
}
