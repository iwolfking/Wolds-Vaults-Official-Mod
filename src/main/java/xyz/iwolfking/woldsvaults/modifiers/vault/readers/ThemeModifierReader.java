package xyz.iwolfking.woldsvaults.modifiers.vault.readers;

import com.google.gson.JsonArray;
import iskallia.vault.core.vault.VaultRegistry;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.reader.VaultGearModifierReader;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class ThemeModifierReader extends VaultGearModifierReader<String> {
    private final String format;

    public ThemeModifierReader(String modifierName, int rgbColor, String format) {
        super(modifierName, rgbColor);
        this.format = format;
    }

    public @Nullable MutableComponent getDisplay(VaultGearAttributeInstance<String> instance, VaultGearModifier.AffixType type) {
        return this.getValueDisplay((String)instance.getValue());
    }

    @Nonnull
    public MutableComponent getValueDisplay(String value) {
        if(VaultRegistry.THEME.getKey(value) != null) {
            return new TextComponent("Theme: ").append(new TextComponent(VaultRegistry.THEME.getKey(value).getName()).withStyle(Style.EMPTY.withColor(VaultRegistry.THEME.getKey(value).getColor())));
        }
        else {
            return new TextComponent("Theme: INVALID").withStyle(ChatFormatting.RED);
        }

    }

    protected void serializeTextElements(JsonArray out, VaultGearAttributeInstance<String> instance, VaultGearModifier.AffixType type) {
        out.add(this.format.formatted(this.getModifierName(), instance.getValue()));
    }
}
