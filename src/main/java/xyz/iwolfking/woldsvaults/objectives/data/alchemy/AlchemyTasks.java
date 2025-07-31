package xyz.iwolfking.woldsvaults.objectives.data.alchemy;

import iskallia.vault.block.VaultOreBlock;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.world.data.entity.EntityPredicate;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.EventPriority;
import xyz.iwolfking.woldsvaults.config.AlchemyObjectiveConfig;

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
                    });
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
