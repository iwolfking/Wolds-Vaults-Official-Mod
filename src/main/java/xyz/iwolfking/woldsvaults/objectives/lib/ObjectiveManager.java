package xyz.iwolfking.woldsvaults.objectives.lib;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.Objective;
import iskallia.vault.core.world.storage.VirtualWorld;

public abstract class ObjectiveManager<T extends Objective> {
    protected final Vault vault;
    protected final VirtualWorld world;
    protected final T objective;
    protected final int level;

    protected ObjectiveManager(Vault vault, VirtualWorld world, T objective) {
        this.vault = vault;
        this.world = world;
        this.objective = objective;
        this.level = vault.get(Vault.LEVEL).get();
    }

    public abstract void tick();
}
