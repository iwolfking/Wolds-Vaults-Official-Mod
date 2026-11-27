package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods.idona;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.util.calc.ManaCostHelper;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.gods.trees.idona.IdonaPowerDump;

@Mixin(value = ManaCostHelper.class, remap = false)
public class MixinManaCostHelperIdona {
    /**
     * @author PoorMansPhysicist
     * @reason Power Dump charges every instant ability the whole mana bar; only stages the surplus
     */
    @ModifyReturnValue(method = "adjustManaCost(Lnet/minecraft/server/level/ServerPlayer;Liskallia/vault/skill/base/Skill;F)F",
            at = @At("RETURN"))
    private static float applyPowerDump(float cost, ServerPlayer player, Skill skill, float baseCost) {
        return IdonaPowerDump.adjustCost(player, skill, cost);
    }

    /**
     * @author PoorMansPhysicist
     * @reason banks the staged surplus only when mana is actually paid
     */
    @ModifyReturnValue(method = "adjustManaCostForPayment", at = @At("RETURN"))
    private static float commitPowerDump(float paid, ServerPlayer player, Skill skill, float baseCost) {
        return IdonaPowerDump.commit(player, skill, paid);
    }
}
