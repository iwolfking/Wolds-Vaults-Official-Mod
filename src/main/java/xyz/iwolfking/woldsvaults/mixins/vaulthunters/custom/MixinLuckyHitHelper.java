package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.util.calc.LuckyHitHelper;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.api.util.LuckHelper;

@Mixin(value = LuckyHitHelper.class, remap = false)
public class MixinLuckyHitHelper {
    /** Luck feeds lucky hit chance, chaining with the god tree's own modifier on this getter. */
    @ModifyReturnValue(method = "getLuckyHitChanceUnlimited", at = @At("RETURN"))
    private static float luckAffectsChance(float chance, LivingEntity entity) {
        return LuckHelper.getLuckAffectedChance(chance, entity);
    }
}
