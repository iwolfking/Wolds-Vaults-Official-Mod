package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import iskallia.vault.init.ModConfigs;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.vhapi.api.datagen.AbstractVaultMobsProvider;
import xyz.iwolfking.vhapi.api.util.builder.description.JsonDescription;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModEntities;

public class ModVaultMobsProvider extends AbstractVaultMobsProvider {
    public ModVaultMobsProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    protected void registerOverrides() {
        add(ModEntities.HOSTILE_SHEEP.getRegistryName(), vaultMobBuilder -> {
             vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                     .xpValue(75)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 30.0, 40.0, "set", 1.0, 0.07, 49)
                                .addLevel(50, 39.0, 58.0, "set", 1.0, 0.09, 64)
                                .addLevel(65, 40.0, 69.0, "set", 1.0, 0.09, -1)
                                .addLevel(80, 44, 75.9, "set", 1.0, 0.09, -1)
                                .addLevel(90, 42.0, 82.8, "set", 1.0, 0.09, -1);
                    })
                    .attributeWithLevels("minecraft:generic.attack_damage", levels -> {
                        levels.addLevel(0, 1.0, 3.0, "set", 1.0, 0.1, 50)
                                .addLevel(65, 2.0, 4.0, "set", 1.0, 0.1, -1)
                                .addLevel(90, 2.5, 5.0, "set", 1.0, 0.1, -1);
                    })
                    .attributeWithLevels("the_vault:generic.crit_chance", levels -> {
                        levels.addLevel(0, 0.05, 0.1, "set", 1.0, 0.0, 49)
                                .addLevel(50, 0.1, 0.15, "set", 1.0, 0.0, 84)
                                .addLevel(85, 0.15, 0.2, "set", 1.0, 0.0, -1);
                    })
                    .attributeWithLevels("the_vault:generic.crit_multiplier", levels -> {
                        levels.addLevel(0, 1.2, 1.2, "set", 1.0, 0.0, 49)
                                .addLevel(50, 1.3, 1.3, "set", 1.0, 0.0, 84)
                                .addLevel(85, 1.4, 1.4, "set", 1.0, 0.0, -1);
                    })
                    .attributeSimple("minecraft:generic.movement_speed", 1.05, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Barnyard");
                    }, 30, descriptions -> {
                        descriptions.add(JsonDescription.simple("Baaaa!", "$text"));
                    });
        });

        add(ModEntities.HOSTILE_TURKEY.getRegistryName(), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("horde"))
                    .xpValue(65)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 25.0, 35.0, "set", 1.0, 0.07, 49)
                                .addLevel(50, 35.0, 45.0, "set", 1.0, 0.09, 64)
                                .addLevel(80, 45.0, 55.0, "set", 1.0, 0.09, -1);
                    })
                    .attributeWithLevels("minecraft:generic.attack_damage", levels -> {
                        levels.addLevel(0, 2.0, 3.0, "set", 1.0, 0.1, 50)
                                .addLevel(65, 2.2, 4.3, "set", 1.0, 0.1, -1)
                                .addLevel(90, 2.4, 4.6, "set", 1.0, 0.1, -1);
                    })
                    .attributeWithLevels("the_vault:generic.crit_chance", levels -> {
                        levels.addLevel(0, 0.05, 0.1, "set", 1.0, 0.0, 49)
                                .addLevel(50, 0.1, 0.15, "set", 1.0, 0.0, 84)
                                .addLevel(85, 0.15, 0.2, "set", 1.0, 0.0, -1);
                    })
                    .attributeWithLevels("the_vault:generic.crit_multiplier", levels -> {
                        levels.addLevel(0, 1.2, 1.2, "set", 1.0, 0.0, 49)
                                .addLevel(50, 1.3, 1.3, "set", 1.0, 0.0, 84)
                                .addLevel(85, 1.4, 1.4, "set", 1.0, 0.0, -1);
                    })
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Harvest");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple("Gobble-gobble!", "$text"));
                    });
        });

        add(ModEntities.HOSTILE_CHICKEN.getRegistryName(), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("horde"))
                    .xpValue(40)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 25.0, 35.0, "set", 1.0, 0.07, 49)
                                .addLevel(50, 35.0, 45.0, "set", 1.0, 0.09, 64)
                                .addLevel(80, 45.0, 55.0, "set", 1.0, 0.09, -1);
                    })
                    .attributeWithLevels("minecraft:generic.attack_damage", levels -> {
                        levels.addLevel(0, 1.0, 2.0, "set", 1.0, 0.1, 50)
                                .addLevel(65, 1.5, 2.5, "set", 1.0, 0.1, -1)
                                .addLevel(90, 2.5, 3.0, "set", 1.0, 0.1, -1);
                    })
                    .attributeWithLevels("the_vault:generic.crit_chance", levels -> {
                        levels.addLevel(0, 0.05, 0.1, "set", 1.0, 0.0, 49)
                                .addLevel(50, 0.1, 0.15, "set", 1.0, 0.0, 84)
                                .addLevel(85, 0.15, 0.2, "set", 1.0, 0.0, -1);
                    })
                    .attributeWithLevels("the_vault:generic.crit_multiplier", levels -> {
                        levels.addLevel(0, 1.2, 1.2, "set", 1.0, 0.0, 49)
                                .addLevel(50, 1.3, 1.3, "set", 1.0, 0.0, 84)
                                .addLevel(85, 1.4, 1.4, "set", 1.0, 0.0, -1);
                    })
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Barnyard");
                    }, 30, descriptions -> {
                        descriptions.add(JsonDescription.simple("Gobble-gobble!", "$text"));
                    });
        });

        add(iskallia.vault.init.ModEntities.VAULT_WRAITH_YELLOW.getRegistryName(), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(70)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 100, 200, "set", 1.0, 0.067, 49)
                                .addLevel(35, 200, 300, "set", 1.0, 0.077, 64)
                                .addLevel(65, 300.0, 375.0, "set", 1.0, 0.077, -1)
                                .addLevel(80, 330, 410.0, "set", 1.0, 0.077, -1)
                                .addLevel(90, 360.0, 450.0, "set", 1.0, 0.077, -1);
                    })
                    .attributeWithLevels("minecraft:generic.attack_damage", levels -> {
                        levels.addLevel(0, 4.0, 7.0, "set", 1.0, 0.1, 50)
                                .addLevel(65, 5.0, 8.0, "set", 1.0, 0.1, -1)
                                .addLevel(90, 7.0, 12.0, "set", 1.0, 0.1, -1);
                    })
                    .attributeWithLevels("the_vault:generic.crit_chance", levels -> {
                        levels.addLevel(0, 0.05, 0.1, "set", 1.0, 0.0, 49)
                                .addLevel(50, 0.1, 0.15, "set", 1.0, 0.0, 84)
                                .addLevel(85, 0.15, 0.2, "set", 1.0, 0.0, -1);
                    })
                    .attributeWithLevels("the_vault:generic.crit_multiplier", levels -> {
                        levels.addLevel(0, 1.2, 1.2, "set", 1.0, 0.0, 49)
                                .addLevel(50, 1.3, 1.3, "set", 1.0, 0.0, 84)
                                .addLevel(85, 1.4, 1.4, "set", 1.0, 0.0, -1);
                    })
                    .attributeSimple("minecraft:generic.movement_speed", 0.15, 0.2, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Raw Cave");
                        themes.add("Andesite Cave");
                        themes.add("Geode Cave");
                        themes.add("Lush Cave");
                        themes.add("Living Cave");
                        themes.add("Wasteland Cave");
                        themes.add("Deepslate Cave");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple("Watch out for this slow rolling killer!", "$text"));
                    });
        });

        add(new ResourceLocation("dungeons_mobs", "windcaller"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("illagers"))
                    .xpValue(40)
                    .attributeSimple("the_vault:generic.crit_chance", 0.25, 0.25, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 2.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 70.0, 90.0, "set", 1.0, 0.3, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 1.2, 1.2, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Brutal Raid");
                    }, 100, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("dungeons_mobs", "royal_guard"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("illagers"))
                    .xpValue(40)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.1, 1.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 6.0, 8.0, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.9, 0.9, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 250.0, 300.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.armor", 8.0, 8.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 1.2, 1.2, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Brutal Raid");
                    }, 100, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("dungeons_mobs", "mountaineer"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("illagers"))
                    .xpValue(40)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 2.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.9, 0.9, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 90.0, 140.0, "set", 1.0, 0.4, -1)
                    .attributeSimple("minecraft:generic.armor", 3.0, 3.0, "set", 1.0, 0.05, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 1.0, 1.0, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Brutal Raid");
                    }, 100, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("dungeons_mobs", "geomancer"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("illagers"))
                    .xpValue(40)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 2.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.9, 0.9, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 90.0, 140.0, "set", 1.0, 0.3, -1)
                    .attributeSimple("minecraft:generic.armor", 4.0, 4.0, "set", 1.0, 0.05, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 1.0, 1.0, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Brutal Raid");
                    }, 100, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("dungeons_mobs", "mage"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("illagers"))
                    .xpValue(40)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 2.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.0, 5.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.5, 0.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 50.0, 70.0, "set", 1.0, 0.3, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 1.0, 1.0, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Brutal Raid");
                    }, 100, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "incinerator"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("illagers")).entityGroup(VaultMod.id("tank"))
                    .xpValue(40)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 2.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.0, 5.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.5, 0.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 50.0, 70.0, "set", 1.0, 0.3, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 1.0, 1.0, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Brutal Raid");
                    }, 100, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "inquisitor"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("illagers"))
                    .xpValue(40)
                    .attributeSimple("the_vault:generic.crit_chance", 0.25, 0.25, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.6, 1.6, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.5, 5.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 80.0, 110.0, "set", 1.0, 0.11, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 1.25, 1.5, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Brutal Raid");
                    }, 100, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("dungeons_mobs", "iceologer"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("illagers"))
                    .xpValue(40)
                    .attributeSimple("the_vault:generic.crit_chance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.4, 1.4, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 5.0, 7.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.5, 0.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 300.0, 400.0, "set", 1.0, 0.075, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 1.05, 1.05, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Brutal Raid");
                    }, 100, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "matango"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(100)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.25, 1.25, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.0, 3.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.5, 0.75, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 220.0, 280.0, "set", 1.0, 0.12, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "selkie"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("horde"))
                    .xpValue(100)
                    .attributeSimple("the_vault:generic.crit_chance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.25, 1.25, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 2.0, 3.5, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 70.0, 90.0, "set", 1.0, 0.07, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Unused");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "yuki_onna"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(100)
                    .attributeSimple("the_vault:generic.crit_chance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.25, 1.25, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.0, 4.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 90.0, 120.0, "set", 1.0, 0.09, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Unused");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "succubus"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(100)
                    .attributeSimple("the_vault:generic.crit_chance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.25, 1.25, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.0, 5.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 120.0, 140.0, "set", 1.0, 0.09, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Unused");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });


        add(new ResourceLocation("grimoireofgaia", "cecaelia"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(100)
                    .attributeSimple("the_vault:generic.crit_chance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 4.0, 6.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 135.0, 175.0, "set", 1.0, 0.09, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Unused");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("wildbackport", "warden"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(250)
                    .attributeSimple("the_vault:generic.crit_chance", 0.05, 0.05, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.1, 1.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 4.0, 8.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.7, 0.7, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 200.0, 300.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Void");
                        themes.add("Warden's Garden Room");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "sharko"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(125)
                    .attributeSimple("the_vault:generic.crit_chance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 2.0, 3.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.8, 0.8, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 150.0, 220.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Amalgam Beach");
                    }, 100, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "toad"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(100)
                    .attributeSimple("the_vault:generic.crit_chance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.0, 3.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.4, 0.4, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 140.0, 190.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Unused");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "oni"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(100)
                    .attributeSimple("the_vault:generic.crit_chance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.0, 3.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 110.0, 150.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Unused");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "siren"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(100)
                    .attributeSimple("the_vault:generic.crit_chance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 6.0, 8.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 110.0, 150.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.25, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Unused");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "naga"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(150)
                    .attributeSimple("the_vault:generic.crit_chance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 5.0, 7.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 130.0, 150.0, "set", 1.0, 0.085, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.6, 0.8, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Labyrinth Room");
                    }, 70, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "kobold"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("horde"))
                    .xpValue(100)
                    .attributeSimple("the_vault:generic.crit_chance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 4.0, 4.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 80.0, 110.0, "set", 1.0, 0.085, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.8, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Unused");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "mermaid"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("horde"))
                    .xpValue(100)
                    .attributeSimple("the_vault:generic.crit_chance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 4.0, 4.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 80.0, 110.0, "set", 1.0, 0.085, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.8, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "werecat"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(100)
                    .attributeSimple("the_vault:generic.crit_chance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier",2.0, 2.0, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 4.0, 4.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 75.0, 100.0, "set", 1.0, 0.085, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Unused");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "anubis"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(100)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier",1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.0, 5.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.65, 0.65, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 95.0, 125.0, "set", 1.0, 0.11, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Unused");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "sphinx"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(125)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier",1.4, 1.4, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.0, 5.5, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.65, 0.65, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 150.0, 255.0, "set", 1.0, 0.11, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Amalgam Desert");
                    }, 100, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "ant"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("horde"))
                    .xpValue(90)
                    .attributeSimple("the_vault:generic.crit_chance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.0, 3.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 75.0, 100.0, "set", 1.0, 0.085, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.8, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Amalgam Desert");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "mandragora"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("horde"))
                    .xpValue(100)
                    .attributeSimple("the_vault:generic.crit_chance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.0, 3.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 75.0, 100.0, "set", 1.0, 0.085, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.8, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Unused");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "bone_knight"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(95)
                    .attributeSimple("the_vault:generic.crit_chance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 4.0, 6.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 100.0, 130.0, "set", 1.0, 0.07, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.7, 0.9, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Dark Cavern");
                        themes.add("Graveyard Room");
                    }, 50, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(WoldsVaults.id("haturkin"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(125)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.25, 1.25, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 2.0, 4.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.3, 0.5, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 42, 50, "set", 1.0, 0.09, 49)
                                .addLevel(35, 55, 100, "set", 1.0, 0.09, 64)
                                .addLevel(65, 100, 130, "set", 1.0, 0.09, -1)
                                .addLevel(80, 140, 170, "set", 1.0, 0.09, -1)
                                .addLevel(90, 170, 200, "set", 1.0, 0.09, -1);
                    })
                    .attributeSimple("minecraft:generic.movement_speed", 1.0, 1.2, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Harvest");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("minecraft", "zombie_villager"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(90)
                    .attributeSimple("the_vault:generic.crit_chance", 0.05, 0.05, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.25, 1.25, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 1.0, 3.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.5, 0.5, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 42, 50, "set", 1.0, 0.09, 49)
                                .addLevel(35, 55, 100, "set", 1.0, 0.09, 64)
                                .addLevel(65, 100, 130, "set", 1.0, 0.09, -1)
                                .addLevel(80, 140, 170, "set", 1.0, 0.09, -1)
                                .addLevel(90, 170, 200, "set", 1.0, 0.09, -1);
                    })
                    .attributeSimple("minecraft:generic.movement_speed", 1.0, 1.2, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Harvest");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(WoldsVaults.id("hostile_pig"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("horde"))
                    .xpValue(55)
                    .attributeSimple("the_vault:generic.crit_chance", 0.05, 0.05, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.25, 1.25, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 1.0, 3.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.5, 0.5, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 25, 35, "set", 1.0, 0.09, 49)
                                .addLevel(35, 35, 55, "set", 1.0, 0.09, 64)
                                .addLevel(65, 55, 65, "set", 1.0, 0.09, -1)
                                .addLevel(80, 65, 90, "set", 1.0, 0.09, -1)
                                .addLevel(90, 90, 140, "set", 1.0, 0.09, -1);
                    })
                    .attributeSimple("minecraft:generic.movement_speed", 1.0, 1.2, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Barnyard");
                    }, 30, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(VaultMod.id("vault_fighter_7"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("fighter"))
                    .xpValue(140)
                    .attributeSimple("the_vault:generic.crit_chance", 0.15, 0.3, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.6, 1.6, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 4.0, 8.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.5, 0.75, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 140.0, 200.0, "set", 1.0, 0.12, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 1.2, 1.45, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Amalgam Void");
                        themes.add("Amalgam Nether");
                    }, 100, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(VaultMod.id("vault_fighter_5"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("fighter"))
                    .xpValue(500)
                    .attributeSimple("the_vault:generic.crit_chance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 2.0, 2.0, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 10.0, 14.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.75, 0.75, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 600.0, 825.0, "set", 1.0, 0.12, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 1.24, 1.5, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("???");
                    }, 100, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(VaultMod.id("vault_fighter_6"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("fighter"))
                    .xpValue(1000)
                    .attributeSimple("the_vault:generic.crit_chance", 0.5, 0.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 2.5, 2.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 14.0, 20.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 1.0, 1.0, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 1200.0, 1824.0, "set", 1.0, 0.12, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 1.48, 2.0, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("???");
                    }, 100, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "goblin_feral"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("horde"))
                    .xpValue(85)
                    .attributeSimple("the_vault:generic.crit_chance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.0, 3.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 80, 90, "set", 1.0, 0.07, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.7, 0.9, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Amalgam Cave");
                        themes.add("Dark Cavern");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("tropicraft", "ashen"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(80)
                    .attributeSimple("the_vault:generic.crit_chance", 0.75, 0.75, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 2.0, 2.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 20, 30, "set", 1.0, 0.09, 49)
                                .addLevel(35, 30, 40, "set", 1.0, 0.09, 64)
                                .addLevel(65, 40, 50, "set", 1.0, 0.09, -1)
                                .addLevel(80, 50, 65, "set", 1.0, 0.09, -1)
                                .addLevel(90, 65, 90, "set", 1.0, 0.09, -1);
                    })
                    .attributeSimple("minecraft:generic.movement_speed", 1.2, 1.2, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Tropical Oasis");
                    }, 30, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });


        add(new ResourceLocation("tropicraft", "tropi_spider"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 2.0, 2.5, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 30, 35, "set", 1.0, 0.09, 49)
                                .addLevel(35, 35, 45, "set", 1.0, 0.09, 64)
                                .addLevel(65, 45, 55, "set", 1.0, 0.09, -1)
                                .addLevel(80, 55, 65, "set", 1.0, 0.09, -1)
                                .addLevel(90, 70, 95, "set", 1.0, 0.09, -1);
                    })
                    .attributeSimple("minecraft:generic.movement_speed", 1.2, 1.2, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Tropical Oasis");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "flesh_lich"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(100)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 4.0, 5, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.33, 0.66, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 40, 55, "set", 1.0, 0.09, 49)
                                .addLevel(35, 59, 85, "set", 1.0, 0.09, 64)
                                .addLevel(65, 90, 125, "set", 1.0, 0.09, -1)
                                .addLevel(80, 125, 165, "set", 1.0, 0.09, -1)
                                .addLevel(90, 165, 210, "set", 1.0, 0.09, -1);
                    })
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Dark Cavern");
                        themes.add("Graveyard Room");
                    }, 50, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "cobblestone_golem"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(125)
                    .attributeSimple("the_vault:generic.crit_chance", 0.25, 0.25, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 4.0, 8.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.9, 0.9, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 80, 100, "set", 1.0, 0.09, 49)
                                .addLevel(35, 100, 120, "set", 1.0, 0.09, 64)
                                .addLevel(65, 120, 140, "set", 1.0, 0.09, -1)
                                .addLevel(80, 140, 180, "set", 1.0, 0.09, -1)
                                .addLevel(90, 180, 260, "set", 1.0, 0.09, -1);
                    })
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Amalgam Cave");
                        themes.add("Dark Caverns");
                    }, 50, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "cobble_golem"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.25, 0.25, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 4.0, 8.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.9, 0.9, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 40, 60, "set", 1.0, 0.09, 49)
                                .addLevel(35, 60, 80, "set", 1.0, 0.09, 64)
                                .addLevel(65, 80, 100, "set", 1.0, 0.09, -1)
                                .addLevel(80, 100, 140, "set", 1.0, 0.09, -1)
                                .addLevel(90, 140, 170, "set", 1.0, 0.09, -1);
                    })
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Amalgam Cave");
                        themes.add("Dark Caverns");
                    }, 50, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("tropicraft", "tropiskelly"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 2.0, 4.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 55.0, 85.0, "set", 1.0, 0.07, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 1.2, 1.2, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Tropical Oasis");
                    }, 30, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("tropicraft", "eih"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(100)
                    .attributeSimple("the_vault:generic.crit_chance", 0.0, 0.0, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 0.0, 0.0, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 5.0, 6.0, "set", 1.0, 0.05, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.85, 0.85, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 68, 81, "set", 1.0, 0.14, 49)
                                .addLevel(35, 80, 100, "set", 1.0, 0.14, 64)
                                .addLevel(65, 100, 130, "set", 1.0, 0.14, -1)
                                .addLevel(80, 135, 155, "set", 1.0, 0.14, -1)
                                .addLevel(90, 160, 200, "set", 1.0, 0.14, -1);
                    })
                    .attributeSimple("minecraft:generic.movement_speed", 0.8, 0.8, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Tropical Oasis");
                    }, 30, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "minotaurus"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.75, 2.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 8.0, 10.0, "set", 1.0, 0.05, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.5, 0.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 110, 150.0, "set", 1.0, 0.13, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 1.1, 1.3, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Unused");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "minotaur"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(125)
                    .attributeSimple("the_vault:generic.crit_chance", 0.05, 0.05, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.75, 2.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 6.0, 8.0, "set", 1.0, 0.05, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.75, 0.75, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 125, 200.0, "set", 1.0, 0.13, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.85, 1.3, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Labyrinth Room");
                    }, 70, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });


        add(new ResourceLocation("alexsmobs", "centipede_head"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(100)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.4, 1.4, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 2.0, 3.0, "set", 1.0, 0.05, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.75, 0.75, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 52, 75, "set", 1.0, 0.14, 49)
                                .addLevel(35, 50, 90, "set", 1.0, 0.14, 64)
                                .addLevel(65, 70, 100, "set", 1.0, 0.14, -1)
                                .addLevel(80, 90, 130, "set", 1.0, 0.14, -1)
                                .addLevel(90, 120, 170, "set", 1.0, 0.14, -1);
                    })
                    .attributeSimple("minecraft:generic.movement_speed", 0.95, 1.3, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });


        add(new ResourceLocation("cloudstorage", "bloviator"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(100)
                    .attributeSimple("the_vault:generic.crit_chance", 0.0, 0.0, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 0.0, 0.0, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 0.0, 0.0, "set", 1.0, 0.05, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.0, 0.0, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 18, 25, "set", 1.0, 0.14, 49)
                                .addLevel(35, 25, 40, "set", 1.0, 0.14, 64)
                                .addLevel(65, 41, 60, "set", 1.0, 0.14, -1)
                                .addLevel(80, 61, 80, "set", 1.0, 0.14, -1)
                                .addLevel(90, 81, 90, "set", 1.0, 0.14, -1);
                    })
                    .attributeSimple("minecraft:generic.movement_speed", 1.0, 1.0, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Enchanted Elixir");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("alexsmobs", "crocodile"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(80)
                    .attributeSimple("the_vault:generic.crit_chance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.6, 1.6, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.0, 3.0, "set", 1.0, 0.05, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.3, 0.3, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 22, 30, "set", 1.0, 0.09, 49)
                                .addLevel(35, 30, 45, "set", 1.0, 0.09, 64)
                                .addLevel(65, 46, 55, "set", 1.0, 0.09, -1)
                                .addLevel(80, 55, 70, "set", 1.0, 0.09, -1)
                                .addLevel(90, 71, 95, "set", 1.0, 0.09, -1);
                    })
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Enchanted Elixir");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("alexsmobs", "gorilla"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(80)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.8, 1.8, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 4.0, 6.0, "set", 1.0, 0.05, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.6, 0.6, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 36, 54, "set", 1.0, 0.09, 49)
                                .addLevel(35, 55, 70, "set", 1.0, 0.09, 64)
                                .addLevel(65, 71, 80, "set", 1.0, 0.09, -1)
                                .addLevel(80, 81, 110, "set", 1.0, 0.09, -1)
                                .addLevel(90, 111, 135, "set", 1.0, 0.09, -1);
                    })
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Enchanted Elixir");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("alexsmobs", "tiger"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(110)
                    .attributeSimple("the_vault:generic.crit_chance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 2.0, 2.0, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 5.0, 5.0, "set", 1.0, 0.05, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 18, 30, "set", 1.0, 0.09, 49)
                                .addLevel(35, 31, 40, "set", 1.0, 0.09, 64)
                                .addLevel(65, 40, 62, "set", 1.0, 0.09, -1)
                                .addLevel(80, 62, 80, "set", 1.0, 0.09, -1)
                                .addLevel(90, 81, 100, "set", 1.0, 0.09, -1);
                    })
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Enchanted Elixir");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("alexsmobs", "dropbear"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(110)
                    .attributeSimple("the_vault:generic.crit_chance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 2.0, 2.0, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 5.0, 5.0, "set", 1.0, 0.05, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 18, 30, "set", 1.0, 0.09, 49)
                                .addLevel(35, 31, 40, "set", 1.0, 0.09, 64)
                                .addLevel(65, 40, 62, "set", 1.0, 0.09, -1)
                                .addLevel(80, 62, 80, "set", 1.0, 0.09, -1)
                                .addLevel(90, 81, 100, "set", 1.0, 0.09, -1);
                    })
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Enchanted Elixir");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("alexsmobs", "snow_leopard"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(125)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 4.0, 4.0, "set", 1.0, 0.05, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 18, 30, "set", 1.0, 0.09, 49)
                                .addLevel(35, 31, 40, "set", 1.0, 0.09, 64)
                                .addLevel(65, 40, 62, "set", 1.0, 0.09, -1)
                                .addLevel(80, 62, 80, "set", 1.0, 0.09, -1)
                                .addLevel(90, 81, 100, "set", 1.0, 0.09, -1);
                    })
                    .attributeSimple("minecraft:generic.movement_speed", 1.1, 1.3, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Enchanted Elixir");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("alexsmobs", "soul_vulture"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(85)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 4.0, 4.0, "set", 1.0, 0.05, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 18, 30, "set", 1.0, 0.09, 49)
                                .addLevel(35, 31, 40, "set", 1.0, 0.09, 64)
                                .addLevel(65, 40, 62, "set", 1.0, 0.09, -1)
                                .addLevel(80, 62, 80, "set", 1.0, 0.09, -1)
                                .addLevel(90, 81, 100, "set", 1.0, 0.09, -1);
                    })
                    .attributeSimple("minecraft:generic.movement_speed", 1.1, 1.3, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Enchanted Elixir");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("alexsmobs", "komodo_dragon"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(80)
                    .attributeSimple("the_vault:generic.crit_chance", 0.35, 0.35, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.8, 1.8, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 2.0, 3.5, "set", 1.0, 0.05, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.6, 0.6, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 28, 48, "set", 1.0, 0.09, 49)
                                .addLevel(35, 49, 69, "set", 1.0, 0.09, 64)
                                .addLevel(65, 70, 90, "set", 1.0, 0.09, -1)
                                .addLevel(80, 91, 101, "set", 1.0, 0.09, -1)
                                .addLevel(90, 101, 110, "set", 1.0, 0.09, -1);
                    })
                    .attributeSimple("minecraft:generic.movement_speed", 0.85, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Enchanted Elixir");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("alexsmobs", "enderiophage"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(125)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 2.0, 3.5, "set", 1.0, 0.05, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 36, 54, "set", 1.0, 0.09, 49)
                                .addLevel(35, 55, 70, "set", 1.0, 0.09, 64)
                                .addLevel(65, 71, 85, "set", 1.0, 0.09, -1)
                                .addLevel(80, 85, 120, "set", 1.0, 0.09, -1)
                                .addLevel(90, 120, 150, "set", 1.0, 0.09, -1);
                    })
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Enchanted Elixir");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("alexsmobs", "mungus"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("horde"))
                    .xpValue(100)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 2.0, 3.5, "set", 1.0, 0.05, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 9, 16, "set", 1.0, 0.09, 49)
                                .addLevel(35, 16, 22, "set", 1.0, 0.09, 64)
                                .addLevel(65, 22, 25, "set", 1.0, 0.09, -1)
                                .addLevel(80, 25, 33, "set", 1.0, 0.09, -1)
                                .addLevel(90, 33, 40, "set", 1.0, 0.09, -1);
                    })
                    .attributeSimple("minecraft:generic.movement_speed", 1.1, 1.3, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Enchanted Elixir");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(WoldsVaults.id("yellow_ghost"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(80)
                    .attributeSimple("the_vault:generic.crit_chance", 0.25, 0.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 3.0, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 2.0, 3.5, "set", 1.0, 0.05, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.0, 0.0, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 20, 40, "set", 1.0, 0.09, 49)
                                .addLevel(35, 30, 55, "set", 1.0, 0.09, 64)
                                .addLevel(65, 50, 60, "set", 1.0, 0.09, -1);
                    })
                    .attributeSimple("minecraft:generic.movement_speed", 1.5, 2.0, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Haunted Braziers");
                        themes.add("Graveyard Room");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(WoldsVaults.id("red_ghost"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(90)
                    .attributeSimple("the_vault:generic.crit_chance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.5, 4.5, "set", 1.0, 0.05, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 20, 40, "set", 1.0, 0.09, 49)
                                .addLevel(35, 60, 120, "set", 1.0, 0.09, 64)
                                .addLevel(65, 120, 150, "set", 1.0, 0.09, -1);
                    })
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.2, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Haunted Braziers");
                        themes.add("Graveyard Room");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(WoldsVaults.id("purple_ghost"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(100)
                    .attributeSimple("the_vault:generic.crit_chance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 4.0, 4.0, "set", 1.0, 0.05, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.0, 0.0, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 20, 40, "set", 1.0, 0.09, 49)
                                .addLevel(35, 60, 120, "set", 1.0, 0.09, 64)
                                .addLevel(65, 120, 150, "set", 1.0, 0.09, -1);
                    })
                    .attributeSimple("minecraft:generic.movement_speed", 1.05, 1.2, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Haunted Braziers");
                        themes.add("Graveyard Room");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(WoldsVaults.id("green_ghost"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(80)
                    .attributeSimple("the_vault:generic.crit_chance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 1.0, 2.0, "set", 1.0, 0.05, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.0, 0.0, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 20, 40, "set", 1.0, 0.09, 49)
                                .addLevel(35, 35, 55, "set", 1.0, 0.09, 64)
                                .addLevel(65, 75, 115, "set", 1.0, 0.09, -1);
                    })
                    .attributeSimple("minecraft:generic.movement_speed", 1.05, 1.2, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Haunted Braziers");
                        themes.add("Graveyard Room");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(WoldsVaults.id("dark_gray_ghost"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(100)
                    .attributeSimple("the_vault:generic.crit_chance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 4.0, 4.0, "set", 1.0, 0.05, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.5, 0.5, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 20, 40, "set", 1.0, 0.09, 49)
                                .addLevel(35, 65, 125, "set", 1.0, 0.09, 64)
                                .addLevel(65, 125, 190, "set", 1.0, 0.09, -1);
                    })
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Haunted Braziers");
                        themes.add("Graveyard Room");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(WoldsVaults.id("dark_red_ghost"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(110)
                    .attributeSimple("the_vault:generic.crit_chance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 4.0, 4.0, "set", 1.0, 0.05, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.5, 0.5, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 20, 40, "set", 1.0, 0.09, 49)
                                .addLevel(35, 65, 125, "set", 1.0, 0.09, 64)
                                .addLevel(65, 125, 190, "set", 1.0, 0.09, -1);
                    })
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Haunted Braziers");
                        themes.add("Graveyard Room");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(WoldsVaults.id("dark_blue_ghost"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(90)
                    .attributeSimple("the_vault:generic.crit_chance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.0, 3.0, "set", 1.0, 0.05, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.25, 0.25, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 20, 40, "set", 1.0, 0.09, 49)
                                .addLevel(35, 60, 90, "set", 1.0, 0.09, 64)
                                .addLevel(65, 90, 120, "set", 1.0, 0.09, -1);
                    })
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Haunted Braziers");
                        themes.add("Graveyard Room");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(WoldsVaults.id("brown_ghost"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(100)
                    .attributeSimple("the_vault:generic.crit_chance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.0, 3.0, "set", 1.0, 0.05, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.0, 0.0, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 20, 40, "set", 1.0, 0.09, 49)
                                .addLevel(35, 45, 80, "set", 1.0, 0.09, 64)
                                .addLevel(65, 90, 120, "set", 1.0, 0.09, -1);
                    })
                    .attributeSimple("minecraft:generic.movement_speed", 0.75, 1.25, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Haunted Braziers");
                        themes.add("Graveyard Room");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(WoldsVaults.id("blue_ghost"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(80)
                    .attributeSimple("the_vault:generic.crit_chance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.0, 3.0, "set", 1.0, 0.05, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.0, 0.0, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 20, 40, "set", 1.0, 0.09, 49)
                                .addLevel(35, 45, 80, "set", 1.0, 0.09, 64)
                                .addLevel(65, 90, 120, "set", 1.0, 0.09, -1);
                    })
                    .attributeSimple("minecraft:generic.movement_speed", 0.75, 1.25, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Haunted Braziers");
                        themes.add("Graveyard Room");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });


        add(WoldsVaults.id("black_ghost"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(100)
                    .attributeSimple("the_vault:generic.crit_chance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.0, 3.0, "set", 1.0, 0.05, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.0, 0.0, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 20, 40, "set", 1.0, 0.09, 49)
                                .addLevel(35, 45, 80, "set", 1.0, 0.09, 64)
                                .addLevel(65, 90, 120, "set", 1.0, 0.09, -1);
                    })
                    .attributeSimple("minecraft:generic.movement_speed", 0.75, 1.25, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Haunted Braziers");
                        themes.add("Graveyard Room");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(VaultMod.id("wold"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("elite"))
                    .xpValue(250)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.25, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 2.0, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 4.0, 8.0, "set", 1.0, 0.05, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.85, 0.85, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 450, 500, "set", 1.0, 0.25, 49)
                                .addLevel(50, 510, 550, "set", 1.0, 0.25, 64)
                                .addLevel(90, 550, 650, "set", 1.0, 0.25, -1);
                    })
                    .attributeSimple("minecraft:generic.movement_speed", 1.0, 1.25, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.indirect_tp_chance", 0.1, 0.3, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.tp_range", 4.0, 16.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Brutal Bosses");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(VaultMod.id("vault_dood"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("elite"))
                    .xpValue(125)
                    .attributeSimple("the_vault:generic.crit_chance", 0.15, 0.4, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 3.0, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.0, 6.0, "set", 1.0, 0.05, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.85, 0.85, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 175, 300, "set", 1.0, 0.25, 49)
                                .addLevel(50, 250, 325, "set", 1.0, 0.25, 64)
                                .addLevel(90, 300, 400, "set", 1.0, 0.25, -1);
                    })
                    .attributeSimple("minecraft:generic.movement_speed", 1.0, 1.25, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Brutal Bosses");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });


        add(new ResourceLocation("dungeons_mobs", "redstone_cube"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(100)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.25, 1.25, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.0, 4.0, "set", 1.0, 0.05, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.5, 0.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 150.0, 300.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 1.2, 1.4, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Unused");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("dungeons_mobs", "redstone_golem"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.75, 1.75, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 6.0, 8.0, "set", 1.0, 0.05, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.95, 0.95, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 500.0, 750.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.armor", 4.0, 4.0, "set", 1.0, 0.05, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.8, 0.8, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Amalgam Nether");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("dungeons_mobs", "skeleton_vanguard"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(110)
                    .attributeSimple("the_vault:generic.crit_chance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.35, 1.35, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 6.0, 6.0, "set", 1.0, 0.05, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.6, 0.6, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 300.0, 400.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.armor", 2.0, 2.0, "set", 1.0, 0.03, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Unused");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("dungeons_mobs", "leapleaf"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(115)
                    .attributeSimple("the_vault:generic.crit_chance", 0.25, 0.25, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.2, 1.2,"set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 5.0, 6.0, "set", 1.0, 0.05, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.5, 0.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 400.0, 500.0, "set", 1.0, 0.09, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 0.9, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Amalgam Cave");
                    }, 100, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("dungeons_mobs", "sunken_skeleton"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("horde"))
                    .xpValue(80)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.25, 1.25,"set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 4.0, 5.0, "set", 1.0, 0.07, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 100.0, 140.0, "set", 1.0, 0.07, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 1.0, 1.0, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Amalgam Beach");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("dungeons_mobs", "mossy_skeleton"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(80)
                    .attributeSimple("the_vault:generic.crit_chance", 0.35, 0.35, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5,"set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 4.5, 4.5, "set", 1.0, 0.07, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 90.0, 110.0, "set", 1.0, 0.07, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 1.0, 1.0, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Unused");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("dungeons_mobs", "whisperer"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(120)
                    .attributeSimple("the_vault:generic.crit_chance", 0.25, 0.25, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5,"set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 5.0, 5.0, "set", 1.0, 0.07, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 140.0, 160.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 1.1, 1.2, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Amalgam Cave");
                    }, 100, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("dungeons_mobs", "jungle_zombie"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("horde"))
                    .xpValue(80)
                    .attributeSimple("the_vault:generic.crit_chance", 0.25, 0.25, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5,"set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 4.0, 5.0, "set", 1.0, 0.07, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 80.0, 110.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 1.1, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Amalgam Cave");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("dungeons_mobs", "frozen_zombie"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("horde"))
                    .xpValue(90)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.2, 1.2,"set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 4.0, 4.0, "set", 1.0, 0.07, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.6, 0.6, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 125.0, 160.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.armor", 1.5, 3.0, "set", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Amalgam Ice");
                    }, 100, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("dungeons_mobs", "drowned_necromancer"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(125)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5,"set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.0, 3.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.4, 0.4, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 230.0, 300.0, "set", 1.0, 0.12, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Amalgam Beach");
                    }, 100, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("dungeons_mobs", "necromancer"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(125)
                    .attributeSimple("the_vault:generic.crit_chance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5,"set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 4.0, 4.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.25, 0.25, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 60, 80, "set", 1.0, 0.14, 49)
                                .addLevel(35, 80, 100, "set", 1.0, 0.14, 64)
                                .addLevel(65, 100, 130, "set", 1.0, 0.14, -1)
                                .addLevel(80, 165, 200, "set", 1.0, 0.14, -1)
                                .addLevel(90, 200, 240, "set", 1.0, 0.14, -1);
                    })
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Amalgam Cave");
                        themes.add("Amalgam Beach");
                        themes.add("Dark Caverns");
                    }, 100, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "banshee"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(125)
                    .attributeSimple("the_vault:generic.crit_chance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5,"set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 1.0, 6.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.4, 0.4, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 40, 80, "set", 1.0, 0.08, 49)
                                .addLevel(35, 80, 90, "set", 1.0, 0.08, 64)
                                .addLevel(65, 90, 130, "set", 1.0, 0.08, -1)
                                .addLevel(80, 130, 150, "set", 1.0, 0.08, -1)
                                .addLevel(90, 150, 190, "set", 1.0, 0.08, -1);
                    })
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Labyrinth Room");
                        themes.add("Amalgam Void");
                    }, 70, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "mummy"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.0, 0.0, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.0, 4.0, "set", 1.0, 0.2, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.95, 0.95, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 700.0, 900.0, "set", 1.0, 0.06, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.2, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("rottencreatures", "frostbitten"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("horde"))
                    .xpValue(80)
                    .attributeSimple("the_vault:generic.crit_chance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5,"set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 2.0, 3.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.25, 0.25, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 65.0, 100.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.2, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Amalgam Ice");
                    }, 100, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "wither_cow"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(80)
                    .attributeSimple("the_vault:generic.crit_chance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.15, 1.15,"set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 2.0, 4.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.95, 0.95, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 160.0, 224.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.2, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Amalgam Nether");
                        themes.add("Amalgam Void");
                        themes.add("Labyrinth Room");
                    }, 70, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });


        add(new ResourceLocation("dungeons_mobs", "icy_creeper"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 2.0, 3.0, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 25.0, 35.0, "set", 1.0, 0.14, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.6, 0.8, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Aurora Cave");
                        themes.add("Ice Cave");
                        themes.add("Ice");
                        themes.add("Winter");
                        themes.add("Tenos");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "creep"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.25, 0.25, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 6.0, 24.0, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.75, 0.75, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 125.0, 250.0, "set", 1.0, 0.14, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 1.2, 1.5, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Unused");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(ModEntities.CRANBERRY_SLIME.getRegistryName(), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(45)
                    .attributeSimple("the_vault:generic.crit_chance", 0.0, 0.0, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 1.0, 2.0, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 12, 18, "set", 1.0, 0.07, 49)
                                .addLevel(50, 18, 40, "set", 1.0, 0.07, 64)
                                .addLevel(65, 30, 45, "set", 1.0, 0.07, 64)
                                .addLevel(80, 35, 52, "set", 1.0, 0.07, -1)
                                .addLevel(90, 40, 60, "set", 1.0, 0.07, -1);
                    })
                    .attributeSimple("minecraft:generic.movement_speed", 1.25, 1.25, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Harvest");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("tropicraft", "tropicreeper"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 2.0, 3.0, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 25.0, 35.0, "set", 1.0, 0.14, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 1.06, 1.25, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Tropical Oasis");
                    }, 30, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("minecraft", "guardian"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("guardian"))
                    .xpValue(85)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.25, 1.25,"set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 1.0, 3.0, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 20, 40, "set", 1.0, 0.07, 49)
                                .addLevel(35, 30, 55, "set", 1.0, 0.07, 64)
                                .addLevel(65, 60, 75, "set", 1.0, 0.07, -1);
                    })
                    .bestiaryEntry(themes -> {
                        themes.add("Undersea");
                        themes.add("Aquarium Room");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("alexsmobs", "frilled_shark"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.25, 1.25,"set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.0, 4.0, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 20, 40, "set", 1.0, 0.077, 49)
                                .addLevel(35, 30, 55, "set", 1.0, 0.077, 64)
                                .addLevel(65, 60, 75, "set", 1.0, 0.077, -1);
                    })
                    .bestiaryEntry(themes -> {
                        themes.add("Undersea");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("alexsmobs", "orca"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(120)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.25, 1.25,"set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 4.0, 5.0, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 40, 65, "set", 1.0, 0.09, 49)
                                .addLevel(35, 65, 90, "set", 1.0, 0.09, 64)
                                .addLevel(65, 90, 125, "set", 1.0, 0.09, -1);
                    })
                    .bestiaryEntry(themes -> {
                        themes.add("Undersea");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("alexsmobs", "guster"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(65)
                    .attributeSimple("minecraft:generic.attack_damage", 2.0, 3.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 30, 60, "set", 1.0, 0.09, 49)
                                .addLevel(35, 40, 65, "set", 1.0, 0.09, 64)
                                .addLevel(65, 45, 85, "set", 1.0, 0.09, -1);
                    })
                    .bestiaryEntry(themes -> {
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("thermal", "basalz"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(65)
                    .attributeSimple("minecraft:generic.attack_damage", 2.0, 3.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 30, 60, "set", 1.0, 0.09, 49)
                                .addLevel(35, 40, 65, "set", 1.0, 0.09, 64)
                                .addLevel(65, 45, 85, "set", 1.0, 0.09, -1);
                    })
                    .bestiaryEntry(themes -> {
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("thermal", "blitz"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(65)
                    .attributeSimple("minecraft:generic.attack_damage", 2.0, 3.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 30, 60, "set", 1.0, 0.09, 49)
                                .addLevel(35, 40, 65, "set", 1.0, 0.09, 64)
                                .addLevel(65, 45, 85, "set", 1.0, 0.09, -1);
                    })
                    .bestiaryEntry(themes -> {
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("thermal", "blizz"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(65)
                    .attributeSimple("minecraft:generic.attack_damage", 2.0, 3.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 30, 60, "set", 1.0, 0.09, 49)
                                .addLevel(35, 40, 65, "set", 1.0, 0.09, 64)
                                .addLevel(65, 45, 85, "set", 1.0, 0.09, -1);
                    })
                    .bestiaryEntry(themes -> {
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "behender"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(150)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5,"set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 4.0, 8.0, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.9, 0.9, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 400.0, 600.0, "set", 1.0, 0.14, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 0.9, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Labyrinth Room");
                        themes.add("Amalgam Void");
                    }, 70, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "deathword"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("horde"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5,"set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 2.0, 2.0, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.9, 0.9, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 100.0, 100.0, "set", 1.0, 0.4, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 1.25, 1.5, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Unused");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "witch"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(125)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5,"set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 5.0, 7.0, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.6, 0.6, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 450.0,600.0, "set", 1.0, 0.4, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 1.25, 1.25, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Labyrinth Room");
                    }, 70, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

    }

    private static boolean bestiaryContainsEntity(ResourceLocation entityId) {
        return !ModConfigs.BESTIARY.getEntities().stream().filter(entityEntry -> entityEntry.getEntityId().equals(entityId)).toList().isEmpty();
    }
}
