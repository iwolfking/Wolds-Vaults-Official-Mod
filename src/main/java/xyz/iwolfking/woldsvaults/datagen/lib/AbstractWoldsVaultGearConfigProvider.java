package xyz.iwolfking.woldsvaults.datagen.lib;

import iskallia.vault.VaultMod;
import iskallia.vault.config.gear.VaultGearTierConfig;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractVaultGearConfigProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractWoldsVaultGearConfigProvider extends AbstractVaultGearConfigProvider {
    private static final List<String> STANDARD_GEAR = List.of("axe", "battlestaff", "trident", "sword", "rang", "plushie", "wand", "chestplate", "leggings", "boots", "helmet", "focus", "loot_sack", "magnet", "shield", "unique");
    private static final List<String> STANDARD_GEAR_NO_UNIQUE = List.of("axe", "battlestaff", "trident", "sword", "rang", "plushie", "wand", "chestplate", "leggings", "boots", "helmet", "focus", "loot_sack", "magnet", "shield");
    private static final List<String> MAINHANDS = List.of("axe", "battlestaff", "trident", "sword", "rang");
    private static final List<String> OFFHANDS = List.of("plushie", "wand", "focus", "loot_sack", "shield");
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
}
