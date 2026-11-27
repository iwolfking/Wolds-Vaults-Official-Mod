package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods.idona;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.util.calc.LuckyHitHelper;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.gods.trees.idona.IdonaLuckyHit;

@Mixin(value = LuckyHitHelper.class, remap = false)
public class MixinLuckyHitHelperIdona {
    /**
     * @author PoorMansPhysicist
     * @reason Luckiest Hit multiplies lucky hit chance by 0.1
     */
    @ModifyReturnValue(method = "getLuckyHitChanceUnlimited", at = @At("RETURN"))
    private static float applyLuckiestHit(float chance, LivingEntity entity) {
        return IdonaLuckyHit.scaleChance(entity, chance);
    }
}
