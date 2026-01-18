package xyz.iwolfking.woldsvaults.api.core.vault_events.impl.tasks;

import iskallia.vault.core.Version;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultRegistry;
import iskallia.vault.core.vault.objective.AwardCrateObjective;
import iskallia.vault.core.world.loot.generator.LootTableGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.VaultEventTask;
import xyz.iwolfking.woldsvaults.api.util.WoldVaultUtils;
import java.util.function.Supplier;

public class AddCrateItemTask implements VaultEventTask {

    private final ResourceLocation lootTableKey;

    public AddCrateItemTask(ResourceLocation lootTableKey) {
        this.lootTableKey = lootTableKey;
    }

    @Override
    public void performTask(Supplier<BlockPos> pos, ServerPlayer player, Vault vault) {
        AwardCrateObjective objective = WoldVaultUtils.getObjective(vault, AwardCrateObjective.class);

        if(objective == null) {
            return;
        }

        if(VaultRegistry.LOOT_TABLE.contains(lootTableKey)) {
            LootTableGenerator generator = new LootTableGenerator(Version.latest(), VaultRegistry.LOOT_TABLE.getKey(lootTableKey), 0F);
            generator.generate(ChunkRandom.ofNanoTime());
            while(generator.getItems().hasNext()) {
                objective.get(AwardCrateObjective.ADDITIONAL_ITEMS).add(generator.getItems().next());
            }
        }
    }
}
