package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.gear;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.event.common.PlayerStatEvent;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.util.calc.AreaOfEffectHelper;
import iskallia.vault.util.calc.PlayerStat;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.spellcraft.gear.ConvertedAffixes;
import xyz.iwolfking.woldsvaults.spellcraft.hit.GlyphArea;

import java.util.List;

@Mixin(value = AreaOfEffectHelper.class, remap = false)
public abstract class MixinAreaOfEffectHelperGlyphs {

    /**
     * Converted keyed area affixes (fireball, storm, fear) come from the step's granted glyphs, applied after VH's area
     * clamp, when the ability read is the step's own.
     */
    @SuppressWarnings("rawtypes")
    @WrapOperation(method = "adjustAreaOfEffect(Lnet/minecraft/world/entity/LivingEntity;Liskallia/vault/skill/base/Skill;F)F",
            at = @At(value = "INVOKE", target = "Liskallia/vault/snapshot/AttributeSnapshot;getAttributeValue(Liskallia/vault/gear/attribute/VaultGearAttribute;Liskallia/vault/gear/attribute/type/VaultGearAttributeTypeMerger;)Ljava/lang/Object;"))
    private static Object woldsvaults$grantedArea(AttributeSnapshot snapshot, VaultGearAttribute attribute, VaultGearAttributeTypeMerger merger,
                                                  Operation<Object> original, @Local(argsOnly = true) LivingEntity entity,
                                                  @Local(argsOnly = true) Skill ability) {
        Object value = original.call(snapshot, attribute, merger);
        if (attribute == ModGearAttributes.ABILITY_AREA_OF_EFFECT_PERCENT && value instanceof List list) {
            return ConvertedAffixes.filterArea(entity, ability, list);
        }
        return value;
    }

    /** The area stat event inside this read knows which skill is read, so a step's Area glyph applies only to its own ability. */
    @WrapOperation(method = "adjustAreaOfEffect(Lnet/minecraft/world/entity/LivingEntity;Liskallia/vault/skill/base/Skill;F)F",
            at = @At(value = "INVOKE", target = "Liskallia/vault/core/event/common/PlayerStatEvent;invoke(Liskallia/vault/util/calc/PlayerStat;Lnet/minecraft/world/entity/LivingEntity;F)Liskallia/vault/core/event/common/PlayerStatEvent$Data;"))
    private static PlayerStatEvent.Data woldsvaults$areaStatForSkill(PlayerStatEvent event, PlayerStat stat, LivingEntity entity, float value,
                                                                     Operation<PlayerStatEvent.Data> original, @Local(argsOnly = true) Skill ability) {
        return GlyphArea.readFor(entity, ability, () -> original.call(event, stat, entity, value));
    }
}
