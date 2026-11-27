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
                "ultimateCooldown",
                context -> label("\n Cooldown: ",binding(context.config(), "ultimateCooldown"),"cooldown")
        );
        FACTORY_MAP.put(
                "attackDamageMultiplier",
                context -> label("\n Attack Damage: ",binding(context.config(), "attackDamageMultiplier"),"damage")
        );
        FACTORY_MAP.put(
                "abilityPowerMultiplier",
                context -> label("\n Ability Power: ",binding(context.config(), "abilityPowerMultiplier"),"ability_power")
        );
        FACTORY_MAP.put(
                "damageCompounding",
                context -> label("\n Dash Damage / Hit: ",binding(context.config(), "damageCompounding"),"damage")
        );
        FACTORY_MAP.put(
                "dodgeChance",
                context -> label("\n Dodge Chance: ",binding(context.config(), "dodgeChance"),"chance")
        );
        FACTORY_MAP.put(
                "bonusAttackSpeed",
                context -> label("\n Attack Speed: ",binding(context.config(), "bonusAttackSpeed"),"speed")
        );
        FACTORY_MAP.put(
                "bonusMovementSpeed",
                context -> label("\n Movement Speed: ",binding(context.config(), "bonusMovementSpeed"),"speed")
        );
        FACTORY_MAP.put(
                "timerStretch",
                context -> label("\n Vault Second Length: ",binding(context.config(), "timerStretch"),"duration")
        );
        FACTORY_MAP.put(
                "blockSize",
                context -> label("\n Block: ",binding(context.config(), "blockSize"),"healthPoints")
        );
        FACTORY_MAP.put(
                "absorptionShield",
                context -> label("\n Absorption Shield: ",binding(context.config(), "absorptionShield"),"absorb")
        );
        FACTORY_MAP.put(
                "transfusion",
                context -> label("\n Transfusion: ",binding(context.config(), "transfusion"),"damage")
        );
        FACTORY_MAP.put(
                "lifeBond",
                context -> label("\n Life Bond: ",binding(context.config(), "lifeBond"),"thornsDamage")
        );
        FACTORY_MAP.put(
                "manaShield",
                context -> label("\n Mana Shield: ",binding(context.config(), "manaShield"),"manaPerDamage")
        );
        FACTORY_MAP.put(
                "manaRegenShare",
                context -> label("\n Mana Regen: ",binding(context.config(), "manaRegenShare"),"manaCost")
        );
        FACTORY_MAP.put(
                "surgingMana",
                context -> label("\n Surging Mana: ",binding(context.config(), "surgingMana"),"areaOfEffect")
        );
        FACTORY_MAP.put(
                "buffDuration",
                context -> label("\n Buff Duration: ",binding(context.config(), "buffDuration"),"duration")
        );
        FACTORY_MAP.put(
                "fastClear",
                context -> label("\n Full Duration If Lifted In: ",binding(context.config(), "fastClear"),"duration")
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
