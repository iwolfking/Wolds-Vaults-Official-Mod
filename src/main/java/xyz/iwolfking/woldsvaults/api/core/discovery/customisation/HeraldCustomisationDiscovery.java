package xyz.iwolfking.woldsvaults.api.core.discovery.customisation;

import com.google.gson.JsonObject;
import iskallia.vault.config.customisation.CustomisationDiscovery;
import iskallia.vault.world.data.PlayerGreedData;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class HeraldCustomisationDiscovery extends CustomisationDiscovery {


    public HeraldCustomisationDiscovery() {
    }

    public boolean test(ServerPlayer player) {
        return PlayerGreedData.get().get(player).hasCompletedHerald();
    }

    public Component getRequirementText(String customisationType) {
        return (new TextComponent("Defeat The Herald to use this " + customisationType)).withStyle(ChatFormatting.WHITE);
    }

    @Override
    public String getShortRequirement() {
        return "Defeat The Herald";
    }

    @Override
    public int getSortKey() {
        return 99999;
    }

    public Optional<CompoundTag> writeNbt() {
        CompoundTag nbt = new CompoundTag();
        return Optional.of(nbt);
    }

    public void readNbt(CompoundTag nbt) {
    }

    public Optional<JsonObject> writeJson() {
        JsonObject json = new JsonObject();
        return Optional.of(json);
    }

    public void readJson(JsonObject json) {
    }
}
