package xyz.iwolfking.woldsvaults.events.client;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.event.event.VaultJoinEvent;
import iskallia.vault.world.data.ServerVaults;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.modifiers.vault.CorruptedVaultModifier;
import xyz.iwolfking.woldsvaults.util.VaultModifierUtils;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ShaderApplyEvent {

    //TODO Change this to check for objective instead of modifier
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onPlayerVaultEnter(VaultJoinEvent event) {
        VaultModifierUtils.isVaultCorrupted = event.getVault().get(Vault.MODIFIERS).getModifiers()
                .stream()
                .anyMatch(mod -> mod instanceof CorruptedVaultModifier);
    }


    @SubscribeEvent @OnlyIn(Dist.CLIENT) // Account for player switching dimensions
    public static void onPlayerDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if(VaultModifierUtils.isVaultCorrupted) {
            VaultModifierUtils.isVaultCorrupted = false;
        }
    }


    @SubscribeEvent // Account for players logging in inside a Vault.
    public static void on(PlayerEvent.PlayerLoggedInEvent event) {
        if(ServerVaults.get(event.getPlayer().level).isPresent()) {
            VaultModifierUtils.isVaultCorrupted = ServerVaults.get(event.getPlayer().level).get().get(Vault.MODIFIERS).getModifiers()
                    .stream()
                    .anyMatch(mod -> mod instanceof CorruptedVaultModifier);
        } else {
            VaultModifierUtils.isVaultCorrupted = false;
        }
    }
}
