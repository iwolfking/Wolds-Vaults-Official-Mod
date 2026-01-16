package xyz.iwolfking.woldsvaults.objectives.survival;

import com.ibm.icu.impl.Pair;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import xyz.iwolfking.woldsvaults.api.util.SpawnHelper;
import xyz.iwolfking.woldsvaults.config.SurvivalObjectiveConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.objectives.SurvivalObjective;

public class SurvivalSpawnManager {
    private static final int MOB_CAP_NEARBY = 50;
    private static final float NEARBY_RADIUS = 10.0F;

    private static final double MIN_SPAWN_RADIUS = 8;
    private static final double MAX_SPAWN_RADIUS = 48;

    private static final int TICKS_PER_ACTION = 20;


    private final Vault vault;
    private final VirtualWorld world;
    private final SurvivalObjective objective;

    private int ticks;

    public SurvivalSpawnManager(Vault vault, VirtualWorld world, SurvivalObjective objective) {
        this.vault = vault;
        this.world = world;
        this.objective = objective;
    }


    public void tick() {
        if(ticks % TICKS_PER_ACTION == 0) {
            vault.get(Vault.LISTENERS).getAll().forEach(listener -> {
                if(listener.getPlayer().isPresent()) {
                    ServerPlayer player = listener.getPlayer().get();
                    SurvivalObjectiveConfig.SurvivalSpawnsEntry spawnsEntry = ModConfigs.SURVIVAL_OBJECTIVE.SURVIVAL_SPAWNS.get("default").getForLevel(0).get();
                    Pair<EntityType<?>, Integer> spawnEntry = spawnsEntry.getRandomSpawn();
                    for(int i = 0; i < spawnEntry.second; i++) {
                        SpawnHelper.doSpawn(world, MIN_SPAWN_RADIUS, MAX_SPAWN_RADIUS, player.getOnPos(), ChunkRandom.ofNanoTime(), spawnEntry.first, null, null);
                    }
                }
            });
        }
        ticks++;
    }
}
