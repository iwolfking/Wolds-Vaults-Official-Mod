package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft;

import iskallia.vault.gear.attribute.ability.special.base.ConfiguredModification;
import iskallia.vault.gear.attribute.ability.special.base.SpecialAbilityModification;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.spellcraft.gear.GearGlyphs;

import java.util.List;

@Mixin(value = SpecialAbilityModification.class, remap = false)
public abstract class MixinSpecialAbilityModificationGlyphs {

    /**
     * Converted special modifications (Frost Nova vulnerability, entropic poison, hypothermia, fireball recast, imploding
     * javelin damage) come from the step's granted glyphs instead of the attribute snapshot.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Inject(method = "getModifications", at = @At("RETURN"), cancellable = true)
    private static void woldsvaults$grantedModifications(LivingEntity entity, Class modClass, CallbackInfoReturnable<List> cir) {
        cir.setReturnValue(GearGlyphs.filterSpecial(entity, modClass, (List<ConfiguredModification>) cir.getReturnValue()));
    }
}
