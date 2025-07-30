package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.BingoObjective;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.task.TaskContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.UUID;

@Mixin(value = BingoObjective.class, remap = false)
public interface BingoObjectiveAccessor {
    @Invoker("getContext")
    TaskContext getContext(VirtualWorld world, Vault vault, UUID uuid);
}
