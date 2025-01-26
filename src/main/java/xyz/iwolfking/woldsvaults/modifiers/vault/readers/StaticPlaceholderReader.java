package xyz.iwolfking.woldsvaults.modifiers.vault.readers;

import com.google.gson.JsonArray;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.reader.VaultGearModifierReader;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class StaticPlaceholderReader extends VaultGearModifierReader<String> {
    private final String format;

    public StaticPlaceholderReader(String modifierName, int rgbColor, String format) {
        super(modifierName, rgbColor);
        this.format = format;
    }

    public @Nullable MutableComponent getDisplay(VaultGearAttributeInstance<String> instance, VaultGearModifier.AffixType type) {
        return this.getValueDisplay((String)instance.getValue());
    }

    @Nonnull
    public MutableComponent getValueDisplay(String value) {
        if(VaultModifierRegistry.get(new ResourceLocation(value)) != null) {
            return new TextComponent("").append(new TextComponent(VaultModifierRegistry.get(new ResourceLocation(value)).getDisplayDescription()).withStyle(Style.EMPTY.withColor(VaultModifierRegistry.get(new ResourceLocation(value)).getDisplayTextColor())));
        }
        else {
            return new TextComponent("Invalid Vault Modifier").withStyle(ChatFormatting.RED);
        }

    }

    protected void serializeTextElements(JsonArray out, VaultGearAttributeInstance<String> instance, VaultGearModifier.AffixType type) {
        out.add(this.format.formatted(this.getModifierName(), instance.getValue()));
    }


    private String getCleanedName(String name) {
        return "+ " + name.replaceAll(":.*: *", "");
    }
}
