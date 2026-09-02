package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.init.ModConfigs;
import iskallia.vault.research.ResearchTree;
import iskallia.vault.research.type.Research;
import iskallia.vault.util.PlayerReference;
import iskallia.vault.world.data.PlayerResearchesData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.api.util.GameruleHelper;
import xyz.iwolfking.woldsvaults.init.ModGameRules;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mixin(value = PlayerResearchesData.class, remap = false)
public abstract class MixinPlayerResearchesData {
    @Shadow
    public abstract ResearchTree getResearches(Player player);

    @Shadow
    public abstract PlayerResearchesData research(ServerPlayer player, Research research, boolean sendMessage);

    @Shadow
    public abstract List<PlayerReference> getTeamMembers(UUID playerId);

    @Shadow
    public abstract ResearchTree getResearches(UUID uuid);

    @Shadow
    @Final
    private Map<UUID, ResearchTree> playerMap;

    @Inject(method = "sync", at = @At("HEAD"))
    private void unlockModBoxTinkeringOnSync(ServerPlayer player, CallbackInfo ci) {
        ResearchTree researchTree = getResearches(player);
        if(!researchTree.isResearched("Mod Box Tinkering")) {
            if(researchTree.getResearchesDone().size() >= 10) {
                if(ModConfigs.RESEARCHES.getByName("Mod Box Tinkering") != null) {
                    research(player, ModConfigs.RESEARCHES.getByName("Mod Box Tinkering"), true);
                }
            }
        }
    }

    @Inject(method = "acceptInvite", at = @At(value = "INVOKE", target = "Liskallia/vault/world/data/PlayerResearchesData;propagateTeams()V"))
    private void syncAllMissingResearches(Player invitee, UUID issuer, CallbackInfoReturnable<Boolean> cir) {
        if(GameruleHelper.isEnabled(ModGameRules.ENABLE_FULL_RESEARCH_TEAM_SYNC, invitee.getLevel()) && invitee instanceof ServerPlayer serverPlayer) {
            PlayerReference teamOwner = this.getTeamMembers(serverPlayer.getUUID()).stream().filter(playerReference -> playerReference.getId() != serverPlayer.getUUID()).findFirst().orElse(null);
            if(teamOwner == null) {
                return;
            }

            ResearchTree ownerTree = this.getResearches(teamOwner.getId());
            ResearchTree inviteeTree = this.getResearches(serverPlayer.getUUID());
            ownerTree.getResearchesDone().forEach(research -> {
                if(!inviteeTree.isResearched(research)) {
                    Research researchObj = ModConfigs.RESEARCHES.getByName(research);
                    if(researchObj == null) {
                        return;
                    }

                    this.research(serverPlayer, researchObj, false);
                }
            });
        }
    }
}
