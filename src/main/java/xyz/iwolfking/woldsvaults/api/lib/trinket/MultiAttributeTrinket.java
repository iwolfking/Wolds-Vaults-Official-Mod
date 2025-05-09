package xyz.iwolfking.woldsvaults.api.lib.trinket;

import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearAttributeRegistry;
import iskallia.vault.gear.trinket.GearAttributeTrinket;
import iskallia.vault.gear.trinket.TrinketEffect;
import iskallia.vault.util.MiscUtils;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class MultiAttributeTrinket<T extends Number> extends TrinketEffect<MultiAttributeTrinket.Config<?>> implements GearAttributeTrinket {
    private final List<VaultGearAttribute<T>> defaultAttribute;
    private final List<T> defaultAttributeValue;


    public MultiAttributeTrinket(ResourceLocation name, List<VaultGearAttribute<T>> defaultAttribute, List<T> defaultAttributeValue) {
        super(name);
        this.defaultAttribute = defaultAttribute;
        this.defaultAttributeValue = defaultAttributeValue;
    }

    public Class<MultiAttributeTrinket.Config<?>> getConfigClass() {
        return MiscUtils.cast(MultiAttributeTrinket.Config.class);
    }

    public MultiAttributeTrinket.Config<T> getDefaultConfig() {
        return new MultiAttributeTrinket.Config<T>(this.defaultAttribute, this.defaultAttributeValue);
    }

    public List<VaultGearAttributeInstance<?>> getAttributes() {
        return this.getConfig().toAttributeInstances();
    }

    public static class Config<T extends Number> extends TrinketEffect.Config {
        @Expose
        private final List<ResourceLocation> keys;
        @Expose
        private final List<Number> values;

        public Config(List<VaultGearAttribute<T>> attributes, List<T> values) {
            this.keys = new ArrayList<>();
            this.values = new ArrayList<>();
            for(VaultGearAttribute<?> attribute : attributes) {
                this.keys.add(attribute.getRegistryName());
            }

            this.values.addAll(values);
        }

        public List<VaultGearAttribute<T>> getAttributes() {
            List<VaultGearAttribute<T>> attributes = new ArrayList<>();
            for(ResourceLocation key : keys) {
                attributes.add((VaultGearAttribute<T>) VaultGearAttributeRegistry.getAttribute(key));
            }
            return attributes;
        }

        public List<VaultGearAttributeInstance<?>> toAttributeInstances() {
            List<VaultGearAttributeInstance<?>> instances = new ArrayList<>();
            List<VaultGearAttribute<T>> attributes = getAttributes();

            for(int i = 0; i < attributes.size(); i++) {
                instances.add(VaultGearAttributeInstance.cast(attributes.get(i), this.values.get(i)));
            }

            return instances;
        }
    }
}
