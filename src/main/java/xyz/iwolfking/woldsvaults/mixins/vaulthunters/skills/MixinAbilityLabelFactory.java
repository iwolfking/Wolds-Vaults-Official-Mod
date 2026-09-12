package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills;

import iskallia.vault.skill.ability.component.AbilityLabelFactory;
import iskallia.vault.skill.base.Skill;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = AbilityLabelFactory.class,remap = false)
public abstract class MixinAbilityLabelFactory {

    @Shadow @Final private static Map<String, AbilityLabelFactory.IAbilityComponentFactory> FACTORY_MAP;

    @Shadow
    private static MutableComponent label(String label, String value, String colorKey) {
        return null;
    }

    @Shadow
    private static <C extends Skill> String binding(C config, String key) {
        return null;
    }

    @Inject(method = "<clinit>",at=@At("TAIL"))
    private static void addLabels(CallbackInfo ci) {
        FACTORY_MAP.put(
                "additionalResistance",
                context -> label("\n Resistance: ",binding(context.config(), "additionalResistance"),"resistance")
        );
        FACTORY_MAP.put(
                "size",
                context -> label("\n Size: ",binding(context.config(), "size"),"amplifier")
        );
        FACTORY_MAP.put(
                "levitateSpeed",
                context -> label("\n Float Speed: ",binding(context.config(), "levitateSpeed"),"amplifier")
        );
        FACTORY_MAP.put(
                "durationMultiplier",
                context -> label("\n Duration Multiplier: ",binding(context.config(), "durationMultiplier"),"force")
        );
        FACTORY_MAP.put(
                "radiusMultiplier",
                context -> label("\n Radius Multiplier: ",binding(context.config(), "radiusMultiplier"),"areaOfEffect")
        );
        FACTORY_MAP.put(
                "amplitudeScaleChance",
                context -> label("\n Amplitude Scale Chance: ",binding(context.config(), "amplitudeScaleChance"),"resistance")
        );
        FACTORY_MAP.put(
                "baseAmplitude",
                context -> label("\n Base Amplitude: ",binding(context.config(), "baseAmplitude"),"force")
        );
        FACTORY_MAP.put(
                "damageMultiplier",
                context -> label("\n Attack Damage Percentage: ",binding(context.config(), "damageMultiplier"),"damageReduction")
        );
        FACTORY_MAP.put(
                "baseDamage",
                context -> label("\n Base Damage: ",binding(context.config(), "baseDamage"),"damage")
        );
        FACTORY_MAP.put(
                "heartFragmentChance",
                context -> label("\n Heart Fragment Chance: ",binding(context.config(), "heartFragmentChance"),"force")
        );
        FACTORY_MAP.put(
                "effectAmplifier",
                context -> label("\n Effect Amplifier: ",binding(context.config(), "effectAmplifier"),"radius")
        );
        FACTORY_MAP.put(
                "executeThreshold",
                context -> label("\n Execute Threshhold: ",binding(context.config(), "executeThreshold"),"levelLo")
        );
        FACTORY_MAP.put(
                "percentageDamageAbsorbed",
                context -> label("\n Percentage Damage Absorbed: ",binding(context.config(), "percentageDamageAbsorbed"),"absorb")
        );
        FACTORY_MAP.put(
                "manaPerDamageScalar",
                context -> label("\n Mana / Damage: ",binding(context.config(), "manaPerDamageScalar"),"manaPerDamage")
        );
        FACTORY_MAP.put(
                "baseManaDrainPerTick",
                context -> label("\n Base Mana / Tick: ",binding(context.config(), "baseManaDrainPerTick"),"manaCost")
        );
        FACTORY_MAP.put(
                "manaDrainRampPerSecond",
                context -> label("\n Mana Ramp Per Second: ",binding(context.config(), "manaDrainRampPerSecond"),"manaCost")
        );
    }
}
