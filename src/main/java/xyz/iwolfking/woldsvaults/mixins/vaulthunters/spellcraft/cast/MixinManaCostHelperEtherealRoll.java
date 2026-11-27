package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.cast;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.util.calc.ManaCostHelper;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.spellcraft.cast.SlotManaBilling;

@Mixin(value = ManaCostHelper.class, remap = false)
public abstract class MixinManaCostHelperEtherealRoll {

    /** Tells slot billing whether the payment's Ethereal roll fired, so a free slot trigger frees the whole slot. */
    @WrapOperation(method = "adjustManaCostForPayment", at = @At(value = "INVOKE",
            target = "Liskallia/vault/util/calc/ManaCostHelper;rollsEthereal(Lnet/minecraft/server/level/ServerPlayer;)Z"))
    private static boolean woldsvaults$noteEtherealRoll(ServerPlayer player, Operation<Boolean> original) {
        boolean free = original.call(player);
        SlotManaBilling.noteEtherealRoll(free);
        return free;
    }
}
