package xyz.iwolfking.woldsvaults.api.core.vault_events.impl.tasks;

import iskallia.vault.core.vault.Vault;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.VaultEventTask;
import xyz.iwolfking.woldsvaults.api.util.VaultModifierUtils;

public class VaultModifierTask implements VaultEventTask {

    protected final ResourceLocation modifierId;
    protected final int count;
    private final int duration;

    public VaultModifierTask(ResourceLocation modifierId, int count) {
        this.modifierId = modifierId;
        this.count = count;
        this.duration = 0;
    }

    public VaultModifierTask(ResourceLocation modifierId, int count, int duration) {
        this.modifierId = modifierId;
        this.count = count;
        this.duration = duration;
    }


    @Override
    public void performTask(BlockPos pos, ServerPlayer player, Vault vault) {
        if(duration > 0) {
            VaultModifierUtils.addTimedModifier(vault, modifierId, count, duration, player);
        }
        else {
            VaultModifierUtils.addModifier(vault, modifierId, count);
        }
    }
}
