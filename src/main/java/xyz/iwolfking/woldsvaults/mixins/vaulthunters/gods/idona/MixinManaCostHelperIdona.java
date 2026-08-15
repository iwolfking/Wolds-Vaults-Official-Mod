package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods.idona;

import iskallia.vault.skill.base.Skill;
import iskallia.vault.util.calc.ManaCostHelper;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.gods.trees.idona.IdonaPowerDump;

@Mixin(value = ManaCostHelper.class, remap = false)
public class MixinManaCostHelperIdona {
    /**
     * @author PoorMansPhysicist
     * @reason Power Dump charges every instant ability the whole mana bar. This is the single
     * choke point every ability's cost passes through, and there is no event carrying an ability's
     * mana cost, so the override has to happen here.
     */
    @Inject(method = "adjustManaCost(Lnet/minecraft/server/level/ServerPlayer;Liskallia/vault/skill/base/Skill;F)F",
            at = @At("RETURN"), cancellable = true)
    private static void applyPowerDump(ServerPlayer player, Skill skill, float cost, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(IdonaPowerDump.adjustCost(player, skill, cir.getReturnValue()));
    }
}
