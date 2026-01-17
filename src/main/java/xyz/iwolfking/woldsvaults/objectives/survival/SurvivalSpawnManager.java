package xyz.iwolfking.woldsvaults.objectives.survival;

import com.ibm.icu.impl.Pair;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import xyz.iwolfking.woldsvaults.api.util.SpawnHelper;
import xyz.iwolfking.woldsvaults.objectives.SurvivalObjective;
import xyz.iwolfking.woldsvaults.objectives.survival.lib.TickTimer;

public class SurvivalSpawnManager {
    private static final int MOB_CAP_NEARBY = 50;
    private static final float NEARBY_RADIUS = 10.0F;

    private static final double MIN_SPAWN_RADIUS = 8;
    private static final double MAX_SPAWN_RADIUS = 48;

    private static final int TICKS_PER_ACTION = 20;
    private static final int TICKS_PER_NEXT_WAVE = 1200;


    private final Vault vault;
    private final VirtualWorld world;
    private final SurvivalObjective objective;

    private final int level;

    public final TickTimer SPAWN_TIMER = new TickTimer(TICKS_PER_ACTION);
    public final TickTimer WAVE_TIMER = new TickTimer(TICKS_PER_NEXT_WAVE);

    public SurvivalSpawnManager(Vault vault, VirtualWorld world, SurvivalObjective objective) {
        this.vault = vault;
        this.world = world;
        this.objective = objective;
        this.level = vault.get(Vault.LEVEL).get();
    }


    public void tick() {
        if(SPAWN_TIMER.ready()) {
            vault.get(Vault.LISTENERS).getAll().forEach(listener -> {
                if(listener.getPlayer().isPresent()) {
                    ServerPlayer player = listener.getPlayer().get();
                    objective.getCurrentWaveEntry(level).ifPresent(survivalSpawnsEntry -> {
                        Pair<EntityType<?>, Integer> spawnEntry = survivalSpawnsEntry.getRandomSpawn();
                        for(int i = 0; i < spawnEntry.second; i++) {
                            SpawnHelper.doSpawn(world, MIN_SPAWN_RADIUS, MAX_SPAWN_RADIUS, player.getOnPos(), ChunkRandom.ofNanoTime(), spawnEntry.first, null, null);
                        }
                    });
                }
            });
        }

        if(WAVE_TIMER.ready()) {
            objective.incrementWave(vault);
        }
    }
}
