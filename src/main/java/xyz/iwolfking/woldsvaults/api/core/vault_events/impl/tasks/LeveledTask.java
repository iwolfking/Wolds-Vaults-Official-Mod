package xyz.iwolfking.woldsvaults.api.core.vault_events.impl.tasks;

import iskallia.vault.core.vault.Vault;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.VaultEventTask;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LeveledTask implements VaultEventTask {

    private final Map<Integer, VaultEventTask> tasks;

    public LeveledTask(Map<Integer, VaultEventTask> tasks) {
        this.tasks = tasks;
    }

    public static LeveledTask of(Consumer<Map<Integer, VaultEventTask>> leveledTaskConsumer) {
        Map<Integer, VaultEventTask> map = new HashMap<>();
        leveledTaskConsumer.accept(map);
        return new LeveledTask(map);
    }

    @Override
    public void performTask(Supplier<BlockPos> pos, ServerPlayer player, Vault vault) {
        int vaultLevel = vault.get(Vault.LEVEL).get();
        for(Integer level : tasks.keySet()) {
            if(vaultLevel <= level) {
                tasks.get(level).performTask(pos, player, vault);
            }
        }
    }

}
