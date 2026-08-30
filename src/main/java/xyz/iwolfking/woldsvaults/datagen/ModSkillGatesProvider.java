package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.config.ResearchesGUIConfig;
import iskallia.vault.config.skillgate.TalentPointsSpentSkillGate;
import iskallia.vault.gear.attribute.talent.TalentLevelAttribute;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.BoosterPackItem;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractSkillGatesProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModSkillGatesProvider extends AbstractSkillGatesProvider {
    protected ModSkillGatesProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        ModConfigs.RESEARCHES_GUI = new ResearchesGUIConfig().readConfig();
        add("wolds_skill_gate_overrides", builder -> {
            builder.add("Automatic Genius", skillGateEntryBuilder -> {
                skillGateEntryBuilder.hideArrow(true);
                ModConfigs.RESEARCHES_GUI.getStyles().forEach((skill, skillStyle) -> {
                   if(skill.equals("Automatic Genius")) {
                       return;
                   }

                   skillGateEntryBuilder.dependsOn(typeBuilder -> {
                      typeBuilder.constant(skill);
                   });

               });
            });
        });

        add("wolds_talent_gates", builder -> {
            builder.add("Potent_Elixir", skillGateEntryBuilder -> {
                skillGateEntryBuilder.dependsOn(entryBuilder -> {
                    entryBuilder.add(new TalentPointsSpentSkillGate(15));
                    entryBuilder.constant("Prudent");
                });
                skillGateEntryBuilder.lockedBy(lockedEntryBuilder -> {
                    lockedEntryBuilder.constant("Healthy_Elixir");
                });
            });

            builder.add("Healthy_Elixir", skillGateEntryBuilder -> {
                skillGateEntryBuilder.dependsOn(entryBuilder -> {
                    entryBuilder.add(new TalentPointsSpentSkillGate(15));
                    entryBuilder.constant("Prudent");
                });
                skillGateEntryBuilder.lockedBy(lockedEntryBuilder -> {
                    lockedEntryBuilder.constant("Potent_Elixir");
                });
            });

            builder.add("Fanged_Strike", skillGateEntryBuilder -> {
                skillGateEntryBuilder.dependsOn(typeBuilder -> {
                    typeBuilder.add(new TalentPointsSpentSkillGate(40));
                    typeBuilder.constant("Life_Steal");
                });
                skillGateEntryBuilder.lockedBy(lockedEntryBuilder -> {
                    lockedEntryBuilder.constant("Arcane_Strike");
                    lockedEntryBuilder.constant("Execution_Strike");
                });
            });

            builder.add("Execution_Strike", skillGateEntryBuilder -> {
                skillGateEntryBuilder.dependsOn(typeBuilder -> {
                    typeBuilder.add(new TalentPointsSpentSkillGate(40));
                    typeBuilder.constant("Fatal_Strike");
                });
                skillGateEntryBuilder.lockedBy(lockedEntryBuilder -> {
                    lockedEntryBuilder.constant("Arcane_Strike");
                    lockedEntryBuilder.constant("Fanged_Strike");
                });
            });

            builder.add("Arcane_Strike", skillGateEntryBuilder -> {
                skillGateEntryBuilder.dependsOn(typeBuilder -> {
                    typeBuilder.add(new TalentPointsSpentSkillGate(40));
                    typeBuilder.constant("Mana_Steal");
                });
                skillGateEntryBuilder.lockedBy(lockedEntryBuilder -> {
                    lockedEntryBuilder.constant("Execution_Strike");
                    lockedEntryBuilder.constant("Fanged_Strike");
                });
            });

            builder.add("Healthy_Harvest", skillGateEntryBuilder -> {
                skillGateEntryBuilder.dependsOn(typeBuilder -> {
                    typeBuilder.add(new TalentPointsSpentSkillGate(5));
                });

                skillGateEntryBuilder.lockedBy(lockedEntryBuilder -> {
                    lockedEntryBuilder.constant("Mana_Harvest");
                });
            });

            builder.add("Mana_Harvest", skillGateEntryBuilder -> {
                skillGateEntryBuilder.dependsOn(typeBuilder -> {
                    typeBuilder.add(new TalentPointsSpentSkillGate(5));
                });

                skillGateEntryBuilder.lockedBy(lockedEntryBuilder -> {
                    lockedEntryBuilder.constant("Healthy_Harvest");
                });
            });

            builder.add("Ransack", skillGateEntryBuilder -> {
                skillGateEntryBuilder.dependsOn(typeBuilder -> {
                    typeBuilder.add(new TalentPointsSpentSkillGate(5));
                    typeBuilder.constant("Haste");
                });
            });

            builder.add("Lunge", skillGateEntryBuilder -> {
                skillGateEntryBuilder.dependsOn(typeBuilder -> {
                    typeBuilder.add(new TalentPointsSpentSkillGate(25));
                    typeBuilder.constant("Strength");
                });
            });

            builder.add("Momentum_Engine", skillGateEntryBuilder -> {
                skillGateEntryBuilder.dependsOn(typeBuilder -> {
                    typeBuilder.add(new TalentPointsSpentSkillGate(15));
                    typeBuilder.constant("Speed");
                });
            });

            builder.add("Blazing", skillGateEntryBuilder -> {
                skillGateEntryBuilder.dependsOn(typeBuilder -> {
                    typeBuilder.add(new TalentPointsSpentSkillGate(40));
                    typeBuilder.constant("Momentum_Engine");
                });
            });

            builder.add("Frostbite", skillGateEntryBuilder -> {
                skillGateEntryBuilder.dependsOn(typeBuilder -> {
                    typeBuilder.add(new TalentPointsSpentSkillGate(25));
                });
            });

            builder.add("Blood_Chakra", skillGateEntryBuilder -> {
                skillGateEntryBuilder.dependsOn(typeBuilder -> {
                    typeBuilder.add(new TalentPointsSpentSkillGate(25));
                });
            });

            builder.add("Fatal_Strike", skillGateEntryBuilder -> {
                skillGateEntryBuilder.dependsOn(typeBuilder -> {
                    typeBuilder.add(new TalentPointsSpentSkillGate(15));
                });
            });

            builder.add("Mind_Meld", skillGateEntryBuilder -> {
                skillGateEntryBuilder.dependsOn(typeBuilder -> {
                    typeBuilder.add(new TalentPointsSpentSkillGate(15));
                    typeBuilder.constant("Intelligence");
                });
            });

            builder.add("Battle_Trance", skillGateEntryBuilder -> {
                skillGateEntryBuilder.dependsOn(typeBuilder -> {
                    typeBuilder.add(new TalentPointsSpentSkillGate(40));
                    typeBuilder.constant("Lunge");
                });
            });

            builder.add("Heart_Fragments", skillGateEntryBuilder -> {
                skillGateEntryBuilder.lockedBy(typeBuilder -> {
                    typeBuilder.constant("Medic");
                });
            });

            builder.add("Medic", skillGateEntryBuilder -> {
                skillGateEntryBuilder.lockedBy(typeBuilder -> {
                    typeBuilder.constant("Heart_Fragments");
                });
            });

            builder.add("Stack_Master", skillGateEntryBuilder -> {
                skillGateEntryBuilder.hideArrow(true);
                skillGateEntryBuilder.dependsOn(typeBuilder -> {
                    typeBuilder.add(new TalentPointsSpentSkillGate(40));
                    typeBuilder.either(choices -> {
                        choices.constant("Blazing");
                        choices.constant("Battle_Trance");
                        choices.constant("Momentum_Engine");
                        choices.constant("Blood_Rush");
                        choices.constant("Arcana");
                        choices.constant("Lucky_Momentum");
                        choices.constant("Frenzy");
                        choices.constant("Blood_Chakra");
                        choices.constant("Treasure_Seeker");
                        choices.constant("Bountiful_Harvest");
                    });
                });
            });

        });

        add("wolds_skill_gates", builder -> {
            builder.add("Bag Inception", skillGateEntryBuilder -> {
               skillGateEntryBuilder.hideArrow(true);
               skillGateEntryBuilder.dependsOn(typeBuilder -> {
                  typeBuilder.constant("Big Backpacks");
                  typeBuilder.constant("Soul Harvester");
                  typeBuilder.constant("Stack Upgrading");
                  typeBuilder.constant("Auto Feeding");
                  typeBuilder.constant("Misc Bag Upgrades");
                  typeBuilder.constant("Scanner");
               });
            });

            builder.add("Misc Bag Upgrades", skillGateEntryBuilder -> {
                skillGateEntryBuilder.hideArrow(true);
                skillGateEntryBuilder.dependsOn(typeBuilder -> {
                    typeBuilder.constant("Backpacks");
                });
            });

            builder.add("Mega Cells", skillGateEntryBuilder -> {
                skillGateEntryBuilder.hideArrow(true);
                skillGateEntryBuilder.dependsOn(typeBuilder -> {
                    typeBuilder.constant("Mekanism");
                    typeBuilder.constant("Applied Energistics");
                });
            });

            builder.add("Cable Tiers", skillGateEntryBuilder -> {
                skillGateEntryBuilder.hideArrow(true);
                skillGateEntryBuilder.dependsOn(typeBuilder -> {
                    typeBuilder.constant("Refined Storage");
                });
            });

            builder.add("Extra Storage", skillGateEntryBuilder -> {
                skillGateEntryBuilder.hideArrow(true);
                skillGateEntryBuilder.dependsOn(typeBuilder -> {
                    typeBuilder.constant("Refined Storage");
                });
            });

            builder.add("Create Crafts and Additions", skillGateEntryBuilder -> {
                skillGateEntryBuilder.hideArrow(true);
                skillGateEntryBuilder.dependsOn(typeBuilder -> {
                    typeBuilder.constant("Create");
                });
            });

            builder.add("Create Mechanical Extruder", skillGateEntryBuilder -> {
                skillGateEntryBuilder.hideArrow(true);
                skillGateEntryBuilder.dependsOn(typeBuilder -> {
                    typeBuilder.constant("Create");
                });
            });

            builder.add("Create Diesel Generators", skillGateEntryBuilder -> {
                skillGateEntryBuilder.hideArrow(true);
                skillGateEntryBuilder.dependsOn(typeBuilder -> {
                    typeBuilder.constant("Create");
                });
            });

            builder.add("Create Ore Excavation", skillGateEntryBuilder -> {
                skillGateEntryBuilder.hideArrow(true);
                skillGateEntryBuilder.dependsOn(typeBuilder -> {
                    typeBuilder.constant("Create");
                });
            });

            builder.add("QuarryPlus", skillGateEntryBuilder -> {
                skillGateEntryBuilder.hideArrow(true);
                skillGateEntryBuilder.dependsOn(typeBuilder -> {
                    typeBuilder.constant("Oops, All Iron Mods");
                    typeBuilder.constant("Digital Miner");
                    typeBuilder.constant("Create Ore Excavation");
                    typeBuilder.constant("Mining Gadgets");
                });
            });

            builder.add("Mystical Agriculture", skillGateEntryBuilder -> {
                skillGateEntryBuilder.hideArrow(true);
                skillGateEntryBuilder.dependsOn(typeBuilder -> {
                    typeBuilder.constant("Botania");
                    typeBuilder.constant("Bonsai Pots");
                    typeBuilder.constant("Botany Pots");
                    typeBuilder.constant("Botanical Machinery");
                    typeBuilder.constant("Botania Flux Field");
                    typeBuilder.constant("Incorporeal");
                });
            });

            builder.add("Botanical Machinery", skillGateEntryBuilder -> {
                skillGateEntryBuilder.hideArrow(true);
                skillGateEntryBuilder.dependsOn(typeBuilder -> {
                    typeBuilder.constant("Botania");
                });
            });

            builder.add("Incorporeal", skillGateEntryBuilder -> {
                skillGateEntryBuilder.hideArrow(true);
                skillGateEntryBuilder.dependsOn(typeBuilder -> {
                    typeBuilder.constant("Botania");
                });
            });

            builder.add("Applied Mekanistics", skillGateEntryBuilder -> {
                skillGateEntryBuilder.hideArrow(true);
                skillGateEntryBuilder.dependsOn(typeBuilder -> {
                    typeBuilder.constant("Mekanism");
                    typeBuilder.constant("Applied Energistics");
                });
            });

            builder.add("Applied Botanics", skillGateEntryBuilder -> {
                skillGateEntryBuilder.hideArrow(true);
                skillGateEntryBuilder.dependsOn(typeBuilder -> {
                    typeBuilder.constant("Botania");
                    typeBuilder.constant("Applied Energistics");
                });
            });

            builder.add("Applied Compatability", skillGateEntryBuilder -> {
                skillGateEntryBuilder.hideArrow(true);
                skillGateEntryBuilder.dependsOn(typeBuilder -> {
                    typeBuilder.constant("Applied Energistics");
                });
                skillGateEntryBuilder.dependsOn(typeBuilder -> {
                    typeBuilder.either(typeBuilder1 -> {
                        typeBuilder1.constant("Mekanism");
                        typeBuilder1.constant("Botania");
                    });
                });
            });

            builder.add("Compressed Creativity", skillGateEntryBuilder -> {
                skillGateEntryBuilder.hideArrow(true);
                skillGateEntryBuilder.dependsOn(typeBuilder -> {
                    typeBuilder.constant("Create");
                    typeBuilder.constant("PneumaticCraft");
                });
            });

            builder.add("Advanced Peripherals", skillGateEntryBuilder -> {
                skillGateEntryBuilder.hideArrow(true);
                skillGateEntryBuilder.dependsOn(typeBuilder -> {
                    typeBuilder.constant("ComputerCraft");
                });
            });

            builder.add("Draconic Evolution", skillGateEntryBuilder -> {
                skillGateEntryBuilder.hideArrow(true);
                skillGateEntryBuilder.dependsOn(typeBuilder -> {
                    typeBuilder.constant("RFTools Power");
                    typeBuilder.constant("Powah");
                    typeBuilder.constant("Flux Networks");
                    typeBuilder.constant("Thermal Dynamos");
                    typeBuilder.constant("Mekanism Generators");
                    typeBuilder.constant("Botania Flux Field");
                });
            });


        });
    }
}
