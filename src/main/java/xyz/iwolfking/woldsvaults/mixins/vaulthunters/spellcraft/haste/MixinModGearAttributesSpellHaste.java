package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.haste;

import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.init.ModGearAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.VaultGearModifierReaderAccessor;

@Mixin(value = ModGearAttributes.class, remap = false)
public class MixinModGearAttributesSpellHaste {

    /**
     * Renames the cooldown reduction attributes, which carry spell haste, on the readers the base mod built for them.
     * Gear tooltips, roll ranges and the statistics tab all read these names.
     */
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void woldsvaults$nameSpellHaste(CallbackInfo ci) {
        woldsvaults$rename(ModGearAttributes.COOLDOWN_REDUCTION, "Spell Haste");
        woldsvaults$rename(ModGearAttributes.COOLDOWN_REDUCTION_CAP, "Spell Haste Cap");
        woldsvaults$rename(ModGearAttributes.COOLDOWN_REDUCTION_PERCENTILE, "Spell Haste Percentile");
    }

    private static void woldsvaults$rename(VaultGearAttribute<?> attribute, String name) {
        ((VaultGearModifierReaderAccessor) attribute.getReader()).woldsvaults$setModifierName(name);
    }
}
