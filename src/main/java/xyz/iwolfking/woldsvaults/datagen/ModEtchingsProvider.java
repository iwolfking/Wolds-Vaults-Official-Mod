package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.attribute.config.BooleanFlagGenerator;
import iskallia.vault.gear.attribute.config.FloatAttributeGenerator;
import iskallia.vault.gear.attribute.config.IntegerAttributeGenerator;
import iskallia.vault.gear.attribute.config.PairAttributeGenerator;
import iskallia.vault.gear.attribute.talent.TalentLevelAttribute;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractEtchingProvider;
import xyz.iwolfking.vhapi.api.util.gear.GearModifierRegistryHelper;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModEtchingsProvider extends AbstractEtchingProvider {
    protected ModEtchingsProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        add("wolds_etchings", builder -> {
            builder.addEtching(WoldsVaults.id("concentrate_drain"), "Concentrated Drain",  "<gray>Concentrate drains <yellow>%.0fpvalue% <gray>current health from mob per effect drained.", 11153926, VaultGearModifier.AffixType.IMPLICIT, etchingEntryBuilder -> {
                etchingEntryBuilder.typeGroups(stringBasicListBuilder -> {
                    stringBasicListBuilder.add("Offensive");
                });
                etchingEntryBuilder.attribute(WoldsVaults.id("concentrate_drain"), "ModEtching", WoldsVaults.id("concentrate_drain_etching"), tierBasicListBuilder -> {
                   tierBasicListBuilder.add(GearModifierRegistryHelper.createEtchingTier(0, 4, 10, 1, new FloatAttributeGenerator.Range(0.02F, 0.04F, 0.01F)));
                   tierBasicListBuilder.add(GearModifierRegistryHelper.createEtchingTier(4, 7, 10, 2, new FloatAttributeGenerator.Range(0.04F, 0.06F, 0.01F)));
                   tierBasicListBuilder.add(GearModifierRegistryHelper.createEtchingTier(7, 12, 10, 3, new FloatAttributeGenerator.Range(0.06F, 0.08F, 0.01F)));
                   tierBasicListBuilder.add(GearModifierRegistryHelper.createEtchingTier(12, -1, 10, 3, new FloatAttributeGenerator.Range(0.08F, 0.1F, 0.01F)));
                }).minGreedTier(0);
            });

            builder.addEtching(WoldsVaults.id("fireball_greedball"), "Greedball",  "<red>Fireball<gray> deals additional damage based on <yellow>%.0fpvalue%<gray> of your <gold>Item Quantity<gray>, <yellow>Item Rarity<gray>, and <light_purple>Copiously<gray>. Base radius set to minimum.", 11153926, VaultGearModifier.AffixType.IMPLICIT, etchingEntryBuilder -> {
                etchingEntryBuilder.typeGroups(stringBasicListBuilder -> {
                    stringBasicListBuilder.add("FOCUS");
                });
                etchingEntryBuilder.attribute(WoldsVaults.id("fireball_greedball"), "ModEtching", WoldsVaults.id("fireball_greedball_etching"), tierBasicListBuilder -> {
                    tierBasicListBuilder.add(GearModifierRegistryHelper.createEtchingTier(0, 3, 10, 1, new FloatAttributeGenerator.Range(0.1F, 0.15F, 0.01F)));
                    tierBasicListBuilder.add(GearModifierRegistryHelper.createEtchingTier(3, 6, 10, 2, new FloatAttributeGenerator.Range(0.15F, 0.2F, 0.01F)));
                    tierBasicListBuilder.add(GearModifierRegistryHelper.createEtchingTier(6, 9, 10, 3, new FloatAttributeGenerator.Range(0.2F, 0.25F, 0.01F)));
                    tierBasicListBuilder.add(GearModifierRegistryHelper.createEtchingTier(9, 11, 10, 3, new FloatAttributeGenerator.Range(0.25F, 0.3F, 0.01F)));
                    tierBasicListBuilder.add(GearModifierRegistryHelper.createEtchingTier(11, -1, 10, 3, new FloatAttributeGenerator.Range(0.3F, 0.4F, 0.01F)));
                }).minGreedTier(0);
            });

            builder.addEtching(WoldsVaults.id("levitation_slow_falling"), "Levitation Hover",  "<#34b7eb>Levitate<gray> adds <yellow>Slow Falling<gray> for a brief period when deactivated", 3093151, VaultGearModifier.AffixType.IMPLICIT, etchingEntryBuilder -> {
                etchingEntryBuilder.typeGroups(stringBasicListBuilder -> {
                    stringBasicListBuilder.add("Utility");
                });
                etchingEntryBuilder.attribute(WoldsVaults.id("levitate_slow_falling"), "ModEtching", WoldsVaults.id("levitate_slow_falling_etching"), tierBasicListBuilder -> {
                    tierBasicListBuilder.add(GearModifierRegistryHelper.createEtchingTier(0, -1, 10, 1, new BooleanFlagGenerator.BooleanFlag(true)));
                }).minGreedTier(0);
            });

            builder.addEtching(WoldsVaults.id("reverberation"), "Reverberating Echo",  "<#691337>Echoing<gray> deals damage on subsequent applications instead of over time", 6886199, VaultGearModifier.AffixType.IMPLICIT, etchingEntryBuilder -> {
                etchingEntryBuilder.typeGroups(stringBasicListBuilder -> {
                    stringBasicListBuilder.add("Offensive");
                });
                etchingEntryBuilder.attribute(WoldsVaults.id("reverberation"), "ModEtching", WoldsVaults.id("reverberation"), tierBasicListBuilder -> {
                    tierBasicListBuilder.add(GearModifierRegistryHelper.createEtchingTier(4, -1, 10, 1, new BooleanFlagGenerator.BooleanFlag(true)));
                });
            });

            builder.addEtching(WoldsVaults.id("purist_common"), "Purist of Normality",  "<#214E92>Purist<gray> counts <aqua>Common<gray> gear in addition to Scrappy", 2182802, VaultGearModifier.AffixType.IMPLICIT, etchingEntryBuilder -> {
                etchingEntryBuilder.typeGroups(stringBasicListBuilder -> {
                    stringBasicListBuilder.add("SHIELD");
                    stringBasicListBuilder.add("FOCUS");
                });
                etchingEntryBuilder.attribute(WoldsVaults.id("purist_common"), "ModEtching", WoldsVaults.id("purist_common_etching"), tierBasicListBuilder -> {
                    tierBasicListBuilder.add(GearModifierRegistryHelper.createEtchingTier(0, -1, 10, 1, new BooleanFlagGenerator.BooleanFlag(true)));
                }).minGreedTier(0);
            });

            builder.addEtching(WoldsVaults.id("divinity"), "Blessed",  "<#5CD916>Immune to <yellow>Most<#5CD916> Negative Effects", 6084886, VaultGearModifier.AffixType.IMPLICIT, etchingEntryBuilder -> {
                etchingEntryBuilder.typeGroups(stringBasicListBuilder -> {
                    stringBasicListBuilder.add("Defensive");
                });
                etchingEntryBuilder.attribute(WoldsVaults.id("divinity"), "ModEtching", WoldsVaults.id("divinity_etching"), tierBasicListBuilder -> {
                    tierBasicListBuilder.add(GearModifierRegistryHelper.createEtchingTier(7, -1, 10, 1, new BooleanFlagGenerator.BooleanFlag(true)));
                }).minGreedTier(7);
            });

            builder.addEtching(WoldsVaults.id("ingenium"), "Ingenium",  "<yellow>+1<gray> to all <aqua>Talent<gray> levels", 6084886, VaultGearModifier.AffixType.IMPLICIT, etchingEntryBuilder -> {
                etchingEntryBuilder.typeGroups(stringBasicListBuilder -> {
                    stringBasicListBuilder.add("Defensive");
                });
                etchingEntryBuilder.attribute(VaultMod.id("added_talent_level"), "ModEtching", WoldsVaults.id("ingenium_etching"), tierBasicListBuilder -> {
                    tierBasicListBuilder.add(GearModifierRegistryHelper.createEtchingTier(0, -1, 10, 1, new TalentLevelAttribute.Config(TalentLevelAttribute.ALL_TALENTS, 1)));
                }).minGreedTier(0);
            });

            builder.addEtching(WoldsVaults.id("prudent_chaos"), "Prudent Chaos",  "<#E87CAC>Prudent<gray> triggers a random <#FF7CAC>Brew<gray> effect when successful", 15236268, VaultGearModifier.AffixType.IMPLICIT, etchingEntryBuilder -> {
                etchingEntryBuilder.typeGroups(stringBasicListBuilder -> {
                    stringBasicListBuilder.add("Defensive");
                });
                etchingEntryBuilder.attribute(WoldsVaults.id("prudent_chaos"), "ModEtching", WoldsVaults.id("prudent_chaos_etching"), tierBasicListBuilder -> {
                    tierBasicListBuilder.add(GearModifierRegistryHelper.createEtchingTier(0, -1, 10, 1, new BooleanFlagGenerator.BooleanFlag(true)));
                }).minGreedTier(0);
            });

            builder.addEtching(WoldsVaults.id("colossus_titan_resistance"), "Colossus Titan",  "<#41880E>Colossus<gray> adds <yellow>%.0fpvalue%<gray> additional <#FF7400>Resistance Cap Limit<gray> while active", 4092928, VaultGearModifier.AffixType.IMPLICIT, etchingEntryBuilder -> {
                etchingEntryBuilder.typeGroups(stringBasicListBuilder -> {
                    stringBasicListBuilder.add("Defensive");
                });
                etchingEntryBuilder.attribute(WoldsVaults.id("colossus_titan_resistance"), "ModEtching", WoldsVaults.id("colossus_titan_resistance_etching"), tierBasicListBuilder -> {
                    tierBasicListBuilder.add(GearModifierRegistryHelper.createEtchingTier(4, 11, 10, 1, new FloatAttributeGenerator.Range(0.04F, 0.06F, 0.01F)));
                    tierBasicListBuilder.add(GearModifierRegistryHelper.createEtchingTier(11, -1, 10, 1, new FloatAttributeGenerator.Range(0.05F, 0.08F, 0.01F)));
                }).minGreedTier(4);
            });

            builder.addEtching(WoldsVaults.id("sneaky_getaway_ninja"), "Sneaky Getaway Ninja",  "<#301749>Sneaky Getaway<gray> adds <yellow>%.0fpvalue%<gray> additional <#30455F>Dodge Chance<gray> while active", 3163487, VaultGearModifier.AffixType.IMPLICIT, etchingEntryBuilder -> {
                etchingEntryBuilder.typeGroups(stringBasicListBuilder -> {
                    stringBasicListBuilder.add("Defensive");
                });
                etchingEntryBuilder.attribute(WoldsVaults.id("sneaky_getaway_ninja"), "ModEtching", WoldsVaults.id("sneaky_getaway_ninja_etching"), tierBasicListBuilder -> {
                    tierBasicListBuilder.add(GearModifierRegistryHelper.createEtchingTier(7, 11, 10, 1, new FloatAttributeGenerator.Range(0.25F, 0.3F, 0.01F)));
                    tierBasicListBuilder.add(GearModifierRegistryHelper.createEtchingTier(11, -1, 10, 1, new FloatAttributeGenerator.Range(0.3F, 0.4F, 0.01F)));
                }).minGreedTier(7);
            });

            builder.addEtching(WoldsVaults.id("diffuse_chemical_bomb"), "Diffuse Chemical Barrage",  "<#83BB6C>Diffuse<gray> throws <yellow>%dvalue<gray> <#83176C>Chaos Grenades<gray> randomly around you", 8874661, VaultGearModifier.AffixType.IMPLICIT, etchingEntryBuilder -> {
                etchingEntryBuilder.typeGroups(stringBasicListBuilder -> {
                    stringBasicListBuilder.add("Offensive");
                });
                etchingEntryBuilder.attribute(WoldsVaults.id("diffuse_chemical_bomb"), "ModEtching", WoldsVaults.id("diffuse_chemical_bomb_etching"), tierBasicListBuilder -> {
                    tierBasicListBuilder.add(GearModifierRegistryHelper.createEtchingTier(0, 4, 10, 1, new IntegerAttributeGenerator.Range(3, 4, 1)));
                    tierBasicListBuilder.add(GearModifierRegistryHelper.createEtchingTier(4, 7, 10, 1, new IntegerAttributeGenerator.Range(4, 5, 1)));
                    tierBasicListBuilder.add(GearModifierRegistryHelper.createEtchingTier(7, 9, 10, 1, new IntegerAttributeGenerator.Range(5, 6, 1)));
                    tierBasicListBuilder.add(GearModifierRegistryHelper.createEtchingTier(9, -1, 10, 1, new IntegerAttributeGenerator.Range(6, 9, 1)));
                }).minGreedTier(0);
            });

            builder.addEtching(WoldsVaults.id("fireball_volley_mitosis"), "Fireball Volley Mitosis",  "<#870018>Fireball Volley<gray> creates <yellow>%dvalue<gray> additional fireball on first bounce", 8847384, VaultGearModifier.AffixType.IMPLICIT, etchingEntryBuilder -> {
                etchingEntryBuilder.typeGroups(stringBasicListBuilder -> {
                    stringBasicListBuilder.add("Offensive");
                });
                etchingEntryBuilder.attribute(WoldsVaults.id("fireball_volley_mitosis"), "ModEtching", WoldsVaults.id("fireball_volley_mitosis_etching"), tierBasicListBuilder -> {
                    tierBasicListBuilder.add(GearModifierRegistryHelper.createEtchingTier(0, 8, 10, 1, new IntegerAttributeGenerator.Range(1, 1, 1)));
                    tierBasicListBuilder.add(GearModifierRegistryHelper.createEtchingTier(8, 10, 10, 1, new IntegerAttributeGenerator.Range(2, 2, 1)));
                    tierBasicListBuilder.add(GearModifierRegistryHelper.createEtchingTier(10, -1, 10, 1, new IntegerAttributeGenerator.Range(2, 4, 1)));
                }).minGreedTier(0);
            });

            builder.addEtching(WoldsVaults.id("reaving_hemmorage"), "Reaving Hemmorage",  "<#98329F>Reaving<gray> inflicts <red>Bleed<gray> <yellow>%dvalue<gray> for <yellow>8<gray> seconds", 9974431, VaultGearModifier.AffixType.IMPLICIT, etchingEntryBuilder -> {
                etchingEntryBuilder.typeGroups(stringBasicListBuilder -> {
                    stringBasicListBuilder.add("Offensive");
                });
                etchingEntryBuilder.attribute(WoldsVaults.id("reaving_hemmorage"), "ModEtching", WoldsVaults.id("reaving_hemmorage_etching"), tierBasicListBuilder -> {
                    tierBasicListBuilder.add(GearModifierRegistryHelper.createEtchingTier(0, 5, 10, 1, new IntegerAttributeGenerator.Range(3, 4, 1)));
                    tierBasicListBuilder.add(GearModifierRegistryHelper.createEtchingTier(5, 7, 10, 1, new IntegerAttributeGenerator.Range(4, 5, 1)));
                    tierBasicListBuilder.add(GearModifierRegistryHelper.createEtchingTier(7, 10, 10, 1, new IntegerAttributeGenerator.Range(5, 7, 1)));
                    tierBasicListBuilder.add(GearModifierRegistryHelper.createEtchingTier(10, -1, 10, 1, new IntegerAttributeGenerator.Range(7, 9, 1)));
                }).minGreedTier(0);
            });
        });
    }
}
