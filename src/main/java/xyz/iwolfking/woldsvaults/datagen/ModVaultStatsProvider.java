package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.core.vault.player.Completion;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractVaultStatsProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModVaultStatsProvider extends AbstractVaultStatsProvider {
    protected ModVaultStatsProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        add("wolds_objectives", builder -> {
            builder.addCompletion("survival", completionFloatMap -> {
                completionFloatMap.put(Completion.FAILED, 0F);
                completionFloatMap.put(Completion.BAILED, 0F);
                completionFloatMap.put(Completion.COMPLETED, 20000F);
            });
            builder.addCompletion("chaos", completionFloatMap -> {
                completionFloatMap.put(Completion.FAILED, 0F);
                completionFloatMap.put(Completion.BAILED, 0F);
                completionFloatMap.put(Completion.COMPLETED, 36000F);
            });
            // Hyper pays like the hardest completion in the pack (matches Corrupted's 100k);
            // there is no bail path — the exit pillar is the only way out alive.
            builder.addCompletion("hyper", completionFloatMap -> {
                completionFloatMap.put(Completion.FAILED, 0F);
                completionFloatMap.put(Completion.BAILED, 0F);
                completionFloatMap.put(Completion.COMPLETED, 100000F);
            });
        });
    }
}
