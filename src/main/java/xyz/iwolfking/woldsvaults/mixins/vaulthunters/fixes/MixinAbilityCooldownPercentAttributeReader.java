package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "iskallia.vault.gear.attribute.ability.AbilityCooldownPercentAttribute$Reader")
public abstract class MixinAbilityCooldownPercentAttributeReader {
    @WrapOperation(method = "getDisplay", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/MutableComponent;append(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;", ordinal = 0))
    private MutableComponent fixDisplay(MutableComponent instance, String pString, Operation<MutableComponent> original, @Local boolean positive) {
        if(positive) {
            return original.call(instance, "Increases the ");
        }

        return original.call(instance, pString);
    }
}
