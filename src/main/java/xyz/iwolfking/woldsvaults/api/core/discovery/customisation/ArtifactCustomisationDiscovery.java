package xyz.iwolfking.woldsvaults.api.core.discovery.customisation;

import com.google.gson.JsonObject;
import iskallia.vault.config.customisation.CustomisationDiscovery;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.world.data.DiscoveredArtifactsData;
import iskallia.vault.world.data.DiscoveredThemesData;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class ArtifactCustomisationDiscovery extends CustomisationDiscovery {
    private int discoveredArtifactsCount;

    public ArtifactCustomisationDiscovery() {
    }

    public ArtifactCustomisationDiscovery(int discoveredArtifactsCount) {
        this.discoveredArtifactsCount = discoveredArtifactsCount;
    }

    public int getDiscoveredArtifactsCount() {
        return this.discoveredArtifactsCount;
    }

    public boolean test(ServerPlayer player) {
        return DiscoveredArtifactsData.get(player.getLevel()).getDiscoveredArtifacts(player).size() >= discoveredArtifactsCount;
    }

    public Component getRequirementText(String customisationType) {
        return (new TextComponent("Discover " + this.discoveredArtifactsCount + " Artifacts to use this " + customisationType)).withStyle(ChatFormatting.WHITE);
    }

    @Override
    public String getShortRequirement() {
        return "Discover " + discoveredArtifactsCount + " Artifacts";
    }

    @Override
    public int getSortKey() {
        return 1000 + discoveredArtifactsCount;
    }

    public Optional<CompoundTag> writeNbt() {
        CompoundTag nbt = new CompoundTag();
        Adapters.INT.writeNbt(this.discoveredArtifactsCount).ifPresent((tag) -> nbt.put("discoveredArtifactsCount", tag));
        return Optional.of(nbt);
    }

    public void readNbt(CompoundTag nbt) {
        this.discoveredArtifactsCount = Adapters.INT.readNbt(nbt.get("discoveredArtifactsCount")).orElse(0);
    }

    public Optional<JsonObject> writeJson() {
        JsonObject json = new JsonObject();
        Adapters.INT.writeJson(this.discoveredArtifactsCount).ifPresent((tag) -> json.add("discoveredArtifactsCount", tag));
        return Optional.of(json);
    }

    public void readJson(JsonObject json) {
        this.discoveredArtifactsCount = Adapters.INT.readJson(json.get("discoveredArtifactsCount")).orElse(0);
    }
}
