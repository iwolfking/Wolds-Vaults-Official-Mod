package xyz.iwolfking.woldsvaults.modifiers.vault.map.modifiers.lib;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import iskallia.vault.VaultMod;
import iskallia.vault.core.vault.modifier.spi.IVaultModifierTextFormatter;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.woldsvaults.modifiers.vault.lib.SettableValueVaultModifier;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class EntityAttributeModifierSettable<P extends SettableValueVaultModifier.Properties> extends SettableValueVaultModifier<EntityAttributeModifierSettable.Properties> {
    private final long leastSignificantBits;

    public EntityAttributeModifierSettable(ResourceLocation id, EntityAttributeModifierSettable.Properties properties, VaultModifier.Display display) {
        super(id, properties, display);
        this.leastSignificantBits = UUID.nameUUIDFromBytes(this.getId().toString().getBytes(StandardCharsets.UTF_8)).getLeastSignificantBits();
        if (properties.getType() != null) {
            this.setDescriptionFormatter(properties.getType().getDescriptionFormatter());
        } else {
            this.setDescriptionFormatter((t, p, s) -> {
                return t;
            });
        }

    }

    public void applyToEntity(LivingEntity entity, UUID contextUUID, ModifierContext context) {
        EntityAttributeModifierSettable.ModifierType modifierType = ((EntityAttributeModifierSettable.Properties)this.properties).getType();
        List<ResourceLocation> ids = modifierType.getAttributeResourceLocations();
        Iterator var6 = ids.iterator();

        while(var6.hasNext()) {
            ResourceLocation id = (ResourceLocation)var6.next();
            Attribute attribute = (Attribute) ForgeRegistries.ATTRIBUTES.getValue(id);
            UUID uuid = this.getId(contextUUID);
            if (attribute == null) {
                VaultMod.LOGGER.error("Invalid entity attribute '%s' configured for vault modifier '%s'".formatted(id, this.getId()));
                return;
            }

            AttributeInstance attributeInstance = entity.getAttribute(attribute);
            if (attributeInstance == null) {
                return;
            }

            AttributeModifier modifier = attributeInstance.getModifier(uuid);
            double amount = ((EntityAttributeModifierSettable.Properties)this.properties).getValue();
            if (modifier == null) {
                attributeInstance.addPermanentModifier(new AttributeModifier(uuid, this.getDisplayName(), amount, modifierType.getAttributeModifierOperation()));
            }
        }

    }

    public void removeFromEntity(LivingEntity entity) {
        EntityAttributeModifierSettable.ModifierType modifierType = ((EntityAttributeModifierSettable.Properties)this.properties).getType();
        if (modifierType != null) {
            List<ResourceLocation> ids = modifierType.getAttributeResourceLocations();
            Iterator var4 = ids.iterator();

            while(true) {
                while(var4.hasNext()) {
                    ResourceLocation attributeResourceLocation = (ResourceLocation)var4.next();
                    Attribute attribute = (Attribute)ForgeRegistries.ATTRIBUTES.getValue(attributeResourceLocation);
                    if (attribute == null) {
                        VaultMod.LOGGER.error("Invalid entity attribute '%s' configured for vault modifier '%s'".formatted(attributeResourceLocation, this.getId()));
                    } else {
                        AttributeInstance attributeInstance = entity.getAttribute(attribute);
                        if (attributeInstance != null) {
                            Set<AttributeModifier> attributeModifiers = new HashSet(attributeInstance.getModifiers());
                            Iterator var9 = attributeModifiers.iterator();

                            while(var9.hasNext()) {
                                AttributeModifier modifier = (AttributeModifier)var9.next();
                                if (this.isId(modifier.getId())) {
                                    attributeInstance.removeModifier(modifier.getId());
                                }
                            }
                        }
                    }
                }

                return;
            }
        }
    }

    protected UUID getId(UUID uuid) {
        return new UUID(uuid.getMostSignificantBits(), this.leastSignificantBits);
    }

    protected boolean isId(UUID uuid) {
        return uuid.getLeastSignificantBits() == this.leastSignificantBits;
    }

    public static class Properties extends SettableValueVaultModifier.Properties {
        @Expose
        private final EntityAttributeModifierSettable.ModifierType type;

        public Properties(EntityAttributeModifierSettable.ModifierType type) {
            this.type = type;
        }

        public EntityAttributeModifierSettable.ModifierType getType() {
            return this.type;
        }

    }

    public enum ModifierType {
        @SerializedName("max_health_additive")
        MAX_HEALTH_ADDITIVE(List.of(new ResourceLocation("generic.max_health")), AttributeModifier.Operation.ADDITION, EntityAttributeModifierSettable.ModifierType.Constants.DESCRIPTION_FORMATTER_ADDITIVE),
        @SerializedName("max_health_additive_percentile")
        MAX_HEALTH_ADDITIVE_PERCENTILE(List.of(new ResourceLocation("generic.max_health")), AttributeModifier.Operation.MULTIPLY_BASE, EntityAttributeModifierSettable.ModifierType.Constants.DESCRIPTION_FORMATTER_ADDITIVE_PERCENTILE),
        @SerializedName("max_health_multiplicative_percentile")
        MAX_HEALTH_MULTIPLICATIVE_PERCENTILE(List.of(new ResourceLocation("generic.max_health")), AttributeModifier.Operation.MULTIPLY_TOTAL, EntityAttributeModifierSettable.ModifierType.Constants.DESCRIPTION_FORMATTER_MULTIPLICATIVE_PERCENTILE),
        @SerializedName("attack_damage_additive_percentile")
        ATTACK_DAMAGE_ADDITIVE_PERCENTILE(List.of(new ResourceLocation("generic.attack_damage")), AttributeModifier.Operation.MULTIPLY_BASE, EntityAttributeModifierSettable.ModifierType.Constants.DESCRIPTION_FORMATTER_ADDITIVE_PERCENTILE),
        @SerializedName("speed_additive_percentile")
        SPEED_ADDITIVE_PERCENTILE(List.of(new ResourceLocation("generic.movement_speed"), new ResourceLocation("generic.flying_speed")), AttributeModifier.Operation.MULTIPLY_BASE, EntityAttributeModifierSettable.ModifierType.Constants.DESCRIPTION_FORMATTER_ADDITIVE_PERCENTILE),
        @SerializedName("mana_max_additive")
        MANA_MAX_ADDITIVE(List.of(VaultMod.id("generic.mana_max")), AttributeModifier.Operation.ADDITION, EntityAttributeModifierSettable.ModifierType.Constants.DESCRIPTION_FORMATTER_ADDITIVE),
        @SerializedName("mana_max_additive_percentile")
        MANA_MAX_ADDITIVE_PERCENTILE(List.of(VaultMod.id("generic.mana_max")), AttributeModifier.Operation.MULTIPLY_BASE, EntityAttributeModifierSettable.ModifierType.Constants.DESCRIPTION_FORMATTER_ADDITIVE_PERCENTILE),
        @SerializedName("mana_regen_additive")
        MANA_REGEN_ADDITIVE(List.of(VaultMod.id("generic.mana_regen")), AttributeModifier.Operation.ADDITION, EntityAttributeModifierSettable.ModifierType.Constants.DESCRIPTION_FORMATTER_ADDITIVE),
        @SerializedName("mana_regen_additive_percentile")
        MANA_REGEN_ADDITIVE_PERCENTILE(List.of(VaultMod.id("generic.mana_regen")), AttributeModifier.Operation.MULTIPLY_BASE, EntityAttributeModifierSettable.ModifierType.Constants.DESCRIPTION_FORMATTER_ADDITIVE_PERCENTILE),
        @SerializedName("crit_chance_additive")
        CRIT_CHANCE_ADDITIVE(List.of(VaultMod.id("generic.crit_chance")), AttributeModifier.Operation.ADDITION, EntityAttributeModifierSettable.ModifierType.Constants.DESCRIPTION_FORMATTER_ADDITIVE),
        @SerializedName("knockback_resistance_additive")
        KNOCKBACK_RESISTANCE_ADDITIVE(List.of(new ResourceLocation("generic.knockback_resistance")), AttributeModifier.Operation.ADDITION, EntityAttributeModifierSettable.ModifierType.Constants.DESCRIPTION_FORMATTER_ADDITIVE),
        @SerializedName("durability_wear_reduction_cap_additive")
        DURABILITY_WEAR_REDUCTION_CAP_ADDITIVE(List.of(new ResourceLocation("generic.durability_wear_reduction_cap")), AttributeModifier.Operation.ADDITION, EntityAttributeModifierSettable.ModifierType.Constants.DESCRIPTION_FORMATTER_ADDITIVE);

        private final List<ResourceLocation> attributeResourceLocations;
        private final AttributeModifier.Operation attributeModifierOperation;
        private final IVaultModifierTextFormatter<EntityAttributeModifierSettable.Properties> descriptionFormatter;

        private ModifierType(List attributeResourceLocations, AttributeModifier.Operation attributeModifierOperation, IVaultModifierTextFormatter descriptionFormatter) {
            this.attributeResourceLocations = attributeResourceLocations;
            this.attributeModifierOperation = attributeModifierOperation;
            this.descriptionFormatter = descriptionFormatter;
        }

        public List<ResourceLocation> getAttributeResourceLocations() {
            return this.attributeResourceLocations;
        }

        public AttributeModifier.Operation getAttributeModifierOperation() {
            return this.attributeModifierOperation;
        }

        public <P extends EntityAttributeModifierSettable.Properties> IVaultModifierTextFormatter<Properties> getDescriptionFormatter() {
            return this.descriptionFormatter;
        }

        private static class Constants {
            public static final IVaultModifierTextFormatter<EntityAttributeModifierSettable.Properties> DESCRIPTION_FORMATTER_ADDITIVE = (t, p, s) -> {
                return t.formatted(Mth.floor(Math.abs(p.getValue() * (double)s * 100.0)));
            };
            public static final IVaultModifierTextFormatter<EntityAttributeModifierSettable.Properties> DESCRIPTION_FORMATTER_ADDITIVE_PERCENTILE = (t, p, s) -> {
                return t.formatted((int)(Math.abs(p.getValue()) * (double)s * 100.0));
            };
            public static final IVaultModifierTextFormatter<EntityAttributeModifierSettable.Properties> DESCRIPTION_FORMATTER_MULTIPLICATIVE_PERCENTILE = (t, p, s) -> {
                return t.formatted((int)(Math.abs(p.getValue()) * (double)s * 100.0));
            };

            private Constants() {
            }
        }
    }
}
