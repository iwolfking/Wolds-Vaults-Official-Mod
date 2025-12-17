package xyz.iwolfking.woldsvaults.api.data.discovery;

import iskallia.vault.core.data.key.ThemeKey;
import iskallia.vault.core.vault.VaultRegistry;
import iskallia.vault.init.ModNetwork;
import iskallia.vault.network.message.transmog.DiscoveredEntriesMessage;
import iskallia.vault.util.MiscUtils;
import iskallia.vault.util.nbt.NBTHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DiscoveredThemesData extends SavedData
{
    protected static final String DATA_NAME = "the_vault_DiscoveredThemes";
    protected Map<UUID, Set<ResourceLocation>> collectedThemes = new HashMap<>();

    private DiscoveredThemesData() {
    }

    private DiscoveredThemesData(CompoundTag tag) {
        this.load(tag);
    }

    public void discoverThemeAndBroadcast(ThemeKey theme, Player player) {
        if(theme != null) {
            if(this.discoverTheme(player.getUUID(), theme.getId())) {
                this.broadcastDiscovery(theme, player);
            }
        }
    }

    private void broadcastDiscovery(ThemeKey theme, Player player) {
        MutableComponent ct = new TextComponent("").withStyle(ChatFormatting.WHITE);
        MutableComponent playerCt = player.getDisplayName().copy();
        playerCt.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(9974168)));
        MutableComponent themeCmp = new TextComponent(theme.getName());
        MiscUtils.broadcast(ct.append(playerCt).append(" has discovered theme: ").append(themeCmp));
    }

    public boolean discoverTheme(UUID playerId, ResourceLocation themeId) {
        Set<ResourceLocation> themeKeys = this.collectedThemes.computeIfAbsent(playerId, id -> new HashSet<>());
        if(themeKeys.add(themeId)) {
            this.setDirty();
            return true;
        }
        else {
            return false;
        }
    }

    public boolean hasDiscovered(Player player, ResourceLocation themeId) {
        return this.hasDiscovered(player.getUUID(), themeId);
    }

    public boolean hasDiscovered(UUID playerId, ResourceLocation themeId) {
        return this.collectedThemes.getOrDefault(playerId, Collections.emptySet()).contains(themeId);
    }

    public Set<ResourceLocation> getDiscoveredThemes(Player player) {
        return this.getDiscoveredThemes(player.getUUID());
    }

    public Set<ResourceLocation> getDiscoveredThemes(UUID playerId) {
        return this.collectedThemes.getOrDefault(playerId, Collections.emptySet());
    }

    public boolean discoveredAllThemes(Player player) {
        return this.discoveredAllThemes(player.getUUID());
    }

    public boolean discoveredAllThemes(UUID playerId) {
        Set<ResourceLocation> discovered = this.getDiscoveredThemes(playerId);

        for (ThemeKey existing : VaultRegistry.THEME.getKeys()) {
            if (!discovered.contains(existing.getId())) {
                return false;
            }
        }

        return true;
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

    private DiscoveredEntriesMessage getUpdatePacket(UUID playerId) {
        return new DiscoveredEntriesMessage(DiscoveredEntriesMessage.Type.valueOf("THEME"), this.collectedThemes.getOrDefault(playerId, Collections.emptySet()));
    }

    public void syncTo(ServerPlayer player) {
        ModNetwork.CHANNEL.sendTo(this.getUpdatePacket(player.getUUID()), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public void load(CompoundTag tag) {
        this.collectedThemes.clear();
        ListTag playerThemes = tag.getList("themes", 10);

        for (int i = 0; i < playerThemes.size(); i++) {
            CompoundTag playerTag = playerThemes.getCompound(i);
            UUID playerId = playerTag.getUUID("player");
            Set<ResourceLocation> themes = NBTHelper.readSet(playerTag, "themes", StringTag.class, strTag -> ResourceLocation.parse(strTag.getAsString()));
            this.collectedThemes.put(playerId, themes);
        }
    }

    @Nonnull
    public CompoundTag save(@Nonnull CompoundTag tag) {
        ListTag playerThemes = new ListTag();
        this.collectedThemes.forEach((playerId, themes) -> {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("player", playerId);
            NBTHelper.writeCollection(playerTag, "themes", themes, StringTag.class, key -> StringTag.valueOf(key.toString()));
            playerThemes.add(playerTag);
        });
        tag.put("themes", playerThemes);
        return tag;
    }

    public static DiscoveredThemesData get(ServerLevel level) {
        return get(level.getServer());
    }

    public static DiscoveredThemesData get(MinecraftServer server) {
        return server.overworld()
                .getDataStorage()
                .computeIfAbsent(DiscoveredThemesData::new, DiscoveredThemesData::new, "the_vault_DiscoveredThemes");
    }
}
