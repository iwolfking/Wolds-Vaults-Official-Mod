package xyz.iwolfking.woldsvaults.modifiers.vault.readers;

import com.google.gson.Gson;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.reader.VaultGearModifierReader;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TextColor;
import xyz.iwolfking.woldsvaults.config.WeaponTypesConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WeaponTypeReader extends VaultGearModifierReader<String> {
    private final String format;
    public static final Gson GSON = new Gson();
    public WeaponTypeReader(String modifierName, int rgbColor, String format) {
        super(modifierName, rgbColor);
        this.format = format;
    }

    public @Nullable MutableComponent getDisplay(VaultGearAttributeInstance<String> instance, VaultGearModifier.AffixType type) {
        return this.getValueDisplay(instance.getValue());
    }

    @Nonnull
    public MutableComponent getValueDisplay(String value) {
        TextComponent component = new TextComponent("Weapon Type: " + value);
        WeaponTypesConfig.Entry entry = ModConfigs.WEAPON_TYPES.WEAPON_TYPES_MAP.get(value);

        if (entry != null) {
            component = (TextComponent) component.withStyle(Style.EMPTY.withColor(TextColor.fromRgb(entry.COLOR)));
        }
        return component;
    }

    @Override
    protected void serializeTextElements(com.google.gson.JsonArray jsonArray, VaultGearAttributeInstance<String> vaultGearAttributeInstance, VaultGearModifier.AffixType affixType) {
        jsonArray.add(this.format.formatted(this.getModifierName(), vaultGearAttributeInstance.getValue()));
    }

}
