package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.haste;

import iskallia.vault.util.calc.CooldownHelper;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.client.spellcraft.state.MirroredStats;
import xyz.iwolfking.woldsvaults.spellcraft.haste.SpellHaste;
import xyz.iwolfking.woldsvaults.spellcraft.play.StatMirror;

@Mixin(value = CooldownHelper.class, remap = false)
public abstract class MixinCooldownHelperSpellHaste {

    /**
     * Replaces the clamped cooldown reduction with the share of a cooldown spell haste removes, so every caller of
     * {@code adjustCooldown} gets {@code base / hasteFactor}. Keeps VH's value when the haste table is missing.
     */
    @Inject(method = "getCooldownMultiplier", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$spellHaste(LivingEntity player, CallbackInfoReturnable<Float> cir) {
        Float factor = SpellHaste.factor(player, 1.0F);
        if (factor != null) {
            cir.setReturnValue(SpellHaste.reduction(factor));
        }
    }

    /** On the client, the local player's value comes from the server's mirror, so server-only bonuses show. */
    @Inject(method = "getCooldownMultiplierUnlimited", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$mirrored(LivingEntity player, CallbackInfoReturnable<Float> cir) {
        Float value = MirroredStats.client(player, StatMirror.HASTE);
        if (value != null) {
            cir.setReturnValue(value);
        }
    }
}
