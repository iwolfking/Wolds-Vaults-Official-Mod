package xyz.iwolfking.woldsvaults.api.util;

import com.github.alexthe666.alexsmobs.entity.EntityCockroach;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.ref.Effect;

import javax.annotation.Nullable;

public class SpawnHelper {
    public static Entity doSpawn(VirtualWorld world, double min, double max, BlockPos pos, RandomSource random, WeightedList<EntityType<?>> entities, @Nullable WeightedList<Effect> effects, @Nullable ItemStack heldStack) {
        Entity spawned = null;
        int attempts = 0;
        int maxAttempts = 100;

        while (spawned == null && attempts++ < maxAttempts) {
            double angle = 2 * Math.PI * random.nextDouble();
            double distance = Math.sqrt(random.nextDouble() * (max * max - min * min) + min * min);
            int x = (int)Math.ceil(distance * Math.cos(angle));
            int z = (int)Math.ceil(distance * Math.sin(angle));
            double xzRadius = Math.sqrt(x * x + z * z);
            double yRange = Math.sqrt(max * max - xzRadius * xzRadius);
            int y = random.nextInt((int)Math.ceil(yRange) * 2 + 1) - (int)Math.ceil(yRange);

            spawned = spawnMob(world, entities, heldStack, effects, pos.getX() + x, pos.getY() + y, pos.getZ() + z, random);
        }

        if (spawned == null) {
            WoldsVaults.LOGGER.warn("Failed to spawn mob after {} attempts at {} in {}", maxAttempts, pos, world.getLevel().dimension());
        }

        return spawned;
    }

    @Nullable
    private static Entity spawnMob(VirtualWorld world, WeightedList<EntityType<?>> entities, @Nullable ItemStack heldStack, @Nullable WeightedList<Effect> effects, int x, int y, int z, RandomSource random) {
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

        if(heldStack != null && !heldStack.equals(ItemStack.EMPTY)) {
            if(entity instanceof EntityCockroach roach) {
                roach.setMaracas(true);
            }
            else {
                entity.setItemSlot(EquipmentSlot.MAINHAND, heldStack);
            }

        }

        if(effects != null && !effects.isEmpty()) {
            effects.forEach((mobEffectInstance, aDouble) -> {
                if(entity instanceof LivingEntity livingEntity) {
                    mobEffectInstance.apply(livingEntity);
                }
            });
        }

        world.addWithUUID(entity);

        return entity;

    }
}
