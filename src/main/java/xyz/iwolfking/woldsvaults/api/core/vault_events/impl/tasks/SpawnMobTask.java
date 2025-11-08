package xyz.iwolfking.woldsvaults.api.core.vault_events.impl.tasks;

import com.github.alexthe666.alexsmobs.entity.EntityCockroach;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.VaultEventTask;
import xyz.iwolfking.woldsvaults.api.util.ref.Effect;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class SpawnMobTask implements VaultEventTask {

    private final WeightedList<EntityType<?>> entities;

    private final WeightedList<Integer> amounts;

    private final WeightedList<Effect> effects;

    private final ItemStack heldStack;

    private final double minSpawnRange;
    private final double maxSpawnRange;

    public SpawnMobTask(WeightedList<EntityType<?>> entities, WeightedList<Integer> amounts, WeightedList<Effect> effects, ItemStack heldStack) {
        this.entities = entities;
        this.amounts = amounts;
        this.effects = effects;
        this.heldStack = heldStack;
        this.minSpawnRange = 10.0;
        this.maxSpawnRange = 13.0;
    }

    public SpawnMobTask(WeightedList<EntityType<?>> entities, WeightedList<Integer> amounts, WeightedList<Effect> effects, ItemStack heldStack, double minSpawnRange, double maxSpawnRange) {
        this.entities = entities;
        this.amounts = amounts;
        this.effects = effects;
        this.heldStack = heldStack;
        this.minSpawnRange = minSpawnRange;
        this.maxSpawnRange = maxSpawnRange;
    }

    @Override
    public void performTask(Supplier<BlockPos> pos, ServerPlayer player, Vault vault) {
        JavaRandom javaRandom = JavaRandom.ofNanoTime();
        for(int i = 0; i < amounts.getRandom().get(); i++) {
            doSpawn((VirtualWorld) player.level, pos.get(), javaRandom);
        }
    }

    public Entity doSpawn(VirtualWorld world, BlockPos pos, RandomSource random) {
        double min = minSpawnRange;
        double max = maxSpawnRange;

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

            spawned = spawnMob(world, pos.getX() + x, pos.getY() + y, pos.getZ() + z, random);
        }

        if (spawned == null) {
            WoldsVaults.LOGGER.warn("Failed to spawn mob after {} attempts at {} in {}", maxAttempts, pos, world.getLevel().dimension());
        }

        return spawned;
    }

    @Nullable
    public Entity spawnMob(VirtualWorld world, int x, int y, int z, RandomSource random) {
        Entity entity;
        EntityType<?> type = null;
        if(entities.getRandom().isPresent()) {
            type = entities.getRandom().get();
        }

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

        entity.moveTo(x + 0.5F, y + 0.2F,z + 0.5F, (float)(random.nextDouble() * 2.0 * Math.PI), 0.0F);

        if(!heldStack.equals(ItemStack.EMPTY)) {
            if(entity instanceof EntityCockroach roach) {
                roach.setMaracas(true);
            }
            else {
                entity.setItemSlot(EquipmentSlot.MAINHAND, heldStack);
            }

        }

        if(!effects.isEmpty()) {
            effects.forEach((mobEffectInstance, aDouble) -> {
                if(entity instanceof LivingEntity livingEntity) {
                    System.out.println("Applying effect!");
                    mobEffectInstance.apply(livingEntity);
                }
                else {
                    System.out.println("NOT A LIVING ENTITY");
                }
            });
        }

        world.addWithUUID(entity);

        return entity;

    }

    public static class Builder {
        private final WeightedList<EntityType<?>> entities = new WeightedList<>();

        private final WeightedList<Integer> amounts = new WeightedList<>();

        private final WeightedList<Effect> effects = new WeightedList<>();

        private ItemStack heldStack = ItemStack.EMPTY;

        private double minSpawnRange = 10.0;

        private double maxSpawnRange = 13.0;

        public Builder entity(EntityType<?> type, double weight) {
            entities.add(type, weight);
            return this;
        }

        public Builder amount(Integer amount, double weight) {
            amounts.add(amount, weight);
            return this;
        }

        public Builder effect(Effect effect, double weight) {
            effects.add(effect, weight);
            return this;
        }

        public Builder heldStack(ItemStack stack) {
            heldStack = stack;
            return this;
        }

        public Builder spawnRanges(double minSpawnRange, double maxSpawnRange) {
            this.minSpawnRange = minSpawnRange;
            this.maxSpawnRange = maxSpawnRange;
            return this;
        }

        public SpawnMobTask build() {
            if(entities.isEmpty()) {
                entities.add(EntityType.BAT, 1.0);
            }

            if(amounts.isEmpty()) {
                amounts.add(1, 1.0);
            }

            return new SpawnMobTask(entities, amounts, effects, heldStack, minSpawnRange, maxSpawnRange);
        }


    }
}
