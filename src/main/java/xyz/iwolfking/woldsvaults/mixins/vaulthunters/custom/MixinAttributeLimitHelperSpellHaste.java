package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.util.calc.AttributeLimitHelper;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.config.SpellHasteConfig;
import xyz.iwolfking.woldsvaults.spellcraft.haste.SpellHaste;

@Mixin(value = AttributeLimitHelper.class, remap = false)
public abstract class MixinAttributeLimitHelperSpellHaste {

    /**
     * The cooldown reduction limit becomes the spell haste cap, in haste (4.0 for +400%). Once
     * {@code getCooldownMultiplier} is replaced, only the stats tab and dumps read it.
     */
    @Inject(method = "getCooldownReductionLimit", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$hasteCap(LivingEntity entity, CallbackInfoReturnable<Float> cir) {
        SpellHasteConfig config = SpellHaste.config();
        if (config != null) {
            cir.setReturnValue(SpellHaste.cap(entity, config));
        }
    }
}
