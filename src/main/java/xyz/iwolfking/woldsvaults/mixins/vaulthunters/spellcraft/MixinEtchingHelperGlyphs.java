package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft;

import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.etching.EtchingHelper;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.spellcraft.gear.EtchingGlyphs;

import java.util.List;

@Mixin(value = EtchingHelper.class, remap = false)
public abstract class MixinEtchingHelperGlyphs {

    /** Converted etchings read as the active etching glyph (or nothing); removed etchings read as absent. */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Inject(method = "getEtchings", at = @At("RETURN"), cancellable = true)
    private static <T> void woldsvaults$grantedEtchings(LivingEntity entity, VaultGearAttribute<T> attribute,
                                                       CallbackInfoReturnable<List<VaultGearAttributeInstance<T>>> cir) {
        List replaced = EtchingGlyphs.replace(entity, attribute);
        if (replaced != null) {
            cir.setReturnValue(replaced);
        }
    }
}
