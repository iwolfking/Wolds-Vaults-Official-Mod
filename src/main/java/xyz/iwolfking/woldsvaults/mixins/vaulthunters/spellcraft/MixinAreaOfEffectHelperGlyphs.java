package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.util.calc.AreaOfEffectHelper;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.spellcraft.gear.GearGlyphs;

import java.util.List;

@Mixin(value = AreaOfEffectHelper.class, remap = false)
public abstract class MixinAreaOfEffectHelperGlyphs {

    /** Converted keyed area affixes (fireball, storm, fear) come from the step's granted glyphs, after VH's area clamp as before. */
    @SuppressWarnings("rawtypes")
    @WrapOperation(method = "adjustAreaOfEffect(Lnet/minecraft/world/entity/LivingEntity;Liskallia/vault/skill/base/Skill;F)F",
            at = @At(value = "INVOKE", target = "Liskallia/vault/snapshot/AttributeSnapshot;getAttributeValue(Liskallia/vault/gear/attribute/VaultGearAttribute;Liskallia/vault/gear/attribute/type/VaultGearAttributeTypeMerger;)Ljava/lang/Object;"))
    private static Object woldsvaults$grantedArea(AttributeSnapshot snapshot, VaultGearAttribute attribute, VaultGearAttributeTypeMerger merger,
                                                  Operation<Object> original, @Local(argsOnly = true) LivingEntity entity) {
        Object value = original.call(snapshot, attribute, merger);
        if (attribute == ModGearAttributes.ABILITY_AREA_OF_EFFECT_PERCENT && value instanceof List list) {
            return GearGlyphs.filterArea(entity, list);
        }
        return value;
    }
}
