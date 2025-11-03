package xyz.iwolfking.woldsvaults.api.core.vault_events.impl.tasks;

import iskallia.vault.core.vault.Vault;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.VaultEventTask;
import xyz.iwolfking.woldsvaults.api.util.VaultModifierUtils;

public class AddVaultModifierTask implements VaultEventTask {

    private final ResourceLocation modifierId;

    public AddVaultModifierTask(ResourceLocation modifierId) {
        this.modifierId = modifierId;
    }


    @Override
    public void performTask(BlockPos pos, ServerPlayer player, Vault vault) {
        VaultModifierUtils.addModifierFromPool(vault, modifierId);
    }
}
