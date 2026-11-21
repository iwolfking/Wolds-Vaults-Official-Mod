package xyz.iwolfking.woldsvaults.api.core.discovery.customisation;

import com.google.gson.JsonObject;
import iskallia.vault.config.customisation.CustomisationDiscovery;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.world.data.DiscoveredThemesData;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class ThemeCustomisationDiscovery extends CustomisationDiscovery {
    private String themeGroup;

    public ThemeCustomisationDiscovery() {
    }

    public ThemeCustomisationDiscovery(String themeGroup) {
        this.themeGroup = themeGroup;
    }

    public String getThemeGroup() {
        return this.themeGroup;
    }

    public boolean test(ServerPlayer player) {
        return DiscoveredThemesData.get(player.getLevel()).getDiscoveredThemes(player.getUUID()).contains(themeGroup);
    }

    public Component getRequirementText(String customisationType) {
        return (new TextComponent("Unlock the " + this.themeGroup + " themes to use this " + customisationType)).withStyle(ChatFormatting.WHITE);
    }

    public String getShortRequirement() {
        return "Complete Theme from Series: " + this.themeGroup;
    }

    //TODO: Change this to something more sensible
    public int getSortKey() {
        return 100000 + themeGroup.length();
    }

    public Optional<CompoundTag> writeNbt() {
        CompoundTag nbt = new CompoundTag();
        Adapters.UTF_8.writeNbt(this.themeGroup).ifPresent((tag) -> nbt.put("themeGroup", tag));
        return Optional.of(nbt);
    }

    public void readNbt(CompoundTag nbt) {
        this.themeGroup = Adapters.UTF_8.readNbt(nbt.get("themeGroup")).orElse("Candoria");
    }

    public Optional<JsonObject> writeJson() {
        JsonObject json = new JsonObject();
        Adapters.UTF_8.writeJson(this.themeGroup).ifPresent((tag) -> json.add("themeGroup", tag));
        return Optional.of(json);
    }

    public void readJson(JsonObject json) {
        this.themeGroup = Adapters.UTF_8.readJson(json.get("themeGroup")).orElse("Candoria");
    }
}
