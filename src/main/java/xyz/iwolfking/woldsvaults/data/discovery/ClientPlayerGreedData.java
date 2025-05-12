package xyz.iwolfking.woldsvaults.data.discovery;

import iskallia.vault.nbt.VMapNBT;
import iskallia.vault.util.function.ObservableSupplier;
import iskallia.vault.world.data.PlayerGreedData;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ClientPlayerGreedData {
    private static Set<ResourceLocation> playerArtifactData = new HashSet<>();

    public static  Set<ResourceLocation> getArtifactData() {
        return playerArtifactData;
    }

    public static ObservableSupplier<Set<ResourceLocation>> getArtifactObservable() {
        return ObservableSupplier.of(
                ClientPlayerGreedData::getArtifactData, (modelSet, newModelSet) -> modelSet.size() == newModelSet.size()
        );
    }


    public static void receiveMessage( Set<ResourceLocation> artifactData) {
        playerArtifactData.clear();
        playerArtifactData.addAll(artifactData);
    }
}
