package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.data;

import iskallia.vault.client.data.ClientDiscoveredEntriesData;
import iskallia.vault.network.message.transmog.DiscoveredEntriesMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.data.discovery.ClientPlayerGreedData;
import xyz.iwolfking.woldsvaults.api.data.discovery.ClientRecipeDiscoveryData;
import xyz.iwolfking.woldsvaults.api.data.discovery.ClientThemeDiscoveryData;

@Mixin(value = ClientDiscoveredEntriesData.class, remap = false)
public class MixinClientDiscoveredEntriesData {
    @Inject(method = "receiveMessage", at = @At("HEAD"), cancellable = true)
    private static void onReceiveThemeMessage(DiscoveredEntriesMessage message, CallbackInfo ci) {
        if(message.type().equals(DiscoveredEntriesMessage.Type.valueOf("THEME"))) {
            ClientThemeDiscoveryData.receiveMessage(message.discoveredEntries());
            ci.cancel();
        }
        if(message.type().equals(DiscoveredEntriesMessage.Type.valueOf("WOLD_RECIPE"))) {
            ClientRecipeDiscoveryData.receiveMessage(message.discoveredEntries());
            ci.cancel();
        }
        if(message.type().equals(DiscoveredEntriesMessage.Type.valueOf("PLAYER_GREED"))) {
            ClientPlayerGreedData.receiveMessage(message.discoveredEntries());
            ci.cancel();
        }
    }

}
