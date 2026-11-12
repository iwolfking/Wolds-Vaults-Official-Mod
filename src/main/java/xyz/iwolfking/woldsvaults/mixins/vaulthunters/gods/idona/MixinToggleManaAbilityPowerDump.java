package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods.idona;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.mana.ManaAction;
import iskallia.vault.mana.ManaPlayer;
import iskallia.vault.skill.ability.effect.RampageBerserkerAbility;
import iskallia.vault.skill.ability.effect.spi.core.ToggleManaAbility;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.gods.trees.idona.IdonaPowerDump;

/** Keeps toggles running through the tick after a sneaking Power Dump empties the mana bar. */
@Mixin(value = {ToggleManaAbility.class, RampageBerserkerAbility.class}, remap = false)
public class MixinToggleManaAbilityPowerDump {

    @WrapOperation(method = "lambda$doActiveTick$1", at = @At(value = "INVOKE",
            target = "Liskallia/vault/mana/ManaPlayer;decreaseMana(Liskallia/vault/mana/ManaAction;F)F"))
    private float woldsvaults$powerDumpToggleGrace(ManaPlayer mana, ManaAction action, float amount, Operation<Float> original) {
        float remaining = original.call(mana, action, amount);
        if (remaining <= 0.0F && mana instanceof ServerPlayer player && IdonaPowerDump.inToggleGrace(player)) {
            return Float.MIN_VALUE;
        }
        return remaining;
    }
}
