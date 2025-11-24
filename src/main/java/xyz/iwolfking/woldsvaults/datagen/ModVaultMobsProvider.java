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
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.VaultMobsConfigAccessor;

public class ModVaultMobsProvider extends AbstractVaultMobsProvider {
    public ModVaultMobsProvider(DataGenerator generator, String modid) {
        super(generator, modid);
    }

    @Override
    protected void registerOverrides() {
        add(AMEntityRegistry.GRIZZLY_BEAR.getId(), vaultMobBuilder -> {
            vaultMobBuilder.xpValue(100)
                    .entityGroup(VaultMod.id("horde"))
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
                    .attributeSimple("minecraft:generic.attack_damage", 1.4, 2.0, "set", 1.0, 0.1, -1)
                    .attributeWithLevels("minecraft:generic.max_health", levels -> {
                        levels.addLevel(0, 16.0, 20.0, "set", 1.0, 0.067, 49)
                        .addLevel(50, 18.0, 22.0, "set", 1.0, 0.067, 64)
                        .addLevel(65, 20.0, 28.0, "set", 1.0, 0.067, 89)
                        .addLevel(90, 26.0, 30.0, "set", 1.0, 0.067, -1);
                     })
                    .attributeSimple("minecraft:generic.movement_speed", 1.05, 1.1, "multiply", 1.0, 0.0, -1)
                    .attributeSimple("forge:swim_speed", 5.0, 5.0, "set", 1.0, 0.0, -1)
                    .bestiaryEntry(themes -> {
                        themes.add("Test");
                    }, 0, descriptions -> {
                        descriptions.add(JsonDescription.simpleDescription("Test description", "yellow"));
                    });
        });

        ModConfigs.VAULT_MOBS = new VaultMobsConfig().readConfig();
        ModConfigs.BESTIARY = new BestiaryConfig().readConfig();

        ((VaultMobsConfigAccessor)ModConfigs.VAULT_MOBS).getAttributeOverrides().forEach((entityPredicate, attributeOverrides) -> {
            if(entityPredicate instanceof PartialEntity partialEntity) {
                if(!bestiaryContainsEntity(partialEntity.getId())) {
                    add(partialEntity.getId(), vaultMobBuilder -> {
                        vaultMobBuilder.bestiaryEntry(themes -> {
                                    themes.add("Not Configured");
                                }, 0, descriptions -> {
                                    descriptions.add(JsonDescription.empty());
                                }
                        );
                    });
                }
            }
        });
    }

    private static boolean bestiaryContainsEntity(ResourceLocation entityId) {
        return !ModConfigs.BESTIARY.getEntities().stream().filter(entityEntry -> entityEntry.getEntityId().equals(entityId)).toList().isEmpty();
    }
}
