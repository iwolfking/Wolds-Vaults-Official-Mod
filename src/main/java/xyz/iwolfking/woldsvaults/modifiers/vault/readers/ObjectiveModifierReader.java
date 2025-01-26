package xyz.iwolfking.woldsvaults.modifiers.vault.readers;

import com.google.gson.JsonArray;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.reader.VaultGearModifierReader;
import iskallia.vault.item.crystal.CrystalData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.vhapi.api.data.api.BountyScreenData;
import xyz.iwolfking.vhapi.api.registry.VaultObjectiveRegistry;

import javax.annotation.Nonnull;

public class ObjectiveModifierReader extends VaultGearModifierReader<String> {
    private final String format;

    public ObjectiveModifierReader(String modifierName, int rgbColor, String format) {
        super(modifierName, rgbColor);
        this.format = format;
    }

    public @Nullable MutableComponent getDisplay(VaultGearAttributeInstance<String> instance, VaultGearModifier.AffixType type) {
        return this.getValueDisplay((String)instance.getValue());
    }

    @Nonnull
    public MutableComponent getValueDisplay(String value) {
        if(CrystalData.OBJECTIVE.getValue(value) != null) {
            String name = BountyScreenData.OBJECTIVE_NAME.containsKey(value) ? BountyScreenData.OBJECTIVE_NAME.get(value).getString() : VaultObjectiveRegistry.CUSTOM_BOUNTY_SCREEN_NAMES.get(value).getString();
            return new TextComponent("Objective: ").append(new TextComponent(name).withStyle(Style.EMPTY.withColor(CrystalData.OBJECTIVE.getValue(value).getColor(0F).orElse(0))));
        }
        else {
            return new TextComponent("Objective: INVALID").withStyle(ChatFormatting.RED);
        }

    }

    protected void serializeTextElements(JsonArray out, VaultGearAttributeInstance<String> instance, VaultGearModifier.AffixType type) {
        out.add(this.format.formatted(this.getModifierName(), instance.getValue()));
    }
}
