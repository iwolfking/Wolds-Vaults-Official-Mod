package xyz.iwolfking.woldsvaults.util;

import com.google.gson.JsonArray;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.attribute.custom.effect.EffectGearAttribute;
import iskallia.vault.gear.reader.VaultGearModifierReader;

import javax.annotation.Nullable;

import iskallia.vault.gear.tooltip.ModifierCategoryTooltip;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import xyz.iwolfking.woldsvaults.effect.mobeffects.SaferSpacePotionEffect;
import xyz.iwolfking.woldsvaults.events.WoldActiveFlags;

public class UniqueEffectGearAttribute {


    public static UniqueEffectGearAttribute.Reader reader() {
        return new UniqueEffectGearAttribute.Reader();
    }

    private static class Reader extends VaultGearModifierReader<EffectGearAttribute> {
        private Reader() {
            super("", 16777215);
        }

        @Nullable
        @Override
        public MutableComponent getDisplay(VaultGearAttributeInstance<EffectGearAttribute> instance, VaultGearModifier.AffixType type) {
            EffectGearAttribute effect = instance.getValue();
            return this.getText(effect);
        }

        @Nullable
        public MutableComponent getValueDisplay(EffectGearAttribute value) {
            return new TextComponent(String.valueOf(value.getAmplifier()));
        }

        @Nullable
        public MutableComponent getText(EffectGearAttribute value) {
            if(value.getEffect() instanceof SaferSpacePotionEffect) {
                return WoldActiveFlags.IS_USING_SAFER_SPACE.isSet()
                        ? WoldTexFX.corruptedEffect(
                        new TextComponent("Recharging Barrier"))
                        .withStyle(Style.EMPTY.withColor(14889348).withBold(true))

                        : new TextComponent("Improved Block").withStyle(Style.EMPTY.withColor(16109454));
            }

            return new TextComponent(value.toString()).append(" not formatted");
        }

        @Override
        protected void serializeTextElements(JsonArray out, VaultGearAttributeInstance<EffectGearAttribute> instance, VaultGearModifier.AffixType type) {
            EffectGearAttribute effect = instance.getValue();
            MutableComponent valueDisplay = this.getValueDisplay(effect);
            if (valueDisplay != null) {
                out.add(type.getAffixPrefix(true));
                out.add(valueDisplay.getString());
                out.add(" ");
                out.add(effect.getEffect().getDescriptionId());
            }
        }
    }
}
