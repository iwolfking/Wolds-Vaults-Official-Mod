package xyz.iwolfking.woldsvaults.modifiers.vault;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.CustomEntitySpawnerConfig;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.util.data.WeightedList;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import javax.annotation.Nullable;
import java.util.Random;

public class RetroSpawnVaultModifier extends VaultModifier<RetroSpawnVaultModifier.Properties> {

    public RetroSpawnVaultModifier(ResourceLocation id, Properties properties, Display display) {
        super(id, properties, display);
    }

    @Override
    public void initServer(VirtualWorld world, Vault vault, ModifierContext context) {
        CommonEvents.PLAYER_TICK.register(context.getUUID(), EventPriority.HIGHEST, event -> {
            if(event.side.isServer()) {
                if((event.player.tickCount % this.properties.getTicksPerCheck()) != 0) {
                    return;
                }

                if(vault.get(Vault.CLOCK).has(TickClock.PAUSED)) {
                    return;
                }

                if(event.player.getRandom().nextDouble() < this.properties.getChance()) {
                    if(!(event.player.getLevel().dimension().equals(world.dimension()))) {
                        return;
                    }

                    for(int i = 0; i < this.properties.amounts.getRandom(event.player.getRandom()); i++) {
                        int livingEntitiesAroundCount = world.getEntitiesOfClass(LivingEntity.class, new AABB(event.player.getOnPos()).inflate(48)).size();

                        if(this.properties.cap < livingEntitiesAroundCount) {
                            return;
                        }

                        doSpawn((VirtualWorld) event.player.level, event.player.getOnPos(), ChunkRandom.ofNanoTime());
                    }
                }
            }
        });
    }

    public static class Properties {
        @Expose
        private final double chance;
        @Expose
        private final int ticksPerCheck;

        @Expose
        private final WeightedList<CustomEntitySpawnerConfig.SpawnerEntity> entities;

        @Expose
        private final WeightedList<Integer> amounts;

        @Expose
        private final int cap;

        public Properties(double chance, int ticksPerCheck, WeightedList<CustomEntitySpawnerConfig.SpawnerEntity> entities, WeightedList<Integer> amounts, int cap) {
            this.chance = chance;
            this.ticksPerCheck = ticksPerCheck;
            this.entities = entities;
            this.amounts = amounts;
            this.cap = cap;
        }



        public double getChance() {
            return this.chance;
        }
        public int getTicksPerCheck() {
            return this.ticksPerCheck;
        }
        public int getCap() {
            return this.cap;
        }
        public WeightedList<Integer>  getAmounts() {
            return this.amounts;
        }
        public WeightedList<CustomEntitySpawnerConfig.SpawnerEntity> getEntities() {
            return this.entities;
        }

    }

    public Entity doSpawn(VirtualWorld world, BlockPos pos, RandomSource random) {
        double min = 20;
        double max = 30;

        Entity spawned = null;
        int attempts = 0;
        int maxAttempts = 50; // stop after 50 tries

        while (spawned == null && attempts++ < maxAttempts) {
            double angle = 2 * Math.PI * random.nextDouble();
            double distance = Math.sqrt(random.nextDouble() * (max * max - min * min) + min * min);
            int x = (int)Math.ceil(distance * Math.cos(angle));
            int z = (int)Math.ceil(distance * Math.sin(angle));
            double xzRadius = Math.sqrt(x * x + z * z);
            double yRange = Math.sqrt(max * max - xzRadius * xzRadius);
            int y = random.nextInt((int)Math.ceil(yRange) * 2 + 1) - (int)Math.ceil(yRange);

            spawned = spawnMob(world, pos.getX() + x, pos.getY() + y, pos.getZ() + z, new Random());
        }

        if (spawned == null) {
            WoldsVaults.LOGGER.warn("Failed to spawn mob after {} attempts at {} in {}", maxAttempts, pos, world.getLevel().dimension());
        }

        return spawned;
    }


    @Nullable
    public LivingEntity spawnMob(VirtualWorld world, int x, int y, int z, Random random) {
        Entity entity;
        EntityType<?> type = ForgeRegistries.ENTITIES.getValue(this.properties.getEntities().getRandom(random).type);


        if(type != null) {
            entity = type.create(world);
        } else {
            entity = null;
        }

        BlockState state = world.getBlockState(new BlockPos(x, y - 1, z));
        if (entity == null || !state.isValidSpawn(world, new BlockPos(x, y - 1, z), entity.getType())) {
            return null;
        }
        AABB entityBox = entity.getType().getAABB(x + 0.5, y, z + 0.5);
        if (!world.noCollision(entityBox)) {
            return null;
        }
        entity.moveTo(x + 0.5F, y + 0.2F, z + 0.5F, (float)(random.nextDouble() * 2.0 * Math.PI), 0.0F);
        world.addWithUUID(entity);
        return (LivingEntity) entity;

    }
}
