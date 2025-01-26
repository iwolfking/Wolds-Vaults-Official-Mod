package xyz.iwolfking.woldsvaults.modifiers.vault.lib;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import xyz.iwolfking.woldsvaults.api.lib.MixinModifierContextAccessor;

public class ValueProperty {
    @Expose
    @SerializedName("value")
    private final float value;

    public ValueProperty(float value) {
        this.value = value;
    }

    public int apply(int value, ModifierContext context) {
        return ((MixinModifierContextAccessor)context).woldsVaults_Dev$getValue().map((x) -> {
            return (int)((double)x * this.value);
        }).orElse(value);
    }

    public float apply(float value, ModifierContext context) {
        return ((MixinModifierContextAccessor)context).woldsVaults_Dev$getValue().map((x) -> {
            return (float)((double)x * this.value);
        }).orElse(value);
    }

    public double apply(double value, ModifierContext context) {
        return ((MixinModifierContextAccessor)context).woldsVaults_Dev$getValue().map((x) -> {
            return (double)x * this.value;
        }).orElse(value);
    }

    public float getValue() {
        return value;
    }
}
