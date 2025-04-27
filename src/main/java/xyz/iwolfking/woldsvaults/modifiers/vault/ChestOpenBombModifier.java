package xyz.iwolfking.woldsvaults.modifiers.vault;

import com.google.gson.annotations.Expose;
import iskallia.vault.block.VaultChestBlock;
import iskallia.vault.block.entity.VaultChestTileEntity;
import iskallia.vault.config.CustomEntitySpawnerConfig;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.util.data.WeightedList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Random;

public class ChestOpenBombModifier extends VaultModifier<ChestOpenBombModifier.Properties> {

    public ChestOpenBombModifier(ResourceLocation id, Properties properties, Display display) {
        super(id, properties, display);
    }

    @Override
    public void initServer(VirtualWorld world, Vault vault, ModifierContext context) {
        CommonEvents.PLAYER_MINE.register(context.getUUID(), EventPriority.HIGHEST, event -> {
            if(!(event.getPlayer().getLevel().dimension().equals(world.dimension()))) {
                return;
            }
            if(event.getState().getBlock() instanceof VaultChestBlock chestBlock) {
                BlockEntity chestEntity = world.getBlockEntity(event.getPos());
                if(chestEntity instanceof VaultChestTileEntity chest) {
                    if(chestBlock.hasStepBreaking(chest) && !chest.isEmpty()) {
                        return;
                    }
                }
                Player player = event.getPlayer();
                if(event.getPlayer() != null) {
                    Random random = player.level.getRandom();
                    if(random.nextDouble() < this.properties.getChance()) {
                        int amountToSpawn = this.properties.getAmounts().getRandom(random);
                        for(int i = 0; i < amountToSpawn; i++) {
                            EntityType<?> typeToSpawn = ForgeRegistries.ENTITIES.getValue(this.properties.getEntities().getRandom(random).type);
                            if(typeToSpawn != null) {
                                Entity toSpawn = typeToSpawn.create(world);
                                if(toSpawn != null) {
                                    toSpawn.setPos(Vec3.atCenterOf(event.getPos()));
                                    world.addFreshEntity(toSpawn);
                                }

                            }
                        }

                    }
                }
            }
        });
    }

    public static class Properties {
        @Expose
        private final double chance;

        @Expose
        private final WeightedList<Integer> amounts;

        @Expose
        private final WeightedList<CustomEntitySpawnerConfig.SpawnerEntity> entities;

        public Properties(double chance, WeightedList<Integer> amounts, WeightedList<CustomEntitySpawnerConfig.SpawnerEntity> entities) {
            this.chance = chance;
            this.amounts = amounts;
            this.entities = entities;
        }



        public double getChance() {
            return this.chance;
        }
        public WeightedList<Integer> getAmounts() {
            return this.amounts;
        }

        public WeightedList<CustomEntitySpawnerConfig.SpawnerEntity> getEntities() {
            return this.entities;
        }

    }
}
