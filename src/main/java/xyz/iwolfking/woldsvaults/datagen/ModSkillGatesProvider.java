package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.config.ResearchesGUIConfig;
import iskallia.vault.init.ModConfigs;
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
