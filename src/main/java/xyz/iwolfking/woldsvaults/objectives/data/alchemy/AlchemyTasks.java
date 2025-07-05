package xyz.iwolfking.woldsvaults.objectives.data.alchemy;

import iskallia.vault.block.VaultOreBlock;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.world.data.entity.EntityPredicate;
import iskallia.vault.core.world.data.entity.PartialCompoundNbt;
import iskallia.vault.core.world.data.tile.PartialBlockState;
import iskallia.vault.core.world.data.tile.PartialTile;
import iskallia.vault.core.world.data.tile.TilePredicate;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.EventPriority;
import xyz.iwolfking.woldsvaults.config.AlchemyObjectiveConfig;
import xyz.iwolfking.woldsvaults.init.ModItems;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("SameParameterValue")
public class AlchemyTasks {

    public static void initServer(VirtualWorld world, Vault vault, Objective objective, AlchemyObjectiveConfig.Entry config) {
        generateChestIngredients(
                world,
                vault,
                objective,
                config.getChestProbability(),
                config.getChestIngredients()
        );

        generateCoinIngredients(
                world,
                vault,
                objective,
                config.getCoinProbability(),
                config.getCoinIngredients()
        );

        generateMobIngredients(
                world,
                vault,
                objective,
                config.getMobProbability(),
                config.getMobIngredientEntries()
        );

        generateOreIngredients(
                world,
                vault,
                objective,
                config.getOreProbabiltiy(),
                config.getOreIngredients()
        );

    }

    private static void generateChestIngredients(VirtualWorld virtualWorld, Vault vault, Objective objective, float probability, WeightedList<ItemStack> entries) {
        CommonEvents.CHEST_LOOT_GENERATION.post().register(objective, (data) -> {
            if (data.getPlayer().level == virtualWorld) {
                if (!(data.getRandom().nextDouble() >= probability)) {
                    entries.getRandom(data.getRandom()).ifPresent((itemStack) -> {
                        List<ItemStack> items = new ArrayList<>();
                        items.add(createStack(vault, itemStack));
                        //CommonEvents.ITEM_SCAVENGE_TASK.invoke(vault, virtualWorld, data.getPos(), items); //TODO
                        data.getLoot().addAll(items);
                    });
                }
            }
        });
    }

    private static void generateCoinIngredients(VirtualWorld world, Vault vault, Objective objective, float probability, WeightedList<ItemStack> entries) {
        CommonEvents.COIN_STACK_LOOT_GENERATION.post().register(objective, (data) -> {
            if (data.getPlayer().level == world) {
                if (!(data.getRandom().nextDouble() >= probability)) {
                    entries.getRandom(data.getRandom()).ifPresent((itemStack) -> {
                        data.getLoot().add(createStack(vault, itemStack));
                        //CommonEvents.ITEM_SCAVENGE_TASK.invoke(vault, world, data.getPos(), data.getLoot()); TODO
                    });
                }
            }
        });
    }

    private static void generateMobIngredients(VirtualWorld world, Vault vault, Objective objective, float probability, List<AlchemyTasks.MobEntry> entries) {
        CommonEvents.ENTITY_DROPS.register(objective, (event) -> {
            LivingEntity entity = event.getEntityLiving();
            if (entity.level == world) {
                if (!(world.getRandom().nextDouble() >= probability)) {
                    List<AlchemyTasks.MobEntry> matchingEntries = entries.stream().filter((entryx) -> entryx.group.stream().anyMatch((entityPredicate) -> entityPredicate.test(entity))).toList();
                    if (!matchingEntries.isEmpty()) {
                        AlchemyTasks.MobEntry entry = matchingEntries.get(world.getRandom().nextInt(matchingEntries.size()));
                        ItemStack stack = createStack(vault, entry.item);
                        List<ItemStack> items = new ArrayList<>();
                        items.add(stack);
                        //CommonEvents.ITEM_SCAVENGE_TASK.invoke(vault, world, entity.blockPosition(), items); TODO

                        for(ItemStack item : items) {
                            ItemEntity itemEntity = new ItemEntity(world, entity.getX(), entity.getY(), entity.getZ(), item);
                            event.getDrops().add(itemEntity);
                        }
                    }
                }
            }
        });
    }

    private static void generateOreIngredients(VirtualWorld world, Vault vault, Objective objective, float probability, WeightedList<ItemStack> entries) {
        CommonEvents.PLAYER_MINE.register(objective, EventPriority.LOW, (data) -> {
            if (data.getPlayer().level == world) {
                if (data.getState().getBlock() instanceof VaultOreBlock) {
                    if (data.getState().getValue(VaultOreBlock.GENERATED)) {
                        ChunkRandom random = ChunkRandom.any();
                        BlockPos pos = data.getPos();
                        random.setBlockSeed(vault.get(Vault.SEED), pos.getX(), pos.getY(), pos.getZ(), 110307L);
                        if (!(random.nextDouble() >= probability)) {
                            entries.getRandom(world.getRandom()).ifPresent((itemStack) -> {
                                List<ItemStack> items = new ArrayList<>();
                                items.add(createStack(vault, itemStack));
                                //CommonEvents.ITEM_SCAVENGE_TASK.invoke(vault, world, data.getPos(), items); //TODO
                                items.forEach((item) -> Block.popResource(world, pos, item));
                            });
                        }
                    }
                }
            }
        });
    }

    private static ItemStack createStack(Vault vault, ItemStack stack) {
        stack = stack.copy();
        stack.getOrCreateTag().putString("VaultId", vault.get(Vault.ID).toString());
        return stack;
    }

    public static class MobEntry {
        public final ItemStack item;
        public final List<EntityPredicate> group;

        public MobEntry(ItemStack item, EntityPredicate... entityPredicate) {
            this.item = item;
            this.group = List.of(entityPredicate);
        }
    }
}
