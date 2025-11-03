package xyz.iwolfking.woldsvaults.api.core.vault_events.legacy;

import com.github.alexthe666.alexsmobs.entity.EntityCockroach;
import com.google.gson.annotations.Expose;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import xyz.iwolfking.woldsvaults.objectives.data.EnchantedEventsRegistry;
import xyz.iwolfking.woldsvaults.api.core.vault_events.LegacyVaultEvent;

import javax.annotation.Nullable;

public class SpawnEntityVaultEvent extends LegacyVaultEvent {
    @Expose
    private final WeightedList<EntityType<?>> entities;
    @Expose
    private final WeightedList<Integer> amounts;
    @Expose
    private final WeightedList<MobEffectInstance> effects;

    private final ItemStack heldStack;


    public SpawnEntityVaultEvent(String eventName, String eventDescription, String primaryColor, WeightedList<EntityType<?>> entities, WeightedList<Integer> amounts) {
        super(eventName, eventDescription, primaryColor);
        this.entities = entities;
        this.amounts = amounts;
        this.effects = new WeightedList<>();
        this.heldStack = ItemStack.EMPTY;
    }

    public SpawnEntityVaultEvent(String eventName, String eventDescription, String primaryColor, WeightedList<EntityType<?>> entities, WeightedList<Integer> amounts, WeightedList<MobEffectInstance> appliedEffects) {
        super(eventName, eventDescription, primaryColor);
        this.entities = entities;
        this.amounts = amounts;
        this.effects = appliedEffects;
        this.heldStack = ItemStack.EMPTY;
    }

    public SpawnEntityVaultEvent(String eventName, String eventDescription, String primaryColor, WeightedList<EntityType<?>> entities, WeightedList<Integer> amounts, ItemStack stack) {
        super(eventName, eventDescription, primaryColor);
        this.entities = entities;
        this.amounts = amounts;
        this.effects = new WeightedList<>();
        this.heldStack = stack;
    }

    @Override
    public void triggerEvent(BlockPos pos, ServerPlayer player, Vault vault) {
        JavaRandom javaRandom = JavaRandom.ofNanoTime();
        for(int i = 0; i < amounts.getRandom().get(); i++) {
            doSpawn((VirtualWorld) player.level, pos, javaRandom);
        }

        if(this.getEventName().equals("La Cucaracha")) {
            EnchantedEventsRegistry.LA_CUCARACHA_RANDOM_EVENT.triggerEvent(pos, player, vault);
        }

        super.triggerEvent(pos, player, vault);
    }

    public LivingEntity doSpawn(VirtualWorld world, BlockPos pos, RandomSource random) {
        double min = 10.0;
        double max = 13.0;

        LivingEntity spawned;
        int x;
        int z;
        int y;
        for(spawned = null; spawned == null; spawned = spawnMob(world, pos.getX() + x, pos.getY() + y, pos.getZ() + z, random)) {
            double angle = 2 * Math.PI * random.nextDouble();
            double distance = Math.sqrt(random.nextDouble() * (max * max - min * min) + min * min);
            x = (int)Math.ceil(distance * Math.cos(angle));
            z = (int)Math.ceil(distance * Math.sin(angle));
            double xzRadius = Math.sqrt(x * x + z * z);
            double yRange = Math.sqrt(max * max - xzRadius * xzRadius);
            y = random.nextInt((int)Math.ceil(yRange) * 2 + 1) - (int)Math.ceil(yRange);
        }

        return spawned;
    }

    @Nullable
    public LivingEntity spawnMob(VirtualWorld world, int x, int y, int z, RandomSource random) {
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
       // entity.finalizeSpawn(world, new DifficultyInstance(Difficulty.PEACEFUL, 13000L, 0L, 0.0F), MobSpawnType.STRUCTURE, (SpawnGroupData)null, (CompoundTag)null);
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
                ((LivingEntity)entity).addEffect(mobEffectInstance);
            });
        }
        world.addWithUUID(entity);

        return (LivingEntity) entity;

    }
}
