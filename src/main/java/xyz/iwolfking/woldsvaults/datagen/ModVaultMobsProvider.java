package xyz.iwolfking.woldsvaults.datagen;

import com.github.alexthe666.alexsmobs.entity.AMEntityRegistry;
import iskallia.vault.VaultMod;
import iskallia.vault.config.BestiaryConfig;
import iskallia.vault.config.VaultMobsConfig;
import iskallia.vault.core.world.data.entity.PartialEntity;
import iskallia.vault.init.ModConfigs;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.vhapi.api.datagen.AbstractVaultMobsProvider;
import xyz.iwolfking.vhapi.api.util.builder.description.JsonDescription;
import xyz.iwolfking.woldsvaults.init.ModEntities;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.VaultMobsConfigAccessor;

public class ModVaultMobsProvider extends AbstractVaultMobsProvider {
    public ModVaultMobsProvider(DataGenerator generator, String modid) {
        super(generator, modid);
    }

    @Override
    protected void registerOverrides() {
        add(ModEntities.HOSTILE_SHEEP.getRegistryName(), vaultMobBuilder -> {
             vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                     .xpValue(70)
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

        add(iskallia.vault.init.ModEntities.VAULT_WRAITH_YELLOW.getRegistryName(), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(125)
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
                        themes.add("Caves");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple("Watch out for this slow rolling killer!", "$text"));
                    });
        });

        add(new ResourceLocation("dungeons_mobs", "windcaller"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("illagers"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.25, 0.25, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 2.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 70.0, 90.0, "set", 1.0, 0.3, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 1.2, 1.2, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Brutal Raid");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("dungeons_mobs", "royal_guard"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("illagers"))
                    .xpValue(65)
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
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("dungeons_mobs", "mountaineer"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("illagers"))
                    .xpValue(65)
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
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("dungeons_mobs", "geomancer"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("illagers"))
                    .xpValue(65)
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
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("dungeons_mobs", "mage"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("illagers"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 2.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.0, 5.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.5, 0.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 50.0, 70.0, "set", 1.0, 0.3, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 1.0, 1.0, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Brutal Raid");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "incinerator"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("illagers")).entityGroup(VaultMod.id("tank"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 2.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.0, 5.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.5, 0.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 50.0, 70.0, "set", 1.0, 0.3, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 1.0, 1.0, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Brutal Raid");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "inquisitor"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("illagers"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.25, 0.25, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.6, 1.6, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.5, 5.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 80.0, 110.0, "set", 1.0, 0.11, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 1.25, 1.5, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Brutal Raid");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("dungeons_mobs", "iceologer"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("illagers"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.4, 1.4, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 5.0, 7.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.5, 0.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 300.0, 400.0, "set", 1.0, 0.075, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 1.05, 1.05, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Brutal Raid");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "matango"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(65)
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
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.25, 1.25, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 2.0, 3.5, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 70.0, 90.0, "set", 1.0, 0.07, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "yuki_onna"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.25, 1.25, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.0, 4.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 90.0, 120.0, "set", 1.0, 0.09, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "succubus"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.25, 1.25, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.0, 5.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 120.0, 140.0, "set", 1.0, 0.09, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });


        add(new ResourceLocation("grimoireofgaia", "cecaelia"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 4.0, 6.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 135.0, 175.0, "set", 1.0, 0.09, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("wildbackport", "warden"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.05, 0.05, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.1, 1.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 4.0, 8.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.7, 0.7, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 200.0, 300.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "sharko"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 2.0, 3.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.8, 0.8, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 150.0, 220.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "toad"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.0, 3.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.4, 0.4, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 140.0, 190.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "oni"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.0, 3.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 110.0, 150.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "siren"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 6.0, 8.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 110.0, 150.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.25, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "naga"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 5.0, 7.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 130.0, 150.0, "set", 1.0, 0.085, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.6, 0.8, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "kobold"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("horde"))
                    .xpValue(65)
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

        add(new ResourceLocation("grimoireofgaia", "mermaid"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("horde"))
                    .xpValue(65)
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
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier",2.0, 2.0, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 4.0, 4.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 75.0, 100.0, "set", 1.0, 0.085, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "werecat"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier",1.7, 1.7, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 4.5, 6.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 75.0, 100.0, "set", 1.0, 0.085, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "anubis"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier",1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.0, 5.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.65, 0.65, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 95.0, 125.0, "set", 1.0, 0.11, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "sphinx"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier",1.4, 1.4, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.0, 5.5, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.65, 0.65, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 150.0, 255.0, "set", 1.0, 0.11, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "ant"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("horde"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.0, 3.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 75.0, 100.0, "set", 1.0, 0.085, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.8, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "mandragora"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("horde"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.0, 3.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 75.0, 100.0, "set", 1.0, 0.085, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.8, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "bone_knight"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 4.0, 6.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 100.0, 130.0, "set", 1.0, 0.07, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.7, 0.9, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "goblin_feral"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("horde"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.0, 3.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 80, 90, "set", 1.0, 0.07, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.7, 0.9, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("tropicraft", "ashen"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(65)
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
                        themes.add("");
                    }, 0, descriptions -> {
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
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "flesh_lich"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(65)
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
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "cobblestone_golem"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(65)
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
                        themes.add("");
                    }, 0, descriptions -> {
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
                        themes.add("");
                    }, 0, descriptions -> {
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
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });



        add(new ResourceLocation("dungeons_mobs", "redstone_cube"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.25, 1.25, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.0, 4.0, "set", 1.0, 0.05, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.5, 0.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 150.0, 300.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 1.2, 1.4, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("");
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
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("dungeons_mobs", "skeleton_vanguard"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.2, 0.2, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.35, 1.35, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 6.0, 6.0, "set", 1.0, 0.05, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.6, 0.6, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 300.0, 400.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.armor", 2.0, 2.0, "set", 1.0, 0.03, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("dungeons_mobs", "skeleton_vanguard"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.25, 1.25, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 5.0, 5.0, "set", 1.0, 0.05, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.8, 0.8, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 150.0, 180.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 1.15, 1.15, "multiply", 1.0, 0.03, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("dungeons_mobs", "leapleaf"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.25, 0.25, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.2, 1.2,"set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 5.0, 6.0, "set", 1.0, 0.05, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.5, 0.5, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 400.0, 500.0, "set", 1.0, 0.09, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 0.9, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("dungeons_mobs", "sunken_skeleton"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("horde"))
                    .xpValue(65)
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
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.35, 0.35, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5,"set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 4.5, 4.5, "set", 1.0, 0.07, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 90.0, 110.0, "set", 1.0, 0.07, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 1.0, 1.0, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("dungeons_mobs", "whisperer"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.25, 0.25, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5,"set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 5.0, 5.0, "set", 1.0, 0.07, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 140.0, 160.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 1.1, 1.2, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("dungeons_mobs", "jungle_zombie"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("horde"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.25, 0.25, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5,"set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 4.0, 5.0, "set", 1.0, 0.07, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 80.0, 110.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 1.1, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("dungeons_mobs", "frozen_zombie"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("horde"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.2, 1.2,"set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 4.0, 4.0, "set", 1.0, 0.07, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.6, 0.6, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 125.0, 160.0, "set", 1.0, 0.08, -1)
                    .attributeSimple("minecraft:generic.armor", 1.5, 3.0, "set", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Amalgam Ice");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("dungeons_mobs", "drowned_necromancer"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5,"set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 3.0, 3.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.4, 0.4, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 230.0, 300.0, "set", 1.0, 0.12, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("dungeons_mobs", "necromancer"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(65)
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
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "banshee"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(65)
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
                        themes.add("");
                    }, 0, descriptions -> {
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

        add(new ResourceLocation("grimoireofgaia", "wither_cow"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("tank"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.15, 0.15, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.15, 1.15,"set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 2.0, 4.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.95, 0.95, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 160.0, 224.0, "set", 1.0, 0.1, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 1.2, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("");
                    }, 0, descriptions -> {
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
                        themes.add("Ice");
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
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("minecraft", "guardian"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("guardian"))
                    .xpValue(65)
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
                    .xpValue(65)
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
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5,"set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 4.0, 8.0, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.9, 0.9, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 400.0, 600.0, "set", 1.0, 0.14, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 0.9, 0.9, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("");
                    }, 0, descriptions -> {
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
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

        add(new ResourceLocation("grimoireofgaia", "witch"), vaultMobBuilder -> {
            vaultMobBuilder.entityGroup(VaultMod.id("assassin"))
                    .xpValue(65)
                    .attributeSimple("the_vault:generic.crit_chance", 0.1, 0.1, "set", 1.0, 0.0, -1)
                    .attributeSimple("the_vault:generic.crit_multiplier", 1.5, 1.5,"set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.attack_damage", 5.0, 7.0, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.knockback_resistance", 0.6, 0.6, "set", 1.0, 0.0, -1)
                    .attributeSimple("minecraft:generic.max_health", 450.0,600.0, "set", 1.0, 0.4, -1)
                    .attributeSimple("minecraft:generic.movement_speed", 1.25, 1.25, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simple(""));
                    });
        });

//        ModConfigs.VAULT_MOBS = new VaultMobsConfig().readConfig();
//        ModConfigs.BESTIARY = new BestiaryConfig().readConfig();
//
//        ((VaultMobsConfigAccessor)ModConfigs.VAULT_MOBS).getAttributeOverrides().forEach((entityPredicate, attributeOverrides) -> {
//            if(entityPredicate instanceof PartialEntity partialEntity) {
//                if(!bestiaryContainsEntity(partialEntity.getId())) {
//                    add(partialEntity.getId(), vaultMobBuilder -> {
//                        vaultMobBuilder.bestiaryEntry(themes -> {
//                                    themes.add("Not Configured");
//                                }, 0, descriptions -> {
//                                    descriptions.add(JsonDescription.empty());
//                                }
//                        );
//                    });
//                }
//            }
//        });
    }

    private static boolean bestiaryContainsEntity(ResourceLocation entityId) {
        return !ModConfigs.BESTIARY.getEntities().stream().filter(entityEntry -> entityEntry.getEntityId().equals(entityId)).toList().isEmpty();
    }
}
