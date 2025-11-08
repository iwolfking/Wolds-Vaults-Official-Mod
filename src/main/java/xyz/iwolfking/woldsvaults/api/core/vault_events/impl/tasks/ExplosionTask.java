package xyz.iwolfking.woldsvaults.api.core.vault_events.impl.tasks;

import iskallia.vault.core.vault.Vault;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Explosion;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.VaultEventTask;

import java.util.function.Supplier;

public class ExplosionTask implements VaultEventTask {

    private final float explosionRadius;
    private final Explosion.BlockInteraction blockInteractionType;
    private final boolean causesFire;

    public ExplosionTask(float explosionRadius, Explosion.BlockInteraction blockInteractionType, boolean causesFire) {
        this.explosionRadius = explosionRadius;
        this.blockInteractionType = blockInteractionType;
        this.causesFire = causesFire;
    }

    @Override
    public void performTask(Supplier<BlockPos> pos, ServerPlayer player, Vault vault) {
        player.getLevel().explode(null, pos.get().getX(), pos.get().getY(), pos.get().getZ(), explosionRadius, causesFire, blockInteractionType);
    }
}
