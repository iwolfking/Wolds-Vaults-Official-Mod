package xyz.iwolfking.woldsvaults.events.client;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.Objectives;
import iskallia.vault.event.event.VaultJoinEvent;
import iskallia.vault.world.data.ServerVaults;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import xyz.iwolfking.woldsvaults.objectives.CorruptedObjective;
import xyz.iwolfking.woldsvaults.util.VaultUtil;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CorruptedVaultEvents {


    //TODO
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onPlayerVaultEnter(VaultJoinEvent event) {
        boolean corruptedModifiers = event.getVault().get(Vault.OBJECTIVES).get(Objectives.LIST)
                .stream()
                .anyMatch(obj -> obj instanceof CorruptedObjective);

        VaultUtil.isVaultCorrupted = corruptedModifiers;
        CorruptedObjective.queuedRefresh = corruptedModifiers;


    }


    @SubscribeEvent @OnlyIn(Dist.CLIENT) // Account for player switching dimensions
    public static void onPlayerDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if(VaultUtil.isVaultCorrupted) {
            VaultUtil.isVaultCorrupted = false;
            CorruptedObjective.queuedRefresh = true;
        }
    }


    @SubscribeEvent // Account for players logging in inside a Vault.
    public static void on(PlayerEvent.PlayerLoggedInEvent event) {
        if(ServerVaults.get(event.getPlayer().level).isPresent()) {
            VaultUtil.isVaultCorrupted = ServerVaults.get(event.getPlayer().level).get().get(Vault.OBJECTIVES).get(Objectives.LIST)
                    .stream()
                    .anyMatch(obj -> obj instanceof CorruptedObjective);
            CorruptedObjective.queuedRefresh = true;
        } else {
            VaultUtil.isVaultCorrupted = false;
        }
    }

    @SubscribeEvent
    public static void playerDeath(PlayerEvent.Clone event) {
        if(event.isWasDeath()) {
            VaultUtil.isVaultCorrupted = false;
            CorruptedObjective.queuedRefresh = true;
        }
    }
}