package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes.bettercombat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.skill.talent.type.luckyhit.LuckyHitTalent;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import xyz.iwolfking.woldsvaults.events.WoldActiveFlags;

@Mixin(value = LuckyHitTalent.class, remap = false)
public class MixinLuckyHitTalent {
    /**
     * The lucky hit attack-scale gate. Fang hits and derived set hits are not swings, so they always pass; every other hit passes
     * from half charge, which is what the earlier constant change to the gate did.
     */
    @WrapOperation(method = "doLuckyHit", at = @At(value = "INVOKE", target = "Liskallia/vault/util/damage/AttackScaleHelper;getLastAttackScale(Lnet/minecraft/world/entity/player/Player;)F"))
    private static float woldsvaults$luckyHitAttackScale(Player player, Operation<Float> original) {
        if (WoldActiveFlags.IS_FANG_ATTACKING.isSet() || WoldActiveFlags.IS_DERIVED_ATTACKING.isSet()) {
            return 1.0F;
        }
        float scale = original.call(player);
        return scale >= 0.5F ? 1.0F : scale;
    }

    /**
     * The lucky hit crit gate (a crit swing never lucky-hits). Fang hits and derived set hits are not swings, so the crit state of
     * the player's last swing does not block them.
     */
    @WrapOperation(method = "doLuckyHit", at = @At(value = "INVOKE", target = "Liskallia/vault/util/damage/CritHelper;getCrit(Lnet/minecraft/world/entity/player/Player;)Z"))
    private static boolean woldsvaults$luckyHitCritGate(Player player, Operation<Boolean> original) {
        if (WoldActiveFlags.IS_FANG_ATTACKING.isSet() || WoldActiveFlags.IS_DERIVED_ATTACKING.isSet()) {
            return false;
        }
        return original.call(player);
    }

    /**
     * Lifts the level 8 clamp on the Lucky Vulnerable etching.
     */
    @ModifyConstant(method = "lambda$doLuckyHit$5", constant = @Constant(intValue = 8))
    private static int woldsvaults$uncapLuckyVulnerable(int constant) {
        return Integer.MAX_VALUE;
    }
}
