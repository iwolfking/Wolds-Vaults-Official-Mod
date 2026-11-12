package xyz.iwolfking.woldsvaults.modifiers.gear.special;

import com.google.gson.JsonArray;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.attribute.ability.special.base.ConfiguredModification;
import iskallia.vault.gear.attribute.ability.special.base.SpecialAbilityGearAttribute;
import iskallia.vault.gear.attribute.ability.special.base.SpecialAbilityModification;
import iskallia.vault.gear.attribute.ability.special.base.template.FloatRangeModification;
import iskallia.vault.gear.attribute.ability.special.base.template.config.FloatRangeConfig;
import iskallia.vault.gear.attribute.ability.special.base.template.value.FloatValue;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import javax.annotation.Nullable;

public class ImplodingJavelinModification extends FloatRangeModification {
    public static final ResourceLocation ID = WoldsVaults.id("imploding_javelin_damage");

    public ImplodingJavelinModification() {
        super(ID);
    }

    /** The summed bonus damage fraction for Implodes cast by {@code entity}'s imploding javelins (0 without the roll). */
    public static float getTotalBonus(LivingEntity entity) {
        float total = 0.0F;
        for (ConfiguredModification<ImplodingJavelinModification, FloatRangeConfig, FloatValue> mod : SpecialAbilityModification.getModifications(entity, ImplodingJavelinModification.class)) {
            total += mod.value().getValue();
        }
        return total;
    }

    @Nullable
    public MutableComponent getDisplay(SpecialAbilityGearAttribute<?, FloatValue> instance, Style style, VaultGearModifier.AffixType type) {
        MutableComponent valueDisplay = this.getValueDisplay(instance.getValue());
        return valueDisplay == null ? null : (new TextComponent("")).append(type.getAffixPrefixComponent(true)).append("Imploding Javelin Implodes deal ").append(getPercentageValue(instance).withStyle(instance.getHighlightStyle())).setStyle(instance.getTextStyle()).append("% more damage");
    }

    public void serializeTextElements(JsonArray out, SpecialAbilityGearAttribute<?, FloatValue> instance, VaultGearModifier.AffixType type) {
        MutableComponent valueDisplay = this.getValueDisplay(instance.getValue());
        if (valueDisplay != null) {
            out.add(type.getAffixPrefix(true));
            out.add("Imploding Javelin Implodes deal ");
            out.add(getPercentageValue(instance).getString());
            out.add("% more damage");
        }
    }

    private static MutableComponent getPercentageValue(SpecialAbilityGearAttribute<?, FloatValue> instance) {
        return new TextComponent(String.format("%.0f", instance.getValue().getValue() * 100));
    }
}
