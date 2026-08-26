package xyz.iwolfking.woldsvaults.datagen.lib;

import iskallia.vault.VaultMod;
import iskallia.vault.config.entry.FloatRollRangeEntry;
import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.gear.attribute.config.FloatAttributeGenerator;
import iskallia.vault.gear.attribute.custom.ability.BroodmotherWebAttribute;
import iskallia.vault.gear.attribute.custom.effect.EffectGearAttribute;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Item;
import xyz.iwolfking.vhapi.api.datagen.AbstractVaultGearConfigProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.MixinModConfigs;
import xyz.iwolfking.woldsvaults.modifiers.vault.lib.StringValueGenerator;

import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractWoldsVaultGearConfigProvider extends AbstractVaultGearConfigProvider {
    private static final List<String> STANDARD_GEAR = List.of("axe", "battlestaff", "trident", "sword", "rang", "plushie", "wand", "chestplate", "leggings", "boots", "helmet", "focus", "loot_sack", "magnet", "shield", "unique");
    private static final List<String> STANDARD_GEAR_NO_UNIQUE = List.of("axe", "battlestaff", "trident", "sword", "rang", "plushie", "wand", "chestplate", "leggings", "boots", "helmet", "focus", "loot_sack", "magnet", "shield");
    private static final List<String> MAINHANDS = List.of("axe", "battlestaff", "trident", "sword", "rang");
    private static final List<String> MAINHANDS_NO_AXE = List.of("battlestaff", "trident", "sword", "rang");
    private static final List<String> OFFHANDS = List.of("plushie", "wand", "focus", "loot_sack", "shield", "magnet");
    private static final List<String> OFFHANDS_NO_SACK = List.of("plushie", "wand", "focus", "shield");
    private static final List<String> OFFHANDS_NO_PLUSHIE = List.of("loot_sack", "wand", "focus", "shield", "magnet");
    private static final List<String> ARMOR = List.of("chestplate", "leggings", "boots", "helmet");
    private static final String MYTHIC = "_mythic";

    protected AbstractWoldsVaultGearConfigProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    public void addToAllStandardGearConfigs(VaultGearTierConfig.ModifierAffixTagGroup tagGroup, Consumer<VaultGearAttributeGroupBuilder> vaultGearAttributeGroupBuilderConsumer) {
        STANDARD_GEAR.forEach(type -> {
            add(type, builder -> builder.key(VaultMod.id(type)).add(tagGroup, vaultGearAttributeGroupBuilderConsumer));
            if(tagGroup.equals(VaultGearTierConfig.ModifierAffixTagGroup.CORRUPTED_IMPLICIT)) {
                add(type + MYTHIC, builder -> builder.key(VaultMod.id(type + MYTHIC)).add(tagGroup, vaultGearAttributeGroupBuilderConsumer));
            }
        });

    }

    public void addToAllMainHands(VaultGearTierConfig.ModifierAffixTagGroup tagGroup, Consumer<VaultGearAttributeGroupBuilder> vaultGearAttributeGroupBuilderConsumer) {
        MAINHANDS.forEach(type -> {
            add(type, builder -> builder.key(VaultMod.id(type)).add(tagGroup, vaultGearAttributeGroupBuilderConsumer));
            if(tagGroup.equals(VaultGearTierConfig.ModifierAffixTagGroup.CORRUPTED_IMPLICIT)) {
                add(type + MYTHIC, builder -> builder.key(VaultMod.id(type + MYTHIC)).add(tagGroup, vaultGearAttributeGroupBuilderConsumer));
            }
        });
    }

    public void addToAllNonAxeMainHands(VaultGearTierConfig.ModifierAffixTagGroup tagGroup, Consumer<VaultGearAttributeGroupBuilder> vaultGearAttributeGroupBuilderConsumer) {
        MAINHANDS_NO_AXE.forEach(type -> {
            add(type, builder -> builder.key(VaultMod.id(type)).add(tagGroup, vaultGearAttributeGroupBuilderConsumer));
            if(tagGroup.equals(VaultGearTierConfig.ModifierAffixTagGroup.CORRUPTED_IMPLICIT)) {
                add(type + MYTHIC, builder -> builder.key(VaultMod.id(type + MYTHIC)).add(tagGroup, vaultGearAttributeGroupBuilderConsumer));
            }
        });
    }

    public void addToAllOffhandsExcept(VaultGearTierConfig.ModifierAffixTagGroup tagGroup, List<String> typesToExclude, Consumer<VaultGearAttributeGroupBuilder> vaultGearAttributeGroupBuilderConsumer) {
        OFFHANDS.forEach(type -> {
            if(typesToExclude.contains(type)) {
                return;
            }

            add(type, builder -> builder.key(VaultMod.id(type)).add(tagGroup, vaultGearAttributeGroupBuilderConsumer));
            if(tagGroup.equals(VaultGearTierConfig.ModifierAffixTagGroup.CORRUPTED_IMPLICIT)) {
                add(type + MYTHIC, builder -> builder.key(VaultMod.id(type + MYTHIC)).add(tagGroup, vaultGearAttributeGroupBuilderConsumer));
            }
        });
    }

    public void addToAllOffhands(VaultGearTierConfig.ModifierAffixTagGroup tagGroup, Consumer<VaultGearAttributeGroupBuilder> vaultGearAttributeGroupBuilderConsumer) {
        OFFHANDS.forEach(type -> {
            add(type, builder -> builder.key(VaultMod.id(type)).add(tagGroup, vaultGearAttributeGroupBuilderConsumer));
            if(tagGroup.equals(VaultGearTierConfig.ModifierAffixTagGroup.CORRUPTED_IMPLICIT)) {
                add(type + MYTHIC, builder -> builder.key(VaultMod.id(type + MYTHIC)).add(tagGroup, vaultGearAttributeGroupBuilderConsumer));
            }
        });
    }

    public void addToAllArmor(VaultGearTierConfig.ModifierAffixTagGroup tagGroup, Consumer<VaultGearAttributeGroupBuilder> vaultGearAttributeGroupBuilderConsumer) {
        ARMOR.forEach(type -> {
            add(type, builder -> builder.key(VaultMod.id(type)).add(tagGroup, vaultGearAttributeGroupBuilderConsumer));
            if(tagGroup.equals(VaultGearTierConfig.ModifierAffixTagGroup.CORRUPTED_IMPLICIT)) {
                add(type + MYTHIC, builder -> builder.key(VaultMod.id(type + MYTHIC)).add(tagGroup, vaultGearAttributeGroupBuilderConsumer));
            }
        });
    }

    public void addTo(Item gearItem, VaultGearTierConfig.ModifierAffixTagGroup tagGroup, Consumer<VaultGearAttributeGroupBuilder> vaultGearAttributeGroupBuilderConsumer) {
        add(gearItem.getRegistryName().getPath(), builder -> builder.key(gearItem.getRegistryName()).add(tagGroup, vaultGearAttributeGroupBuilderConsumer));
        if(tagGroup.equals(VaultGearTierConfig.ModifierAffixTagGroup.CORRUPTED_IMPLICIT)) {
            add(gearItem.getRegistryName().getPath() + MYTHIC, builder -> builder.key(VaultMod.id(gearItem.getRegistryName().getPath() + MYTHIC)).add(tagGroup, vaultGearAttributeGroupBuilderConsumer));
        }
    }

    public void addToJewel(VaultGearTierConfig.ModifierAffixTagGroup tagGroup, Consumer<VaultGearAttributeGroupBuilder> vaultGearAttributeGroupBuilderConsumer) {
        add("jewel", builder -> builder.key(VaultMod.id("jewel")).add(tagGroup, vaultGearAttributeGroupBuilderConsumer));
    }

    public void addToAxe(VaultGearTierConfig.ModifierAffixTagGroup tagGroup, Consumer<VaultGearAttributeGroupBuilder> vaultGearAttributeGroupBuilderConsumer) {
        add("axe", builder -> builder.key(VaultMod.id("axe")).add(tagGroup, vaultGearAttributeGroupBuilderConsumer));
    }

    public void addToMaps(VaultGearTierConfig.ModifierAffixTagGroup tagGroup, Consumer<VaultGearAttributeGroupBuilder> vaultGearAttributeGroupBuilderConsumer) {
        addToMaps(tagGroup, List.of(0, 1, 2, 3, 4, 5), vaultGearAttributeGroupBuilderConsumer);
    }

    public void addToMaps(VaultGearTierConfig.ModifierAffixTagGroup tagGroup, List<Integer> tiers, Consumer<VaultGearAttributeGroupBuilder> vaultGearAttributeGroupBuilderConsumer) {
        tiers.forEach(tier -> {
            if(tier == 0) {
                add("map", builder -> builder.key(VaultMod.id("map")).add(tagGroup, vaultGearAttributeGroupBuilderConsumer));
            }
            else {
                String mapId = "map_" + tier;
                add(mapId, builder -> builder.key(VaultMod.id(mapId)).add(tagGroup, vaultGearAttributeGroupBuilderConsumer));
            }
        });
    }

    public static VaultGearModifierTiersBuilder addBroodmotherWeb(
            VaultGearModifierTiersBuilder builder,
            int minLevel, int maxLevel, int weight,
            float minChance, float maxChance, float stepChance,
            float minDamage, float maxDamage, float stepDamage) {

        BroodmotherWebAttribute.Config config = new BroodmotherWebAttribute.Config(
                new FloatRollRangeEntry(minChance, maxChance, stepChance),
                new FloatRollRangeEntry(minDamage, maxDamage, stepDamage)
        );

        return builder.add(new VaultGearTierConfig.ModifierTier<>(minLevel, weight, config), maxLevel);
    }

    public static VaultGearModifierTiersBuilder addString(
            VaultGearModifierTiersBuilder builder,
            int minLevel, int maxLevel, int weight,
            String value) {
        return builder.add(new VaultGearTierConfig.ModifierTier<>(minLevel, weight, new StringValueGenerator.StringValue(value)), maxLevel);
    }
}
