package xyz.iwolfking.woldsvaults.discovery.themes;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.DiscoveryGoalsManager;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.discoverylogic.goal.base.InVaultDiscoveryGoal;
import iskallia.vault.task.source.EntityTaskSource;
import net.minecraft.server.level.ServerPlayer;

public class GodThemesDiscoveryGoal extends InVaultDiscoveryGoal<GodThemesDiscoveryGoal> {

    private final VaultGod god;

    public GodThemesDiscoveryGoal(float targetProgress, VaultGod god) {
        super(targetProgress);
        this.god = god;
    }

    @Override
    public void initServer(DiscoveryGoalsManager discoveryGoalsManager, VirtualWorld virtualWorld, Vault vault) {
        CommonEvents.GOD_ALTAR_EVENT.register(discoveryGoalsManager, event -> {
            if (event.getTask().getGod().equals(god) && event.isCompleted() && event.getContext().getSource() instanceof EntityTaskSource entitySource) {
                for (ServerPlayer player : entitySource.getEntities(ServerPlayer.class)) {
                    this.progress(player, 1.0F);
                }
            }
        });
    }

}
