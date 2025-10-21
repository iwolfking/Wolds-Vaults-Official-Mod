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
    }
}
