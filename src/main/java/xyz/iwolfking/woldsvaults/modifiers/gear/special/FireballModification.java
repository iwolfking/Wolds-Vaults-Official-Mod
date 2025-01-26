package xyz.iwolfking.woldsvaults.modifiers.gear.special;

import com.google.gson.JsonArray;
import iskallia.vault.VaultMod;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.attribute.ability.special.base.SpecialAbilityGearAttribute;
import iskallia.vault.gear.attribute.ability.special.base.template.FloatRangeModification;
import iskallia.vault.gear.attribute.ability.special.base.template.value.FloatValue;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class FireballModification extends FloatRangeModification {
    public static final ResourceLocation ID = VaultMod.id("fireball_special_modification");

    public FireballModification() {
        super(ID);
    }

    @Nullable
    public MutableComponent getDisplay(SpecialAbilityGearAttribute<?, FloatValue> instance, Style style, VaultGearModifier.AffixType type) {
        MutableComponent valueDisplay = this.getValueDisplay(instance.getValue());
        return valueDisplay == null ? null : (new TextComponent("")).append(type.getAffixPrefixComponent(true)).append("Fireball has ").append(getPercentageValue(instance).withStyle(instance.getHighlightStyle())).setStyle(instance.getTextStyle()).append("% chance to fire twice");
    }


    public void serializeTextElements(JsonArray out, SpecialAbilityGearAttribute<?, FloatValue> instance, VaultGearModifier.AffixType type) {
        MutableComponent valueDisplay = this.getValueDisplay(instance.getValue());
        if (valueDisplay != null) {
            out.add(type.getAffixPrefix(true));
            out.add("Fireball has ");
            out.add(getPercentageValue(instance).getString());
            out.add("% chance to fire twice");
        }
    }

    private static MutableComponent getPercentageValue(SpecialAbilityGearAttribute<?, FloatValue> instance) {
        return new TextComponent(String.format("%.2f", instance.getValue().getValue() * 100));
    }

}
