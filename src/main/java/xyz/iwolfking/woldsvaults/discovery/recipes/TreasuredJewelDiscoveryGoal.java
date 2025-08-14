package xyz.iwolfking.woldsvaults.discovery.recipes;


import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.DiscoveryGoalsManager;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultLevel;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.discoverylogic.goal.base.InVaultDiscoveryGoal;
import net.minecraft.server.level.ServerPlayer;

public class TreasuredJewelDiscoveryGoal extends InVaultDiscoveryGoal<TreasuredJewelDiscoveryGoal> {

    public TreasuredJewelDiscoveryGoal(float targetProgress) {
        super(targetProgress);
    }

    @Override
    public void initServer(DiscoveryGoalsManager discoveryGoalsManager, VirtualWorld virtualWorld, Vault vault) {
        CommonEvents.TREASURE_ROOM_OPEN.register(discoveryGoalsManager, event -> {
            if(event.getPlayer() instanceof ServerPlayer sPlayer) {
                this.progress(sPlayer, 1.0F);
            }
        });
    }
}
