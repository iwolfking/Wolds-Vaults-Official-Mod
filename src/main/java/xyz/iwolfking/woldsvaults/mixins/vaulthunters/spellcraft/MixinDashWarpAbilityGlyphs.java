package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.skill.ability.effect.DashWarpAbility;
import iskallia.vault.snapshot.AttributeSnapshot;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.augments.AugmentType;
import xyz.iwolfking.woldsvaults.spellcraft.gear.GearGlyphs;
import xyz.iwolfking.woldsvaults.spellcraft.gear.GlyphGrants;

@Mixin(value = DashWarpAbility.class, remap = false)
public abstract class MixinDashWarpAbilityGlyphs {

    /** Warp arrow speed comes from the step's Warp Velocity glyph instead of the gear's warp projectile speed. */
    @SuppressWarnings("rawtypes")
    @WrapOperation(method = "doAction(Lnet/minecraft/server/level/ServerPlayer;)Liskallia/vault/skill/ability/effect/spi/core/Ability$ActionResult;",
            at = @At(value = "INVOKE", target = "Liskallia/vault/snapshot/AttributeSnapshot;getAttributeValue(Liskallia/vault/gear/attribute/VaultGearAttribute;Liskallia/vault/gear/attribute/type/VaultGearAttributeTypeMerger;)Ljava/lang/Object;"))
    private Object woldsvaults$warpVelocity(AttributeSnapshot snapshot, VaultGearAttribute attribute, VaultGearAttributeTypeMerger merger,
                                            Operation<Object> original, @Local(argsOnly = true) ServerPlayer player) {
        GlyphGrants.Grant grant = GearGlyphs.grant(player, AugmentType.WARP_VELOCITY);
        return grant == null ? 0.0F : grant.potency();
    }
}
