package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills;

import iskallia.vault.init.ModAbilityLabelBindings;
import iskallia.vault.skill.ability.component.AbilityLabelFormatters;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.abilities.*;

import java.util.Map;

@Mixin(value = ModAbilityLabelBindings.class,remap = false)
public abstract class MixinModAbilityLabelBindings {


    @Inject(method = "register()V",at = @At("TAIL"))
    private static void registerDescriptions(CallbackInfo ci) {
        ModAbilityLabelBindings.register(ColossusAbility.class, Map.of(
                "additionalResistance",
                ability -> AbilityLabelFormatters.percentRounded(ability.getAdditionalResistance()),
                "size",
                ability -> AbilityLabelFormatters.percentRounded(ability.getSize()),
                "cooldown",
                ability -> AbilityLabelFormatters.ticks(ability.getCooldownTicks()),
                "manaCost",
                ability -> AbilityLabelFormatters.integer((int) ability.getManaCost()),
                "duration",
                ability -> AbilityLabelFormatters.ticks(ability.getDurationTicks())

        ));
        ModAbilityLabelBindings.register(SneakyGetawayAbility.class, Map.of(
                "speed",
                ability -> AbilityLabelFormatters.percentRounded(ability.getSpeedPercentAdded()),
                "size",
                ability -> AbilityLabelFormatters.percentRounded(ability.getSize()),
                "cooldown",
                ability -> AbilityLabelFormatters.ticks(ability.getCooldownTicks()),
                "manaCost",
                ability -> AbilityLabelFormatters.integer((int) ability.getManaCost()),
                "duration",
                ability -> AbilityLabelFormatters.ticks(ability.getDurationTicks())

        ));
        ModAbilityLabelBindings.register(VeinMinerChainAbility.class, Map.of(
                "blocks",
                ability -> AbilityLabelFormatters.integer(ability.getUnmodifiedBlockLimit()),
                "distance",
                ability -> AbilityLabelFormatters.integer(ability.getRange())

        ));
        ModAbilityLabelBindings.register(LevitateAbility.class, Map.of(
                "levitateSpeed",
                ability -> AbilityLabelFormatters.decimal(ability.getLevitateSpeed())

        ));

        ModAbilityLabelBindings.register(ExpungeAbility.class, Map.of(
                "durationMultiplier",
                ability -> AbilityLabelFormatters.percentRounded(ability.getDurationMultiplier()),
                "radiusMultiplier",
                ability -> AbilityLabelFormatters.percentRounded(ability.getRadiusMultiplier()),
                "cooldown",
                ability -> AbilityLabelFormatters.ticks(ability.getCooldownTicks()),
                "manaCost",
                ability -> AbilityLabelFormatters.integer((int) ability.getManaCost())
        ));

        ModAbilityLabelBindings.register(ConcentrateAbility.class, Map.of(
                "radius",
                ability -> AbilityLabelFormatters.decimal(ability.getRadius()),
                "adjustedRadius",
                ability -> AbilityLabelFormatters.decimal(ability.getRadius()),
                "baseEffectDuration",
                ability -> AbilityLabelFormatters.ticks(ability.getBaseEffectDuration()),
                "adjustedDuration",
                ability -> AbilityLabelFormatters.ticks(ability.getBaseEffectDuration()),
                "baseAmplitude",
                ability -> AbilityLabelFormatters.integer(ability.getBaseAmplitude()),
                "amplitudeScaleChance",
                ability -> AbilityLabelFormatters.percentRounded(ability.getAmplitudeScaleChance()),
                "cooldown",
                ability -> AbilityLabelFormatters.ticks(ability.getCooldownTicks()),
                "manaCost",
                ability -> AbilityLabelFormatters.integer((int) ability.getManaCost())
        ));


    }
}
