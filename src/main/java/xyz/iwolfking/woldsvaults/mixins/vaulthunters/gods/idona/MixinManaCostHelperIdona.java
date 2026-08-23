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
     * @reason Power Dump charges every instant ability the whole mana bar. This is the single
     * choke point every ability's cost passes through, and there is no event carrying an ability's
     * mana cost, so the override has to happen here. It only stages the surplus: the affordability
     * check runs this too, and a cast that is refused after the check must not bank anything.
     */
    @ModifyReturnValue(method = "adjustManaCost(Lnet/minecraft/server/level/ServerPlayer;Liskallia/vault/skill/base/Skill;F)F",
            at = @At("RETURN"))
    private static float applyPowerDump(float cost, ServerPlayer player, Skill skill, float baseCost) {
        return IdonaPowerDump.adjustCost(player, skill, cost);
    }

    /**
     * @author PoorMansPhysicist
     * @reason the surplus staged above is banked only when mana is actually paid. This is the
     * payment path's own adjuster, which runs after the affordability check has passed, and its
     * result is zero for an Ethereal free cast - so a free cast never banks a surplus it did not pay.
     */
    @ModifyReturnValue(method = "adjustManaCostForPayment", at = @At("RETURN"))
    private static float commitPowerDump(float paid, ServerPlayer player, Skill skill, float baseCost) {
        return IdonaPowerDump.commit(player, skill, paid);
    }
}
