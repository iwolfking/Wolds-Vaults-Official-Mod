package xyz.iwolfking.woldsvaults.modifiers.vault.readers;

import com.google.gson.JsonArray;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.reader.VaultGearModifierReader;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.Nullable;

public class InscriptionModifierReader extends VaultGearModifierReader<Float> {

    private final String format;

    public InscriptionModifierReader(String modifierName, int rgbColor, String format) {
        super(modifierName, rgbColor);
        this.format = format;
    }

    @Nullable
    @Override
    public MutableComponent getDisplay(VaultGearAttributeInstance<Float> instance, VaultGearModifier.AffixType affixType) {
        return getValueDisplay(instance.getValue()).append(" " + this.getModifierName()).withStyle(Style.EMPTY.withColor(this.getRgbColor()));
    }

    @Nullable
    @Override
    public MutableComponent getValueDisplay(Float value) {
        MutableComponent display = new TextComponent("+");
        display.append(String.valueOf(value.intValue()));
        return display;
    }

    @Override
    protected void serializeTextElements(JsonArray out, VaultGearAttributeInstance<Float> instance, VaultGearModifier.AffixType affixType) {
        out.add(this.format.formatted(this.getModifierName(), instance.getValue()));
    }
}
