package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.gear;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.util.calc.CooldownHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.spellcraft.gear.ConvertedAffixes;

import java.util.List;

@Mixin(value = CooldownHelper.class, remap = false)
public abstract class MixinCooldownHelperGlyphs {

    /**
     * Converted keyed cooldown affixes (warp, storm, group heal) stop acting here; their glyphs are a spell cooldown
     * factor.
     */
    @SuppressWarnings("rawtypes")
    @WrapOperation(method = "adjustCooldown",
            at = @At(value = "INVOKE", target = "Liskallia/vault/snapshot/AttributeSnapshot;getAttributeValue(Liskallia/vault/gear/attribute/VaultGearAttribute;Liskallia/vault/gear/attribute/type/VaultGearAttributeTypeMerger;)Ljava/lang/Object;"))
    private static Object woldsvaults$withoutConvertedCooldowns(AttributeSnapshot snapshot, VaultGearAttribute attribute, VaultGearAttributeTypeMerger merger,
                                                                Operation<Object> original) {
        Object value = original.call(snapshot, attribute, merger);
        if (attribute == ModGearAttributes.ABILITY_COOLDOWN_PERCENT && value instanceof List list) {
            return ConvertedAffixes.filterCooldown(list);
        }
        return value;
    }
}
