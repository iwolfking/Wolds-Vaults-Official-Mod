package xyz.iwolfking.woldsvaults.api.core.vault_events.impl.tasks;

import iskallia.vault.VaultMod;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.init.ModConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.VaultEventTask;
import xyz.iwolfking.woldsvaults.api.util.VaultModifierUtils;

import java.util.List;
import java.util.function.Supplier;

public class VaultModifierFromPoolTask implements VaultEventTask {

    private final ResourceLocation modifierPoolId;

    public VaultModifierFromPoolTask(ResourceLocation modifierId) {
        this.modifierPoolId = modifierId;
    }


    @Override
    public void performTask(Supplier<BlockPos> pos, ServerPlayer player, Vault vault) {
        VaultModifierUtils.addModifierFromPool(vault, modifierPoolId);
    }
}
