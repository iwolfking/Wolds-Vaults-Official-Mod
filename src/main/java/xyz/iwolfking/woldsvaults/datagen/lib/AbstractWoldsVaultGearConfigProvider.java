package xyz.iwolfking.woldsvaults.datagen.lib;

import iskallia.vault.VaultMod;
import iskallia.vault.config.gear.VaultGearTierConfig;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractVaultGearConfigProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.MixinModConfigs;

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

    protected AbstractWoldsVaultGearConfigProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    public void addToAllStandardGearConfigs(VaultGearTierConfig.ModifierAffixTagGroup tagGroup, Consumer<VaultGearAttributeGroupBuilder> vaultGearAttributeGroupBuilderConsumer) {
        STANDARD_GEAR.forEach(type -> {
            add(type, builder -> builder.key(VaultMod.id(type)).add(tagGroup, vaultGearAttributeGroupBuilderConsumer));
        });
    }

    public void addToAllMainHands(VaultGearTierConfig.ModifierAffixTagGroup tagGroup, Consumer<VaultGearAttributeGroupBuilder> vaultGearAttributeGroupBuilderConsumer) {
        MAINHANDS.forEach(type -> {
            add(type, builder -> builder.key(VaultMod.id(type)).add(tagGroup, vaultGearAttributeGroupBuilderConsumer));
        });
    }

    public void addToAllNonAxeMainHands(VaultGearTierConfig.ModifierAffixTagGroup tagGroup, Consumer<VaultGearAttributeGroupBuilder> vaultGearAttributeGroupBuilderConsumer) {
        MAINHANDS_NO_AXE.forEach(type -> {
            add(type, builder -> builder.key(VaultMod.id(type)).add(tagGroup, vaultGearAttributeGroupBuilderConsumer));
        });
    }

    public void addToAllNonSackOffhands(VaultGearTierConfig.ModifierAffixTagGroup tagGroup, Consumer<VaultGearAttributeGroupBuilder> vaultGearAttributeGroupBuilderConsumer) {
        OFFHANDS_NO_SACK.forEach(type -> {
            add(type, builder -> builder.key(VaultMod.id(type)).add(tagGroup, vaultGearAttributeGroupBuilderConsumer));
        });
    }

    public void addToAllNonPlushieOffhands(VaultGearTierConfig.ModifierAffixTagGroup tagGroup, Consumer<VaultGearAttributeGroupBuilder> vaultGearAttributeGroupBuilderConsumer) {
        OFFHANDS_NO_PLUSHIE.forEach(type -> {
            add(type, builder -> builder.key(VaultMod.id(type)).add(tagGroup, vaultGearAttributeGroupBuilderConsumer));
        });
    }

    public void addToAllOffhands(VaultGearTierConfig.ModifierAffixTagGroup tagGroup, Consumer<VaultGearAttributeGroupBuilder> vaultGearAttributeGroupBuilderConsumer) {
        OFFHANDS.forEach(type -> {
            add(type, builder -> builder.key(VaultMod.id(type)).add(tagGroup, vaultGearAttributeGroupBuilderConsumer));
        });
    }

    public void addToAllArmor(VaultGearTierConfig.ModifierAffixTagGroup tagGroup, Consumer<VaultGearAttributeGroupBuilder> vaultGearAttributeGroupBuilderConsumer) {
        ARMOR.forEach(type -> {
            add(type, builder -> builder.key(VaultMod.id(type)).add(tagGroup, vaultGearAttributeGroupBuilderConsumer));
        });
    }

    public void addToJewel(VaultGearTierConfig.ModifierAffixTagGroup tagGroup, Consumer<VaultGearAttributeGroupBuilder> vaultGearAttributeGroupBuilderConsumer) {
        add("jewel", builder -> builder.key(VaultMod.id("jewel")).add(tagGroup, vaultGearAttributeGroupBuilderConsumer));
    }

    public void addToAxe(VaultGearTierConfig.ModifierAffixTagGroup tagGroup, Consumer<VaultGearAttributeGroupBuilder> vaultGearAttributeGroupBuilderConsumer) {
        add("axe", builder -> builder.key(VaultMod.id("axe")).add(tagGroup, vaultGearAttributeGroupBuilderConsumer));
    }

    public void addToMaps(VaultGearTierConfig.ModifierAffixTagGroup tagGroup, Consumer<VaultGearAttributeGroupBuilder> vaultGearAttributeGroupBuilderConsumer) {
        for(int i = 0; i < 6; i++) {
            if(i == 0) {
                add("map", builder -> builder.key(VaultMod.id("map")).add(tagGroup, vaultGearAttributeGroupBuilderConsumer));
            }
            else {
                String mapId = "map_" + i;
                add(mapId, builder -> builder.key(VaultMod.id(mapId)).add(tagGroup, vaultGearAttributeGroupBuilderConsumer));
            }
        }
    }
}
