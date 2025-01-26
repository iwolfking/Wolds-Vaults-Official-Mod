package xyz.iwolfking.woldsvaults.modifiers.vault.readers;

import com.google.gson.JsonArray;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.reader.VaultGearModifierReader;
import iskallia.vault.util.StringUtils;
import iskallia.vault.world.VaultDifficulty;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class DifficultyModifierReader extends VaultGearModifierReader<String> {
    private final String format;

    public DifficultyModifierReader(String modifierName, int rgbColor, String format) {
        super(modifierName, rgbColor);
        this.format = format;
    }

    public @Nullable MutableComponent getDisplay(VaultGearAttributeInstance<String> instance, VaultGearModifier.AffixType type) {
        return this.getValueDisplay((String)instance.getValue());
    }

    @Nonnull
    public MutableComponent getValueDisplay(String value) {
            return new TextComponent("Difficulty: ").append(new TextComponent(StringUtils.convertToTitleCase(VaultDifficulty.valueOf(value).getKey())).withStyle(Style.EMPTY.withColor(this.getRgbColor())));
    }

    protected void serializeTextElements(JsonArray out, VaultGearAttributeInstance<String> instance, VaultGearModifier.AffixType type) {
        out.add(this.format.formatted(this.getModifierName(), instance.getValue()));
    }
}
