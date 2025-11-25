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
                        descriptions.add(JsonDescription.simpleDescription("Baaaa!", "$text"));
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
                        descriptions.add(JsonDescription.simpleDescription("Gobble-gobble!", "$text"));
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
                        descriptions.add(JsonDescription.simpleDescription("Watch out for this slow rolling killer!", "$text"));
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
