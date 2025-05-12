package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.init.ModNetwork;
import iskallia.vault.nbt.VMapNBT;
import iskallia.vault.network.message.transmog.DiscoveredEntriesMessage;
import iskallia.vault.world.data.PlayerGreedData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.lib.PlayerGreedDataExtension;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@Mixin(value = PlayerGreedData.class, remap = false)
public abstract class MixinPlayerGreedData extends SavedData implements PlayerGreedDataExtension {

    @Shadow protected VMapNBT<UUID, PlayerGreedData.ArtifactData> data;

    @Shadow public abstract PlayerGreedData.ArtifactData get(UUID playerId);

    @Override
    public DiscoveredEntriesMessage getUpdatePacket(UUID playerId) {
        PlayerGreedData.ArtifactData artifactData = this.data.getOrDefault(playerId, null);
        if(artifactData == null) {
            return new DiscoveredEntriesMessage(DiscoveredEntriesMessage.Type.valueOf("PLAYER_GREED"), Collections.emptySet());
        }

        if(artifactData.hasCompletedHerald()) {
            return new DiscoveredEntriesMessage(DiscoveredEntriesMessage.Type.valueOf("PLAYER_GREED"), Set.of(WoldsVaults.id("has_herald_completed")));
        }

        return new DiscoveredEntriesMessage(DiscoveredEntriesMessage.Type.valueOf("PLAYER_GREED"), Collections.emptySet());
    }

    @Override
    public void syncTo(ServerPlayer player) {
        ModNetwork.CHANNEL.sendTo(this.getUpdatePacket(player.getUUID()), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }


    @Override
    public void setDirty(boolean dirty) {
        super.setDirty(dirty);
        if (dirty) {
            MinecraftServer srv = ServerLifecycleHooks.getCurrentServer();
            if (srv != null) {
                srv.getPlayerList().getPlayers().forEach(this::syncTo);
            }
        }
    }
}
