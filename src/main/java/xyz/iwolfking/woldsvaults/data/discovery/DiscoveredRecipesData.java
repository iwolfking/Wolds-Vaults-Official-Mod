package xyz.iwolfking.woldsvaults.data.discovery;

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
import xyz.iwolfking.woldsvaults.config.RecipeUnlocksConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import javax.annotation.Nonnull;
import java.util.*;

public class DiscoveredRecipesData extends SavedData
{
    protected static final String DATA_NAME = "woldsvaults_Discovered_Recipes";
    protected Map<UUID, Set<ResourceLocation>> collectedRecipes = new HashMap<>();

    private DiscoveredRecipesData() {
    }

    private DiscoveredRecipesData(CompoundTag tag) {
        this.load(tag);
    }

    public void discoverRecipeAndBroadcast(ResourceLocation recipe, Player player) {
        if(recipe != null) {
            if(this.discoverRecipe(player.getUUID(), recipe)) {
                this.broadcastDiscovery(recipe, player);
            }
        }
    }

    private void broadcastDiscovery(ResourceLocation recipe, Player player) {
        MutableComponent ct = new TextComponent("").withStyle(ChatFormatting.WHITE);
        MutableComponent playerCt = player.getDisplayName().copy();
        playerCt.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(9974168)));
        if(ModConfigs.RECIPE_UNLOCKS.RECIPE_UNLOCKS.containsKey(recipe)) {
            RecipeUnlocksConfig.Entry entry = ModConfigs.RECIPE_UNLOCKS.RECIPE_UNLOCKS.get(recipe);
            MutableComponent recipeCmp = new TextComponent(entry.NAME).withStyle(ChatFormatting.GOLD);
            MiscUtils.broadcast(ct.append(playerCt).append(" has discovered recipe for: ").append(recipeCmp));
        }
    }

    public boolean discoverRecipe(UUID playerId, ResourceLocation recipeId) {
        Set<ResourceLocation> recipeKeys = this.collectedRecipes.computeIfAbsent(playerId, id -> new HashSet<>());
        if(recipeKeys.add(recipeId)) {
            this.setDirty();
            return true;
        }
        else {
            return false;
        }
    }

    public boolean hasDiscovered(Player player, ResourceLocation recipeId) {
        return this.hasDiscovered(player.getUUID(), recipeId);
    }

    public boolean hasDiscovered(UUID playerId, ResourceLocation recipeId) {
        return this.collectedRecipes.getOrDefault(playerId, Collections.emptySet()).contains(recipeId);
    }

    public Set<ResourceLocation> getDiscoveredRecipes(Player player) {
        return this.getDiscoveredRecipes(player.getUUID());
    }

    public Set<ResourceLocation> getDiscoveredRecipes(UUID playerId) {
        return this.collectedRecipes.getOrDefault(playerId, Collections.emptySet());
    }

    public boolean discoveredAllRecipes(Player player) {
        return this.discoveredAllRecipes(player.getUUID());
    }

    public boolean discoveredAllRecipes(UUID playerId) {
        Set<ResourceLocation> discovered = this.getDiscoveredRecipes(playerId);

        for (ResourceLocation existing : ModConfigs.RECIPE_UNLOCKS.RECIPE_UNLOCKS.keySet()) {
            if (!discovered.contains(existing)) {
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
        return new DiscoveredEntriesMessage(DiscoveredEntriesMessage.Type.valueOf("WOLD_RECIPE"), this.collectedRecipes.getOrDefault(playerId, Collections.emptySet()));
    }

    public void syncTo(ServerPlayer player) {
        ModNetwork.CHANNEL.sendTo(this.getUpdatePacket(player.getUUID()), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public void load(CompoundTag tag) {
        this.collectedRecipes.clear();
        ListTag playerRecipes = tag.getList("recipes", 10);

        for (int i = 0; i < playerRecipes.size(); i++) {
            CompoundTag playerTag = playerRecipes.getCompound(i);
            UUID playerId = playerTag.getUUID("player");
            Set<ResourceLocation> themes = NBTHelper.readSet(playerTag, "recipes", StringTag.class, strTag -> ResourceLocation.parse(strTag.getAsString()));
            this.collectedRecipes.put(playerId, themes);
        }
    }

    @Nonnull
    public CompoundTag save(@Nonnull CompoundTag tag) {
        ListTag playerThemes = new ListTag();
        this.collectedRecipes.forEach((playerId, recipes) -> {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("player", playerId);
            NBTHelper.writeCollection(playerTag, "recipes", recipes, StringTag.class, key -> StringTag.valueOf(key.toString()));
            playerThemes.add(playerTag);
        });
        tag.put("recipes", playerThemes);
        return tag;
    }

    public static DiscoveredRecipesData get(ServerLevel level) {
        return get(level.getServer());
    }

    public static DiscoveredRecipesData get(MinecraftServer server) {
        return server.overworld()
                .getDataStorage()
                .computeIfAbsent(DiscoveredRecipesData::new, DiscoveredRecipesData::new, "woldsvaults_Discovered_Recipes");
    }
}
