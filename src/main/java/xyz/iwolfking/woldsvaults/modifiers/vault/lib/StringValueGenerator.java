package xyz.iwolfking.woldsvaults.modifiers.vault.lib;

import com.google.gson.annotations.Expose;
import iskallia.vault.gear.attribute.config.BooleanFlagGenerator;
import iskallia.vault.gear.attribute.config.ConstantObjectGenerator;
import iskallia.vault.gear.reader.VaultGearModifierReader;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class StringValueGenerator extends ConstantObjectGenerator<String, StringValueGenerator.StringValue> {
    public StringValueGenerator() {
        super(StringValueGenerator.StringValue.class);
    }

    @Nullable
    public MutableComponent getConfigDisplay(VaultGearModifierReader<String> reader, StringValueGenerator.StringValue object) {
        return (new TextComponent(reader.getModifierName()).append(": ").append(object.get())).withStyle(reader.getColoredTextStyle());
    }

    public Optional<Float> getRollPercentage(String value, List<StringValue> configurations) {
        return !value.isEmpty() ? Optional.of(1.0F) : Optional.of(0.0F);
    }

    public static class StringValue implements Supplier<String> {
        @Expose
        private String value;

        public StringValue(String value) {
            this.value = value;
        }

        public String get() {
            return this.value;
        }
    }
}
