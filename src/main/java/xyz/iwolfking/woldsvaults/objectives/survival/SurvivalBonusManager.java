package xyz.iwolfking.woldsvaults.objectives.survival;

import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.api.util.SpawnHelper;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.objectives.SurvivalObjective;

public class SurvivalBonusManager {
    private static final int MOB_CAP_NEARBY = 50;
    private static final float NEARBY_RADIUS = 10.0F;

    private static final double MIN_SPAWN_RADIUS = 8;
    private static final double MAX_SPAWN_RADIUS = 48;

    private static final int TICKS_PER_ACTION = 20;


    private final Vault vault;
    private final VirtualWorld world;
    private final SurvivalObjective objective;

    public SurvivalBonusManager(Vault vault, VirtualWorld world, SurvivalObjective objective) {
        this.vault = vault;
        this.world = world;
        this.objective = objective;
    }

    private int ticks;
    public void tick() {
        if(ticks % TICKS_PER_ACTION == 0) {

        }
        ticks++;
    }
}
