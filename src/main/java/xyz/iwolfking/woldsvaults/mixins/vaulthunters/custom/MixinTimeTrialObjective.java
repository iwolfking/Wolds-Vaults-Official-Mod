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
import iskallia.vault.world.data.PlayerTimeTrialData;
import net.minecraft.network.chat.TranslatableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.vhapi.api.util.MessageUtils;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.TimeUtils;
import xyz.iwolfking.woldsvaults.api.core.competition.TimeTrialCompetition;

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

                        int bestEverTime = PlayerTimeTrialData.get().getBestTime(serverPlayer, objectiveName);
                        if(bestEverTime > completionTime) {
                            if(bestEverTime == Integer.MAX_VALUE) {
                                MessageUtils.broadcastMessage(serverPlayer.getLevel(), new TranslatableComponent("vault_objective.woldsvaults.time_trial_player_best", serverPlayer.getDisplayName(), objectiveName, "No time recorded", TimeUtils.formatTime(bestEverTime)));
                            }
                            else {
                                MessageUtils.broadcastMessage(serverPlayer.getLevel(), new TranslatableComponent("vault_objective.woldsvaults.time_trial_player_best", serverPlayer.getDisplayName(), objectiveName,  TimeUtils.formatTime(completionTime), TimeUtils.formatTime(bestEverTime)));
                            }
                        }


                        // Record the player's time
                        TimeTrialCompetition competition = TimeTrialCompetition.get();
                        if (competition != null) {

                            long competitionBest = competition.getBestTime().getValue();

                            if(completionTime < competitionBest) {
                                if(competitionBest == Long.MAX_VALUE) {
                                    MessageUtils.broadcastMessage(serverPlayer.getLevel(), new TranslatableComponent("vault_objective.woldsvaults.time_trial_new_competition_best", serverPlayer.getDisplayName(), TimeUtils.formatTime(competitionBest), TimeUtils.formatTime(completionTime)));
                                }
                                else {
                                    MessageUtils.broadcastMessage(serverPlayer.getLevel(), new TranslatableComponent("vault_objective.woldsvaults.time_trial_new_competition_first", serverPlayer.getDisplayName(), TimeUtils.formatTime(completionTime)));
                                }
                            }

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