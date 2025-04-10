package xyz.iwolfking.woldsvaults.modifiers.vault.readers;

import com.google.gson.JsonArray;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.reader.VaultGearModifierReader;
import iskallia.vault.util.StringUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class ThemePoolModifierReader extends VaultGearModifierReader<String> {
    private final String format;

    public ThemePoolModifierReader(String modifierName, int rgbColor, String format) {
        super(modifierName, rgbColor);
        this.format = format;
    }

    public @Nullable MutableComponent getDisplay(VaultGearAttributeInstance<String> instance, VaultGearModifier.AffixType type) {
        return this.getValueDisplay((String)instance.getValue());
    }

    @Nonnull
    public MutableComponent getValueDisplay(String value) {
        return new TextComponent("Theme: ").append(new TextComponent(StringUtils.convertToTitleCase(new ResourceLocation(value).getPath())).withStyle(Style.EMPTY.withColor(this.getRgbColor())));

    }

    protected void serializeTextElements(JsonArray out, VaultGearAttributeInstance<String> instance, VaultGearModifier.AffixType type) {
        out.add(this.format.formatted(this.getModifierName(), instance.getValue()));
    }
}
