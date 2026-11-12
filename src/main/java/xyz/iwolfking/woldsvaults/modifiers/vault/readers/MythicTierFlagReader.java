package xyz.iwolfking.woldsvaults.modifiers.vault.readers;

import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.reader.FlagModifierReader;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * A flag reader that names the stronger mythic version of its effect with a " II" suffix, so mythic scepters show
 * "Beam II" and "Sparkles II".
 */
public class MythicTierFlagReader extends FlagModifierReader {
    public MythicTierFlagReader(String modifierName, int rgbColor) {
        super(modifierName, rgbColor);
    }

    @Override
    @Nullable
    public MutableComponent getDisplay(VaultGearAttributeInstance<Boolean> instance, VaultGearData data, VaultGearModifier.AffixType type, ItemStack stack, boolean displayDetail) {
        if (data != null && data.getRarity() == VaultGearRarity.valueOf("MYTHIC")) {
            return new TextComponent(type.getAffixPrefix(true) + this.getModifierName() + " II").withStyle(this.getColoredTextStyle());
        }
        return super.getDisplay(instance, data, type, stack, displayDetail);
    }
}
