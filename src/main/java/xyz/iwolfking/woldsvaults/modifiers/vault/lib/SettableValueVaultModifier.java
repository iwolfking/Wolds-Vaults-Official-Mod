package xyz.iwolfking.woldsvaults.modifiers.vault.lib;

import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Optional;

public abstract class SettableValueVaultModifier<P extends SettableValueVaultModifier.Properties> extends VaultModifier<P> {
    public SettableValueVaultModifier(ResourceLocation id, P properties, VaultModifier.Display display) {
        super(id, properties, display);
        this.setDescriptionFormatter((t, p, s) -> {
            return t.formatted((int)Math.abs(p.getValue() * (float)s * 100.0F));
        });
    }

    @Override
    public String getDisplayNameFormatted(int modifierStackSize) {
        return (String)this.display.getDescriptionFormatted().map((descriptionFormatted) -> {
            return (String)Optional.ofNullable(this.descriptionFormatter).map((formatter) -> {
                return formatter.format(descriptionFormatted, this.properties, modifierStackSize);
            }).orElse(descriptionFormatted);
        }).orElse(this.getDisplayDescription());
    }

    public static class Properties {

        private ValueProperty value = new ValueProperty(0F);

        public Properties() {
        }

        public float getValue() {
            return Objects.requireNonNullElseGet(this.value, () -> new ValueProperty(0F)).getValue();

        }

        //TODO: Fix issues with getting value from context.
        public float getValue(ModifierContext context) {
            if(this.value != null) {
                return this.getValue();
            }

            return new ValueProperty(0F).getValue();
        }

        public void setValue(Float val) {
            this.value = new ValueProperty(val);
        }

    }
}
