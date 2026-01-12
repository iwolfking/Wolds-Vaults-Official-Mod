package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.event.common.ListenerLeaveEvent;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.core.vault.objective.TimeTrialObjective;
import iskallia.vault.core.vault.player.Completion;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.core.vault.stat.StatsCollector;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.core.world.storage.VirtualWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.competition.TimeTrialCompetition;

@Mixin(TimeTrialObjective.class)
public abstract class MixinTimeTrialObjective {

    @Inject(
        method = "lambda$initServer$1",
        at = @At(
            value = "INVOKE",
            target = "Liskallia/vault/core/vault/player/Runner;getPlayer()Ljava/util/Optional;",
            remap = false
        ),
        remap = false
    )
    private static void onTimeTrialComplete(Vault vault, VirtualWorld world, ListenerLeaveEvent.Data data, CallbackInfo ci, @Local Runner runner) {
        // Get the stats collector and check if this is a completion event
        StatsCollector stats = vault.get(Vault.STATS);
        if (stats == null) return;

        if (stats.get(runner).getCompletion() == Completion.COMPLETED) {
            // Get the player and record their time
            runner.getPlayer().ifPresent(serverPlayer -> {
                    // Get the completion time from the vault's clock
                    TickClock clock = vault.get(Vault.CLOCK);
                    if (clock != null) {
                        int completionTime = clock.get(TickClock.LOGICAL_TIME);
                        String objectiveName = VaultUtils.getMainObjectiveKey(vault);
                        
                        // Record the player's time
                        TimeTrialCompetition competition = TimeTrialCompetition.get();
                        if (competition != null) {
                            competition.recordTime(
                                serverPlayer.getUUID(),
                                serverPlayer.getDisplayName().getString(),
                                completionTime,
                                objectiveName
                            );
                            
                            WoldsVaults.LOGGER.info("Recorded time trial completion for {}: {} ticks on {}", 
                                serverPlayer.getDisplayName().getString(), 
                                completionTime, 
                                objectiveName);
                        }
                    }
            });
        }
    }
}